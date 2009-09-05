package com.goodworkalan.go.go;

import com.goodworkalan.cassandra.CassandraException;

public class Catcher {
    public void examine(CassandraException e) {
        throw e;
    }
}
