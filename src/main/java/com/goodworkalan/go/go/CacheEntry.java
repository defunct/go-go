package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.List;

import com.goodworkalan.ilk.Ilk;

public class CacheEntry {
    public final List<Class<? extends Commandable>> transients = new ArrayList<Class<? extends Commandable>>();
    
    public final List<Ilk.Box> outputs = new ArrayList<Ilk.Box>();
}
