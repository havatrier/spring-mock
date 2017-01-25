package com.kevin.spring.mock.provider;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kevin.spring.mock.cfg.*;
import com.kevin.spring.mock.exception.MockRuntimeException;
import com.kevin.spring.mock.exception.MockTypeInvalidException;
import com.kevin.spring.mock.json.JSONResourceContentLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.xml.XmlValidationModeDetector;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Iterator;

/**
 * A concrete implementation of {@link MockDataProvider} to get mock configuration
 * from XML configuration files
 * <p>
 * <p>
 * Created by shuchuanjun on 17/1/6.
 */
public class XmlMockDataProvider extends AbstractMockDataProvider {
    private static final Logger logger = LoggerFactory.getLogger(XmlMockDataProvider.class);
    private static final String DELIMITER = ",";
    /**
     * JAXP attribute used to configure the schema language for validation.
     */
    private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * JAXP attribute value indicating the XSD schema language.
     */
    private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    private static final String ROOT_ELEMENT = "services";
    private static final String SERVICE_ELEMENT = "service";
    private static final String METHOD_ELEMENT = "method";
    private static final String PARAMS_ELEMENT = "params";
    private static final String RETURNS_ELEMENT = "returns";
    private static final String PARAM_ELEMENT = "param";
    private static final String RETURN_ELEMENT = "return";
    private static final String VALUE_ELEMENT = "value";
    private static final String TYPE_ELEMENT = "type";

    private static final String ID_ATTRIBUTE = "id";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String TEST_ATTRIBUTE = "test";
    private static final String RESOURCE_ATTRIBUTE = "resource";

    private DocumentBuilder docBuilder;
    private ResourceLoader resourceLoader;
    private JSONResourceContentLoader jsonResourceLoader;
    private MockParseContext mockParseContext;

    private String locations; // 逗号分隔的URI
    public XmlMockDataProvider(String locations) {
        this.locations = locations;
        this.resourceLoader = new DefaultResourceLoader();
        this.jsonResourceLoader = new JSONResourceContentLoader(resourceLoader);
        this.mockParseContext = new MockParseContext();
        init();
    }

    @Override
    protected void init() {
        if (Strings.isNullOrEmpty(this.locations)) {
            throw new MockRuntimeException("locations is empty");
        }
        this.typeConfigMap = Maps.newHashMap();
        this.configs = Lists.newArrayList();

        try {
            DocumentBuilderFactory builderFactory = createDocumentBuilderFactory(XmlValidationModeDetector.VALIDATION_NONE, true);
            this.docBuilder = createDocumentBuilder(builderFactory, null, new SimpleSaxErrorHandler(logger));
        } catch (ParserConfigurationException e) {
            throw new MockRuntimeException(e);
        }


        Iterable<String> locations = Splitter.on(DELIMITER).trimResults().split(this.locations);
        Iterator<String> it = locations.iterator();
        while (it.hasNext()) {
            String location = it.next();
            loadMockConfigs(location);
        }
    }

    private void loadMockConfigs(String location) {
        Resource resource = resourceLoader.getResource(location);
        try {
            InputSource inputSource = new InputSource(resource.getInputStream());
            Document doc = docBuilder.parse(inputSource);
            doLoadMockConfigs(doc);
        } catch (SAXParseException ex) {
                throw new MockRuntimeException("Line " + ex.getLineNumber() + " in XML document from " + location + " is invalid", ex);
        } catch (SAXException ex) {
                throw new MockRuntimeException("XML document from " + location + " is invalid", ex);
        } catch (IOException ex) {
            throw new MockRuntimeException("IOException parsing XML document from " + location, ex);
        } catch (ClassNotFoundException ex) {
            throw new MockRuntimeException("Type not found", ex);
        } catch (MockRuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new MockRuntimeException("Unexpected exception parsing XML document from " + location, ex);
        }
    }

    private void doLoadMockConfigs(Document doc) throws ClassNotFoundException {
        Element root = doc.getDocumentElement();
        if (!nodeNameEquals(root, ROOT_ELEMENT)) {
            throw new MockRuntimeException("Root xml node must be <" + ROOT_ELEMENT + ">: " + root);
        }
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if (nodeNameEquals(ele, SERVICE_ELEMENT)) {
                    processServiceMockConfig(ele);
                } else {
                    throw new MockRuntimeException("Error, Expected xml node <" + SERVICE_ELEMENT + ">: " + node);
                }
            } else if (nodeIsIgnorableNode(node)) {
                continue;
            } else {
                throw new MockRuntimeException("Unexpected xml node: " + node);
            }
        }
    }

    private void processServiceMockConfig(Element element) throws ClassNotFoundException {
        String name = element.getAttribute(NAME_ATTRIBUTE);
        String type = element.getAttribute(TYPE_ATTRIBUTE);
        if (Strings.isNullOrEmpty(type)) {
            throw new MockRuntimeException("<service> element missing attribute '" + TYPE_ATTRIBUTE + "': " + element);
        }
        if (Strings.isNullOrEmpty(name)) {
            // use type as service name
            name = type;
        }
        ServiceMockConfig srvMockCfg = new ServiceMockConfig();
        srvMockCfg.setServiceName(name);
        srvMockCfg.setServiceType(parseType(type));

        mockParseContext.setServiceMock(srvMockCfg);

        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if (nodeNameEquals(ele, METHOD_ELEMENT)) {
                    processMethodMockConfig(ele, srvMockCfg);
                } else if (nodeIsIgnorableNode(node)) {
                    continue;
                } else {
                    throw new MockRuntimeException("Error, Expected xml node <" + METHOD_ELEMENT + ">: "  + node);
                }
            } else if (nodeIsIgnorableNode(node)) {
                continue;
            } else {
                throw new MockRuntimeException("Unexpected xml node: " + node);
            }
        }


        registerServiceConfig(srvMockCfg);
    }

    private void processMethodMockConfig(Element element, ServiceMockConfig srvMockCfg) throws ClassNotFoundException {
        String name = element.getAttribute(NAME_ATTRIBUTE);
        if (Strings.isNullOrEmpty(name)) {
            throw new MockRuntimeException("<" + METHOD_ELEMENT + "> element missing attribute '" + NAME_ATTRIBUTE + "': " + element);
        }

        MethodMockConfig methodMockCfg = new MethodMockConfig();
        methodMockCfg.setMethodName(name);

        mockParseContext.setMethodMock(methodMockCfg);

        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if (nodeNameEquals(ele, PARAMS_ELEMENT)) {
                    processParamsElement(ele, methodMockCfg);
                } else if (nodeNameEquals(ele, RETURNS_ELEMENT)) {
                    processReturnsElement(ele, methodMockCfg);
                } else {
                    throw new MockRuntimeException("Error, Expected xml node <" + PARAMS_ELEMENT + ">" +
                            "or <" + RETURNS_ELEMENT + ">: "  + node);
                }
            } else if (nodeIsIgnorableNode(node)) {
                continue;
            } else {
                throw new MockRuntimeException("Unexpected xml node: " + node);
            }
        }

        srvMockCfg.addMethodMock(methodMockCfg);
    }

    private void processParamsElement(Element element, MethodMockConfig methodMockCfg) throws ClassNotFoundException {
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if (nodeNameEquals(ele, PARAM_ELEMENT)) {
                    String name = ele.getAttribute(NAME_ATTRIBUTE);
                    String type = ele.getAttribute(TYPE_ATTRIBUTE);
                    if (Strings.isNullOrEmpty(name)) {
                        throw new MockRuntimeException("<" + PARAM_ELEMENT + "> missing attribute '" + NAME_ATTRIBUTE + "': " + ele);
                    }
                    if (Strings.isNullOrEmpty(type)
                            && ele.hasChildNodes()) { // try to use <type> element
                        NodeList typeNl = ele.getElementsByTagName(TYPE_ELEMENT);
                        if (typeNl.getLength() == 1) {
                            Node typeNd = typeNl.item(0);
                            if (typeNd instanceof Element) {
                                Element typeEle = (Element) typeNd;
                                type = getNodeContent(typeEle);
                            } else {
                                throw new MockRuntimeException("Unexpected error for <" + PARAM_ELEMENT + ">: " + ele);
                            }
                        } else if (typeNl.getLength() == 0) {
                            throw new MockRuntimeException("<" + PARAM_ELEMENT + "> missing attribute '" + NAME_ATTRIBUTE +
                                    "' and element <" + TYPE_ELEMENT + ">: " + ele);
                        } else {
                            throw new MockRuntimeException("<" + PARAM_ELEMENT + "> has duplicate element <" + TYPE_ELEMENT + ">: " + ele);
                        }
                    }
                    ParamMeta paramMeta = new ParamMeta(name, JavaType.forName(type));
                    methodMockCfg.addParam(paramMeta);
                } else {
                    throw new MockRuntimeException("Error, Expected xml node <" + PARAM_ELEMENT + "> : " + node);
                }
            } else if (nodeIsIgnorableNode(node)) {
                continue;
            } else {
                throw new MockRuntimeException("Unexpected xml node: " + node);
            }
        }
    }

    private void processReturnsElement(Element element, MethodMockConfig methodMockCfg) throws ClassNotFoundException {
        String type = element.getAttribute(TYPE_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(type)) { // prefer to use type attribute
            methodMockCfg.setReturnType(JavaType.forName(type));
        }
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                ReturnMock returnMock = new ReturnMock();
                if (nodeNameEquals(ele, RETURN_ELEMENT)) {
                    String id = ele.getAttribute(ID_ATTRIBUTE);
                    if (Strings.isNullOrEmpty(id)) {
                        // use auto generated I
                        id = mockParseContext.autoGenerateReturnMockID();
                    }
                    returnMock.setId(id);

                    String testExpr = ele.getAttribute(TEST_ATTRIBUTE);
                    if (!Strings.isNullOrEmpty(testExpr))
                        returnMock.setTestExpr(testExpr);

                    if (!ele.hasAttribute(RESOURCE_ATTRIBUTE) && !ele.hasChildNodes()) {
                        throw new MockRuntimeException("<" + RETURN_ELEMENT + "> miss mock json data setting (ether with '" +
                                RESOURCE_ATTRIBUTE + "', or setting directly(also <value> element): " + ele);
                    }

                    // prefer to use resource attribute
                    String jsonResource = ele.getAttribute(RESOURCE_ATTRIBUTE);
                    if (!Strings.isNullOrEmpty(jsonResource)) {
                        try {
                            String returnJson = jsonResourceLoader.load(jsonResource);
                            returnMock.setReturnJson(returnJson);
                        } catch (IOException e) {
                            throw new MockRuntimeException("Error to load JSON resource (" + jsonResource + ") defined in <" + RETURN_ELEMENT + ">: " + ele);
                        }
                    }

                    if (Strings.isNullOrEmpty(returnMock.getReturnJson())) {
                        if (ele.hasChildNodes()) {
                            NodeList retNl = ele.getChildNodes();
                            for (int j = 0; j < retNl.getLength(); j++) { // try to <value> element
                                Node retNode = retNl.item(j);
                                if (retNode instanceof Element) {
                                    Element valEle = (Element) retNode;
                                    if (nodeNameEquals(valEle, VALUE_ELEMENT)) {
                                        returnMock.setReturnJson(getNodeContent(valEle));
                                    } else {
                                        throw new MockRuntimeException("Error, Expected xml node <" + VALUE_ELEMENT + "> : " + valEle);
                                    }

                                } else if (retNode instanceof Text) { // use text directly
                                    returnMock.setReturnJson(getNodeContent(retNode));
                                } else if (nodeIsIgnorableNode(retNode)) {
                                    continue;
                                } else {
                                    throw new MockRuntimeException("Unexpected xml node: " + retNode);
                                }
                            }
                        }
                        if (Strings.isNullOrEmpty(returnMock.getReturnJson())) { // try to use <return> element's content
                            returnMock.setReturnJson(ele.getTextContent());
                        }
                    }

                    if (Strings.isNullOrEmpty(returnMock.getReturnJson())) {
                        throw new MockRuntimeException("<" + RETURN_ELEMENT + "> miss mock json data setting (ether with '" +
                                RESOURCE_ATTRIBUTE + "', or setting directly(also <value> element): " + ele);
                    }

                    methodMockCfg.addReturnMock(returnMock);
                } else if (nodeNameEquals(ele, TYPE_ELEMENT)) {
                    if (methodMockCfg.getReturnType() == null) { // type attribute is no set, use <type> element
                        type = getNodeContent(ele);
                        methodMockCfg.setReturnType(JavaType.forName(type));
                    }
                } else {
                    throw new MockRuntimeException("Error, Expected xml node <" + RETURN_ELEMENT + "> : " + node);
                }
            } else if (nodeIsIgnorableNode(node)) {
                continue;
            } else {
                throw new MockRuntimeException("Unexpected xml node: " + node);
            }
        }

    }

    private String getNodeContent(Node node) {
        NodeList nl = node.getChildNodes();
        if (nl.getLength() == 1) {
            return nl.item(0).getTextContent().trim();
        }
        return node.getTextContent().trim();
    }
    private Class<?> parseType(String type) throws MockTypeInvalidException {
        try {
            return ClassUtils.forName(type.trim(), ClassUtils.getDefaultClassLoader());
        } catch (ClassNotFoundException e) {
            throw new MockTypeInvalidException(type, e);
        }
    }

    private boolean nodeIsIgnorableNode(Node node) {
        if (node instanceof Text){
            return Strings.isNullOrEmpty(CharMatcher.WHITESPACE.removeFrom(node.getTextContent()));
        } else if (node instanceof Comment) {
            return true;
        }
        return false;
    }

    private boolean nodeNameEquals(Node node, String name) {
        return name.equals(node.getNodeName()) || name.equals(node.getLocalName());
    }


    /**
    * Create the {@link DocumentBuilderFactory} instance.
    * @param validationMode the type of validation: {@link XmlValidationModeDetector#VALIDATION_DTD DTD}
    * or {@link XmlValidationModeDetector#VALIDATION_XSD XSD})
            * @param namespaceAware whether the returned factory is to provide support for XML namespaces
    * @return the JAXP DocumentBuilderFactory
    * @throws ParserConfigurationException if we failed to build a proper DocumentBuilderFactory
    */
    protected DocumentBuilderFactory createDocumentBuilderFactory(int validationMode, boolean namespaceAware)
            throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaceAware);

        if (validationMode != XmlValidationModeDetector.VALIDATION_NONE) {
            factory.setValidating(true);

            if (validationMode == XmlValidationModeDetector.VALIDATION_XSD) {
                // Enforce namespace aware for XSD...
                factory.setNamespaceAware(true);
                try {
                    factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
                }
                catch (IllegalArgumentException ex) {
                    ParserConfigurationException pcex = new ParserConfigurationException(
                            "Unable to validate using XSD: Your JAXP provider [" + factory +
                                    "] does not support XML Schema. Are you running on Java 1.4 with Apache Crimson? " +
                                    "Upgrade to Apache Xerces (or Java 1.5) for full XSD support.");
                    pcex.initCause(ex);
                    throw pcex;
                }
            }
        }

        return factory;
    }
    /**
     * Create a JAXP DocumentBuilder that this bean definition reader
     * will use for parsing XML documents. Can be overridden in subclasses,
     * adding further initialization of the builder.
     * @param factory the JAXP DocumentBuilderFactory that the DocumentBuilder
     * should be created with
     * @param entityResolver the SAX EntityResolver to use
     * @param errorHandler the SAX ErrorHandler to use
     * @return the JAXP DocumentBuilder
     * @throws ParserConfigurationException if thrown by JAXP methods
     */
    protected DocumentBuilder createDocumentBuilder(
            DocumentBuilderFactory factory, EntityResolver entityResolver, ErrorHandler errorHandler)
            throws ParserConfigurationException {

        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        if (entityResolver != null) {
            docBuilder.setEntityResolver(entityResolver);
        }
        if (errorHandler != null) {
            docBuilder.setErrorHandler(errorHandler);
        }
        return docBuilder;
    }

    static class SimpleSaxErrorHandler implements ErrorHandler {
        private final Logger logger;


        /**
         * Create a new SimpleSaxErrorHandler for the given
         * Commons Logging logger instance.
         */
        public SimpleSaxErrorHandler(Logger logger) {
            this.logger = logger;
        }


        public void warning(SAXParseException ex) throws SAXException {
            logger.warn("Ignored XML validation warning", ex);
        }

        public void error(SAXParseException ex) throws SAXException {
            throw ex;
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }
    }

    static class MockParseContext { // used to auto generate ReturnMock ID
        private String serviceName;
        private String methodName;
        private int idx;

        public MockParseContext() {
            this.serviceName = "";
            this.methodName = "";
            this.idx = 1;
        }
        public void setServiceMock(ServiceMockConfig serviceMock) {
            if (!serviceName.equals(serviceMock.getServiceName())) {
                serviceName = serviceMock.getServiceName();
                methodName = "";
                idx = 1;
            }
        }
        public void setMethodMock(MethodMockConfig methodMock) {
            if (!methodName.equals(methodMock.getMethodName())) {
                methodName = methodMock.getMethodName();
                idx = 1;
            }
        }

        public String autoGenerateReturnMockID() {
            String id = String.format("%s%s%s-%d", serviceName, "#", methodName, idx);
            idx++;
            return id;
        }
    }
}
