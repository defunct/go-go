package com.goodworkalan.go.go;

public class Conversion {
    public final String name;
    
    public final Object value;
    
    public Conversion(String name, Object value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }
    
    public Object getValue() {
        return value;
    }
}
