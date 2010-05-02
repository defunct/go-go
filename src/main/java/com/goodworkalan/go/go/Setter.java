package com.goodworkalan.go.go;

import java.lang.reflect.Member;

import com.goodworkalan.reflective.ReflectiveException;

interface Setter {
    public void set(Object object, Object value) throws ReflectiveException;
    
    public Member getNative();
    
    public Class<?> getType();
}
