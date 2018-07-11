package org.springframework.social.cafe24.config.xml;

import org.springframework.social.config.xml.AbstractProviderConfigBeanDefinitionParser;
import org.springframework.social.config.xml.AbstractProviderConfigNamespaceHandler;

public class Cafe24NamespaceHanlder extends AbstractProviderConfigNamespaceHandler {
    @Override
    protected AbstractProviderConfigBeanDefinitionParser getProviderConfigBeanDefinitionParser() {
        return new Cafe24ConfigBeanDefinitionParser();
    }
}
