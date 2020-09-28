package com.osallek.clausewitzparser.model;

import java.io.BufferedWriter;
import java.io.IOException;

public abstract class ClausewitzObject {

    protected String name;

    protected int order;

    public ClausewitzObject(String name, int order) {
        this.name = name == null ? null : name.intern();
        this.order = order;
    }

    public ClausewitzObject(ClausewitzObject other) {
        this.name = other.name;
        this.order = other.order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public abstract void write(BufferedWriter bufferedWriter, int depth) throws IOException;

    @Override
    public String toString() {
        return name;
    }
}
