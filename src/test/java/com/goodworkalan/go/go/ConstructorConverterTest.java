package com.goodworkalan.go.go;

import org.testng.annotations.Test;

import com.goodworkalan.reflective.ReflectiveFactory;

public class ConstructorConverterTest {
    @Test
    public void noConstructor() {
        new GoExceptionCatcher(GoException.CANNOT_CREATE_FROM_STRING, new Runnable() {
            public void run() {
                new ConstructorConverter(new ReflectiveFactory(), Character.class);
            }
        }).run();
    }

    @Test
    public void cannotConvert() {
        new GoExceptionCatcher(GoException.STRING_CONVERSION_ERROR, new Runnable() {
            public void run() {
                new ConstructorConverter(new ReflectiveFactory(), Integer.class).convert("FRED");
            }
        }).run();
    }
    
    @Test
    public void conversionException() {
        new GoExceptionCatcher(GoException.STRING_CONSTRUCTOR_ERROR, new Runnable() {
            public void run() {
                new ConstructorConverter(new ReflectiveFactory(), BadConversion.class).convert("FRED");
            }
        }).run();
    }
    
    
    @Test
    public void constructAbstract() {
        new GoExceptionCatcher(GoException.STRING_CONSTRUCTOR_ERROR, new Runnable() {
            public void run() {
                new ConstructorConverter(new ReflectiveFactory(), AbstractConversion.class).convert("FRED");
            }
        }).run();
    }
}
