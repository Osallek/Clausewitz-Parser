package com.osallek.clausewitzparser.model;

import java.io.BufferedWriter;
import java.io.IOException;

public abstract class ClausewitzObject {

    protected final ClausewitzObject parent;

    protected String name;

    protected int order;

    public ClausewitzObject(String name, ClausewitzObject parent, int order) {
        this.name = name.intern();
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

    public void setName(String name) {
        this.name = name;
    }

    public ClausewitzObject getParent() {
        return parent;
    }

    public int getOrder() {
        return order;
    }

    public abstract void write(BufferedWriter bufferedWriter, int depth) throws IOException;

    protected void printTabs(BufferedWriter bufferedWriter, int depth) throws IOException {
        bufferedWriter.write(new String(new char[depth]).replace('\0', '\t'));
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
