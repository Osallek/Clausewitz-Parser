package fr.osallek.clausewitzparser.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class ClausewitzObject {

    protected String name;

    protected int order;

    protected ClausewitzObject(String name, int order) {
        this.name = name;
        this.order = order;
    }

    protected ClausewitzObject(ClausewitzObject other) {
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

    public abstract void write(BufferedWriter bufferedWriter, int depth, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException;

    public abstract void write(BufferedWriter bufferedWriter, boolean spaced, int depth, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException;

    @Override
    public String toString() {
        return name;
    }
}
