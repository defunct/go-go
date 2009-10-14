package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.Collection;

public class TransactionsPart extends ExpandingPathPart {
    private final Include transaction;
    
    public TransactionsPart(Include transaction) {
        this.transaction = transaction;
    }
    
    public Collection<PathPart> expand(Library library, Collection<PathPart> expand) {
        Collection<PathPart> resolvers = new ArrayList<PathPart>();
        expand.addAll(transaction.getPathParts());
        Collection<PathPart> resolved = new ArrayList<PathPart>();
        for (PathPart resolver : resolvers) {
            resolved.addAll(resolver.expand(library, expand));
        }
        return resolved;
    }
}
