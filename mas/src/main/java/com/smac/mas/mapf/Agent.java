package com.smac.mas.mapf;

import com.smac.mas.mapf.Goal.GoalType;

public class Agent {

    public int agentNumber;
    public boolean isFat;
    public Goal currentGoal;
    public boolean isFree;

    public Agent(int agentNumber) {
        this.agentNumber = agentNumber;
        this.isFat = false;
        this.isFree = true;
        this.currentGoal = new Goal(GoalType.NO_GOAL);
    }

    public Box getBox() {
        return this.currentGoal.getBox();
    }

    public void setFat(boolean isFat) {
        this.isFat = isFat;
    }

    public boolean isFat() {
        return this.isFat;
    }

    public void setCurrentGoal(Goal currentGoal) {
        this.currentGoal = currentGoal;
    }

    public Goal getCurrentGoal() {
        return this.currentGoal;
    }

    public int getAgentNumber() {
        return this.agentNumber;
    }

    public void setIsFree(boolean isFree) {
        this.isFree = isFree;
    }

    public boolean isFree() {
        return this.isFree;
    }

}
