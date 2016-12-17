package com.qwiki.servlet3;
 

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.qwiki.config.WebInterfaceConfig;
 
public class WebInterfaceInit extends AbstractAnnotationConfigDispatcherServletInitializer {
 
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { WebInterfaceConfig.class };
    }
  
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }
  
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
 
}