package com.project.QLDSV_HTC;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * SpringContext cho phép lấy các bean Spring từ bất cứ đâu trong code (ví dụ từ JavaFX controller).
 */
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;
    }

    /**
     * Lấy bean theo kiểu Class<T>.
     *
     * @param clazz class của bean cần lấy
     * @param <T>   kiểu của bean
     * @return biến bean do Spring quản lý
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }
}
