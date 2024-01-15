package com.smac.mas.mapf;


/*
 * This class represents an action in the simulation
 */
public class SimulationAction {

    public Agent agent;
    public int newPos;
    public int prevPos;
    public Box box; // might delete if not used in the future
    public int time;

    // TODO (sim): should this be copies of the agents and boxes instead of references?

    public SimulationAction(Agent agent, int newPos, int prevPos, int time) {

        this.agent = agent;
        this.newPos = newPos;
        this.prevPos = prevPos;
        this.box = null;
        this.time = time;

    }

    public SimulationAction(Agent agent, Box box, int newPos, int prevPos, int time) {

        this.agent = agent;
        this.newPos = newPos;
        this.prevPos = prevPos;
        this.box = box;
        this.time = time;

    }

}
