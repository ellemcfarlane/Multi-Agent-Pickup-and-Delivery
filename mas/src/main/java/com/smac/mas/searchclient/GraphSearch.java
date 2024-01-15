package com.smac.mas.searchclient;

import java.util.HashSet;

import com.smac.mas.mapf.Agent;
import com.smac.mas.mapf.Box;
import com.smac.mas.mapf.DoubleLinkList;
import com.smac.mas.mapf.SimulationAction;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphSearch {

    /*
     * Classical search method that takes a frontier and returns a plan or null if
     * non-existent
     */
    public static int print_every = 1;
    public static boolean shouldPrint = false;

    public static ActionWrapper[][] search(State initialState, Frontier frontier) {
        boolean outputFixedSolution = false;

        if (outputFixedSolution) {
            return new ActionWrapper[][] {};
        } else {
            int iterations = 0;
            State state = null;
            // if any agent starts off in constrained time-space, return null
            if (!initialState.isValid()) {
                return null;
            }

            frontier.add(initialState);
            HashSet<State> expanded = new HashSet<>();
            while (!frontier.isEmpty()) {
                // print a status message every 10000 iteration
                if (++iterations % print_every == 0) {
                    printSearchStatus(expanded, frontier);
                }

                state = frontier.pop();
                if (state.isGoalState()) {
                    // printSearchStatus(expanded, frontier);
                    ActionWrapper[][] plan = state.extractPlan();
                    return plan;
                }

                expanded.add(state);
                ArrayList<State> children = state.getExpandedStates();
                for (State childState : children) {
                    if (!(frontier.contains(childState) || expanded.contains(childState))) {
                        frontier.add(childState);
                    }
                }
            }
            // failed to find solution
            return null;
        }
    }

    public static ActionWrapper[][] search(State initialState, Agent currentAgent,
            Frontier frontier) {
        boolean outputFixedSolution = false;

        if (outputFixedSolution) {
            return new ActionWrapper[][] {};
        } else {
            int iterations = 0;
            State state = null;
            // if any agent starts off in constrained time-space, return null
            if (!initialState.isValid()) {
                return null;
            }

            frontier.add(initialState);
            HashSet<State> expanded = new HashSet<>();
            while (!frontier.isEmpty()) {
                // print a status message every 10000 iteration
                if (++iterations % print_every == 0) {
                    printSearchStatus(expanded, frontier);
                }

                state = frontier.pop();
                if (state.isGoalState(currentAgent)) {
                    // printSearchStatus(expanded, frontier);
                    ActionWrapper[][] plan = state.extractPlan();
                    return plan;
                }

                expanded.add(state);

                ArrayList<State> children = state.getExpandedStates();
                for (State childState : children) {
                    if (!(frontier.contains(childState) || expanded.contains(childState))) {
                        frontier.add(childState);
                    }
                }
            }
            // failed to find solution
            return null;
        }
    }

    // latest version of search
    public static ActionWrapper[][] search(State initialState, Agent currentAgent,
            Frontier frontier,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            int desiredTime) {
        boolean outputFixedSolution = false;

        State.agentInPartialState = currentAgent; // set staticly for heuristics

        if (outputFixedSolution) {
            return new ActionWrapper[][] {};
        } else {
            int iterations = 0;
            State state = null;
            // if any agent starts off in constrained time-space, return null
            if (!initialState.isValid(currentAgent)) {
                return null;
            }

            frontier.add(initialState);
            HashSet<State> expanded = new HashSet<>();
            while (!frontier.isEmpty()) {
                if (++iterations % print_every == 0 && shouldPrint) {
                    printSearchStatus(expanded, frontier);
                }

                state = frontier.pop();
                if (state.isGoalState(currentAgent) && state.g() >= desiredTime) {
                    // System.err.println(state);
                    // printSearchStatus(expanded, frontier);
                    ActionWrapper[][] plan = state.extractPlan();
                    return plan;
                }

                expanded.add(state);

                ArrayList<State> children = state.getExpandedStates(currentAgent);
                for (State childState : children) {

                    // simulate changes at this time step in the child state
                    if (boxSimulation != null)
                        GraphSearch.simulate(childState, boxSimulation);

                    if (!(frontier.contains(childState) || expanded.contains(childState))) {
                        frontier.add(childState);
                    }
                }
            }
            // failed to find solution
            return null;
        }
    }

    /*
     * Simulate all changes
     */
    private static void simulate(State childState,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {

        int time = childState.g();

        // nothing happens at this time slot
        if (!boxSimulation.containsKey(time)) {
            return;
        }

        // get boxes that are moved at this time slot
        for (int boxType : boxSimulation.get(time).keySet()) {

            if (childState.boxMap[boxType].size() == 0) { // shouldnt move with this box for this agent - only as
                                                          // shadowed - not implemented yet
                continue;
            }

            for (int boxInd : boxSimulation.get(time).get(boxType).keySet()) {

                SimulationAction action = boxSimulation.get(time).get(boxType).get(boxInd);

                // Box box = action.box;
                int newPos = action.newPos;

                // update its position
                int oldPosition = childState.boxMap[boxType].get(boxInd);

                // only allow simulation here if the box is still there and no box is in the new
                // position
                if (oldPosition == action.prevPos && !childState.node2box.containsKey(newPos)) {
                    childState.boxMap[boxType].set(boxInd, newPos);

                    childState.node2box.remove(oldPosition);
                    childState.node2boxIdx.remove(oldPosition);
                    childState.node2box.put(newPos, (char) (boxType + 'A'));
                    childState.node2boxIdx.put(newPos, boxInd);

                }

                // System.err.println("box " + boxType + "_" + boxInd + " moved to " + newPos
                // + " from " + oldPosition);

            }
        }

    }

    private static long startTime = System.nanoTime();

    private static void printSearchStatus(HashSet<State> expanded, Frontier frontier) {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        System.err.format(statusTemplate, expanded.size(), frontier.size(),
                expanded.size() + frontier.size(), elapsedTime, Memory.stringRep());
    }
}

// if(iterations == 1){
// // check_state = state;
// State newState = new State(state);
// check_state = newState;
// System.err.println(check_state.agentPosition.hashCode());
// System.err.println(state.agentPosition.hashCode());
// for(int i : check_state.agentPosition)
// System.err.println(i);

// for(int i : state.agentPosition)
// System.err.println(i);

// System.err.println(check_state.agentPosition.equals(state.agentPosition));
// // System.err.println(check_state.equals(state));
// // System.err.println(check_state == state);
// // System.err.println(check_state.hashCode());
// // System.err.println(state.hashCode());
// // System.err.println(expanded.contains(check_state));
// }
