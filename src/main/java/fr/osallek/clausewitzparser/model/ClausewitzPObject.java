package fr.osallek.clausewitzparser.model;

public abstract class ClausewitzPObject extends ClausewitzObject {

    protected final ClausewitzItem parent;

    protected ClausewitzPObject(String name, int order, ClausewitzItem parent, boolean increaseOrder) {
        super(name, order);
        this.parent = parent;

        if (this.parent != null) {
            this.parent.addObject(this, increaseOrder);
        }
    }

    protected ClausewitzPObject(ClausewitzObject other, ClausewitzItem parent) {
        this(other, parent, false);
    }

    protected ClausewitzPObject(ClausewitzObject other, ClausewitzItem parent, boolean increaseOrder) {
        super(other);
        this.parent = parent;

        if (this.parent != null) {
            this.parent.addObject(this, increaseOrder);
        }
    }

    protected ClausewitzPObject(ClausewitzPObject other) {
        super(other);
        this.parent = other.parent;

        if (this.parent != null) {
            this.parent.addObject(this, false);
        }
    }

    public ClausewitzPObject getRoot() {
        return (this.parent == null ? this : this.parent.getRoot());
    }

    public ClausewitzItem getParent() {
        return parent;
    }
}
