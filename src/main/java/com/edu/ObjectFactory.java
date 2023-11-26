package com.edu;

import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;

public class ObjectFactory {

    private static ObjectFactory ourInstance = new ObjectFactory();
    private Config config;

    public static ObjectFactory getInstance() {
        return ourInstance;
    }

    private ObjectFactory() {
        new JavaConfig("com.edu", new HashMap<>(Map.of(Policeman.class, AngryPoliceman.class)));
    }

    @SneakyThrows
    public <T> T createObject(Class<T> type) {
        Class<? extends T> implClass = type;
        if(implClass.isInterface()) {
            implClass = config.getImplClass(type);
        }
        T t = implClass.getDeclaredConstructor().newInstance();
        return t;
    }
}
