package com.goodworkalan.go.go.library;

import static com.goodworkalan.go.go.GoException.INVALID_EXCLUDE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.goodworkalan.go.go.GoException;

// TODO Document.
public class Exclude extends ArrayList<String> {
    /** The serial version id. */
    private static final long serialVersionUID = 1L;
    
    /**
     * Create a new exclude by splitting the group and project out of the slash
     * delimited exclude token string.
     * 
     * @param exclude The exclude token string.
     */
    public Exclude(String exclude) {
        String[] split = exclude.split("/");
        if (split.length != 2) {
            throw new GoException(INVALID_EXCLUDE, exclude);
        }
        addAll(Arrays.asList(split[0], split[1]));
    }
    
    // TODO Document.
    public Exclude(List<String> copy) {
        super(copy);
        if (copy.size() != 2) {
            throw new IllegalArgumentException();
        }
    }
    
    // TODO Document.
    public static Set<Exclude> excludes(String...excludes) {
        Set<Exclude> set = new LinkedHashSet<Exclude>();
        for (String exclude : excludes) {
            set.add(new Exclude(exclude));
        }
        return set;
    }
    
    // TODO Document.
    public String toString() {
        return get(0) + "/" + get(1);
    }

}
