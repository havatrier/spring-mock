package com.kevin.spring.mock;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kevin.spring.mock.env.MockEnvironment;
import com.kevin.spring.mock.exception.MockException;
import com.kevin.spring.mock.exception.MockRuntimeException;
import com.kevin.spring.mock.provider.MockDataProvider;
import com.kevin.spring.mock.provider.XmlMockDataProvider;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.*;

/**
 * The parser for spring-mock defined xml labels (<mock>)
 * Created by shuchuanjun on 17/1/6.
 */
public class MockBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {
    @Override
    protected Class<?> getBeanClass(Element element) {
        return MockBeanFactoryPostProcessor.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext,  BeanDefinitionBuilder builder) {
        NamedNodeMap attributes = element.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            if (isEligibleAttribute(attribute, parserContext)) {
                String propertyName = extractPropertyName(attribute.getLocalName());
                Preconditions.checkState(StringUtils.hasText(propertyName),
                        "Illegal property name returned from 'extractPropertyName(String)': cannot be null or empty.");
                builder.addPropertyValue(propertyName, attribute.getValue());
            }
        }

        MockDataProvider mockDataProvider = getMockDataProvider(element);
        if (mockDataProvider == null) {
            throw new MockRuntimeException("MockDataProvider must be provided using <xml-provider> under <mt-mock>");
        }
        builder.addPropertyValue("mockDataProvider", mockDataProvider);
    }

    private MockDataProvider getMockDataProvider(Element element) {
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if (nodeNameEquals(ele, "xml-provider")) { // use XmlMockDataProvider
                    return getXmlMockDataProvider(ele);
                }
                else if (nodeNameEquals(ele, "env")) { // process environment variable element <env>
                    processEnvElement(ele);
                } else {
                    throw new MockRuntimeException("Unexpected element: " + node);
                }
            } else if (nodeIsIgnorableNode(node)) {
                continue;
            } else {
                throw new MockRuntimeException("Unexpected node: " + node);
            }

        }
        return null;
    }

    private XmlMockDataProvider getXmlMockDataProvider(Element element) {
        String locations = element.getAttribute("locations");
        return new XmlMockDataProvider(locations);
    }

    private void processEnvElement(Element element) {
        String name = element.getAttribute("name");
        String value = element.getAttribute("value");
        if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(value)) {
            throw new MockRuntimeException("<env> attribute missing 'name' or 'value' attribute");
        }
        MockEnvironment.getInstance().addEnvVariable(name, value);
    }

    private boolean nodeIsIgnorableNode(Node node) {
        return (node instanceof Text &&
                node.getTextContent().trim().isEmpty()) || node instanceof Comment;
    }

    private boolean nodeNameEquals(Node node, String name) {
        return name.equals(node.getNodeName()) || name.equals(node.getLocalName());
    }

}
