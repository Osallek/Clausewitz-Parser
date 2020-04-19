package com.osallek.clausewitzparser.model;

import java.io.BufferedWriter;
import java.io.IOException;

public abstract class ClausewitzObject {

    protected final String name;

    protected final ClausewitzObject parent;

    protected int order;

    public ClausewitzObject(String name, ClausewitzObject parent, int order) {
        this.name = name;
        this.parent = parent;
        this.order = order;
    }

    public ClausewitzObject(ClausewitzObject other) {
        this.name = other.name;
        this.parent = other.parent;
        this.order = other.order;
    }

    public ClausewitzObject getRoot() {
        return (this.parent == null ? this : this.parent.getRoot());
    }

    public String getName() {
        return name;
    }

    public ClausewitzObject getParent() {
        return parent;
    }

    public int getOrder() {
        return order;
    }

    public abstract void write(BufferedWriter bufferedWriter, int depth) throws IOException;

    protected void printTabs(BufferedWriter bufferedWriter, int depth) throws IOException {
        bufferedWriter.write("\t".repeat(depth));
    }

    protected void printEqualsOpen(BufferedWriter bufferedWriter) throws IOException {
        printEquals(bufferedWriter);
        printOpen(bufferedWriter);
    }

    protected void printEquals(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("=");
    }

    protected void printOpen(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("{");
    }

    protected void printClose(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("}");
    }

    protected void printSpace(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(" ");
    }
}
