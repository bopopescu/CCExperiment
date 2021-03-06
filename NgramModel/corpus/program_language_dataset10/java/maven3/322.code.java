package org.apache.maven.configuration;
public interface BeanConfigurationRequest
{
    Object getBean();
    BeanConfigurationRequest setBean( Object bean );
    Object getConfiguration();
    BeanConfigurationRequest setConfiguration( Object configuration );
    BeanConfigurationRequest setConfiguration( Object configuration, String element );
    String getConfigurationElement();
    ClassLoader getClassLoader();
    BeanConfigurationRequest setClassLoader( ClassLoader classLoader );
    BeanConfigurationValuePreprocessor getValuePreprocessor();
    BeanConfigurationRequest setValuePreprocessor( BeanConfigurationValuePreprocessor valuePreprocessor );
    BeanConfigurationPathTranslator getPathTranslator();
    BeanConfigurationRequest setPathTranslator( BeanConfigurationPathTranslator pathTranslator );
}
