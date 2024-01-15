package com.smac.mas.mapf;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class Conflict {

    public int agent1;
    public int agent2;
    public Box box1;
    public Box box2;
    public int node_id;
    public int t;
    public int tStart;
    HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation = null;

    public Conflict(int agent1, int agent2, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        this.agent1 = agent1;
        this.agent2 = agent2;
        this.node_id = node_id;
        this.t = t;
        this.boxSimulation = boxSimulation;
    }

    public Conflict(int agent, Box box, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        this.agent1 = agent;
        this.box1 = box;
        this.node_id = node_id;
        this.t = t;

        this.boxSimulation = boxSimulation;
    }

    public Conflict(int agent, int node_id, int t) {
        this.agent1 = agent;
        this.node_id = node_id;
        this.t = t;
    }

    public Conflict(int agent1, int agent2, Box box1, Box box2, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        this.agent1 = agent1;
        this.agent2 = agent2;
        this.node_id = node_id;
        this.box1 = box1;
        this.box2 = box2;
        this.t = t;
        this.boxSimulation = boxSimulation;
    }


    Conflict(int agent1, int agent2, Box box, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        this.agent1 = agent1;
        this.agent2 = agent2;
        this.node_id = node_id;
        this.box1 = box;
        this.t = t;
        this.boxSimulation = boxSimulation;
    }

    public Conflict(int agent1, Box box1, Box box2, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        this.agent1 = agent1;
        this.node_id = node_id;
        this.box1 = box1;
        this.box2 = box2;
        this.t = t;
        this.boxSimulation = boxSimulation;
    }

    public Conflict(int agent1, int node_id,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            int tStart, int tUntil) {
        this.agent1 = agent1;
        this.box1 = null;
        this.node_id = node_id;

        this.tStart = tStart;
        this.t = tUntil;

        this.boxSimulation = boxSimulation;
    }

    public String toString() {
        return "Conflict: " + this.agent1 + " " + this.agent2 + " " + this.box1 + " " + this.box2
                + " " + this.node_id + " " + this.t;
    }

    public String nameStr() {
        return this.getClass().getSimpleName();
    }

    abstract public ArrayList<Constraint> getConstraints();

}

// just triggers simulation since it has no constraints
class ResetPlanConflict extends Conflict {
    public ResetPlanConflict(int agent1, int node_id, int t) {
        super(agent1, node_id, t);
    }


    @Override
    public ArrayList<Constraint> getConstraints() {
        return null;
    }
}


class TimePlaceConflict extends Conflict {

    public TimePlaceConflict(int agent1, int agent2, int node_id, int t) {
        // todo: add simulations
        super(agent1, agent2, null, null, node_id, t, null);
    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        ArrayList<Constraint> constraints = new ArrayList<Constraint>();
        constraints.add(new TimePlaceConstraint(this.agent1, this.box1, this.node_id, this.t));
        constraints.add(new TimePlaceConstraint(this.agent2, this.box2, this.node_id, this.t));
        return constraints;
    }

}


/*
 * Conflict with a box of different color
 * Somebody is going for the box already, create a constraint
 * for the other agent to wait
 * 
 */
class Different_Color_Conflict_Wait extends Conflict {

    public Different_Color_Conflict_Wait(int agent1, int node_id, int tStart, int timeUntil,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        super(agent1, node_id, boxSimulation, tStart, timeUntil);
    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        ArrayList<Constraint> constraints = new ArrayList<Constraint>();

        constraints.add(new IntervalConstraint(this.agent1, this.node_id, this.tStart, this.t));


        return constraints;
    }

}


/*
 * Conflict with a box of different color Nobody is going for that box
 * 
 * Args: agent1: the agent that is in conflict box: the box that is in conflict
 * 
 */
class Call_For_Help_Conflict extends Conflict {


    public Call_For_Help_Conflict(int agent1, Box box, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        super(agent1, box, node_id, t, boxSimulation);

        // System.err.println("Call for help!");
        // System.err.println("agent: " + agent1 + "boxtype: " + box.boxType + "index: " + box.boxIndex);

    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        return null;
    }

}


class TimePlaceBoxAgentConflict extends Conflict {

    public TimePlaceBoxAgentConflict(int agent1, int agent2, Box box1, Box box2, int node_id,
            int t) {
        // todo: add simulations
        super(agent1, agent2, box1, box2, node_id, t, null);
    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        ArrayList<Constraint> constraints = new ArrayList<Constraint>();
        constraints.add(new TimePlaceConstraint(this.agent1, this.box1, this.node_id, this.t));
        constraints.add(new TimePlaceConstraint(this.agent2, this.box2, this.node_id, this.t));
        return constraints;
    }

}


// TODO: again, reduce code duplication with these conflicts, have them separated for now for
// thoroughness
// just in case
class TimePlaceBoxBoxConflict extends Conflict {

    public TimePlaceBoxBoxConflict(int agent1, int agent2, Box box1, Box box2, int node_id, int t) {
        // todo: add simulations
        super(agent1, agent2, box1, box2, node_id, t, null);
    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        ArrayList<Constraint> constraints = new ArrayList<Constraint>();
        constraints.add(new TimePlaceConstraint(this.agent1, this.box1, this.node_id, this.t));
        constraints.add(new TimePlaceConstraint(this.agent2, this.box2, this.node_id, this.t));
        return constraints;
    }

}


class SameColorStaticBoxAgentConflict extends Conflict {

    public SameColorStaticBoxAgentConflict(int agent, Box box, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        super(agent, box, node_id, t, boxSimulation);
    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        // ArrayList<Constraint> constraints = new ArrayList<Constraint>();
        // constraints.add(new TimePlaceConstraint(agent1, this.node_id, this.t));
        // return constraints;
        return null;
    }

}


class SameColorStaticBoxBoxConflict extends Conflict {

    public SameColorStaticBoxBoxConflict(int agent, Box staticBox, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        super(agent, staticBox, node_id, t, boxSimulation);
    }


    @Override
    public ArrayList<Constraint> getConstraints() {
        // for now just simulate, TODO: ask for help to move staticBox
        return null;
    }

}

// NOTE: not currently used, just using Call_For_Help_Conflict
class DiffColorStaticBoxBoxConflict extends Conflict {

    public DiffColorStaticBoxBoxConflict(int agent, Box staticBox, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        super(agent, staticBox, node_id, t, boxSimulation);
        // TODO: ask for help to move box2 (static box)
        throw new RuntimeException("DiffColorStaticBoxBoxConflict: not yet implemented!");
    }


    @Override
    public ArrayList<Constraint> getConstraints() {
        return null;
    }

}

// NOTE: not currently used, just using Call_For_Help_Conflict
class DiffColorStaticBoxAgentConflict extends Conflict {
    public DiffColorStaticBoxAgentConflict(int agent, Box box, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        super(agent, box, node_id, t, boxSimulation);
        throw new RuntimeException("DiffColorStaticBoxAgentConflict: not yet implemented!");
    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        return null;
    }

}


// MissingRSCConflict
class MissingRSCConflict extends Conflict {

    public MissingRSCConflict(int agent1, Box box, int node_id, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {
        super(agent1, box, node_id, t, boxSimulation);
        // throw new RuntimeException("MissingRSCConflict: not yet implemented!");
    }


    @Override
    public ArrayList<Constraint> getConstraints() {
        return null;
    }

}


class ResourceConflict extends Conflict {

    public int box_dest1;
    public int box_dest2;
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation1;
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation2;

    public ResourceConflict(int agent1, int agent2, Box box, int node_id, int box_dest1,
            int box_dest2, int t,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation1,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation2) {

        super(agent1, agent2, box, node_id, t, null);
        this.box_dest1 = box_dest1;
        this.box_dest2 = box_dest2;
        this.boxSimulation1 = boxSimulation1;
        this.boxSimulation2 = boxSimulation2;

    }

    private ArrayList<Constraint> generateRSCConstraints(int agent, Box box, int node_id, int t,
            int box_dest) {
        ArrayList<Constraint> constraints = new ArrayList<Constraint>();
        // the TimePlace constraints are basically to avoid conflicts we know may happen in RSC
        // conflicts so let's prevent them from happening for efficiency
        // although things also break if I remove them so... don't :)
        constraints.add(new ResourceConstraint(agent, box, node_id, t));
        constraints.add(new TimePlaceConstraint(agent, box_dest, t - 1));
        constraints.add(new TimePlaceConstraint(agent, box_dest, t));
        constraints.add(new TimePlaceConstraint(agent, node_id, t + 1));
        return constraints;
    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        ArrayList<Constraint> constraints1 =
                generateRSCConstraints(agent1, box1, node_id, t, box_dest1);

        // note: passing in box1 to this constraint is not a typo, they should be same box
        ArrayList<Constraint> constraints2 =
                generateRSCConstraints(agent2, box1, node_id, t, box_dest2);

        ArrayList<Constraint> constraints = new ArrayList<Constraint>();
        constraints.add(new MetaConstraint(agent1, constraints1, boxSimulation1));
        constraints.add(new MetaConstraint(agent2, constraints2, boxSimulation2));

        return constraints;
    }

}


class AgentFollowedConflict extends Conflict {

    public AgentFollowedConflict(int agent1, int agent2, Box box1, Box box2, int node_id, int t) {
        super(agent1, agent2, box1, box2, node_id, t, null);
    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        ArrayList<Constraint> constraints = new ArrayList<Constraint>();
        // agent1 is the one that follows agent2
        constraints.add(new TimePlaceConstraint(this.agent1, this.box1, this.node_id, this.t));
        constraints.add(new TimePlaceConstraint(this.agent2, this.box2, this.node_id, this.t - 1));
        return constraints;
    }

}


// TODO: refactor to reduce code duplication
// I made separate classes for debugging purposes
class BoxFollowedConflict extends Conflict {

    public BoxFollowedConflict(int agent1, int agent2, Box box1, Box box2, int node_id, int t) {
        super(agent1, agent2, box1, box2, node_id, t, null);
    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        ArrayList<Constraint> constraints = new ArrayList<Constraint>();
        // agent1 is the one that follows agent2
        constraints.add(new TimePlaceConstraint(this.agent1, this.box1, this.node_id, this.t));
        constraints.add(new TimePlaceConstraint(this.agent2, this.box2, this.node_id, this.t - 1));
        return constraints;
    }

}


class LazyConflict extends Conflict {
    public LazyConflict(int agent1, int agent2, int node_id, int t) {
        super(agent1, agent2, node_id, t, null);
    }

    @Override
    public ArrayList<Constraint> getConstraints() {
        ArrayList<Constraint> constraints = new ArrayList<Constraint>();
        // agent2 is the one that won't move out of agent1's way
        // constraint is only on the agent2 bc he won't move
        constraints.add(new TimePlaceConstraint(this.agent2, this.node_id, this.t));
        return constraints;
    }
}
