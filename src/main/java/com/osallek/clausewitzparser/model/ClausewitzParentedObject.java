package com.osallek.clausewitzparser.model;

public abstract class ClausewitzParentedObject extends ClausewitzObject {

    protected final ClausewitzParentedObject parent;

    public ClausewitzParentedObject(String name, int order, ClausewitzParentedObject parent) {
        super(name, order);
        this.parent = parent;
    }

    public ClausewitzParentedObject(ClausewitzObject other, ClausewitzParentedObject parent) {
        super(other);
        this.parent = parent;
    }

    public ClausewitzParentedObject(ClausewitzParentedObject other) {
        super(other);
        this.parent = other.parent;
    }

    public ClausewitzParentedObject getRoot() {
        return (this.parent == null ? this : this.parent.getRoot());
    }

    public ClausewitzParentedObject getParent() {
        return parent;
    }
}
