package fr.osallek.clausewitzparser.model;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ClausewitzVariable extends ClausewitzObject {

    private String value;

    public ClausewitzVariable(String name, int order, String value) {
        super(name, order);
        setValue(value);
    }

    public ClausewitzVariable(String name, int order, int value) {
        super(name, order);
        setValue(value);
    }

    public ClausewitzVariable(String name, int order, long value) {
        super(name, order);
        setValue(value);
    }

    public ClausewitzVariable(String name, int order, double value) {
        super(name, order);
        setValue(value);
    }

    public ClausewitzVariable(String name, int order, boolean value) {
        super(name, order);
        setValue(value);
    }

    public ClausewitzVariable(String name, int order, LocalDate value) {
        this(name, order, value, false);
    }

    public ClausewitzVariable(String name, int order, LocalDate value, boolean quotes) {
        super(name, order);
        setValue(value, quotes);
    }

    public ClausewitzVariable(ClausewitzVariable other) {
        super(other);
        this.value = other.value;
    }

    public String getValue() {
        return this.value;
    }

    public Integer getAsInt() {
        String s = getValue();

        if (ClausewitzUtils.isNotBlank(s)) {
            return Integer.parseInt(ClausewitzUtils.removeQuotes(s));
        } else {
            return null;
        }
    }

    public Long getAsLong() {
        String s = getValue();

        if (ClausewitzUtils.isNotBlank(s)) {
            return Long.parseLong(ClausewitzUtils.removeQuotes(s));
        } else {
            return null;
        }
    }

    public Double getAsDouble() {
        String s = getValue();

        if (ClausewitzUtils.isNotBlank(s)) {
            return Double.parseDouble(ClausewitzUtils.removeQuotes(s));
        } else {
            return null;
        }
    }

    public Boolean getAsBool() {
        String s = getValue();

        if (ClausewitzUtils.isNotBlank(s)) {
            return "yes".equals(ClausewitzUtils.removeQuotes(s));
        } else {
            return null;
        }
    }

    public LocalDate getAsDate() {
        String s = getValue();

        if (ClausewitzUtils.isNotBlank(s)) {
            try {
                return ClausewitzUtils.stringToDate(ClausewitzUtils.removeQuotes(s));
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(int value) {
        setValue(Integer.toString(value));
    }

    public void setValue(long value) {
        setValue(Long.toString(value));
    }

    public void setValue(double value) {
        setValue(ClausewitzUtils.doubleToString(value));
    }

    public void setValue(boolean value) {
        setValue(value ? "yes" : "no");
    }

    public void setValue(LocalDate value) {
        setValue(value, false);
    }

    public void setValue(LocalDate value, boolean quotes) {
        if (quotes) {
            setValue(ClausewitzUtils.addQuotes(ClausewitzUtils.dateToString(value)));
        } else {
            setValue(ClausewitzUtils.dateToString(value));
        }
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
    public void write(BufferedWriter bufferedWriter, int depth, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException {
        write(bufferedWriter, false, depth, listeners);
    }

    @Override
    public void write(BufferedWriter bufferedWriter, boolean spaced, int depth, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException {
        ClausewitzUtils.printTabs(bufferedWriter, depth);
        bufferedWriter.write(this.name);
        ClausewitzUtils.printEquals(bufferedWriter, spaced);
        bufferedWriter.write(this.value);
    }
}
