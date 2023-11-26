package com.edu;

public interface Config {
    <T> Class <? extends T> getImplClass(Class<T> ifc);
}
