package com.osallek.clausewitzparser.model;

import com.osallek.clausewitzparser.common.ClausewitzUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public Integer getAsInt(int id) {
        String var = get(id);

        if (ClausewitzUtils.isNotBlank(var)) {
            return Integer.parseInt(var);
        } else {
            return null;
        }
    }

    public Long getAsLong(int id) {
        String var = get(id);

        if (ClausewitzUtils.isNotBlank(var)) {
            return Long.parseLong(var);
        } else {
            return null;
        }
    }

    public Double getAsDouble(int id) {
        String var = get(id);

        if (ClausewitzUtils.isNotBlank(var)) {
            return Double.parseDouble(var);
        } else {
            return null;
        }
    }

    public Boolean getAsBool(int id) {
        String var = get(id);

        if (ClausewitzUtils.isNotBlank(var)) {
            return "yes".equals(var);
        } else {
            return null;
        }
    }

    public int size() {
        return this.values.size();
    }

    public int indexOf(String val) {
        return this.values.indexOf(val);
    }

    public void remove(int id) {
        this.values.remove(id);
    }

    public void remove(String value) {
        for (int i = 0; i < this.values.size(); i++) {
            if (this.values.get(i).equalsIgnoreCase(value)) {
                this.values.remove(i);
                break;
            }
        }
    }

    public void removeLast(String value) {
        for (int i = this.values.size() - 1; i >= 0; i--) {
            if (this.values.get(i).equalsIgnoreCase(value)) {
                this.values.remove(i);
                break;
            }
        }
    }

    public void removeAll(String value) {
        this.values.removeIf(s -> s.equalsIgnoreCase(value));
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

    public void add(long val) {
        this.values.add(Long.toString(val));
    }

    public void add(double val) {
        this.values.add(Double.toString(val));
    }

    public void add(boolean val) {
        this.values.add(val ? "yes" : "no");
    }

    public void set(int id, String val) {
        this.values.set(id, val);
    }

    public void set(int id, int val) {
        set(id, Integer.toString(val));
    }

    public void set(int id, long val) {
        set(id, Long.toString(val));
    }

    public void set(int id, double val) {
        set(id, Double.toString(val));
    }

    public void set(int id, boolean val) {
        set(id, val ? "yes" : "no");
    }

    public void change(String previous, String newOne) {
        Integer index = null;

        for (int i = 0; i < this.values.size(); i++) {
            if (this.values.get(i).equalsIgnoreCase(previous)) {
                index = i;
                break;
            }
        }

        if (index != null) {
            set(index, newOne);
        }
    }

    public void addAll(List<String> values) {
        for (String s : values) {
            add(s);
        }
    }

    public void addAll(String... values) {
        for (String s : values) {
            add(s);
        }
    }

    public void addAll(Integer... values) {
        for (Integer s : values) {
            add(s);
        }
    }

    public void addAll(Double... values) {
        for (Double s : values) {
            add(s);
        }
    }

    public void addAll(Boolean... values) {
        for (Boolean s : values) {
            add(s);
        }
    }

    public boolean contains(String val) {
        return this.values.contains(val);
    }

    public boolean contains(int val) {
        return this.contains(String.valueOf(val));
    }

    public boolean contains(double val) {
        return this.contains(String.valueOf(val));
    }

    public boolean contains(boolean val) {
        return this.contains(val ? "yes" : "no");
    }

    public List<String> getValues() {
        return new ArrayList<>(this.values);
    }

    public List<Integer> getValuesAsInt() {
        return getValues().stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public List<Double> getValuesAsDouble() {
        return getValues().stream().map(Double::parseDouble).collect(Collectors.toList());
    }

    public List<Boolean> getValuesAsBool() {
        return getValues().stream().map("yes"::equalsIgnoreCase).collect(Collectors.toList());
    }

    public boolean isSameLine() {
        return this.sameLine;
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
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
