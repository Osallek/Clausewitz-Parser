package com.osallek.clausewitzparser.model;

import com.osallek.clausewitzparser.common.Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ClausewitzItem extends ClausewitzObject {

    private final List<ClausewitzItem> children;

    private final List<ClausewitzVariable> variables;

    private final List<ClausewitzList> lists;

    private int index;

    private final boolean hasEquals;

    public ClausewitzItem() {
        this(null, "root", 0);
    }

    ClausewitzItem(ClausewitzItem parent, String name, int order) {
        this(parent, name, order, true);
    }

    ClausewitzItem(ClausewitzItem parent, String name, int order, boolean hasEquals) {
        super(name, parent, order);
        this.hasEquals = hasEquals;
        this.children = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.lists = new ArrayList<>();
    }

    ClausewitzItem(ClausewitzItem other) {
        super(other);
        this.children = other.children;
        this.variables = other.variables;
        this.lists = other.lists;
        this.index = other.index;
        this.hasEquals = other.hasEquals;
    }

    public void addChild(ClausewitzItem child) {
        if (child == null) {
            throw new NullPointerException("Can't add a null child");
        }

        child.order = this.index;
        this.index++;
        this.children.add(child);
    }

    public ClausewitzItem addChild(String name) {
        ClausewitzItem child = new ClausewitzItem(this, name, this.index);
        addChild(child);
        return child;
    }

    public ClausewitzItem addChild(String name, boolean hasEquals) {
        ClausewitzItem child = new ClausewitzItem(this, name, this.index, hasEquals);
        addChild(child);
        return child;
    }

    public void removeChild(String childName) {
        for (int i = 0; i < this.children.size(); i++) {
            if (this.children.get(i).getName().equalsIgnoreCase(childName)) {
                this.children.remove(i);
                break;
            }
        }
    }

    public void removeLastChild(String childName) {
        for (int i = this.children.size() - 1; i >= 0; i--) {
            if (this.children.get(i).getName().equalsIgnoreCase(childName)) {
                this.children.remove(i);
                break;
            }
        }
    }

    public void addVariable(ClausewitzVariable variable) {
        if (variable == null) {
            throw new NullPointerException("Can't add a null variable");
        }

        variable.order = this.index;
        this.index++;
        this.variables.add(variable);
    }

    public ClausewitzVariable addVariable(String name, String value) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, this.index, value);
        addVariable(variable);
        return variable;
    }

    public ClausewitzList addList(ClausewitzList list) {
        list.order = this.index;
        this.index++;
        this.lists.add(list);

        return list;
    }

    public ClausewitzList addToExistingList(String name, String value) {
        ClausewitzList list = getList(name);

        if (list == null) {
            list = new ClausewitzList(this, name, this.index);
            list.add(value);
            addList(list);
        } else {
            list.add(value);
        }

        return list;
    }

    public ClausewitzList addToExistingList(String name, String[] values, boolean sameLine) {
        ClausewitzList list = getList(name);

        if (list == null) {
            list = new ClausewitzList(this, name, this.index, sameLine);
            list.addAll(values);
            addList(list);
        } else {
            list.addAll(values);
        }

        return list;
    }

    public ClausewitzList addToExistingList(String name, String[] values) {
        ClausewitzList list = getList(name);

        if (list == null) {
            list = new ClausewitzList(this, name, this.index);
            list.addAll(values);
            addList(list);
        } else {
            list.addAll(values);
        }

        return list;
    }

    public ClausewitzList addList(String name, String value) {
        ClausewitzList list = new ClausewitzList(this, name, this.index);
        list.add(value);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, String[] values) {
        ClausewitzList list = new ClausewitzList(this, name, this.index);
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, String[] values, boolean sameLine) {
        ClausewitzList list = new ClausewitzList(this, name, this.index, sameLine);
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, List<String> values) {
        ClausewitzList list = new ClausewitzList(this, name, this.index);
        list.addAll(values);
        addList(list);

        return list;
    }

    public int getNbChildren() {
        return this.children.size();
    }

    public ClausewitzItem getChild(int index) {
        if (index < 0 || index >= this.children.size()) {
            return null;
        }

        return this.children.get(index);
    }

    public ClausewitzItem getChild(String childName) {
        for (ClausewitzItem child : this.children) {
            if (child.getName().equalsIgnoreCase(childName)) {
                return child;
            }
        }

        return null;
    }

    public ClausewitzItem getLastChild(String childName) {
        for (int i = this.children.size() - 1; i >= 0; i--) {
            if (this.children.get(i).getName().equalsIgnoreCase(childName)) {
                return children.get(i);
            }
        }

        return null;
    }

    public List<ClausewitzItem> getChildren(String name) {
        List<ClausewitzItem> list = new ArrayList<>();
        for (ClausewitzItem child : this.children) {
            if (child.getName().equalsIgnoreCase(name)) {
                list.add(child);
            }
        }

        return list;
    }

    public List<ClausewitzItem> getChildrenNot(String name) {
        List<ClausewitzItem> list = new ArrayList<>();
        for (ClausewitzItem child : this.children) {
            if (!child.getName().equalsIgnoreCase(name)) {
                list.add(child);
            }
        }

        return list;
    }
    public ClausewitzList getList(String listName) {
        for (ClausewitzList list : this.lists) {
            if (list.getName().equalsIgnoreCase(listName)) {
                return list;
            }
        }

        return null;
    }

    public ClausewitzVariable getVar(int index) {
        if (index < 0 || index >= this.variables.size()) {
            return null;
        } else {
            return this.variables.get(index);
        }
    }

    public String getVarAsString(String varName) {
        for (ClausewitzVariable var : this.variables) {
            if (var.getName().equalsIgnoreCase(varName)) {
                return var.getValue();
            }
        }

        return null;
    }

    public String getLastVarAsString(String varName) {
        for (int i = this.variables.size() - 1; i >= 0; i--) {
            if (this.variables.get(i).getName().equalsIgnoreCase(varName)) {
                return this.variables.get(i).getValue();
            }
        }

        return null;
    }

    public List<String> getVarsAsStrings(String varName) {
        List<String> list = new ArrayList<>();
        for (ClausewitzVariable var : this.variables) {
            if (var.getName().equalsIgnoreCase(varName)) {
                list.add(var.getValue());
            }
        }

        return list;
    }

    public List<ClausewitzVariable> getVarsNot(String varName) {
        List<ClausewitzVariable> list = new ArrayList<>();
        for (ClausewitzVariable var : this.variables) {
            if (!var.getName().equalsIgnoreCase(varName)) {
                list.add(new ClausewitzVariable(var));
            }
        }

        return list;
    }

    public Integer getVarAsInt(String varName) {
        String var = getVarAsString(varName);

        if (Utils.isNotBlank(var)) {
            return Integer.parseInt(var);
        } else {
            return null;
        }
    }

    public Integer getLastVarAsInt(String varName) {
        String var = getLastVarAsString(varName);

        if (Utils.isNotBlank(var)) {
            return Integer.parseInt(var);
        } else {
            return null;
        }
    }

    public Double getVarAsDouble(String varName) {
        String var = getVarAsString(varName);

        if (Utils.isNotBlank(var)) {
            return Double.parseDouble(var);
        } else {
            return null;
        }
    }

    public Double getLastVarAsDouble(String varName) {
        String var = getLastVarAsString(varName);

        if (Utils.isNotBlank(var)) {
            return Double.parseDouble(var);
        } else {
            return null;
        }
    }

    public Boolean getVarAsBool(String varName) {
        String var = getVarAsString(varName);
        return var == null ? null : var.equalsIgnoreCase("yes");
    }

    public Boolean getLastVarAsBool(String varName) {
        String var = getLastVarAsString(varName);
        return var == null ? null : var.equalsIgnoreCase("yes");
    }

    public List<ClausewitzItem> getChildren() {
        return this.children;
    }

    public List<ClausewitzVariable> getVariables() {
        return this.variables;
    }

    public List<ClausewitzList> getLists() {
        return this.lists;
    }

    public List<ClausewitzObject> getAllOrdered() {
        List<ClausewitzObject> objects = new ArrayList<>(this.children);
        objects.addAll(this.variables);
        objects.addAll(this.lists);
        objects.sort(Comparator.comparingInt(ClausewitzObject::getOrder));

        return objects;
    }

    @Override
    public void write(BufferedWriter bufferedWriter, int depth) throws IOException {
        if (!"root".equals(this.name)) {
            printTabs(bufferedWriter, depth);
            bufferedWriter.write(this.name);
            if (!hasEquals) {
                printOpen(bufferedWriter);
            } else {
                printEqualsOpen(bufferedWriter);
            }

            bufferedWriter.newLine();

            for (ClausewitzObject object : this.getAllOrdered()) {
                object.write(bufferedWriter, depth + 1);
                bufferedWriter.newLine();
            }

            printTabs(bufferedWriter, depth);
            printClose(bufferedWriter);
        } else {
            List<ClausewitzObject> objects = this.getAllOrdered();

            for (int i = 0; i < objects.size(); i++) {
                objects.get(i).write(bufferedWriter, depth);

                if (i != objects.size() - 1) {
                    bufferedWriter.newLine();
                }
            }
        }
    }
}
