package fr.osallek.clausewitzparser.model;

public abstract class ClausewitzPObject extends ClausewitzObject {

    protected final ClausewitzPObject parent;

    public ClausewitzPObject(String name, int order, ClausewitzPObject parent) {
        super(name, order);
        this.parent = parent;
    }

    public ClausewitzPObject(ClausewitzObject other, ClausewitzPObject parent) {
        super(other);
        this.parent = parent;
    }

    public ClausewitzPObject(ClausewitzPObject other) {
        super(other);
        this.parent = other.parent;
    }

    public ClausewitzPObject getRoot() {
        return (this.parent == null ? this : this.parent.getRoot());
    }

    public ClausewitzPObject getParent() {
        return parent;
    }
}
