package com.smac.mas.searchclient;

import java.util.Objects;

public class Tuple<X, Y> {
    public final X x;
    public final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof Tuple))
            return false;
        Tuple<X, Y> other = (Tuple<X, Y>) obj;
        return other.x.equals(this.x) && other.y.equals(this.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
};