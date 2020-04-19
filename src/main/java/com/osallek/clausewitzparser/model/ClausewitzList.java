package com.osallek.clausewitzparser.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ClausewitzList extends ClausewitzObject {

    private final List<String> values;

    private final boolean sameLine;

    ClausewitzList(ClausewitzItem parent, String name, int order) {
        this(parent, name, order, false);
    }

    ClausewitzList(ClausewitzItem parent, String name, int order, boolean sameLine) {
        super(name, parent, order);
        this.values = new ArrayList<>();
        this.sameLine = sameLine;
    }

    ClausewitzList(ClausewitzList other) {
        super(other);
        this.values = other.values;
        this.sameLine = other.sameLine;
    }

    public String get(int id) {
        if (id < 0 || id >= this.values.size()) {
            return null;
        }

        return this.values.get(id);
    }

    public int size() {
        return values.size();
    }

    public int indexOf(String val) {
        return values.indexOf(val);
    }

    public void remove(int id) {
        values.remove(id);
    }

    public boolean delete(String val) {
        return values.remove(val);
    }

    public void clear() {
        this.values.clear();
    }

    public void add(String val) {
        this.values.add(val);
    }

    public void add(int val) {
        this.values.add(Integer.toString(val));
    }

    public void add(double val) {
        this.values.add(Double.toString(val));
    }

    public void addAll(List<String> values) {
        for (String s : values) {
            add(s);
        }
    }

    public void addAll(String[] values) {
        for (String s : values) {
            add(s);
        }
    }

    public boolean contains(String val) {
        return this.values.contains(val);
    }

    public List<String> getValues() {
        return new ArrayList<>(values);
    }

    public boolean isSameLine() {
        return sameLine;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ClausewitzList)) {
            return false;
        }

        final ClausewitzList gl = (ClausewitzList) obj;

        return name.equals(gl.name)
               && values.equals(gl.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, values);
    }

    @Override
    public void write(BufferedWriter bufferedWriter, int depth) throws IOException {
        printTabs(bufferedWriter, depth);
        bufferedWriter.write(this.name);
        printEqualsOpen(bufferedWriter);
        bufferedWriter.newLine();

        if (this.sameLine) {
            printTabs(bufferedWriter, depth + 1);
            for (String str : this.values) {
                bufferedWriter.write(str);
                printSpace(bufferedWriter);
            }
            bufferedWriter.newLine();
        } else {
            for (String str : this.values) {
                printTabs(bufferedWriter, depth + 1);
                bufferedWriter.write(str);
                bufferedWriter.newLine();
            }
        }

        printTabs(bufferedWriter, depth);
        printClose(bufferedWriter);
    }
}
