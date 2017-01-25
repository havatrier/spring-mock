# spring-mock
spring mock framework to mock "any" spring managed beans

## How spring-mock to proxy a bean with mock data?
An AOP proxy will be created before a bean is created to proxy the functions of
the bean, returning mock data for the methods whose return results need to be mocked.

```Java
@Override
public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    ServiceMockConfig serviceMock = getServiceMockConfigForBean(bean);
    if (serviceMock != null) {
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.addAdvice(new MTMockMethodInterceptor(serviceMock));
        proxyFactory.setProxyTargetClass(shouldProxyTargetClass(serviceMock));
        Object newBean = proxyFactory.getProxy();
        logger.info("[ServiceType '{}'] use mock proxy to replace bean(name={}): {}", serviceMock.getServiceType(), beanName, bean);
        return newBean;
    }
    return bean;
}
```

```Java
public class MockMethodInterceptor implements MethodInterceptor {
    private ServiceMockConfig serviceMockConfig;

    public MTMockMethodInterceptor(ServiceMockConfig serviceMockConfig) {
        this.serviceMockConfig = serviceMockConfig;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        MethodMockConfig methodMock = serviceMockConfig.getApplicableMethodMockConfig(invocation);
        if (methodMock != null) {
            Object returnVal = tryInvokeWithMethodMock(methodMock, invocation.getArguments());
            if (returnVal == null) { // No ReturnMock is applicable, invoke original method
                return invocation.proceed();
            }
            return returnVal;
        }
        return invocation.proceed();
    }

    private Object tryInvokeWithMethodMock(MethodMockConfig methodMock, Object[] arguments) {
        return methodMock.invoke(arguments);
    }

}
```

## How to define mock data
Mock data is provided by the implementation of interface ``MockDataProvider``

```Java
public interface MockDataProvider {
    /**
     * get {@link ServiceMockConfig} by bean's type
     * @param clazz the type of the bean
     * @return the <tt>ServiceMockConfig</tt> corresponding to the bean's type; returns null if none
     */
    ServiceMockConfig getServiceMockConfig(Class<?> clazz);
}
```

Currently the only one implementation is ``XmlMockDataProvider``

### XmlMockDataProvider
Mock data is defined with XML files. XML files containing the following
labels (refer to mtmock-config.xsd for more details):

#### \<service\>
Root label to define mock configuration for a service (bean).

* attribute ``type``: **required**; define service's Java type
* attribute ``name``: optional; define service's name; default is same to ``type``'s value

#### \<method\>
Under **\<service>** label; define mock configuration for a method.

* attribute ``name``: **required**; method name

#### \<params\>
Under **\<method>** label; define parameter list for the method.

#### \<param\>
Under **\<params>** label; define a specific parameter.

* attribute ``name``: **required**; parameter name
* attribute ``type``: optional; define parameter type; If not defined, a <type> label must be
provided under \<param\> label;

#### \<returns\>
Under **\<method>** label; define return mock data configuration list.

* attribute ``type``: optional; define method return type; If not defined, a \<type> label must be
defined under \<returns>

#### \<return\>
* attribute ``id``: optional; if not defined, auto generate it
* attribute ``test``: optional; define the condition (expression) when to use this return mock data; The expression is defined with SpEL:
variables available includes parameters (names) of current method and user defined environment variables

#### \<type\>
Under **\<param>** and **\<returns>** labels; define java type


An example xml configuration:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<services xmlns="http://www.kevin.com/schema/mtmock_cfg"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://www.kevin.com/schema/mtmock_cfg http://www.kevin.com/schema/mtmock_cfg.xsd">

    <service type="com.kevin.spring.mock.service.IMockTestService">
        <method name="getMockIdList">
            <params>
                <param name="userIds">
                    <type><![CDATA[List<Integer>]]></type>
                </param>
                <param name="operator" type="com.kevin.spring.mock.domain.Operator"/>
            </params>
            <returns>
                <type><![CDATA[List<Integer>]]></type>
                <return id="kevin" test="#operator.name == 'kevin'" resource="classpath:mock-json/MyMockTestService.getMockIdList.operator.name-1.json"/>
            </returns>
        </method>
    </service>
</services>
```



