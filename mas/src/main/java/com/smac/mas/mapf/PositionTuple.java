package com.smac.mas.mapf;
import java.util.Objects;

public class PositionTuple {
    public final int agent;
    public final int box;
    public final int time;

    public PositionTuple(int x) {
        this.agent = x;
        this.box = x;
        this.time = 0;
    }

    public PositionTuple(int x, int y) {
        this.agent = x;
        this.box = y;
        this.time = 0;
    }

    public PositionTuple(int x, int y, int t) {
        this.agent = x;
        this.box = y;
        this.time = t;
    }

    public PositionTuple(PositionTuple t, int shift) {
        this.agent = t.agent;
        this.box = t.box;
        this.time = t.time + shift;
    }

    public boolean positionEquals(PositionTuple other) {
        return this.box == other.box;
    }

    @Override
    public String toString() {
        return "(" + agent + ", " + box + ", " + time + ")";
    }

    @Override
    public boolean equals(Object obj) {
        PositionTuple other = (PositionTuple) obj;
        return this.agent == other.agent && this.box == other.box && this.time == other.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.agent, this.box, this.time);
    }
}