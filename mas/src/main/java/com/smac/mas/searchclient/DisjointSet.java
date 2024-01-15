package com.smac.mas.searchclient;

class Operation {
    int id;
    int l;
    int r;
    boolean rankdelta;

    Operation() {
        l = -1;
    }
};

public class DisjointSet {
    private int[] parent; // parent[i] is the parent of i
    private int[] rank; // rank[i] is the rank of the set containing i

    public DisjointSet(int size) {
        parent = new int[size];
        rank = new int[size];

        // Initialize each element to be its own parent
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    public DisjointSet(DisjointSet ds) {
        this.parent = ds.parent.clone();
        this.rank = ds.rank.clone();
    }

    public void reset() {
        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    public void revert(Operation op) {
        if (op.l == -1)
            return;
        parent[op.l] = op.l;
        if (op.rankdelta)
            rank[op.r]--;
    }

    // Find the root of the set containing i, with path compression
    private int find(int i) {
        if (parent[i] != i) {
            parent[i] = find(parent[i]); // path compression
        }
        return parent[i];
    }

    public int find_without_compression(int i) {
        if (parent[i] != i) {
            return find_without_compression(parent[i]);
        }
        return parent[i];
    }

    public Operation unionWithRetOps(int i, int j) {

        int rootI = find_without_compression(i);
        int rootJ = find_without_compression(j);
        Operation op = new Operation();

        if (rootI == rootJ) {
            return op; // Already in the same set
        }

        // Union by rank
        if (rank[rootI] < rank[rootJ]) {
            parent[rootI] = rootJ;
            op.l = rootI;
            op.r = rootJ;
            op.rankdelta = false;
        } else if (rank[rootI] > rank[rootJ]) {
            parent[rootJ] = rootI;
            op.l = rootJ;
            op.r = rootI;
            op.rankdelta = false;
        } else {
            parent[rootI] = rootJ;
            rank[rootJ]++;
            op.l = rootI;
            op.r = rootJ;
            op.rankdelta = true;
        }
        return op;
    }

    // Union the sets containing i and j, with union by rank
    public void union(int i, int j) {
        int rootI = find(i);
        int rootJ = find(j);

        if (rootI == rootJ) {
            return; // Already in the same set
        }

        // Union by rank
        if (rank[rootI] < rank[rootJ]) {
            parent[rootI] = rootJ;
        } else if (rank[rootI] > rank[rootJ]) {
            parent[rootJ] = rootI;
        } else {
            parent[rootI] = rootJ;
            rank[rootJ]++;
        }
    }
}
