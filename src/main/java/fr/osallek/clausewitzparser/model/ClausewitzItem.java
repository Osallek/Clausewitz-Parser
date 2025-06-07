package fr.osallek.clausewitzparser.model;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ClausewitzItem extends ClausewitzPObject {

    public static final String DEFAULT_NAME = "clausewitzparser";

    private List<ClausewitzItem> children;

    private Map<String, List<ClausewitzItem>> childrenMap;

    private List<ClausewitzVariable> variables;

    private Map<String, List<ClausewitzVariable>> variablesMap;

    private List<ClausewitzList> lists;

    private Map<String, List<ClausewitzList>> listsMap;

    private boolean sameLine = false;

    private final boolean hasEquals;

    public ClausewitzItem() {
        this(null, DEFAULT_NAME, 0);
    }

    public ClausewitzItem(ClausewitzItem parent, String name, int order) {
        this(parent, name, order, true);
    }

    public ClausewitzItem(ClausewitzItem parent, String name, int order, boolean hasEquals) {
        this(parent, name, order, hasEquals, false);
    }

    public ClausewitzItem(ClausewitzItem parent, String name, int order, boolean hasEquals, boolean increaseOrder) {
        super(name, order, parent, increaseOrder);
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
            this.children = new ArrayList<>(1);
            this.childrenMap = new HashMap<>();
        }

        return this.children;
    }

    private List<ClausewitzVariable> getInternalVariables() {
        if (this.variables == null) {
            this.variables = new ArrayList<>(1);
            this.variablesMap = new HashMap<>();
        }

        return this.variables;
    }

    private List<ClausewitzList> getInternalLists() {
        if (this.lists == null) {
            this.lists = new ArrayList<>(1);
            this.listsMap = new HashMap<>();
        }

        return this.lists;
    }

    public void addObject(ClausewitzPObject object, boolean increaseOrder) {
        if (ClausewitzItem.class.equals(object.getClass())) {
            addChild((ClausewitzItem) object, increaseOrder);
        } else if (ClausewitzList.class.equals(object.getClass())) {
            addList((ClausewitzList) object, increaseOrder);
        }
    }

    public void addChild(ClausewitzItem child) {
        addChild(child, false);
    }

    public void addChild(ClausewitzItem child, boolean increaseOrders) {
        if (child == null) {
            throw new NullPointerException("Can't add a null child");
        }

        if (increaseOrders) {
            getAllOrdered().stream().filter(co -> co.order >= child.order).forEach(co -> co.order++);
        } else {
            child.order = getNbObjects();
        }

        getInternalChildren().add(child);
        this.childrenMap.computeIfAbsent(child.getName(), k -> new ArrayList<>(1)).add(child);
    }

    public ClausewitzItem addChild(String name) {
        return new ClausewitzItem(this, name, getNbObjects());
    }

    public ClausewitzItem addChild(String name, boolean hasEquals) {
        return new ClausewitzItem(this, name, getNbObjects(), hasEquals);
    }

    public boolean removeChild(int id) {
        if (this.children != null) {
            ClausewitzItem child = this.children.remove(id);

            if (child != null) {
                List<ClausewitzItem> childrenList = this.childrenMap.get(child.getName());
                childrenList.remove(child);

                if (childrenList.isEmpty()) {
                    this.childrenMap.remove(child.getName());
                }

                return true;
            }

            return false;
        }

        return false;
    }

    public boolean removeChild(ClausewitzItem child) {
        if (this.children != null) {
            boolean removed = this.children.remove(child);

            if (removed) {
                List<ClausewitzItem> childrenList = this.childrenMap.get(child.getName());
                childrenList.remove(child);

                if (childrenList.isEmpty()) {
                    this.childrenMap.remove(child.getName());
                }

                return true;
            }

            return false;
        }

        return false;
    }

    public boolean removeChild(String childName, int id) {
        if (this.children != null) {
            List<ClausewitzItem> items = this.childrenMap.get(childName);

            if (items != null && items.size() > id) {
                boolean removed = this.children.remove(items.remove(id));

                if (removed && items.isEmpty()) {
                    this.childrenMap.remove(childName);
                }

                return removed;
            }
        }

        return false;
    }

    public boolean removeChild(String childName) {
        if (this.children != null) {
            List<ClausewitzItem> items = this.childrenMap.get(childName);

            if (items != null) {
                boolean removed = this.children.remove(items.removeFirst());

                if (removed && items.isEmpty()) {
                    this.childrenMap.remove(childName);
                }

                return removed;
            }
        }

        return false;
    }

    public boolean removeLastChild(String childName) {
        if (this.children != null) {
            List<ClausewitzItem> items = this.childrenMap.get(childName);

            if (items != null) {
                boolean removed = this.children.remove(items.removeLast());

                if (removed && items.isEmpty()) {
                    this.childrenMap.remove(childName);
                }

                return removed;
            }
        }

        return false;
    }

    public boolean removeChildren(String childName) {
        if (this.children != null) {
            List<ClausewitzItem> items = this.childrenMap.remove(childName);

            if (items != null) {
                return this.children.removeAll(items);
            }
        }

        return false;
    }

    public void removeAllChildren() {
        if (this.children != null) {
            this.children.clear();
            this.childrenMap.clear();
        }
    }

    public boolean removeChildIf(Predicate<ClausewitzItem> filter) {
        if (this.children != null) {
            for (ClausewitzItem child : this.children) {
                if (filter.test(child)) {
                    removeChild(child);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean removeChildrenIf(Predicate<ClausewitzItem> filter) {
        if (this.children != null) {
            boolean removed = false;
            for (ClausewitzItem child : this.children) {
                if (filter.test(child)) {
                    removeChild(child);
                    removed = true;
                }
            }

            return removed;
        }

        return false;
    }

    public void addVariable(ClausewitzVariable variable) {
        addVariable(variable, false);
    }

    public void addVariable(ClausewitzVariable variable, boolean increaseOrders) {
        if (variable == null) {
            throw new NullPointerException("Can't add a null variable");
        }

        if (increaseOrders) {
            getAllOrdered().stream().filter(co -> co.order >= variable.order).forEach(co -> co.order++);
        } else {
            variable.order = getNbObjects();
        }

        getInternalVariables().add(variable);
        this.variablesMap.computeIfAbsent(variable.getName(), k -> new ArrayList<>(1)).add(variable);
    }

    public boolean removeVariableIf(Predicate<ClausewitzVariable> filter) {
        if (this.variables != null) {
            for (ClausewitzVariable variable : this.variables) {
                if (filter.test(variable)) {
                    removeVariable(variable);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean removeVariablesIf(Predicate<ClausewitzVariable> filter) {
        if (this.variables != null) {
            boolean removed = false;
            for (ClausewitzVariable variable : this.variables) {
                if (filter.test(variable)) {
                    removeVariable(variable);
                    removed = true;
                }
            }

            return removed;
        }

        return false;
    }

    public ClausewitzVariable setVariableName(int index, String name) {
        ClausewitzVariable variable = getVar(index);

        if (variable != null) {
            variable.setName(name);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(int index, String value) {
        ClausewitzVariable variable = getVar(index);

        if (variable != null) {
            variable.setValue(value);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(int index, int value) {
        ClausewitzVariable variable = getVar(index);

        if (variable != null) {
            variable.setValue(value);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(int index, double value) {
        ClausewitzVariable variable = getVar(index);

        if (variable != null) {
            variable.setValue(value);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(int index, boolean value) {
        ClausewitzVariable variable = getVar(index);

        if (variable != null) {
            variable.setValue(value);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(int index, LocalDate value) {
        return setVariable(index, value, false);
    }

    public ClausewitzVariable setVariable(int index, LocalDate value, boolean quotes) {
        ClausewitzVariable variable = getVar(index);

        if (variable != null) {
            variable.setValue(value, quotes);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(String name, String value) {
        ClausewitzVariable variable = getVar(name);

        if (variable != null) {
            variable.setValue(value);
        } else {
            variable = addVariable(name, value);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(String name, int value) {
        ClausewitzVariable variable = getVar(name);

        if (variable != null) {
            variable.setValue(value);
        } else {
            variable = addVariable(name, value);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(String name, double value) {
        ClausewitzVariable variable = getVar(name);

        if (variable != null) {
            variable.setValue(value);
        } else {
            variable = addVariable(name, value);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(String name, boolean value) {
        ClausewitzVariable variable = getVar(name);

        if (variable != null) {
            variable.setValue(value);
        } else {
            variable = addVariable(name, value);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(String name, LocalDate value) {
        return setVariable(name, value, false);
    }

    public ClausewitzVariable setVariable(String name, LocalDate value, boolean quotes) {
        ClausewitzVariable variable = getVar(name);

        if (variable != null) {
            variable.setValue(value, quotes);
        } else {
            variable = addVariable(name, value, quotes);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(String name, String value, int order) {
        ClausewitzVariable variable = getVar(name);

        if (variable != null) {
            variable.setValue(value);
        } else {
            variable = addVariable(name, value, order);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(String name, int value, int order) {
        ClausewitzVariable variable = getVar(name);

        if (variable != null) {
            variable.setValue(value);
        } else {
            variable = addVariable(name, value, order);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(String name, double value, int order) {
        ClausewitzVariable variable = getVar(name);

        if (variable != null) {
            variable.setValue(value);
        } else {
            variable = addVariable(name, value, order);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(String name, boolean value, int order) {
        ClausewitzVariable variable = getVar(name);

        if (variable != null) {
            variable.setValue(value);
        } else {
            variable = addVariable(name, value, order);
        }

        return variable;
    }

    public ClausewitzVariable setVariable(String name, LocalDate value, int order) {
        return setVariable(name, value, false, order);
    }

    public ClausewitzVariable setVariable(String name, LocalDate value, boolean quotes, int order) {
        ClausewitzVariable variable = getVar(name);

        if (variable != null) {
            variable.setValue(value, quotes);
        } else {
            variable = addVariable(name, value, quotes, order);
        }

        return variable;
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

    public boolean removeVariable(int id) {
        if (this.variables != null) {
            ClausewitzVariable variable = this.variables.remove(id);

            if (variable != null) {
                List<ClausewitzVariable> variablesList = this.variablesMap.get(variable.getName());
                variablesList.remove(variable);

                if (variablesList.isEmpty()) {
                    this.variablesMap.remove(variable.getName());
                }

                return true;
            }

            return false;
        }

        return false;
    }

    public boolean removeVariable(ClausewitzVariable variable) {
        if (this.variables != null) {
            boolean removed = this.variables.remove(variable);

            if (removed) {
                List<ClausewitzVariable> variablesList = this.variablesMap.get(variable.getName());
                variablesList.remove(variable);

                if (variablesList.isEmpty()) {
                    this.variablesMap.remove(variable.getName());
                }

                return true;
            }

            return false;
        }

        return false;
    }

    public boolean removeVariable(String variable, int id) {
        if (this.variables != null) {
            List<ClausewitzVariable> items = this.variablesMap.get(variable);

            if (items != null && items.size() > id) {
                boolean removed = this.variables.remove(items.remove(id));

                if (removed && items.isEmpty()) {
                    this.variablesMap.remove(variable);
                }

                return removed;
            }
        }

        return false;
    }

    public boolean removeVariable(String childName) {
        if (this.variables != null) {
            List<ClausewitzVariable> items = this.variablesMap.get(childName);

            if (items != null) {
                boolean removed = this.variables.remove(items.removeFirst());

                if (removed && items.isEmpty()) {
                    this.variablesMap.remove(childName);
                }

                return removed;
            }
        }

        return false;
    }

    public boolean removeLastVariable(String childName) {
        if (this.variables != null) {
            List<ClausewitzVariable> items = this.variablesMap.get(childName);

            if (items != null) {
                boolean removed = this.variables.remove(items.removeLast());

                if (removed && items.isEmpty()) {
                    this.variablesMap.remove(childName);
                }

                return removed;
            }
        }

        return false;
    }

    public boolean removeVariables(String childName) {
        if (this.variables != null) {
            List<ClausewitzVariable> items = this.variablesMap.remove(childName);

            if (items != null) {
                return this.variables.removeAll(items);
            }
        }

        return false;
    }

    public boolean removeVariable(String varName, String value) {
        if (this.variables != null) {
            List<ClausewitzVariable> items = this.variablesMap.get(varName);

            if (items != null) {
                return removeVariable(items.getFirst());
            }
        }

        return false;
    }

    public void removeAllVariables() {
        if (this.variables != null) {
            this.variables.clear();
            this.variablesMap.clear();
        }
    }

    public boolean removeVariableByValue(String value) {
        return removeVariablesIf(v -> v.getValue().equals(value));
    }

    public void addList(ClausewitzList list) {
        addList(list, false);
    }

    public ClausewitzList addList(ClausewitzList list, boolean increaseOrders) {
        if (list == null) {
            throw new NullPointerException("Can't add a null list");
        }

        if (increaseOrders) {
            this.getAllOrdered().stream().filter(co -> co.order >= list.order).forEach(co -> co.order++);
        } else {
            list.order = getNbObjects();
        }

        getInternalLists().add(list);
        this.listsMap.computeIfAbsent(list.getName(), k -> new ArrayList<>(1)).add(list);

        return list;
    }

    public ClausewitzList addList(String name, boolean sameLine, Collection<String> values) {
        return addList(name, sameLine, values.toArray(String[]::new));
    }

    public ClausewitzList addList(String name, boolean sameLine, boolean hasBrackets, Collection<String> values) {
        return addList(name, sameLine, hasBrackets, values.toArray(String[]::new));
    }

    public ClausewitzList addList(String name, boolean sameLine, String... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects(), sameLine);
        list.addAll(values);

        return list;
    }

    public ClausewitzList addList(String name, boolean sameLine, boolean hasBrackets, String... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects(), sameLine, hasBrackets);
        list.addAll(values);

        return list;
    }

    public ClausewitzList addList(String name, boolean sameLine, boolean hasBrackets, boolean increaseOrder, String... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects(), sameLine, hasBrackets, increaseOrder);
        list.addAll(values);

        return list;
    }

    public ClausewitzList addList(String name, int order, boolean sameLine, boolean hasBrackets, boolean increaseOrder, String... values) {
        ClausewitzList list = new ClausewitzList(this, name, order, sameLine, hasBrackets, increaseOrder);
        list.addAll(values);

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
        } else {
            list.addAll(values);
        }

        return list;
    }

    public ClausewitzList addList(String name, String value) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.add(value);

        return list;
    }

    public ClausewitzList addList(String name, Integer value) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.add(value);

        return list;
    }

    public ClausewitzList addList(String name, String... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.addAll(values);

        return list;
    }

    public ClausewitzList addList(String name, List<String> values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.addAll(values);

        return list;
    }

    public ClausewitzList addList(String name, Integer... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.addAll(values);

        return list;
    }

    public ClausewitzList addList(String name, Double... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.addAll(values);

        return list;
    }

    public ClausewitzList addList(String name, Boolean... values) {
        ClausewitzList list = new ClausewitzList(this, name, getNbObjects());
        list.addAll(values);

        return list;
    }

    public ClausewitzList changeChildToList(int childOrder, String listName, String... values) {
        return changeChildToList(childOrder, listName, false, values);
    }

    public ClausewitzList changeChildToList(int childOrder, String listName, Collection<String> values) {
        return changeChildToList(childOrder, listName, false, values.toArray(String[]::new));
    }

    public ClausewitzList changeChildToList(int childOrder, String listName, boolean sameLine, Collection<String> values) {
        return changeChildToList(childOrder, listName, sameLine, values.toArray(String[]::new));
    }

    public ClausewitzList changeChildToList(int childOrder, String listName, boolean sameLine, String... values) {
        removeChildIf(c -> c.order == childOrder);

        ClausewitzList list = new ClausewitzList(this, listName, childOrder, sameLine);
        list.addAll(values);
        this.lists.sort(Comparator.comparingInt(ClausewitzObject::getOrder));

        return list;
    }

    public boolean removeList(int id) {
        if (this.lists != null) {
            ClausewitzList list = this.lists.remove(id);

            if (list != null) {
                List<ClausewitzList> listsList = this.listsMap.get(list.getName());
                listsList.remove(list);

                if (listsList.isEmpty()) {
                    this.listsMap.remove(list.getName());
                }

                return true;
            }

            return false;
        }

        return false;
    }

    public boolean removeList(ClausewitzList child) {
        if (this.lists != null) {
            boolean removed = this.lists.remove(child);

            if (removed) {
                List<ClausewitzList> listsList = this.listsMap.get(child.getName());
                listsList.remove(child);

                if (listsList.isEmpty()) {
                    this.listsMap.remove(child.getName());
                }

                return true;
            }

            return false;
        }

        return false;
    }

    public boolean removeList(String listName, int id) {
        if (this.lists != null) {
            List<ClausewitzList> items = this.listsMap.get(listName);

            if (items != null && items.size() > id) {
                boolean removed = this.lists.remove(items.remove(id));

                if (removed && items.isEmpty()) {
                    this.listsMap.remove(listName);
                }

                return removed;
            }
        }

        return false;
    }

    public boolean removeList(String listName) {
        if (this.lists != null) {
            List<ClausewitzList> items = this.listsMap.get(listName);

            if (items != null) {
                boolean removed = this.lists.remove(items.removeFirst());

                if (removed && items.isEmpty()) {
                    this.listsMap.remove(listName);
                }

                return removed;
            }
        }

        return false;
    }

    public boolean removeLastList(String childName) {
        if (this.lists != null) {
            List<ClausewitzList> items = this.listsMap.get(childName);

            if (items != null) {
                boolean removed = this.lists.remove(items.removeLast());

                if (removed && items.isEmpty()) {
                    this.listsMap.remove(childName);
                }

                return removed;
            }
        }

        return false;
    }

    public boolean removeLists(String listName) {
        if (this.lists != null) {
            List<ClausewitzList> items = this.listsMap.remove(listName);

            if (items != null) {
                return this.lists.removeAll(items);
            }
        }

        return false;
    }

    public void removeAllLists() {
        if (this.lists != null) {
            this.lists.clear();
            this.listsMap.clear();
        }
    }

    public boolean removeListIf(Predicate<ClausewitzList> filter) {
        if (this.lists != null) {
            for (ClausewitzList child : this.lists) {
                if (filter.test(child)) {
                    removeList(child);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean removeListsIf(Predicate<ClausewitzList> filter) {
        if (this.lists != null) {
            boolean removed = false;
            for (ClausewitzList child : this.lists) {
                if (filter.test(child)) {
                    removeList(child);
                    removed = true;
                }
            }

            return removed;
        }

        return false;
    }

    public void removeAll() {
        removeAllChildren();
        removeAllVariables();
        removeAllLists();
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
        if (this.childrenMap != null) {
            List<ClausewitzItem> children = this.childrenMap.get(childName);

            return children == null ? null : children.getFirst();
        }

        return null;
    }

    public boolean hasChild(String childName) {
        return this.childrenMap != null && this.childrenMap.containsKey(childName);
    }

    public ClausewitzItem getChild(String childName, int index) {
        if (this.childrenMap != null) {
            List<ClausewitzItem> children = this.childrenMap.get(childName);

            return (children == null || children.size() < index) ? null : children.get(index);
        }

        return null;
    }

    public ClausewitzItem getLastChild(String childName) {
        if (this.childrenMap != null) {
            List<ClausewitzItem> children = this.childrenMap.get(childName);

            return children == null ? null : children.getLast();
        }

        return null;
    }

    public List<ClausewitzItem> getChildren(String childName) {
        List<ClausewitzItem> list = new ArrayList<>();

        if (this.childrenMap != null) {
            List<ClausewitzItem> children = this.childrenMap.get(childName);

            if (children != null) {
                list.addAll(children);
            }
        }

        return list;
    }

    public List<ClausewitzItem> getChildrenNot(String childName) {
        List<ClausewitzItem> list = new ArrayList<>();

        if (this.childrenMap != null) {
            for (Map.Entry<String, List<ClausewitzItem>> entry : this.childrenMap.entrySet()) {
                if (!entry.getKey().equals(childName)) {
                    list.addAll(entry.getValue());
                }
            }
        }

        return list;
    }

    public List<ClausewitzItem> getChildrenNot(String... childNames) {
        List<ClausewitzItem> list = new ArrayList<>();

        if (this.childrenMap != null) {
            Set<String> nameSet = new HashSet<>(Arrays.asList(childNames));
            for (Map.Entry<String, List<ClausewitzItem>> entry : this.childrenMap.entrySet()) {
                if (!nameSet.contains(entry.getKey())) {
                    list.addAll(entry.getValue());
                }
            }
        }

        return list;
    }

    public List<ClausewitzItem> getChildrenStartWith(String start) {
        List<ClausewitzItem> list = new ArrayList<>();

        if (this.childrenMap != null) {
            for (Map.Entry<String, List<ClausewitzItem>> entry : this.childrenMap.entrySet()) {
                if (!entry.getKey().startsWith(start)) {
                    list.addAll(entry.getValue());
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
        if (this.listsMap != null) {
            List<ClausewitzList> listsList = this.listsMap.get(listName);

            return listsList == null ? null : listsList.getFirst();
        }

        return null;
    }

    public ClausewitzList getLastList(String childName) {
        if (this.listsMap != null) {
            List<ClausewitzList> listsList = this.listsMap.get(childName);

            return listsList == null ? null : listsList.getLast();
        }

        return null;
    }

    public boolean hasList(String listName) {
        return this.listsMap != null && this.listsMap.containsKey(listName);
    }

    public List<ClausewitzList> getLists(String listName) {
        List<ClausewitzList> clausewitzLists = new ArrayList<>();

        if (this.listsMap != null) {
            List<ClausewitzList> children = this.listsMap.get(listName);

            if (children != null) {
                clausewitzLists.addAll(children);
            }
        }

        return clausewitzLists;
    }

    public List<ClausewitzList> getListsNot(String listName) {
        List<ClausewitzList> listsList = new ArrayList<>();

        if (this.listsMap != null) {
            for (Map.Entry<String, List<ClausewitzList>> entry : this.listsMap.entrySet()) {
                if (!entry.getKey().equals(listName)) {
                    listsList.addAll(entry.getValue());
                }
            }
        }

        return listsList;
    }

    public ClausewitzVariable getVar(int index) {
        if (index < 0 || this.variables == null || index >= this.variables.size()) {
            return null;
        } else {
            return this.variables.get(index);
        }
    }

    public ClausewitzVariable getVar(String varName) {
        if (this.variablesMap != null) {
            List<ClausewitzVariable> list = this.variablesMap.get(varName);

            if (list != null) {
                return list.getFirst();
            }
        }

        return null;
    }

    public ClausewitzVariable getVar(String varName, String value) {
        if (this.variables != null) {
            List<ClausewitzVariable> list = this.variablesMap.get(varName);

            if (list != null) {
                return list.stream().filter(v -> v.getValue().equals(value)).findFirst().orElse(null);
            }
        }

        return null;
    }

    public boolean hasVar(String varName) {
        return this.variablesMap != null && this.variablesMap.containsKey(varName);
    }

    public boolean hasVar(String varName, String value) {
        return getVar(varName, value) != null;
    }

    public ClausewitzVariable getVar(String varName, int index) {
        if (this.variablesMap != null) {
            List<ClausewitzVariable> items = this.variablesMap.get(varName);

            return (items == null || items.size() < index) ? null : items.get(index);
        }

        return null;
    }

    public ClausewitzVariable getLastVar(String varName) {
        if (this.variablesMap != null) {
            List<ClausewitzVariable> list = this.variablesMap.get(varName);

            if (list != null) {
                return list.getLast();
            }
        }

        return null;
    }

    public String getVarAsString(int index) {
        ClausewitzVariable variable = getVar(index);

        if (variable != null) {
            return variable.getValue();
        } else {
            return null;
        }
    }

    public String getVarAsString(String varName) {
        ClausewitzVariable variable = getVar(varName);

        if (variable != null) {
            return variable.getValue();
        } else {
            return null;
        }
    }

    public String getVarAsString(String varName, int index) {
        ClausewitzVariable variable = getVar(varName, index);

        if (variable != null) {
            return variable.getValue();
        } else {
            return null;
        }
    }

    public String getLastVarAsString(String varName) {
        ClausewitzVariable variable = getLastVar(varName);

        if (variable != null) {
            return variable.getValue();
        } else {
            return null;
        }
    }

    public List<ClausewitzVariable> getVars(String varName) {
        List<ClausewitzVariable> vars = new ArrayList<>();

        if (this.variablesMap != null) {
            List<ClausewitzVariable> children = this.variablesMap.get(varName);

            if (children != null) {
                vars.addAll(children);
            }
        }

        return vars;
    }

    public List<String> getVarsAsStrings(String varName) {
        return getVars(varName).stream().map(ClausewitzVariable::getValue).collect(Collectors.toList());
    }

    public List<ClausewitzVariable> getVarsNot(String... varNames) {
        List<ClausewitzVariable> list = new ArrayList<>();
        List<String> names = Arrays.stream(varNames).map(String::toLowerCase).toList();

        if (this.variablesMap != null) {
            for (Map.Entry<String, List<ClausewitzVariable>> entry : this.variablesMap.entrySet()) {
                if (!names.contains(entry.getKey().toLowerCase())) {
                    list.addAll(entry.getValue());
                }
            }
        }

        return list;
    }

    public Integer getVarAsInt(int index) {
        ClausewitzVariable variable = getVar(index);
        return variable == null ? null : variable.getAsInt();
    }

    public Integer getVarAsInt(String varName) {
        ClausewitzVariable variable = getVar(varName);
        return variable == null ? null : variable.getAsInt();
    }

    public Integer getVarAsInt(String varName, int index) {
        ClausewitzVariable variable = getVar(varName, index);
        return variable == null ? null : variable.getAsInt();
    }

    public Integer getLastVarAsInt(String varName) {
        ClausewitzVariable variable = getLastVar(varName);
        return variable == null ? null : variable.getAsInt();
    }

    public Double getVarAsDouble(int index) {
        ClausewitzVariable variable = getVar(index);
        return variable == null ? null : variable.getAsDouble();
    }

    public Double getVarAsDouble(String varName) {
        ClausewitzVariable variable = getVar(varName);
        return variable == null ? null : variable.getAsDouble();
    }

    public Double getVarAsDouble(String varName, int index) {
        ClausewitzVariable variable = getVar(varName, index);
        return variable == null ? null : variable.getAsDouble();
    }

    public Double getLastVarAsDouble(String varName) {
        ClausewitzVariable variable = getLastVar(varName);
        return variable == null ? null : variable.getAsDouble();
    }

    public Boolean getVarAsBool(int index) {
        ClausewitzVariable variable = getVar(index);
        return variable == null ? null : variable.getAsBool();
    }

    public Boolean getVarAsBool(String varName) {
        ClausewitzVariable variable = getVar(varName);
        return variable == null ? null : variable.getAsBool();
    }

    public Boolean getVarAsBool(String varName, int index) {
        ClausewitzVariable variable = getVar(varName, index);
        return variable == null ? null : variable.getAsBool();
    }

    public Boolean getLastVarAsBool(String varName) {
        ClausewitzVariable variable = getLastVar(varName);
        return variable == null ? null : variable.getAsBool();
    }

    public LocalDate getVarAsDate(int index) {
        ClausewitzVariable variable = getVar(index);
        return variable == null ? null : variable.getAsDate();
    }

    public LocalDate getVarAsDate(String varName) {
        ClausewitzVariable variable = getVar(varName);
        return variable == null ? null : variable.getAsDate();
    }

    public LocalDate getVarAsDate(String varName, int index) {
        ClausewitzVariable variable = getVar(varName, index);
        return variable == null ? null : variable.getAsDate();
    }

    public LocalDate getLastVarAsDate(String varName) {
        ClausewitzVariable variable = getLastVar(varName);
        return variable == null ? null : variable.getAsDate();
    }

    public List<ClausewitzVariable> getVariables(String varName) {
        List<ClausewitzVariable> list = new ArrayList<>();

        if (this.variablesMap != null) {
            List<ClausewitzVariable> items = this.variablesMap.get(varName);

            if (items != null) {
                list.addAll(items);
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

    public int getMaxOrder() {
        return getAllOrdered().stream().mapToInt(ClausewitzObject::getOrder).max().orElse(0);
    }

    public boolean removeByOrder(int order) {
        if (this.lists != null) {
            if (removeListIf(l -> l.order == order)) {
                return true;
            }
        }

        if (this.variables != null) {
            if (removeVariablesIf(v -> v.order == order)) {
                return true;
            }
        }

        if (this.children != null) {
            if (removeChildIf(c -> c.order == order)) {
                return true;
            }
        }

        return false;
    }

    public boolean isEmpty() {
        return (this.variables == null || this.variables.isEmpty())
               && (this.lists == null || this.lists.isEmpty())
               && (this.children == null || this.children.isEmpty());
    }

    public List<ClausewitzList> getEmptyLists() {
        return Stream.concat(getLists().stream().filter(ClausewitzList::isEmpty),
                             getChildren().stream().map(ClausewitzItem::getEmptyLists).flatMap(Collection::stream)).collect(Collectors.toList());
    }

    public List<ClausewitzList> getListsSize(int size) {
        return Stream.concat(getLists().stream().filter(list -> list.size() == size),
                             getChildren().stream().map(item -> item.getListsSize(size)).flatMap(Collection::stream)).collect(Collectors.toList());
    }

    public List<ClausewitzItem> getChildrenSize(int size) {
        return Stream.concat(getChildren().stream().filter(item -> item.getNbObjects() == size),
                             getChildren().stream().map(item -> item.getChildrenSize(size)).flatMap(Collection::stream)).collect(Collectors.toList());
    }

    public List<ClausewitzVariable> getBlankVariable() {
        return Stream.concat(getVariables().stream().filter(variable -> ClausewitzUtils.isBlank(variable.getValue())),
                             getChildren().stream().map(ClausewitzItem::getBlankVariable).flatMap(Collection::stream)).collect(Collectors.toList());
    }

    public List<ClausewitzVariable> getBlankNameVariable() {
        return Stream.concat(getVariables().stream().filter(variable -> ClausewitzUtils.isBlank(variable.getName())),
                             getChildren().stream().map(ClausewitzItem::getBlankNameVariable).flatMap(Collection::stream)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ClausewitzItem item)) {
            return false;
        }

        return sameLine == item.sameLine &&
               Objects.equals(name, item.name) &&
               Objects.equals(children, item.children) &&
               Objects.equals(variables, item.variables) &&
               Objects.equals(lists, item.lists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, variables, lists, sameLine);
    }

    @Override
    public void write(BufferedWriter bufferedWriter, int depth, Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException {
        write(bufferedWriter, false, depth, listeners);
    }

    @Override
    public void write(BufferedWriter bufferedWriter, boolean spaced, int depth,
                      Map<Predicate<ClausewitzPObject>, Consumer<String>> listeners) throws IOException {
        if (!DEFAULT_NAME.equals(getName())) {
            listeners.entrySet().stream().filter(entry -> entry.getKey().test(this)).forEach(entry -> entry.getValue().accept(this.getName()));
            ClausewitzUtils.printTabs(bufferedWriter, depth);
            bufferedWriter.write(this.name);

            if (this.hasEquals) {
                ClausewitzUtils.printEqualsOpen(bufferedWriter, spaced);
            } else {
                ClausewitzUtils.printOpen(bufferedWriter);
            }

            bufferedWriter.newLine();

            if (this.sameLine && this.children != null && !this.children.isEmpty()) {
                ClausewitzUtils.printTabs(bufferedWriter, depth + 1);

                for (ClausewitzObject object : getAllOrdered()) {
                    object.write(bufferedWriter, spaced, 0, listeners);
                    ClausewitzUtils.printSpace(bufferedWriter);
                }

                bufferedWriter.newLine();
            } else {
                for (ClausewitzObject object : getAllOrdered()) {
                    object.write(bufferedWriter, spaced, depth + 1, listeners);
                    bufferedWriter.newLine();
                }
            }

            ClausewitzUtils.printTabs(bufferedWriter, depth);
            ClausewitzUtils.printClose(bufferedWriter);
        } else {
            List<ClausewitzObject> objects = getAllOrdered();

            for (int i = 0; i < objects.size(); i++) {
                objects.get(i).write(bufferedWriter, spaced, depth, listeners);

                if (i != objects.size() - 1) {
                    bufferedWriter.newLine();
                }
            }
        }
    }
}
