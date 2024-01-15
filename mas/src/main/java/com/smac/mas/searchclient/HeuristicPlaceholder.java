package com.smac.mas.searchclient;

// just used for testing in HeuristicTest.class
public class HeuristicPlaceholder extends Heuristic {
    public HeuristicPlaceholder(State initialState) {
        super(initialState);
    }

    @Override
    public int f(State s) {
        return s.g() + this.h(s);
    }

    @Override
    public String toString() {
        return "Place holder heuristic used for testing";
    }
}
