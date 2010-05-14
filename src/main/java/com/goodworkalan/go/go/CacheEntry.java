package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.goodworkalan.ilk.Ilk;

/**
 * A cache entry of command output.
 *
 * @author Alan Gutierrez
 */
class CacheEntry {
    /**
     * The list of transient commands to be executed after this cache entry is
     * restored. These were the commands present in the invoke after list after
     * the last cachable command was run.
     */
    public final List<Class<? extends Commandable>> transients = new ArrayList<Class<? extends Commandable>>();
    
    /**
     * The map of outputs objects by type generated by the command.
     */
    public final Map<Ilk.Key, Ilk.Box> outputs = new HashMap<Ilk.Key, Ilk.Box>();
}
