package fr.osallek.clausewitzparser.model;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ClausewitzList extends ClausewitzPObject {

    private List<String> values;

    private final boolean sameLine;

    private final boolean hasBrackets;

    public ClausewitzList(ClausewitzItem parent, String name, int order) {
        this(parent, name, order, null);
    }

    public ClausewitzList(ClausewitzItem parent, String name, int order, Boolean sameLine) {
        this(parent, name, order, sameLine, true);
    }

    public ClausewitzList(ClausewitzItem parent, String name, int order, Boolean sameLine, boolean hasBrackets) {
        this(parent, name, order, sameLine, hasBrackets, false);
    }

    public ClausewitzList(ClausewitzItem parent, String name, int order, Boolean sameLine, boolean hasBrackets, boolean increaseOrder) {
        super(name, order, parent, increaseOrder);
        this.sameLine = sameLine != null && sameLine;
        this.hasBrackets = hasBrackets;

        if (increaseOrder) {
            parent.getAllOrdered().stream().filter(co -> !co.equals(this)).filter(co -> co.order >= order).forEach(co -> co.order++);
        }
    }

    public ClausewitzList(ClausewitzList other) {
        super(other);
        this.values = other.values;
        this.sameLine = other.sameLine;
        this.hasBrackets = other.hasBrackets;
    }

    private List<String> getInternalValues() {
        if (this.values == null) {
            this.values = new ArrayList<>(1);
        }

        return this.values;
    }

    public String get(int id) {
        if (id < 0 || this.values == null || id >= this.values.size()) {
            return null;
        }

        return this.values.get(id);
    }

    public Integer getAsInt(int id) {
        String s = get(id);

        if (ClausewitzUtils.isNotBlank(s)) {
            return Integer.parseInt(s);
        } else {
            return null;
        }
    }

    public Double getAsDouble(int id) {
        String s = get(id);

        if (ClausewitzUtils.isNotBlank(s)) {
            return Double.parseDouble(s);
        } else {
            return null;
        }
    }

    public Boolean getAsBool(int id) {
        String s = get(id);

        if (ClausewitzUtils.isNotBlank(s)) {
            return "yes".equals(s);
        } else {
            return null;
        }
    }

    public LocalDate getAsDate(int id) {
        String s = get(id);

        if (ClausewitzUtils.isNotBlank(s)) {
            try {
                return ClausewitzUtils.stringToDate(ClausewitzUtils.removeQuotes(s));
            } catch (DateTimeException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public int size() {
        return this.values == null ? 0 : this.values.size();
    }

    public int indexOf(String val) {
        return this.values == null ? -1 : this.values.indexOf(val);
    }

    public boolean remove(int id) {
        if (this.values != null) {
            return this.values.remove(id) != null;
        }

        return false;
    }

    public boolean remove(String value) {
        if (this.values != null) {
            for (int i = 0; i < this.values.size(); i++) {
                if (this.values.get(i).equalsIgnoreCase(value)) {
                    return this.values.remove(i) != null;
                }
            }
        }

        return false;
    }

    public boolean removeLast(String value) {
        if (this.values != null) {
            for (int i = this.values.size() - 1; i >= 0; i--) {
                if (this.values.get(i).equalsIgnoreCase(value)) {
                    return this.values.remove(i) != null;
                }
            }
        }

        return false;
    }

    public boolean removeAll(String value) {
        if (this.values != null) {
            return this.values.removeIf(s -> s.equalsIgnoreCase(value));
        }

        return false;
    }

    public void clear() {
        if (this.values != null) {
            this.values.clear();
        }
    }

    public void add(String val) {
        if (ClausewitzUtils.isNotBlank(val)) {
            if (val.indexOf(' ') >= 0 && !ClausewitzUtils.hasQuotes(val)) {
                val = ClausewitzUtils.addQuotes(val);
            }

            getInternalValues().add(val);
        }
    }

    public void add(int val) {
        add(Integer.toString(val));
    }

    public void add(double val) {
        add(String.format(Locale.ENGLISH, "%.3f", val));
    }

    public void add(boolean val) {
        add(val ? "yes" : "no");
    }

    public void add(LocalDate date) {
        add(ClausewitzUtils.dateToString(date));
    }

    public void set(int id, String val) {
        if (ClausewitzUtils.isNotBlank(val)) {
            getInternalValues().set(id, val);
        }
    }

    public void set(int id, int val) {
        set(id, Integer.toString(val));
    }

    public void set(int id, double val) {
        set(id, String.format(Locale.ENGLISH, "%.3f", val));
    }

    public void set(int id, boolean val) {
        set(id, val ? "yes" : "no");
    }

    public void set(int id, LocalDate val) {
        set(id, ClausewitzUtils.dateToString(val));
    }

    public void setAll(List<String> values) {
        clear();
        addAll(values);
    }

    public void setAll(String... values) {
        clear();
        addAll(values);
    }

    public void setAll(Integer... values) {
        clear();
        addAll(values);
    }

    public void setAll(Double... values) {
        clear();
        addAll(values);
    }

    public void setAll(Boolean... values) {
        clear();
        addAll(values);
    }

    public void change(String previous, String newOne) {
        if (this.values != null && ClausewitzUtils.isNotBlank(newOne)) {
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
    }

    public void addAll(List<String> values) {
        values.forEach(this::add);
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
        if (this.values == null) {
            return false;
        }

        return this.values.contains(val);
    }

    public boolean contains(int val) {
        return contains(String.valueOf(val));
    }

    public boolean contains(double val) {
        return contains(String.format(Locale.ENGLISH, "%.3f", val));
    }

    public boolean contains(boolean val) {
        return contains(val ? "yes" : "no");
    }

    public List<String> getValues() {
        if (this.values != null) {
            return new ArrayList<>(this.values);
        }

        return new ArrayList<>();
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

    public void sort(Comparator<String> comparator) {
        if (this.values != null) {
            this.values.sort(comparator);
        }
    }

    public void sort() {
        sort(Comparator.naturalOrder());
    }

    public void sortInt() {
        sort(Comparator.comparingInt(Integer::parseInt));
    }

    public void sortDouble() {
        sort(Comparator.comparingDouble(Double::parseDouble));
    }

    public boolean isSameLine() {
        return sameLine;
    }

    public boolean isHasBrackets() {
        return hasBrackets;
    }

    public boolean isEmpty() {
        return this.values == null || this.values.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ClausewitzList clausewitzList)) {
            return false;
        }

        return name.equals(clausewitzList.name) && values.equals(clausewitzList.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, values);
    }

    @Override
    public void write(BufferedWriter bufferedWriter, int depth, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException {
        write(bufferedWriter, false, depth, listeners);
    }

    @Override
    public void write(BufferedWriter bufferedWriter, boolean spaced, int depth,
                      Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException {
        listeners.entrySet().stream().filter(entry -> entry.getKey().test(this)).forEach(entry -> entry.getValue().accept(this.getName()));
        ClausewitzUtils.printTabs(bufferedWriter, depth);

        if (ClausewitzUtils.isNotBlank(this.name)) {
            bufferedWriter.write(this.name);
            ClausewitzUtils.printEquals(bufferedWriter, spaced);
        }

        if (this.hasBrackets) {
            ClausewitzUtils.printOpen(bufferedWriter);
            bufferedWriter.newLine();
        }

        if (this.sameLine) {
            if (this.hasBrackets) {
                ClausewitzUtils.printTabs(bufferedWriter, depth + 1);
            }

            for (String str : getInternalValues()) {
                bufferedWriter.write(str);
                ClausewitzUtils.printSpace(bufferedWriter);
            }

            if (this.hasBrackets) {
                bufferedWriter.newLine();
            }
        } else {
            for (String str : getInternalValues()) {
                ClausewitzUtils.printTabs(bufferedWriter, depth + 1);
                bufferedWriter.write(str);
                bufferedWriter.newLine();
            }
        }

        if (this.hasBrackets) {
            ClausewitzUtils.printTabs(bufferedWriter, depth);
            ClausewitzUtils.printClose(bufferedWriter);
        }
    }
}
