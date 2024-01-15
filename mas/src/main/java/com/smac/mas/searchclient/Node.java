package com.smac.mas.searchclient;

import java.util.ArrayList;

public class Node {
    public int id;
    public int row;
    public int col;
    public int goal_type; // -1 means not a goal, 0 means box goal, 1 means agent goal
    public char goal;
    public ArrayList<Integer> neighbor;
    public ArrayList<Character> edge2Dir;

    public Node(int id, int goal_type, char goal, int row, int col) {
        this.row = row;
        this.col = col;
        this.goal_type = goal_type;
        this.goal = goal;
        this.id = id;
        this.neighbor = new ArrayList<Integer>();
        this.edge2Dir = new ArrayList<Character>();
    }
};
