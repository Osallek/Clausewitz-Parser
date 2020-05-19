package com.osallek.clausewitzparser.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public final class ClausewitzItem extends ClausewitzObject {

    private final List<ClausewitzItem> children;

    private final List<ClausewitzVariable> variables;

    private final List<ClausewitzList> lists;

    private int index;

    private final boolean hasEquals;

    private boolean sameLine = false;

    public ClausewitzItem() {
        this(null, "root", 0);
    }

    public ClausewitzItem(ClausewitzItem parent, String name, int order) {
        this(parent, name, order, true);
    }

    public ClausewitzItem(ClausewitzItem parent, String name, int order, boolean hasEquals) {
        super(name, parent, order);
        this.hasEquals = hasEquals;
        this.children = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.lists = new ArrayList<>();
    }

    public ClausewitzItem(ClausewitzItem other) {
        super(other);
        this.children = other.children;
        this.variables = other.variables;
        this.lists = other.lists;
        this.index = other.index;
        this.hasEquals = other.hasEquals;
    }

    public boolean isSameLine() {
        return sameLine;
    }

    public void setSameLine(boolean sameLine) {
        this.sameLine = sameLine;
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

    public void removeChild(int id) {
        this.children.remove(id);
    }

    public void removeChild(String childName, int id) {
        int j = 0;
        for (int i = 0; i < this.children.size(); i++) {
            if (this.children.get(i).getName().equalsIgnoreCase(childName)) {
                if (j == id) {
                    this.children.remove(i);
                    break;
                }

                j++;
            }
        }
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

    public void removeAllChildren() {
        this.children.clear();
    }

    public void addVariable(ClausewitzVariable variable) {
        addVariable(variable, false);
    }

    public void addVariable(ClausewitzVariable variable, boolean increaseOrders) {
        if (variable == null) {
            throw new NullPointerException("Can't add a null variable");
        }

        if (variable.getValue() == null) {
            return;
        }

        if (increaseOrders) {
            this.getAllOrdered().stream().filter(co -> co.order >= variable.order).forEach(co -> co.order++);
        } else {
            variable.order = this.index;
        }

        this.index++;
        this.variables.add(variable);
    }

    public ClausewitzVariable setVariableName(int index, String name) {
        ClausewitzVariable var = getVar(index);

        if (var != null) {
            var.setName(name);
        }

        return var;
    }

    public ClausewitzVariable setVariable(int index, String value) {
        ClausewitzVariable var = getVar(index);

        if (var != null) {
            var.setValue(value);
        }

        return var;
    }

    public ClausewitzVariable setVariable(int index, int value) {
        ClausewitzVariable var = getVar(index);

        if (var != null) {
            var.setValue(value);
        }

        return var;
    }

    public ClausewitzVariable setVariable(int index, long value) {
        ClausewitzVariable var = getVar(index);

        if (var != null) {
            var.setValue(value);
        }

        return var;
    }

    public ClausewitzVariable setVariable(int index, double value) {
        ClausewitzVariable var = getVar(index);

        if (var != null) {
            var.setValue(value);
        }

        return var;
    }

    public ClausewitzVariable setVariable(int index, boolean value) {
        ClausewitzVariable var = getVar(index);

        if (var != null) {
            var.setValue(value);
        }

        return var;
    }

    public ClausewitzVariable setVariable(int index, Date value) {
        return setVariable(index, value, false);
    }

    public ClausewitzVariable setVariable(int index, Date value, boolean quotes) {
        ClausewitzVariable var = getVar(index);

        if (var != null) {
            var.setValue(value, quotes);
        }

        return var;
    }

    public ClausewitzVariable setVariable(String name, String value) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value);
        } else {
            var = addVariable(name, value);
        }

        return var;
    }

    public ClausewitzVariable setVariable(String name, int value) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value);
        } else {
            var = addVariable(name, value);
        }

        return var;
    }

    public ClausewitzVariable setVariable(String name, double value) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value);
        } else {
            var = addVariable(name, value);
        }

        return var;
    }

    public ClausewitzVariable setVariable(String name, boolean value) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value);
        } else {
            var = addVariable(name, value);
        }

        return var;
    }

    public ClausewitzVariable setVariable(String name, Date value) {
        return setVariable(name, value, false);
    }

    public ClausewitzVariable setVariable(String name, Date value, boolean quotes) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value, quotes);
        } else {
            var = addVariable(name, value, quotes);
        }

        return var;
    }

    public ClausewitzVariable setVariable(String name, String value, int order) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value);
        } else {
            var = addVariable(name, value, order);
        }

        return var;
    }

    public ClausewitzVariable setVariable(String name, int value, int order) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value);
        } else {
            var = addVariable(name, value, order);
        }

        return var;
    }

    public ClausewitzVariable setVariable(String name, double value, int order) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value);
        } else {
            var = addVariable(name, value, order);
        }

        return var;
    }

    public ClausewitzVariable setVariable(String name, boolean value, int order) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value);
        } else {
            var = addVariable(name, value, order);
        }

        return var;
    }

    public ClausewitzVariable setVariable(String name, Date value, int order) {
        return setVariable(name, value, false, order);
    }

    public ClausewitzVariable setVariable(String name, Date value, boolean quotes, int order) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value, quotes);
        } else {
            var = addVariable(name, value, quotes, order);
        }

        return var;
    }

    public ClausewitzVariable addVariable(String name, String value) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, this.index, value);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, int value) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, this.index, value);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, long value) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, this.index, value);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, double value) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, this.index, value);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, boolean value) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, this.index, value);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, Date value) {
        return addVariable(name, value, false);
    }

    public ClausewitzVariable addVariable(String name, Date value, boolean quotes) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, this.index, value, quotes);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, String value, int order) {
        return addVariable(name, value, order, true);
    }

    public ClausewitzVariable addVariable(String name, String value, int order, boolean increaseOrders) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, order, value);
        addVariable(variable, increaseOrders);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, int value, int order) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, order, value);
        addVariable(variable, true);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, long value, int order) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, order, value);
        addVariable(variable, true);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, double value, int order) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, order, value);
        addVariable(variable, true);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, boolean value, int order) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, order, value);
        addVariable(variable, true);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, Date value, int order) {
        return addVariable(name, value, false, order);
    }

    public ClausewitzVariable addVariable(String name, Date value, boolean quotes, int order) {
        ClausewitzVariable variable = new ClausewitzVariable(this, name, order, value, quotes);
        addVariable(variable, true);
        return variable;
    }

    public void removeVariable(int id) {
        this.variables.remove(id);
    }

    public void removeVariable(String varName, int id) {
        int j = 0;
        for (int i = 0; i < this.variables.size(); i++) {
            if (this.variables.get(i).getName().equalsIgnoreCase(varName)) {
                j++;

                if (j == id) {
                    this.variables.remove(i);
                    break;
                }
            }
        }
    }

    public void removeVariable(String varName) {
        for (int i = 0; i < this.variables.size(); i++) {
            if (this.variables.get(i).getName().equalsIgnoreCase(varName)) {
                this.variables.remove(i);
                break;
            }
        }
    }

    public void removeVariable(String varName, String value) {
        for (int i = 0; i < this.variables.size(); i++) {
            ClausewitzVariable variable = this.variables.get(i);

            if (variable.getName().equalsIgnoreCase(varName) && variable.getValue().equalsIgnoreCase(value)) {
                this.variables.remove(i);
                break;
            }
        }
    }

    public void removeLastVariable(String varName) {
        for (int i = this.variables.size() - 1; i >= 0; i--) {
            if (this.variables.get(i).getName().equalsIgnoreCase(varName)) {
                this.variables.remove(i);
                break;
            }
        }
    }

    public void removeAllVariables() {
        this.variables.clear();
    }

    public void removeVariableByValue(String value) {
        for (int i = 0; i < this.variables.size(); i++) {
            if (this.variables.get(i).getValue().equalsIgnoreCase(value)) {
                this.variables.remove(i);
                break;
            }
        }
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

    public ClausewitzList addList(String name, Integer value) {
        ClausewitzList list = new ClausewitzList(this, name, this.index);
        list.add(value);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, String... values) {
        ClausewitzList list = new ClausewitzList(this, name, this.index);
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, boolean sameLine, String... values) {
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

    public ClausewitzList addList(String name, Integer... values) {
        ClausewitzList list = new ClausewitzList(this, name, this.index);
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, Double... values) {
        ClausewitzList list = new ClausewitzList(this, name, this.index);
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, Boolean... values) {
        ClausewitzList list = new ClausewitzList(this, name, this.index);
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList changeChildToList(int childIndex, String listName, String value) {
        for (int i = 0; i < this.children.size(); i++) {
            if (childIndex == this.children.get(i).order) {
                this.children.remove(i);
                break;
            }
        }

        ClausewitzList list = new ClausewitzList(this, listName, childIndex);
        list.add(value);
        this.lists.add(list);
        this.lists.sort(Comparator.comparingInt(ClausewitzObject::getOrder));

        return list;
    }

    public ClausewitzList changeChildToList(int childOrder, String listName, boolean sameLine, String... values) {
        for (int i = 0; i < this.children.size(); i++) {
            if (childOrder == this.children.get(i).order) {
                this.children.remove(i);
                break;
            }
        }

        ClausewitzList list = new ClausewitzList(this, listName, childOrder, sameLine);
        list.addAll(values);
        this.lists.add(list);
        this.lists.sort(Comparator.comparingInt(ClausewitzObject::getOrder));

        return list;
    }

    public void removeList(int id) {
        this.lists.remove(id);
    }

    public void removeList(String varName) {
        for (int i = 0; i < this.lists.size(); i++) {
            if (this.lists.get(i).getName().equalsIgnoreCase(varName)) {
                this.lists.remove(i);
                break;
            }
        }
    }

    public void removeAllLists() {
        this.lists.clear();
    }

    public void removeAll() {
        this.removeAllChildren();
        this.removeAllVariables();
        this.removeAllLists();
    }

    public int getNbChildren() {
        return this.children.size();
    }

    public int getNbVariables() {
        return this.variables.size();
    }

    public int getNbLists() {
        return this.lists.size();
    }

    public int getNbObjects() {
        return getNbChildren() + getNbVariables() + getNbLists();
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

    public ClausewitzItem getChild(String childName, int index) {
        int j = 0;
        for (ClausewitzItem child : this.children) {
            if (child.getName().equalsIgnoreCase(childName)) {
                if (j == index) {
                    return child;
                }

                j++;
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

    public List<ClausewitzList> getLists(String varName) {
        List<ClausewitzList> clausewitzLists = new ArrayList<>();
        for (ClausewitzList list : this.lists) {
            if (list.getName().equalsIgnoreCase(varName)) {
                clausewitzLists.add(list);
            }
        }

        return clausewitzLists;
    }

    public ClausewitzVariable getVar(int index) {
        if (index < 0 || index >= this.variables.size()) {
            return null;
        } else {
            return this.variables.get(index);
        }
    }

    public ClausewitzVariable getVar(String varName) {
        for (ClausewitzVariable var : this.variables) {
            if (var.getName().equalsIgnoreCase(varName)) {
                return var;
            }
        }

        return null;
    }

    public ClausewitzVariable getVar(String varName, int index) {
        int j = 0;
        for (ClausewitzVariable variable : this.variables) {
            if (variable.getName().equalsIgnoreCase(varName)) {
                if (j == index) {
                    return variable;
                }

                j++;
            }
        }

        return null;
    }

    public ClausewitzVariable getLastVar(String varName) {
        for (int i = this.variables.size() - 1; i >= 0; i--) {
            if (this.variables.get(i).getName().equalsIgnoreCase(varName)) {
                return this.variables.get(i);
            }
        }

        return null;
    }

    public String getVarAsString(int index) {
        ClausewitzVariable var = getVar(index);

        if (var != null) {
            return var.getValue();
        } else {
            return null;
        }
    }

    public String getVarAsString(String varName) {
        ClausewitzVariable var = getVar(varName);

        if (var != null) {
            return var.getValue();
        } else {
            return null;
        }
    }

    public String getVarAsString(String varName, int index) {
        ClausewitzVariable var = getVar(varName, index);

        if (var != null) {
            return var.getValue();
        } else {
            return null;
        }
    }

    public String getLastVarAsString(String varName) {
        for (int i = this.variables.size() - 1; i >= 0; i--) {
            if (this.variables.get(i).getName().equalsIgnoreCase(varName)) {
                return this.variables.get(i).getValue();
            }
        }

        return null;
    }

    public List<ClausewitzVariable> getVars(String varName) {
        List<ClausewitzVariable> vars = new ArrayList<>();
        for (ClausewitzVariable var : this.variables) {
            if (var.getName().equalsIgnoreCase(varName)) {
                vars.add(var);
            }
        }

        return vars;
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

    public Integer getVarAsInt(int index) {
        ClausewitzVariable var = getVar(index);
        return var == null ? null : var.getAsInt();
    }

    public Integer getVarAsInt(String varName) {
        ClausewitzVariable var = getVar(varName);
        return var == null ? null : var.getAsInt();
    }

    public Integer getVarAsInt(String varName, int index) {
        ClausewitzVariable var = getVar(varName, index);
        return var == null ? null : var.getAsInt();
    }

    public Integer getLastVarAsInt(String varName) {
        ClausewitzVariable var = getLastVar(varName);
        return var == null ? null : var.getAsInt();
    }

    public Long getVarAsLong(int index) {
        ClausewitzVariable var = getVar(index);
        return var == null ? null : var.getAsLong();
    }

    public Long getVarAsLong(String varName) {
        ClausewitzVariable var = getVar(varName);
        return var == null ? null : var.getAsLong();
    }

    public Long getVarAsLong(String varName, int index) {
        ClausewitzVariable var = getVar(varName, index);
        return var == null ? null : var.getAsLong();
    }

    public Long getLastVarAsLong(String varName) {
        ClausewitzVariable var = getLastVar(varName);
        return var == null ? null : var.getAsLong();
    }

    public Double getVarAsDouble(int index) {
        ClausewitzVariable var = getVar(index);
        return var == null ? null : var.getAsDouble();
    }

    public Double getVarAsDouble(String varName) {
        ClausewitzVariable var = getVar(varName);
        return var == null ? null : var.getAsDouble();
    }

    public Double getVarAsDouble(String varName, int index) {
        ClausewitzVariable var = getVar(varName, index);
        return var == null ? null : var.getAsDouble();
    }

    public Double getLastVarAsDouble(String varName) {
        ClausewitzVariable var = getLastVar(varName);
        return var == null ? null : var.getAsDouble();
    }

    public Boolean getVarAsBool(int index) {
        ClausewitzVariable var = getVar(index);
        return var == null ? null : var.getAsBool();
    }

    public Boolean getVarAsBool(String varName) {
        ClausewitzVariable var = getVar(varName);
        return var == null ? null : var.getAsBool();
    }

    public Boolean getVarAsBool(String varName, int index) {
        ClausewitzVariable var = getVar(varName, index);
        return var == null ? null : var.getAsBool();
    }

    public Boolean getLastVarAsBool(String varName) {
        ClausewitzVariable var = getLastVar(varName);
        return var == null ? null : var.getAsBool();
    }

    public Date getVarAsDate(int index) {
        ClausewitzVariable var = getVar(index);
        return var == null ? null : var.getAsDate();
    }

    public Date getVarAsDate(String varName) {
        ClausewitzVariable var = getVar(varName);
        return var == null ? null : var.getAsDate();
    }

    public Date getVarAsDate(String varName, int index) {
        ClausewitzVariable var = getVar(varName, index);
        return var == null ? null : var.getAsDate();
    }

    public Date getLastVarAsDate(String varName) {
        ClausewitzVariable var = getLastVar(varName);
        return var == null ? null : var.getAsDate();
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

    public List<ClausewitzObject> getAllOrdered(String name) {
        List<ClausewitzObject> objects = new ArrayList<>(getChildren(name));
        objects.addAll(getVars(name));
        objects.addAll(getLists(name));
        objects.sort(Comparator.comparingInt(ClausewitzObject::getOrder));

        return objects;
    }

    public void removeByOrder(int order) {
        for (int i = 0; i < this.lists.size(); i++) {
            if (this.lists.get(i).getOrder() == order) {
                this.lists.remove(i);
                break;
            }
        }

        for (int i = 0; i < this.variables.size(); i++) {
            if (this.variables.get(i).getOrder() == order) {
                this.variables.remove(i);
                break;
            }
        }

        for (int i = 0; i < this.children.size(); i++) {
            if (this.children.get(i).getOrder() == order) {
                this.children.remove(i);
                break;
            }
        }
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

            if (this.sameLine) {
                printTabs(bufferedWriter, depth + 1);
                for (ClausewitzObject object : this.getAllOrdered()) {
                    object.write(bufferedWriter, 0);
                    printSpace(bufferedWriter);
                }

                bufferedWriter.newLine();
            } else {
                for (ClausewitzObject object : this.getAllOrdered()) {
                    object.write(bufferedWriter, depth + 1);
                    bufferedWriter.newLine();
                }
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
