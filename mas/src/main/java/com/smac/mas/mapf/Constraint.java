package com.smac.mas.mapf;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class Constraint {

    public int agent;
    public int node_id;
    public int t;
    public int tStart; // just for interval constraints
    public Box box;
    public ArrayList<Constraint> constraints;
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation;

    public Constraint(int agent, Box box, int node_id, int t) {
        this.agent = agent;
        this.box = box;
        this.node_id = node_id;
        this.t = t;
        this.constraints = null;
        this.boxSimulation = null;

        this.tStart = -1;
    }

    public Constraint(int agent, int node_id, int t) {
        this.agent = agent;
        this.box = null;
        this.node_id = node_id;
        this.t = t;
        this.constraints = null;
        this.boxSimulation = null;

        this.tStart = -1;
    }

    public Constraint(int agent, int node_id, int tStart, int tEnd) {
        this.agent = agent;
        this.box = null;
        this.node_id = node_id;
        this.t = tEnd;
        this.tStart = tStart;
        this.constraints = null;
        this.boxSimulation = null;
    }

    public Constraint(int agent, ArrayList<Constraint> constraints) {
        this.agent = agent;
        this.constraints = constraints;
        this.boxSimulation = null;
        this.tStart = -1;
    }

    public boolean isIntervalConstraint(){
        return false;
    }

    public String toString() {
        return "Constraint: " + "agent: " + this.agent + " " + ",node: "
                                + this.node_id + " " + ",t: " + this.t + ",box:"
                                + this.box;
    }
    

}


class TimePlaceConstraint extends Constraint {

    public TimePlaceConstraint(int agent, int node_id, int t) {
        super(agent, node_id, t);
    }

    public TimePlaceConstraint(int agent, Box box, int node_id, int t) {
        super(agent, box, node_id, t);
    }

    public boolean isIntervalConstraint() {
        return false;
    }

}

class IntervalConstraint extends Constraint {

    public IntervalConstraint(int agent, int node_id, int tStart, int t) {
        super(agent, node_id, tStart, t);
        
    }

    public boolean isIntervalConstraint() {
        return true;
    }

}

class ResourceConstraint extends Constraint {

    public ResourceConstraint(int agent, int node_id, int t) {
        super(agent, node_id, t);
    }

    public ResourceConstraint(int agent, Box box, int node_id, int t) {
        super(agent, box, node_id, t);
    }

    public boolean isIntervalConstraint() {
        return false;
    }
}


// basically a wrapper class to hold a list of constraints
class MetaConstraint extends Constraint {

    public MetaConstraint(int agent, ArrayList<Constraint> constraints,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        super(agent, constraints);
        this.boxSimulation = boxSimulation;
    }

    public boolean isIntervalConstraint() {
        return false;
    }
}
