package com.osallek.clausewitzparser.model;

import com.osallek.clausewitzparser.common.ClausewitzUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ClausewitzItem extends ClausewitzPObject {

    public static final String DEFAULT_NAME = "clausewitzparser";

    private List<ClausewitzItem> children;

    private List<ClausewitzVariable> variables;

    private List<ClausewitzList> lists;

    private boolean sameLine = false;

    private final boolean hasEquals;

    public ClausewitzItem() {
        this(null, DEFAULT_NAME, 0);
    }

    public ClausewitzItem(ClausewitzItem parent, String name, int order) {
        this(parent, name, order, true);
    }

    public ClausewitzItem(ClausewitzItem parent, String name, int order, boolean hasEquals) {
        super(name, order, parent);
        this.hasEquals = hasEquals;
    }

    public boolean isSameLine() {
        return sameLine;
    }

    public void setSameLine(boolean sameLine) {
        this.sameLine = sameLine;
    }

    private List<ClausewitzItem> getInternalChildren() {
        if (this.children == null) {
            this.children = new ArrayList<>(0);
        }

        return this.children;
    }

    private List<ClausewitzVariable> getInternalVariables() {
        if (this.variables == null) {
            this.variables = new ArrayList<>(0);
        }

        return this.variables;
    }

    private List<ClausewitzList> getInternalLists() {
        if (this.lists == null) {
            this.lists = new ArrayList<>(0);
        }

        return this.lists;
    }

    public void addChild(ClausewitzItem child) {
        if (child == null) {
            throw new NullPointerException("Can't add a null child");
        }

        child.order = getNbObjects();
        getInternalChildren().add(child);
    }

    public ClausewitzItem addChild(String name) {
        ClausewitzItem child = new ClausewitzItem(this, name, getNbObjects());
        addChild(child);
        return child;
    }

    public ClausewitzItem addChild(String name, boolean hasEquals) {
        ClausewitzItem child = new ClausewitzItem(this, name, getNbObjects(), hasEquals);
        addChild(child);
        return child;
    }

    public void removeChild(int id) {
        if (this.children != null) {
            this.children.remove(id);
        }
    }

    public void removeChild(String childName, int id) {
        if (this.children != null) {
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
    }

    public void removeChild(String childName) {
        if (this.children != null) {
            for (int i = 0; i < this.children.size(); i++) {
                if (this.children.get(i).getName().equalsIgnoreCase(childName)) {
                    this.children.remove(i);
                    break;
                }
            }
        }
    }

    public void removeLastChild(String childName) {
        if (this.children != null) {
            for (int i = this.children.size() - 1; i >= 0; i--) {
                if (this.children.get(i).getName().equalsIgnoreCase(childName)) {
                    this.children.remove(i);
                    break;
                }
            }
        }
    }

    public void removeChildren(String childName) {
        if (this.children != null) {
            this.children.removeIf(child -> child.getName().equalsIgnoreCase(childName));
        }
    }

    public void removeAllChildren() {
        if (this.children != null) {
            this.children.clear();
        }
    }

    public void removeChildIf(Predicate<ClausewitzItem> filter) {
        this.children.removeIf(filter);
    }

    public void addVariable(ClausewitzVariable variable) {
        addVariable(variable, false);
    }

    public void addVariable(ClausewitzVariable variable, boolean increaseOrders) {
        if (variable == null) {
            throw new NullPointerException("Can't add a null variable");
        }

        if (increaseOrders) {
            this.getAllOrdered().stream().filter(co -> co.order >= variable.order).forEach(co -> co.order++);
        } else {
            variable.order = getNbObjects();
        }

        getInternalVariables().add(variable);
    }

    public void removeVariableIf(Predicate<ClausewitzVariable> filter) {
        this.variables.removeIf(filter);
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

    public ClausewitzVariable setVariable(int index, LocalDate value) {
        return setVariable(index, value, false);
    }

    public ClausewitzVariable setVariable(int index, LocalDate value, boolean quotes) {
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

    public ClausewitzVariable setVariable(String name, LocalDate value) {
        return setVariable(name, value, false);
    }

    public ClausewitzVariable setVariable(String name, LocalDate value, boolean quotes) {
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

    public ClausewitzVariable setVariable(String name, LocalDate value, int order) {
        return setVariable(name, value, false, order);
    }

    public ClausewitzVariable setVariable(String name, LocalDate value, boolean quotes, int order) {
        ClausewitzVariable var = getVar(name);

        if (var != null) {
            var.setValue(value, quotes);
        } else {
            var = addVariable(name, value, quotes, order);
        }

        return var;
    }

    public ClausewitzVariable addVariable(String name, String value) {
        ClausewitzVariable variable = new ClausewitzVariable(name, getNbObjects(), value);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, int value) {
        ClausewitzVariable variable = new ClausewitzVariable(name, getNbObjects(), value);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, double value) {
        ClausewitzVariable variable = new ClausewitzVariable(name, getNbObjects(), value);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, boolean value) {
        ClausewitzVariable variable = new ClausewitzVariable(name, getNbObjects(), value);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, LocalDate value) {
        return addVariable(name, value, false);
    }

    public ClausewitzVariable addVariable(String name, LocalDate value, boolean quotes) {
        ClausewitzVariable variable = new ClausewitzVariable(name, getNbObjects(), value, quotes);
        addVariable(variable);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, String value, int order) {
        return addVariable(name, value, order, true);
    }

    public ClausewitzVariable addVariable(String name, String value, int order, boolean increaseOrders) {
        ClausewitzVariable variable = new ClausewitzVariable(name, order, value);
        addVariable(variable, increaseOrders);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, int value, int order) {
        ClausewitzVariable variable = new ClausewitzVariable(name, order, value);
        addVariable(variable, true);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, double value, int order) {
        ClausewitzVariable variable = new ClausewitzVariable(name, order, value);
        addVariable(variable, true);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, boolean value, int order) {
        ClausewitzVariable variable = new ClausewitzVariable(name, order, value);
        addVariable(variable, true);
        return variable;
    }

    public ClausewitzVariable addVariable(String name, LocalDate value, int order) {
        return addVariable(name, value, false, order);
    }

    public ClausewitzVariable addVariable(String name, LocalDate value, boolean quotes, int order) {
        ClausewitzVariable variable = new ClausewitzVariable(name, order, value, quotes);
        addVariable(variable, true);
        return variable;
    }

    public void removeVariable(int id) {
        if (this.variables != null) {
            this.variables.remove(id);
        }
    }

    public void removeVariable(String varName, int id) {
        if (this.variables != null) {
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
    }

    public void removeVariable(String varName) {
        if (this.variables != null) {
            for (int i = 0; i < this.variables.size(); i++) {
                if (this.variables.get(i).getName().equalsIgnoreCase(varName)) {
                    this.variables.remove(i);
                    break;
                }
            }
        }
    }

    public void removeVariables(String varName) {
        if (this.variables != null) {
            this.variables.removeIf(variable -> variable.getName().equalsIgnoreCase(varName));
        }
    }

    public void removeVariable(String varName, String value) {
        if (this.variables != null) {
            for (int i = 0; i < this.variables.size(); i++) {
                ClausewitzVariable variable = this.variables.get(i);

                if (variable.getName().equalsIgnoreCase(varName) && variable.getValue().equalsIgnoreCase(value)) {
                    this.variables.remove(i);
                    break;
                }
            }
        }
    }

    public void removeLastVariable(String varName) {
        if (this.variables != null) {
            for (int i = this.variables.size() - 1; i >= 0; i--) {
                if (this.variables.get(i).getName().equalsIgnoreCase(varName)) {
                    this.variables.remove(i);
                    break;
                }
            }
        }
    }

    public void removeAllVariables() {
        if (this.variables != null) {
            this.variables.clear();
        }
    }

    public void removeVariableByValue(String value) {
        if (this.variables != null) {
            for (int i = 0; i < this.variables.size(); i++) {
                if (this.variables.get(i).getValue().equalsIgnoreCase(value)) {
                    this.variables.remove(i);
                    break;
                }
            }
        }
    }

    public ClausewitzList addList(ClausewitzList list) {
        list.order = getNbObjects();
        getInternalLists().add(list);

        return list;
    }

    public ClausewitzList addList(String name, boolean sameLine, String... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects(), sameLine);
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList addToExistingList(String name, String... values) {
        return addToExistingList(name, false, values);
    }

    public ClausewitzList addToExistingList(String name, boolean sameLine, String... values) {
        ClausewitzList list = getList(name);

        if (list == null) {
            list = new ClausewitzList(this, name, getNbObjects(), sameLine);
            list.addAll(values);
            addList(list);
        } else {
            list.addAll(values);
        }

        return list;
    }

    public ClausewitzList addToLastExistingList(String name, String... values) {
        return addToLastExistingList(name, false, values);
    }

    public ClausewitzList addToLastExistingList(String name, boolean sameLine, String... values) {
        ClausewitzList list = getLastList(name);

        if (list == null) {
            list = new ClausewitzList(this, name, getNbObjects(), sameLine);
            list.addAll(values);
            addList(list);
        } else {
            list.addAll(values);
        }

        return list;
    }

    public ClausewitzList addList(String name, String value) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.add(value);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, Integer value) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.add(value);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, String... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, List<String> values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, Integer... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, Double... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList addList(String name, Boolean... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.addAll(values);
        addList(list);

        return list;
    }

    public ClausewitzList changeChildToList(int childOrder, String listName, String... values) {
        return changeChildToList(childOrder, listName, false, values);
    }

    public ClausewitzList changeChildToList(int childOrder, String listName, boolean sameLine, String... values) {
        if (this.children != null) {
            for (int i = 0; i < this.children.size(); i++) {
                if (childOrder == this.children.get(i).order) {
                    this.children.remove(i);
                    break;
                }
            }
        }

        ClausewitzList list = new ClausewitzList(this, listName, childOrder, sameLine);
        list.addAll(values);
        getInternalLists().add(list);
        this.lists.sort(Comparator.comparingInt(ClausewitzObject::getOrder));

        return list;
    }

    public void removeList(int id) {
        if (this.lists != null) {
            this.lists.remove(id);
        }
    }

    public void removeList(String varName) {
        if (this.lists != null) {
            for (int i = 0; i < this.lists.size(); i++) {
                if (this.lists.get(i).getName().equalsIgnoreCase(varName)) {
                    this.lists.remove(i);
                    break;
                }
            }
        }
    }

    public void removeAllLists() {
        if (this.lists != null) {
            this.lists.clear();
        }
    }

    public void removeListIf(Predicate<ClausewitzList> filter) {
        this.lists.removeIf(filter);
    }

    public void removeAll() {
        this.removeAllChildren();
        this.removeAllVariables();
        this.removeAllLists();
    }

    public int getNbChildren() {
        return this.children == null ? 0 : this.children.size();
    }

    public int getNbVariables() {
        return this.variables == null ? 0 : this.variables.size();
    }

    public int getNbLists() {
        return this.lists == null ? 0 : this.lists.size();
    }

    public int getNbObjects() {
        return getNbChildren() + getNbVariables() + getNbLists();
    }

    public ClausewitzItem getChild(int index) {
        if (index < 0 || this.children == null || index >= this.children.size()) {
            return null;
        }

        return this.children.get(index);
    }

    public ClausewitzItem getChild(String childName) {
        if (this.children != null) {
            for (ClausewitzItem child : this.children) {
                if (child.getName().equalsIgnoreCase(childName)) {
                    return child;
                }
            }
        }

        return null;
    }

    public boolean hasChild(String childName) {
        return getChild(childName) != null;
    }

    public ClausewitzItem getChild(String childName, int index) {
        if (this.children != null) {
            int j = 0;
            for (ClausewitzItem child : this.children) {
                if (child.getName().equalsIgnoreCase(childName)) {
                    if (j == index) {
                        return child;
                    }

                    j++;
                }
            }
        }

        return null;
    }

    public ClausewitzItem getLastChild(String childName) {
        if (this.children != null) {
            for (int i = this.children.size() - 1; i >= 0; i--) {
                if (this.children.get(i).getName().equalsIgnoreCase(childName)) {
                    return children.get(i);
                }
            }
        }

        return null;
    }

    public List<ClausewitzItem> getChildren(String name) {
        List<ClausewitzItem> list = new ArrayList<>();

        if (this.children != null) {
            for (ClausewitzItem child : this.children) {
                if (child.getName().equalsIgnoreCase(name)) {
                    list.add(child);
                }
            }
        }

        return list;
    }

    public List<ClausewitzItem> getChildrenNot(String name) {
        List<ClausewitzItem> list = new ArrayList<>();

        if (this.children != null) {
            for (ClausewitzItem child : this.children) {
                if (!child.getName().equalsIgnoreCase(name)) {
                    list.add(child);
                }
            }
        }

        return list;
    }

    public List<ClausewitzItem> getChildrenNot(String... names) {
        List<ClausewitzItem> list = new ArrayList<>();

        if (this.children != null) {
            for (ClausewitzItem child : this.children) {
                if (Arrays.stream(names).noneMatch(child.getName()::equalsIgnoreCase)) {
                    list.add(child);
                }
            }
        }

        return list;
    }

    public ClausewitzList getList(int index) {
        if (index < 0 || this.lists == null || index >= this.lists.size()) {
            return null;
        }

        return this.lists.get(index);
    }

    public ClausewitzList getList(String listName) {
        if (this.lists != null) {
            for (ClausewitzList list : this.lists) {
                if (list.getName().equalsIgnoreCase(listName)) {
                    return list;
                }
            }
        }

        return null;
    }

    public ClausewitzList getLastList(String childName) {
        if (this.lists != null) {
            for (int i = this.lists.size() - 1; i >= 0; i--) {
                if (this.lists.get(i).getName().equalsIgnoreCase(childName)) {
                    return lists.get(i);
                }
            }
        }

        return null;
    }

    public boolean hasList(String listName) {
        return getList(listName) != null;
    }

    public List<ClausewitzList> getLists(String varName) {
        List<ClausewitzList> clausewitzLists = new ArrayList<>();

        if (this.lists != null) {
            for (ClausewitzList list : this.lists) {
                if (list.getName().equalsIgnoreCase(varName)) {
                    clausewitzLists.add(list);
                }
            }
        }

        return clausewitzLists;
    }

    public List<ClausewitzList> getListsNot(String name) {
        List<ClausewitzList> listList = new ArrayList<>();

        if (this.lists != null) {
            for (ClausewitzList list : this.lists) {
                if (!list.getName().equalsIgnoreCase(name)) {
                    listList.add(list);
                }
            }
        }

        return listList;
    }

    public ClausewitzVariable getVar(int index) {
        if (index < 0 || this.variables == null || index >= this.variables.size()) {
            return null;
        } else {
            return this.variables.get(index);
        }
    }

    public ClausewitzVariable getVar(String varName) {
        if (this.variables != null) {
            for (ClausewitzVariable var : this.variables) {
                if (var.getName().equalsIgnoreCase(varName)) {
                    return var;
                }
            }
        }

        return null;
    }

    public boolean hasVar(String varName) {
        return getVar(varName) != null;
    }

    public ClausewitzVariable getVar(String varName, int index) {
        if (this.variables != null) {
            int j = 0;
            for (ClausewitzVariable variable : this.variables) {
                if (variable.getName().equalsIgnoreCase(varName)) {
                    if (j == index) {
                        return variable;
                    }

                    j++;
                }
            }
        }

        return null;
    }

    public ClausewitzVariable getLastVar(String varName) {
        if (this.variables != null) {
            for (int i = this.variables.size() - 1; i >= 0; i--) {
                if (this.variables.get(i).getName().equalsIgnoreCase(varName)) {
                    return this.variables.get(i);
                }
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
        if (this.variables != null) {
            for (int i = this.variables.size() - 1; i >= 0; i--) {
                if (this.variables.get(i).getName().equalsIgnoreCase(varName)) {
                    return this.variables.get(i).getValue();
                }
            }
        }

        return null;
    }

    public List<ClausewitzVariable> getVars(String varName) {
        List<ClausewitzVariable> vars = new ArrayList<>();

        if (this.variables != null) {
            for (ClausewitzVariable var : this.variables) {
                if (var.getName().equalsIgnoreCase(varName)) {
                    vars.add(var);
                }
            }
        }

        return vars;
    }

    public List<String> getVarsAsStrings(String varName) {
        List<String> list = new ArrayList<>();

        if (this.variables != null) {
            for (ClausewitzVariable var : this.variables) {
                if (var.getName().equalsIgnoreCase(varName)) {
                    list.add(var.getValue());
                }
            }
        }

        return list;
    }

    public List<ClausewitzVariable> getVarsNot(String... varNames) {
        List<ClausewitzVariable> list = new ArrayList<>();
        List<String> names = Arrays.stream(varNames).map(String::toLowerCase).collect(Collectors.toList());

        if (this.variables != null) {
            for (ClausewitzVariable var : this.variables) {
                if (!names.contains(var.getName().toLowerCase())) {
                    list.add(new ClausewitzVariable(var));
                }
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

    public LocalDate getVarAsDate(int index) {
        ClausewitzVariable var = getVar(index);
        return var == null ? null : var.getAsDate();
    }

    public LocalDate getVarAsDate(String varName) {
        ClausewitzVariable var = getVar(varName);
        return var == null ? null : var.getAsDate();
    }

    public LocalDate getVarAsDate(String varName, int index) {
        ClausewitzVariable var = getVar(varName, index);
        return var == null ? null : var.getAsDate();
    }

    public LocalDate getLastVarAsDate(String varName) {
        ClausewitzVariable var = getLastVar(varName);
        return var == null ? null : var.getAsDate();
    }

    public List<ClausewitzVariable> getVariables(String varName) {
        List<ClausewitzVariable> list = new ArrayList<>();

        if (this.variables != null) {
            for (ClausewitzVariable variable : this.variables) {
                if (variable.getName().equalsIgnoreCase(varName)) {
                    list.add(variable);
                }
            }
        }

        return list;
    }

    public List<ClausewitzItem> getChildren() {
        return this.children == null ? new ArrayList<>() : this.children;
    }

    public List<ClausewitzVariable> getVariables() {
        return this.variables == null ? new ArrayList<>() : this.variables;
    }

    public List<ClausewitzList> getLists() {
        return this.lists == null ? new ArrayList<>() : this.lists;
    }

    public List<ClausewitzObject> getAllOrdered() {
        List<ClausewitzObject> objects = new ArrayList<>();

        if (this.children != null) {
            objects.addAll(this.children);
        }

        if (this.variables != null) {
            objects.addAll(this.variables);
        }

        if (this.lists != null) {
            objects.addAll(this.lists);
        }

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
        if (this.lists != null) {
            for (int i = 0; i < this.lists.size(); i++) {
                if (this.lists.get(i).getOrder() == order) {
                    this.lists.remove(i);
                    break;
                }
            }
        }

        if (this.variables != null) {
            for (int i = 0; i < this.variables.size(); i++) {
                if (this.variables.get(i).getOrder() == order) {
                    this.variables.remove(i);
                    break;
                }
            }
        }

        if (this.children != null) {
            for (int i = 0; i < this.children.size(); i++) {
                if (this.children.get(i).getOrder() == order) {
                    this.children.remove(i);
                    break;
                }
            }
        }
    }

    public List<ClausewitzList> getEmptyLists() {
        List<ClausewitzList> listList = new ArrayList<>();
        listList.addAll(this.getLists().stream().filter(ClausewitzList::isEmpty).collect(Collectors.toList()));
        listList.addAll(this.getChildren().stream().map(ClausewitzItem::getEmptyLists).flatMap(Collection::stream).collect(Collectors.toList()));

        return listList;
    }

    public List<ClausewitzList> getListsSize(int size) {
        List<ClausewitzList> listList = new ArrayList<>();
        listList.addAll(this.getLists().stream().filter(clausewitzList -> clausewitzList.size() == size).collect(Collectors.toList()));
        listList.addAll(this.getChildren().stream().map(item -> item.getListsSize(size)).flatMap(Collection::stream).collect(Collectors.toList()));

        return listList;
    }

    public List<ClausewitzItem> getChildrenSize(int size) {
        List<ClausewitzItem> listList = new ArrayList<>();
        listList.addAll(this.getChildren().stream().filter(item -> item.getNbObjects() == size).collect(Collectors.toList()));
        listList.addAll(this.getChildren().stream().map(item -> item.getChildrenSize(size)).flatMap(Collection::stream).collect(Collectors.toList()));

        return listList;
    }

    public List<ClausewitzVariable> getBlankVariable() {
        List<ClausewitzVariable> listList = new ArrayList<>();
        listList.addAll(this.getVariables().stream().filter(var -> ClausewitzUtils.isBlank(var.getValue())).collect(Collectors.toList()));
        listList.addAll(this.getChildren().stream().map(ClausewitzItem::getBlankVariable).flatMap(Collection::stream).collect(Collectors.toList()));

        return listList;
    }

    public List<ClausewitzVariable> getBlankNameVariable() {
        List<ClausewitzVariable> listList = new ArrayList<>();
        listList.addAll(this.getVariables().stream().filter(var -> ClausewitzUtils.isBlank(var.getName())).collect(Collectors.toList()));
        listList.addAll(this.getChildren().stream().map(ClausewitzItem::getBlankNameVariable).flatMap(Collection::stream).collect(Collectors.toList()));

        return listList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ClausewitzItem)) {
            return false;
        }

        ClausewitzItem item = (ClausewitzItem) o;
        return sameLine == item.sameLine &&
               Objects.equals(children, item.children) &&
               Objects.equals(variables, item.variables) &&
               Objects.equals(lists, item.lists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, variables, lists, sameLine);
    }

    @Override
    public void write(BufferedWriter bufferedWriter, int depth) throws IOException {
        if (!DEFAULT_NAME.equals(getName())) {
            ClausewitzUtils.printTabs(bufferedWriter, depth);
            bufferedWriter.write(this.name);

            if (this.hasEquals) {
                ClausewitzUtils.printEqualsOpen(bufferedWriter);
            } else {
                ClausewitzUtils.printOpen(bufferedWriter);
            }

            bufferedWriter.newLine();

            if (this.sameLine && this.getInternalChildren().isEmpty()) {
                ClausewitzUtils.printTabs(bufferedWriter, depth + 1);

                for (ClausewitzObject object : this.getAllOrdered()) {
                    object.write(bufferedWriter, 0);
                    ClausewitzUtils.printSpace(bufferedWriter);
                }

                bufferedWriter.newLine();
            } else {
                for (ClausewitzObject object : this.getAllOrdered()) {
                    object.write(bufferedWriter, depth + 1);
                    bufferedWriter.newLine();
                }
            }

            ClausewitzUtils.printTabs(bufferedWriter, depth);
            ClausewitzUtils.printClose(bufferedWriter);
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
