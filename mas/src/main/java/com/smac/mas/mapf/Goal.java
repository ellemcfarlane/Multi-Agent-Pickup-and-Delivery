package com.smac.mas.mapf;

public class Goal {

    public enum GoalType {
        REACH_BOX,
        REACH_AGENT_GOAL, // can be only 1 per agent - does not need destination
        NO_GOAL,
        MOVE_BOX,
        HELP_WITH_BOX
    }

    // private char goalType;
    public GoalType type;
    public Box box;

    public Goal(GoalType type) {
        this.type = type;
        this.box = null;
    }

    public Goal(GoalType type, Box box) {
        this.type = type;
        this.box = box;
    }

    public GoalType getType() {
        return type;
    }

    public void setBox(Box box) {
        this.box = box;
    }

    public Box getBox() {
        return box;
    }

    // public char getGoalType() {
    // return goalType;
    // }
}
