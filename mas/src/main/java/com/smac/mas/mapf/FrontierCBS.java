package com.smac.mas.mapf;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.TreeSet;

public class FrontierCBS {
    private final PriorityQueue<CTNode> primaryQueue;
    private final PriorityQueue<CTNode> secondaryQueue;
    private final TreeSet<CTNode> tracker;
    private final HashSet<CTNode> set;
    private final float w;

    public FrontierCBS(float w, Comparator<CTNode> primaryComparator, Comparator<CTNode> secondaryComparator) {
        primaryQueue = new PriorityQueue<>(65536, primaryComparator);
        secondaryQueue = new PriorityQueue<>(65536, secondaryComparator);
        tracker = new TreeSet<>(primaryComparator);
        set = new HashSet<>(65536);
        this.w = w;
    }

    public void add(CTNode node) {
        this.primaryQueue.add(node);
        this.set.add(node);
    }

    public CTNode pop() {
        int bestCost = Integer.MAX_VALUE;

        if (!primaryQueue.isEmpty()) {
            bestCost = primaryQueue.peek().cost;
        }

        if (!tracker.isEmpty()) {
            bestCost = Math.min(bestCost, tracker.first().cost);
        }

        if (bestCost == Integer.MAX_VALUE)
            return null;

        while (!primaryQueue.isEmpty() && primaryQueue.peek().cost <= this.w * ((float) bestCost)) {
            CTNode candidate = primaryQueue.remove();
            secondaryQueue.add(candidate);
            tracker.add(candidate);
        }

        CTNode next = secondaryQueue.remove();
        tracker.remove(next);
        set.remove(next);

        return next;
    }

    public CTNode peek() {
        return this.primaryQueue.peek();
    }

    public boolean isEmpty() {
        return this.primaryQueue.isEmpty() && this.secondaryQueue.isEmpty();
    }

    public int size() {
        return this.primaryQueue.size() + this.secondaryQueue.size();
    }

    public boolean contains(CTNode node) {
        return this.set.contains(node);
    }

    public void clean() {
        primaryQueue.clear();
        secondaryQueue.clear();
        tracker.clear();
        set.clear();
    }

    public String getName() {
        return "conflict based search";
    }
}

class CTNodeComparator implements Comparator<CTNode> {
    // Overriding compare()method of Comparator
    // for ascending order of CTNode
    public int compare(CTNode n1, CTNode n2) {
        if (n1.cost < n2.cost)
            return -1;
        else if (n1.cost > n2.cost)
            return 1;
        return 0;
    }
}

class SecondaryCTNodeComparator implements Comparator<CTNode> {
    // Should ideally prefer node where less amount of conflicts are left to be
    // resolved. But for now, we prioritize the node with more constraints i.e depth
    public int compare(CTNode n1, CTNode n2) {
        return ConflictEstimator.getNumConflictLowerBound(n1) - ConflictEstimator.getNumConflictLowerBound(n2);
    }
}
