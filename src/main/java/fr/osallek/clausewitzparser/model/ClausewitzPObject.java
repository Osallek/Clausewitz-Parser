package fr.osallek.clausewitzparser.model;

public abstract class ClausewitzPObject extends ClausewitzObject {

    protected final ClausewitzItem parent;

    public ClausewitzPObject(String name, int order, ClausewitzItem parent) {
        super(name, order);
        this.parent = parent;

        if (this.parent != null) {
            this.parent.addObject(this);
        }
    }

    public ClausewitzPObject(ClausewitzObject other, ClausewitzItem parent) {
        super(other);
        this.parent = parent;

        if (this.parent != null) {
            this.parent.addObject(this);
        }
    }

    public ClausewitzPObject(ClausewitzPObject other) {
        super(other);
        this.parent = other.parent;

        if (this.parent != null) {
            this.parent.addObject(this);
        }
    }

    public ClausewitzPObject getRoot() {
        return (this.parent == null ? this : this.parent.getRoot());
    }

    public ClausewitzItem getParent() {
        return parent;
    }
}
