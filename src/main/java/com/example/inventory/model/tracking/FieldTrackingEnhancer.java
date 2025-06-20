package com.example.inventory.model.tracking;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bean post-processor that enhances entities with FieldChangeTracker functionality
 */
@Component
public class FieldTrackingEnhancer implements BeanPostProcessor {

    private static final Map<Object, Set<String>> ENTITY_UPDATED_FIELDS = new HashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean.getClass().isAnnotationPresent(TrackChanges.class)) {
            ProxyFactory factory = new ProxyFactory(bean);
            factory.addAdvice((org.aopalliance.intercept.MethodInterceptor) invocation -> {
                String methodName = invocation.getMethod().getName();
                
                if (methodName.equals("getUpdatedFields")) {
                    return new HashSet<>(ENTITY_UPDATED_FIELDS.computeIfAbsent(bean, k -> new HashSet<>()));
                } else if (methodName.equals("isFieldUpdated")) {
                    String fieldName = (String) invocation.getArguments()[0];
                    return ENTITY_UPDATED_FIELDS.computeIfAbsent(bean, k -> new HashSet<>()).contains(fieldName);
                } else if (methodName.equals("clearUpdatedFields")) {
                    ENTITY_UPDATED_FIELDS.computeIfAbsent(bean, k -> new HashSet<>()).clear();
                    return null;
                } else if (methodName.equals("markFieldUpdated")) {
                    String fieldName = (String) invocation.getArguments()[0];
                    ENTITY_UPDATED_FIELDS.computeIfAbsent(bean, k -> new HashSet<>()).add(fieldName);
                    return null;
                }
                
                return invocation.proceed();
            });
            
            return factory.getProxy();
        }
        
        return bean;
    }
}