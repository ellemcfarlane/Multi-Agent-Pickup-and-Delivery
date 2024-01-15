package com.smac.mas.searchclient;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.PriorityQueue;

public interface Frontier {
    void add(State state);

    State pop();

    boolean isEmpty();

    int size();

    boolean contains(State state);

    void clean();

    String getName();
}

class FrontierBFS
        implements Frontier {
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state) {
        this.queue.addLast(state);
        this.set.add(state);
    }

    @Override
    public State pop() {
        State state = this.queue.pollFirst();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public int size() {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state) {
        return this.set.contains(state);
    }

    @Override
    public void clean() {
        queue.clear();
        set.clear();
    }

    @Override
    public String getName() {
        return "breadth-first search";
    }
}

class FrontierDFS
        implements Frontier {
    private final ArrayDeque<State> stack = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state) {
        this.set.add(state);
        this.stack.addFirst(state);
    }

    @Override
    public State pop() {
        State state = this.stack.pollFirst();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    @Override
    public int size() {
        return this.stack.size();
    }

    @Override
    public boolean contains(State state) {
        return this.set.contains(state);
    }

    @Override
    public void clean() {
        stack.clear();
        set.clear();
    }

    @Override
    public String getName() {
        return "depth-first search";
    }
}

class FrontierBestFirst
        implements Frontier {
    private Heuristic heuristic;
    private final PriorityQueue<State> queue;
    private final HashSet<State> set = new HashSet<>(65536);

    public FrontierBestFirst(Heuristic h) {
        this.heuristic = h;
        this.queue = new PriorityQueue<>(65536, this.heuristic);
    }

    @Override
    public void add(State state) {
        this.queue.add(state);
        this.set.add(state);
    }

    @Override
    public State pop() {
        State state = this.queue.remove();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public int size() {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state) {
        return this.set.contains(state);
    }

    @Override
    public void clean() {
        queue.clear();
        set.clear();
    }

    @Override
    public String getName() {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}