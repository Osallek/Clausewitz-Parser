package com.osallek.clausewitzparser.model;

import com.osallek.clausewitzparser.common.Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;

public final class ClausewitzVariable extends ClausewitzObject {

    private String value;

    public ClausewitzVariable(ClausewitzObject parent, String name, int order, String value) {
        super(name, parent, order);
        this.value = value;
    }

    public ClausewitzVariable(ClausewitzVariable other) {
        super(other);
        this.value = other.value;
    }

    public String getValue() {
        return this.value;
    }

    public Integer getAsInt() {
        String var = getValue();

        if (Utils.isNotBlank(var)) {
            return Integer.parseInt(var);
        } else {
            return null;
        }
    }

    public Double getAsDouble() {
        String var = getValue();

        if (Utils.isNotBlank(var)) {
            return Double.parseDouble(var);
        } else {
            return null;
        }
    }

    public Boolean getAsBool() {
        String var = getValue();

        if (Utils.isNotBlank(var)) {
            return "yes".equals(var);
        } else {
            return null;
        }
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = Integer.toString(value);
    }

    public void setValue(double value) {
        this.value = Double.toString(value);
    }

    public void setValue(boolean value) {
        this.value = value ? "yes" : "no";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ClausewitzVariable)) {
            return false;
        }

        ClausewitzVariable ov = (ClausewitzVariable) obj;

        return name.equalsIgnoreCase(ov.name) && value.equals(ov.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.value);
    }

    @Override
    public void write(BufferedWriter bufferedWriter, int depth) throws IOException {
        printTabs(bufferedWriter, depth);
        bufferedWriter.write(this.name);
        printEquals(bufferedWriter);
        bufferedWriter.write(this.value);
    }
}
