package com.smac.mas.mapf;

import com.smac.mas.searchclient.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.smac.mas.mapf.Goal.GoalType;

public class CBS {
    public static CTNode searchforBDI(State currentState, Frontier frontier, Agent[] agents,
            HashMap<Integer, ArrayList<Box>> boxes,
            DoubleLinkList<ActionWrapper>[] previousSolution) {

        // create partial states
        // System.err.println(agents.length);
        CTNode root = new CTNode(currentState.numAgents, agents, boxes);
        State[] partialStates = currentState.partialStates(agents, boxes);
        CTNode node = null;
        // reset constraints to ensure optimal path for each agent
        // in initial call to LowLevelSearch before CBS
        State.constraints = null;
        State.resourceConstraints = null;

        ////////////////////////////////// set already planned routes
        ////////////////////////////////// /////////////////////////////

        if (previousSolution != null) {

            for (int agent = 0; agent < currentState.numAgents; agent++) {

                DoubleLinkList<ActionWrapper> solution = previousSolution[agent];

                // agent did not finish his plan in previous BDI iteration
                // i.e. still has non-conflicting plan
                if (solution.size() != 0) {
                    root.solution[agent] = solution;
                }
            }
        }

        /////////////////////////////////// plan only for free agents
        /////////////////////////////////// /////////////////////////////

        for (int freeAgent = 0; freeAgent < currentState.numAgents; freeAgent++) {
            Goal agentsGoal = agents[freeAgent].getCurrentGoal();

            // agent already has a plan or has no goal
            if (root.solution[freeAgent].size() != 0 || agentsGoal.getType() == GoalType.NO_GOAL) {
                continue;
            }

            // sets solution for free agent in the root.solution[freeAgent]
            // stops search and returns null if any of the agents has no solution
            int desiredTime = -1; // don't care about time goal is found
            if (!call_LowLevelSearch(partialStates, freeAgent, frontier, root, null, desiredTime)) {
                // TODO (elle): if we want partial solutions, we need to change
                // the return to a continue and decide how to handle in CBS
                // test with MATestPartialFail

                // stops search and returns null if any of the agents has no solution
                return null;
            }
        }

        /////////////////////////////////// CBS search
        /////////////////////////////////// ///////////////////////////////////////////

        FrontierCBS open = new FrontierCBS(1.5f, new CTNodeComparator(), new SecondaryCTNodeComparator());
        root.setCost();
        open.add(root);
        int CBSNodesPopped = 0;

        while (!open.isEmpty()) {
            node = open.pop();
            // keep track of CBS nodes for performance reasons & debugging
            CBSNodesPopped++;
            // leave for debugging
            System.err.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.err.println(
            "CBS Nodes Popped: " + CBSNodesPopped + " with solution COST of: " +
            node.cost);
            System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
            //////// check if found a non-conflicting plan ////////

            if (node.validate(partialStates, currentState)) {
                // System.err.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                // System.err.println("CBS Nodes Popped: " + CBSNodesPopped
                // + " with solution COST of: " + node.cost);
                // System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
                return node;
            }
            System.err.println(node.conflict.nameStr());
            // get conflict / simulation
            ArrayList<Constraint> newConstraints = node.conflict.getConstraints();
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation = node.conflict.boxSimulation;

            /////////// an agent calls for help ///////////

            if (node.conflict instanceof Call_For_Help_Conflict) {

                // create a new node

                CTNode newnode = CallForHelp(node, currentState, partialStates, frontier, boxSimulation);

                partialStates = currentState.partialStates(newnode.agents, newnode.boxes);
                newnode.setCost();
                open.clean();
                open.add(newnode);

                continue; // in the next pop - interval constraint should happen (if helping agent
                          // was assigned)

            }

            /////////// just simulate the box ///////////

            // just simulate the box (aka for SameColorStaticBoxConflict)
            if (newConstraints == null) {

                // so far we dont need to create a new node - BUT I (elle) NOW AM IN CASE IT
                // CREATES PROBLEM!
                CTNode newnode = new CTNode(node.cost, node.solution, node.constraints,
                        node.conflict, agents, boxes, node.helpingColors, node.tempGoalLocation,
                        node.additionalConstraints);

                int agent = newnode.conflict.agent1;

                newnode.solution[agent].clear();

                // set constraints for the next low-level search
                State.constraints = newnode.constraints;
                State.resourceConstraints = newnode.resourceConstraints;

                // add newnode only if a solution was found
                if (call_LowLevelSearch(partialStates, agent, frontier, newnode, boxSimulation, -1)) {
                    newnode.setCost();
                    open.add(newnode);
                }

            }

            /////////// add constraints //////////
            else {
                for (Constraint constraint : newConstraints) {

                    CTNode newnode = new CTNode(node.cost, node.solution, node.constraints,
                            node.conflict, agents, boxes, node.helpingColors, node.tempGoalLocation,
                            node.additionalConstraints);

                    int agent = constraint.agent;
                    if (constraint instanceof TimePlaceConstraint || constraint instanceof IntervalConstraint) {
                        newnode.addNormalConstraint(constraint);
                        // leave for debugging
                        System.err.println(constraint);
                        // System.err.println("Number of constraints: " + newnode.constraints.size());
                        // System.err.println(newnode.constraints. );

                    } else {
                        for (Constraint subConstraint : ((MetaConstraint) constraint).constraints) {
                            if (subConstraint instanceof ResourceConstraint) {
                                newnode.addResourceConstraint(subConstraint);
                            } else {
                                newnode.addNormalConstraint(subConstraint);
                            }
                        }
                    }
                    newnode.solution[agent].clear();

                    // override boxSimulation from conflict if constraint has one
                    if (constraint.boxSimulation != null) {
                        boxSimulation = constraint.boxSimulation;
                    }
                    // set constraints for the next low-level search
                    State.constraints = newnode.constraints;
                    State.resourceConstraints = newnode.resourceConstraints;
                    // TODO (elle): ensure A* favors NoOp over moving, not sure it does if cost of
                    // NoOp
                    // is same as other actions
                    int desiredTime = constraint.t + 1;
                    // add newnode only if a solution was found
                    if (call_LowLevelSearch(partialStates, agent, frontier, newnode, boxSimulation,
                            desiredTime)) {

                        System.err.println("new sol. len.: " + newnode.solution[agent].size());

                        newnode.setCost();
                        // int prevCost = newnode.cost;
                        // if (prevCost == newnode.cost) {
                        // System.err.println(
                        // "WARNING: child CBS node has same cost as parent. Could be a BUG!");
                        // }

                        // agents with help goal should be constraint with bigger priority
                        if (agents[agent].currentGoal.type != GoalType.HELP_WITH_BOX
                                && agents[agent].currentGoal.type != GoalType.REACH_BOX) {
                            newnode.setCost(50);
                        }

                        open.add(newnode);
                    }
                }
            }

        }
        // System.err.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        // System.err.println("CBS Nodes Popped BEFORE FAILURE: " + CBSNodesPopped);
        // System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
        return null;
    }

    /*
     * call the low level search for a specific agent, which sets a solution in the
     * solution[agent]
     * link list
     * 
     * @param partialStates - the partial states of the agents
     * 
     * @param freeAgent - the agent to plan for
     * 
     * @param frontier - the frontier to use
     * 
     * @param currentNode - the current CT node
     */
    private static boolean call_LowLevelSearch(State[] partialStates, int agent, Frontier frontier,
            CTNode currentNode,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            int desiredTime) {

        try {
            Agent currentAgent = currentNode.agents[agent];
            ActionWrapper[][] path = GraphSearch.search(partialStates[agent], currentAgent,
                    frontier, boxSimulation, desiredTime);
            frontier.clean();
            if (path != null) {
                for (ActionWrapper[] actions : path) {
                    for (ActionWrapper action : actions) {
                        // this would not work for jointAction
                        currentNode.solution[agent].add(action);
                        // for debugging
                        // System.err.println(action);
                    }
                }
                return true;
            } else {
                // System.err
                // .println("WARNING: no path found for agent" + agent + "'s partial state.");
                return false;
            }

        } catch (OutOfMemoryError ex) {
            System.err.println("Maximum memory usage exceeded.");
            return false;
        }
    }

    private static HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>> copyConstraints(
            HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>> oldConstr) {

        return null; // todo

    }

    /*
     * 
     * Deep copy oldBoxes hashmap.
     * 
     * Args: oldBoxes: the old boxes hashmap Returns: newBoxes: the new boxes
     * hashmap
     * 
     */
    private static HashMap<Integer, ArrayList<Box>> BoxesClone(
            HashMap<Integer, ArrayList<Box>> oldBoxes, Agent[] newAgents) {

        HashMap<Integer, ArrayList<Box>> newBoxes = new HashMap<Integer, ArrayList<Box>>();

        for (int BoxType : oldBoxes.keySet()) {

            if (!newBoxes.containsKey(BoxType)) {
                newBoxes.put(BoxType, new ArrayList<Box>());
            }

            for (Box box : oldBoxes.get(BoxType)) {

                newBoxes.get(BoxType).add(new Box(box, newAgents));
            }

        }

        return newBoxes;

    }

    /*
     * Setter for new agents
     * 
     */
    private static void setNewAgents(Agent[] newAgents, Agent[] oldAgents,
            HashMap<Integer, ArrayList<Box>> newBoxes) {

        for (int agent = 0; agent < newAgents.length; agent++) {

            Agent agentNew = newAgents[agent];
            Agent agentOld = oldAgents[agent];
            // System.err.println(agentNew.isFat);
            agentNew.isFat = agentOld.isFat;
            agentNew.agentNumber = agentOld.agentNumber;
            agentNew.isFree = agentOld.isFree;

            Goal goal = agentOld.getCurrentGoal();

            // set goal with box with a new box instace
            if (goal.type == GoalType.HELP_WITH_BOX || goal.type == GoalType.MOVE_BOX
                    || goal.type == GoalType.REACH_BOX) {
                agentNew.setCurrentGoal(
                        new Goal(goal.type, newBoxes.get(goal.box.boxType).get(goal.box.boxIndex)));
            } else {
                agentNew.setCurrentGoal(new Goal(goal.type));
            }

        }
    }

    /*
     * Function which assigns agents to help out with a task. Function creates a new
     * node with the
     * newly assigned agents and goals
     * 
     * what it does - deletes all constraints so far, scrapes the whole tree, and
     * assigns agents to
     * help out with a task is this good? We somehow delete our progress of
     * constraints, but if we
     * dont do it we need to copy agents and boxes - that does not work though (bug)
     * -- and
     * eventually, we will call for help again
     * 
     * Args: node: the node to be expanded currentState: the current state
     * partialStates: the
     * partial states of the agents frontier: the frontier to use boxSimulation: the
     * box simulation
     * 
     * Returns: newnode: the new node with the newly assigned agents and goals
     * 
     * 
     */
    private static CTNode CallForHelp(CTNode node, State currentState, State[] partialStates,
            Frontier frontier,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {

        Agent[] newAgents = node.agents;
        HashMap<Integer, ArrayList<Box>> newBoxes = node.boxes;

        // get data about conflict
        int needHelpAgent = node.conflict.agent1;
        int boxToRemovePosition = node.conflict.node_id;
        Box boxToRemoveOld = node.conflict.box1;
        Box boxToRemove = newBoxes.get(boxToRemoveOld.getBoxType()).get(boxToRemoveOld.boxIndex); // new
                                                                                                  // box
                                                                                                  // object
        Color boxColor = State.boxColors[boxToRemove.boxType];

        // check if any of the agents can helpout
        boolean canHelp = false;
        for (int agent = 0; agent < newAgents.length; agent++) {
            if (State.agentColors[agent] == boxColor) {

                if (node.agents[agent].currentGoal.type != GoalType.HELP_WITH_BOX) { // dont include
                                                                                     // agents that
                                                                                     // are already
                                                                                     // helping out
                    canHelp = true;
                }
            }
        }

        // nobody can help out
        if (!canHelp) {
            node.solution[needHelpAgent].clear();
            node.agents[needHelpAgent].setCurrentGoal(new Goal(GoalType.NO_GOAL));
            call_LowLevelSearch(partialStates, needHelpAgent, frontier, node, boxSimulation, -1);
            node.agents[needHelpAgent].isFree = true;
            node.agents[needHelpAgent].isFat = false;
            return node; // this will scrape the whole tree again - lets not do that : TODO
        }

        CTNode newnode = new CTNode(node.cost, node.solution, node.constraints, node.conflict,
                newAgents, newBoxes, node.helpingColors, node.tempGoalLocation,
                node.additionalConstraints);

        newnode.helpingColors.add(boxColor);
        newnode.tempGoalLocation.add(boxToRemovePosition);

        // specify callee g_i => pickup box g_j
        ArrayList<Integer> newConstr = new ArrayList<Integer>();

        Goal calleeGoal = newAgents[needHelpAgent].currentGoal;

        int goalCalleeLocation = -1;
        if (calleeGoal.type == GoalType.REACH_AGENT_GOAL) {
            goalCalleeLocation = currentState.goals[needHelpAgent].get(0);
        } else if (calleeGoal.type == GoalType.MOVE_BOX) { // involves box
            goalCalleeLocation = calleeGoal.box.getGoalNode();

        } else if (calleeGoal.type == GoalType.REACH_BOX) {
            goalCalleeLocation = currentState.boxMap[calleeGoal.box.boxType].get(calleeGoal.box.boxIndex);
        }

        // no constraints for NO_GOAL
        if (calleeGoal.type != GoalType.NO_GOAL && calleeGoal.type != GoalType.HELP_WITH_BOX) {

            newConstr.add(goalCalleeLocation);
            newConstr.add(boxToRemovePosition);

            newnode.additionalConstraints.add(newConstr);

        }

        // make all agents from that team free - otherwise the IP cannot assign them to
        // help out
        for (int agent = 0; agent < newAgents.length; agent++) {
            if (State.agentColors[agent] == boxColor) {
                newAgents[agent].isFree = true;
            }
        }

        // call IP - reassign the tasks

        // System.err.println(newAgents[0].currentGoal.type);
        // System.err.println(newAgents[1].currentGoal.type);
        // System.err.println(newAgents[2].currentGoal.type);
        // System.err.println(newAgents[3].currentGoal.type);

        TaskManager.assignTasks(currentState, newAgents, newnode.tempGoalLocation,
                newnode.helpingColors, newnode.additionalConstraints, newBoxes);

        // System.err.println(newAgents[0].currentGoal.type);
        // System.err.println(newAgents[1].currentGoal.type);
        // System.err.println(newAgents[2].currentGoal.type);
        // System.err.println(newAgents[3].currentGoal.type);

        // redo the partial states
        partialStates = currentState.partialStates(newAgents, newBoxes);

        // apply already computed constraints
        State.constraints = newnode.constraints;
        State.resourceConstraints = newnode.resourceConstraints;
        // State.constraints = null;
        // State.resourceConstraints = null;

        boolean somebodyHelpedMeOut = false;

        // replan for new assignments
        for (Agent newAgent : newAgents) {

            int desiredTime = -1;

            int agentID = newAgent.agentNumber;

            newnode.solution[agentID].clear();

            // if new agent was assigned to reach the box - to be removed - make him
            // HELP_WITH_BOX:
            if (newAgent.currentGoal.type == GoalType.REACH_BOX
                    && newAgent.currentGoal.box.boxType == boxToRemove.boxType
                    && newAgent.currentGoal.box.boxIndex == boxToRemove.boxIndex) {

                newAgent.setCurrentGoal(new Goal(GoalType.HELP_WITH_BOX, boxToRemove));

                somebodyHelpedMeOut = true;

                newAgent.isFat = true;
                boxToRemove.isFat = true;

            }
            // replan
            if (!call_LowLevelSearch(partialStates, agentID, frontier, newnode, boxSimulation, desiredTime)) {

                throw new RuntimeException("This should always have a solution.");

            }
        }

        if (!somebodyHelpedMeOut) {

            newnode.solution[needHelpAgent].clear();
            newnode.agents[needHelpAgent].setCurrentGoal(new Goal(GoalType.NO_GOAL));
            call_LowLevelSearch(partialStates, needHelpAgent, frontier, newnode, boxSimulation, -1);
            newnode.agents[needHelpAgent].isFree = true;
            newnode.agents[needHelpAgent].isFat = false;
            // System.err.println("nobody:(");
            // System.exit(-1);

        }

        return newnode;

    }

}
