package com.roncoo.eshop.cache.spring;

import org.springframework.context.ApplicationContext;

/**
 * @author yangfan
 * @date 2018/02/20
 */
public class SpringContext {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContext.applicationContext = applicationContext;
    }
}
