package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TransactionsPart extends ExpandingPathPart {
    private final List<Transaction> transactions;
    
    public TransactionsPart(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    public TransactionsPart(Transaction transaction) {
        this.transactions = Collections.singletonList(transaction);
    }
    
    public Collection<PathPart> expand(Library library, Collection<PathPart> expand) {
        Collection<PathPart> resolvers = new ArrayList<PathPart>();
        for (Transaction transaction : transactions) {
            expand.addAll(transaction.getPathParts());
        }
        Collection<PathPart> resolved = new ArrayList<PathPart>();
        for (PathPart resolver : resolvers) {
            resolved.addAll(resolver.expand(library, expand));
        }
        return resolved;
    }
}
