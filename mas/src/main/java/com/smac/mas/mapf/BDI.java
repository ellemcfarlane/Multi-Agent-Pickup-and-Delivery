package com.smac.mas.mapf;

import com.smac.mas.searchclient.State;
import com.smac.mas.searchclient.Action;
import com.smac.mas.searchclient.Frontier;
import com.smac.mas.mapf.Goal.GoalType;

import java.util.ArrayList;
import java.util.HashMap;

import com.smac.mas.searchclient.*;

/*
 * This class implements BDI multiagent architecture and should be called as a main search in the
 * SearchClient.java
 * 
 */

public class BDI {

    /*
     * Function that implements the BDI architecture
     * 
     * @param initialState the initial state of the problem
     * 
     * @param frontier the frontier to be used in the search
     * 
     * @return the plan of the agents
     * 
     */
    public static ActionWrapper[][] search(State initialState, Frontier frontier) {

        // set current state to the initial state
        State currentState = initialState;
        // list with stored solutions for a current task but not for all agents
        DoubleLinkList<ActionWrapper>[] subSolution = null;
        int iterationNumber = 0;

        // create agents
        Agent[] agents = new Agent[initialState.numAgents];
        for (int i = 0; i < initialState.numAgents; i++) {
            // current goal is null, all of them are free
            agents[i] = new Agent(initialState.agentLabel[i]);
        }

        // create Box instances
        HashMap<Integer, ArrayList<Box>> boxes = new HashMap<>();
        for (int boxType = 0; boxType < initialState.boxMap.length; boxType++) {
            for (int boxIndex = 0; boxIndex < initialState.boxMap[boxType].size(); boxIndex++) {

                if (!boxes.containsKey(boxType)) {
                    boxes.put(boxType, new ArrayList<Box>());
                }

                boxes.get(boxType).add(new Box(boxType, boxIndex));

            }
        }

        // while the goal is not reached
        while (!currentState.isGoalState()) {

            // print status
            // if (iterationNumber++ % 1000 == 0) {
            // printStatus(iterationNumber);
            // }
            printStatus(iterationNumber++);
            // pass unsatisfied goals and free agents to the TaskManager - TaskManager sets
            // goals in
            // the Agent class
            TaskManager.assignTasks(currentState, agents, null, null, null, boxes);

            // plan low level for each agent - goals can change in the CBS tree
            // TODO: handle when no overall solution found (solutionNode == null)
            CTNode solutionNode = CBS.searchforBDI(currentState, frontier, agents, boxes, subSolution);
            if (solutionNode == null) {
                System.err.println("ERROR: no solution found");
                return null;
            }
            agents = solutionNode.agents;
            boxes = solutionNode.boxes;

            // execute first N steps and get new state + set free agents
            currentState = BDI.execute(solutionNode, currentState);
            // save partially executed solution
            subSolution = solutionNode.solution;

            // if (iterationNumber == 2) {
            // System.err.println(agents[0].getCurrentGoal().type);
            // System.err.println(agents[0].getCurrentGoal().box.boxType);
            // System.err.println("WARNING RETURNING BDI EARLY!");
            // return currentState.extractPlan();
            // }

            // System.err.println("WARNING: RETURNING BDI EARLY!");
            // return currentState.extractPlan();

        }

        return currentState.extractPlan();

    }

    /*
     * Function that changes the current state and returns a new one with executed
     * actions These
     * actions are not yet executed, but will be later after the BDI Agents isFree
     * booleans are set
     * in this function
     * 
     * @param currentState the current state of the problem
     * 
     * @param node solution node
     * 
     * @return new state
     * 
     */
    private static State execute(CTNode solutionNode, State currentState) {

        int smallestN = solutionNode.getSmallestPlanSize();
        // if (smallestN == 0) {
        // System.err.println("debug");
        // }
        ActionWrapper[] jointAction = new ActionWrapper[currentState.numAgents];
        Agent[] agents = solutionNode.agents;

        // System.err.println("smallestN: " + smallestN);
        // System.err.print(currentState);
        // execute smallestN steps for all agents
        for (int step = 0; step < smallestN; step++) {
            for (int agent = 0; agent < currentState.numAgents; agent++) {

                ActionWrapper action;
                boolean agentHasNextAction = solutionNode.solution[agent].size() > 0;
                action = agentHasNextAction ? solutionNode.solution[agent].removeFirst()
                        : new ActionWrapper(Action.NoOp, "NoOp");
                // get joint action
                jointAction[agent] = action;
                // System.err.print(action + "|");

            }

            // if (step == 3) // todo delete
            // return currentState;

            // apply joint action
            currentState = currentState.step(jointAction);
            // System.err.print(currentState);
        }

        // set free agents which just finished or have no goal
        for (int agent = 0; agent < currentState.numAgents; agent++) {
            if (solutionNode.solution[agent].size() == 0
                    || agents[agent].getCurrentGoal().getType() == GoalType.NO_GOAL) {
                agents[agent].setIsFree(true);
            }

            // if agent wanted to help and finished - this means that there are not more
            // conflicts
            if (solutionNode.solution[agent].size() == 0
                    && agents[agent].getCurrentGoal().getType() == GoalType.HELP_WITH_BOX) {
                agents[agent].setCurrentGoal(new Goal(GoalType.NO_GOAL));
                agents[agent].isFat = false;
                // agents[agent].setIsFree(true); // todo: try this out
            }
        }

        return currentState;
    }

    /*
     * Function that prints out the current status of the BDI
     * 
     * @param iteration: current iteration of the BDI
     */
    private static void printStatus(int iteration) {

        System.err.println("\n----------------------------");
        System.err.println("BDI iteration: " + iteration); // print out current status
        System.err.println("----------------------------\n");

    }

}
