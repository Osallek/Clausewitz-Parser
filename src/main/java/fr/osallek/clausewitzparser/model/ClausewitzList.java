package fr.osallek.clausewitzparser.model;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
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

    ClausewitzList(ClausewitzItem parent, String name, int order) {
        this(parent, name, order, false);
    }

    ClausewitzList(ClausewitzItem parent, String name, int order, boolean sameLine) {
        super(name, order, parent);
        this.sameLine = sameLine;
    }

    ClausewitzList(ClausewitzList other) {
        super(other);
        this.values = other.values;
        this.sameLine = other.sameLine;
    }

    private List<String> getInternalValues() {
        if (this.values == null) {
            this.values = new ArrayList<>(0);
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
        String var = get(id);

        if (ClausewitzUtils.isNotBlank(var)) {
            return Integer.parseInt(var);
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

    public LocalDate getAsDate(int id) {
        String var = get(id);

        if (ClausewitzUtils.isNotBlank(var)) {
            try {
                return ClausewitzUtils.stringToDate(ClausewitzUtils.removeQuotes(var));
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
            getInternalValues().add(val.intern());
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

    public void change(String previous, String newOne) {
        if (this.values != null) {
            if (ClausewitzUtils.isNotBlank(newOne)) {
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

    public boolean isSameLine() {
        return sameLine;
    }

    public boolean isEmpty() {
        return this.values == null || this.values.isEmpty();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ClausewitzList)) {
            return false;
        }

        final ClausewitzList clausewitzList = (ClausewitzList) obj;

        return name.equals(clausewitzList.name)
               && values.equals(clausewitzList.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, values);
    }

    @Override
    public void write(BufferedWriter bufferedWriter, int depth, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException {
        listeners.entrySet().stream().filter(entry -> entry.getKey().test(this)).forEach(entry -> entry.getValue().accept(this.getName()));
        ClausewitzUtils.printTabs(bufferedWriter, depth);

        if (ClausewitzUtils.isNotBlank(this.name)) {
            bufferedWriter.write(this.name);
            ClausewitzUtils.printEquals(bufferedWriter);
        }

        ClausewitzUtils.printOpen(bufferedWriter);
        bufferedWriter.newLine();

        if (this.sameLine) {
            ClausewitzUtils.printTabs(bufferedWriter, depth + 1);

            for (String str : getInternalValues()) {
                bufferedWriter.write(str);
                ClausewitzUtils.printSpace(bufferedWriter);
            }

            bufferedWriter.newLine();
        } else {
            for (String str : getInternalValues()) {
                ClausewitzUtils.printTabs(bufferedWriter, depth + 1);
                bufferedWriter.write(str);
                bufferedWriter.newLine();
            }
        }

        ClausewitzUtils.printTabs(bufferedWriter, depth);
        ClausewitzUtils.printClose(bufferedWriter);
    }
}