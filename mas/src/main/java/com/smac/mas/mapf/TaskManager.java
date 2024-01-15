package com.smac.mas.mapf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.smac.mas.mapf.Goal.GoalType;
import com.smac.mas.searchclient.Color;
import com.smac.mas.searchclient.CostStruct;
import com.smac.mas.searchclient.GraphUtils;
import com.smac.mas.searchclient.Node;
import com.smac.mas.searchclient.State;

import gurobi.*;

public class TaskManager {

    private static double M;
    private static GRBEnv env;

    public static int BDI_iter = 0; // just for debugging and hardcoding

    public TaskManager() {
        // Create empty environment, set options, and start
        try {
            env = new GRBEnv(true);
            env.set("logFile", "ilp_solver.log");
            env.set("LogToConsole", "0");
            env.start();
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
        M = 1000;
    }

    /*
     * Function that implements the assign tasks It iterates over the agents and if
     * an agent isFree
     * = True, then he is added to the agents that need assignment. All teams of all
     * such agents
     * will be assigned a goal which is set to the Agent.goal property. This
     * function is also
     * responsible for setting a fat agent
     *
     * @param agents: list of all agents
     * 
     * @param currentState: current state of the problem
     * 
     * @param unsatisfiedGoals: list of unsatisfied goals in goal nodes
     * 
     * @return -
     * 
     */
    public static void assignTasksHardCoded(Agent[] agents, ArrayList<Box>[] boxes,
            State currentState, Node[] unsatisfiedGoals) {

        // first iteration
        if (BDI_iter == 0) {

            // hardcoded plan
            for (Agent agent : agents) {

                agent.setIsFree(false);
                agent.setCurrentGoal(new Goal(GoalType.REACH_AGENT_GOAL));
                // agent.setCurrentGoal(new Goal(GoalType.NO_GOAL));

            }

            // hardcoded plan for MAPF02 level
            // agents[0].setCurrentGoal(new Goal(GoalType.REACH_AGENT_GOAL));
            // agents[1].setCurrentGoal(new Goal(GoalType.NO_GOAL));
            // agents[2].setCurrentGoal(new Goal(GoalType.NO_GOAL));

            // if not next to a goal - set reachbox
            // Box box = boxes[0].get(0);
            // agents[0].setCurrentGoal(new Goal(GoalType.REACH_BOX, box));

            // box.pickupGuy = agents[0];
            // box.setGoalNode(currentState.goals['A' - '0' +
            // box.boxType].get(box.boxIndex));

            // box = boxes[1].get(0);
            // agents[1].setCurrentGoal(new Goal(GoalType.REACH_BOX, box));

            // box.pickupGuy = agents[0];
            // box.setGoalNode(currentState.goals['A' - '0' +
            // box.boxType].get(box.boxIndex));
        }

        // second iteration
        if (BDI_iter == 1) {

            // rest of hardcoded plan for SAD1 level
            // if agent reached a box - set him to move box and fat
            if (agents[0].getCurrentGoal().getType() == GoalType.REACH_BOX) {

                System.err.println("MOVE BOX");

                Box box = agents[0].getCurrentGoal().box;
                agents[0].setCurrentGoal(new Goal(GoalType.MOVE_BOX, box));
                agents[0].setFat(true);

                box.isFat = true;
                box.pickupGuy = agents[0];
                box.setGoalNode(currentState.goals['A' - '0' + box.boxType].get(box.boxIndex));

                box = agents[1].getCurrentGoal().box;
                agents[1].setCurrentGoal(new Goal(GoalType.MOVE_BOX, box));
                agents[1].setFat(true);

                box.isFat = true;
                box.pickupGuy = agents[1];
                box.setGoalNode(currentState.goals['A' - '0' + box.boxType].get(box.boxIndex));

            }
        }

        // second iteration
        if (BDI_iter == 2) {

            Box box = boxes[1].get(0);
            agents[0].setCurrentGoal(new Goal(GoalType.REACH_BOX, box));

            box.pickupGuy = agents[0];
            box.setGoalNode(currentState.goals['A' - '0' + box.boxType].get(box.boxIndex));

        }

        if (BDI_iter == 3) {

            if (agents[0].getCurrentGoal().getType() == GoalType.REACH_BOX) {

                System.err.println("MOVE BOX");

                Box box = agents[0].getCurrentGoal().box;
                agents[0].setCurrentGoal(new Goal(GoalType.MOVE_BOX, box));
                agents[0].setFat(true);

                box.isFat = true;
                box.pickupGuy = agents[0];
                box.setGoalNode(currentState.goals['A' - '0' + box.boxType].get(box.boxIndex));
            }

        }

        if (BDI_iter == 4) {

            Box box = boxes[2].get(0);
            agents[0].setCurrentGoal(new Goal(GoalType.REACH_BOX, box));

            box.pickupGuy = agents[0];
            box.setGoalNode(currentState.goals['A' - '0' + box.boxType].get(box.boxIndex));

        }
        if (BDI_iter == 5) {

            if (agents[0].getCurrentGoal().getType() == GoalType.REACH_BOX) {

                System.err.println("MOVE BOX");

                Box box = agents[0].getCurrentGoal().box;
                agents[0].setCurrentGoal(new Goal(GoalType.MOVE_BOX, box));
                agents[0].setFat(true);

                box.isFat = true;
                box.pickupGuy = agents[0];
                box.setGoalNode(currentState.goals['A' - '0' + box.boxType].get(box.boxIndex));
            }

        }

        BDI_iter++;

    }

    public static void assignTasks(Agent[] agents) {
        // hardcoded plan
        agents[0].setIsFree(false);
        agents[1].setIsFree(false);
        agents[0].setCurrentGoal(new Goal(GoalType.REACH_AGENT_GOAL));
        agents[1].setCurrentGoal(new Goal(GoalType.NO_GOAL));
    }

    private static Color[] getDistinctColors(ArrayList<Agent> freeAgents) {
        HashMap<Color, Boolean> agentColors = new HashMap<>();
        for (Agent a : freeAgents) {
            agentColors.put(State.agentColors[a.getAgentNumber()], true);
        }
        Color[] colors = new Color[agentColors.size()];
        int i = 0;
        for (Color c : agentColors.keySet()) {
            colors[i] = c;
            i++;
        }
        return colors;
    }

    private static boolean isBoxSatisfied(State state, int location, char boxType) {
        return state.node2goal.containsKey(location) && state.node2goal.get(location) == boxType;
    }

    private static boolean isGoalSatisfied(State state, int location, char goalType) {
        return state.node2box.containsKey(location) && state.node2box.get(location) == goalType;
    }

    private static <T> boolean contains(ArrayList<T> arr, T element) {
        for (T a : arr) {
            if (a.equals(element))
                return true;
        }
        return false;
    }

    private static <T> boolean contains(T[] arr, T element) {
        for (T a : arr) {
            if (a.equals(element))
                return true;
        }
        return false;
    }

    private static boolean isNeighbor(int x, int y) {
        return contains(GraphUtils.node_list[x].neighbor, y);
    }

    private static int getBoxNumber(State state, int box_position, char box_type) {
        int c = box_type - 'A';
        for (int i = 0; i < state.boxMap[c].size(); i++) {
            if (state.boxMap[c].get(i) == box_position)
                return i;
        }
        return -1;
    }

    // TODO: Penalize goals that are to be placed on bottlenecks, Add goal
    // importance number
    private static void ILPAssign(CostStruct cs, ArrayList<ArrayList<Integer>>[] constraints,
            Color[] colors, ArrayList<Agent> freeAgents, int[] label2index, int[] agentPosition,
            ArrayList<ArrayList<Integer>> additionalConstraints, HashMap<Integer, ArrayList<Box>> boxes, boolean debug)
            throws GRBException {

        GRBModel model = new GRBModel(env);
        ArrayList<GRBVar>[] agentXbox = new ArrayList[cs.agentXbox.length];
        ArrayList<GRBVar>[] boxXagent = new ArrayList[cs.boxXagent.length];
        ArrayList<GRBVar>[] boxXgoal = new ArrayList[cs.boxXgoal.length];
        ArrayList<GRBVar>[] goalXentity = new ArrayList[cs.goalXentity.length];
        ArrayList<GRBVar>[] agentXgoal = new ArrayList[cs.agentXgoal.length];

        for (int i = 0; i < cs.agentXbox.length; i++) {
            agentXbox[i] = new ArrayList<GRBVar>(cs.agentXbox[i].size());
            for (int j = 0; j < cs.agentXbox[i].size(); j++) {
                String varname = "b_{" + i + "}{" + cs.agentXbox[i].get(j) + "}";
                agentXbox[i].add(
                        model.addVar(0.0, 1.0, cs.agentXboxCost[i].get(j), GRB.BINARY, varname));
                if (boxXagent[cs.agentXbox[i].get(j)] == null)
                    boxXagent[cs.agentXbox[i].get(j)] = new ArrayList<GRBVar>();
                boxXagent[cs.agentXbox[i].get(j)].add(agentXbox[i].get(j));
            }
        }

        for (int i = 0; i < cs.goalXentity.length; i++) {
            if (cs.goalType.get(i) > '9') {
                goalXentity[i] = new ArrayList<GRBVar>(cs.goalXentity[i].size());
                for (int j = 0; j < cs.goalXentity[i].size(); j++) {
                    int destination = cs.goalXentity[i].get(j);
                    String varname = "gb_{" + destination + "}{" + i + "}";
                    goalXentity[i].add(model.addVar(0.0, 1.0, cs.goalXentityCost[i].get(j),
                            GRB.BINARY, varname));
                    if (boxXgoal[cs.goalXentity[i].get(j)] == null)
                        boxXgoal[cs.goalXentity[i].get(j)] = new ArrayList<GRBVar>();
                    boxXgoal[cs.goalXentity[i].get(j)].add(goalXentity[i].get(j));
                }
            } else {
                goalXentity[i] = new ArrayList<GRBVar>(cs.goalXentity[i].size());
                for (int j = 0; j < cs.goalXentity[i].size(); j++) {
                    String varname = "a_{" + cs.goalXentity[i].get(j) + "}{" + i + "}";
                    goalXentity[i].add(model.addVar(0.0, 1.0, cs.goalXentityCost[i].get(j),
                            GRB.BINARY, varname));
                    if (agentXgoal[cs.goalXentity[i].get(j)] == null)
                        agentXgoal[cs.goalXentity[i].get(j)] = new ArrayList<GRBVar>();
                    agentXgoal[cs.goalXentity[i].get(j)].add(goalXentity[i].get(j));
                }
            }
        }

        /*
         * set condition that each box can be allocated to atmost one agent and one goal
         * and goal is
         * allocated to a box iff agent is allocated to a box
         */
        for (int i = 0; i < cs.boxXagent.length; i++) { // what if there are no boxes?
            GRBLinExpr expr1 = new GRBLinExpr();
            GRBLinExpr expr2 = new GRBLinExpr();
            for (int j = 0; j < cs.boxXagent[i].size(); j++) {
                expr1.addTerm(1.0, boxXagent[i].get(j));
                expr2.addTerm(1.0, boxXagent[i].get(j));
            }
            for (int j = 0; j < cs.boxXgoal[i].size(); j++) {
                expr1.addTerm(1.0, boxXgoal[i].get(j));
                expr2.addTerm(-1.0, boxXgoal[i].get(j));
            }
            model.addConstr(expr1, GRB.LESS_EQUAL, 2.0, "a" + i);
            model.addConstr(expr2, GRB.EQUAL, 0.0, "b" + i);
        }

        int count = 0;
        HashMap<Integer, Integer> inverseDescMap = new HashMap<>();
        for (int d : cs.goalDescription) {
            inverseDescMap.put(d, count);
            count++;
        }

        /*
         * Add soft linearization constraints
         */
        count = 0;
        for (int i = 0; i < constraints.length; i++) {
            for (int j = 0; j < constraints[i].size(); j++) {
                ArrayList<Integer> current = constraints[i].get(j);
                int n = current.size();
                int last = current.get(n - 1);
                int k = 0;
                for (; k < n - 1; k++) {
                    if (!inverseDescMap.containsKey(current.get(k))) {
                        break;
                    }
                }
                if (k == n - 1) {
                    GRBLinExpr expr = new GRBLinExpr();
                    for (k = 0; k < n - 1; k++) {
                        int goalDes = inverseDescMap.get(current.get(k));
                        for (int l = 0; l < cs.goalXentity[goalDes].size(); l++) {
                            expr.addTerm(1.0, goalXentity[goalDes].get(l));
                        }
                    }
                    if (inverseDescMap.containsKey(last)) {
                        int goalDes = inverseDescMap.get(last);
                        for (int l = 0; l < cs.goalXentity[goalDes].size(); l++) {
                            expr.addTerm(-1.0, goalXentity[goalDes].get(l));
                        }
                    }
                    GRBVar softVar = model.addVar(0, 1, TaskManager.M, GRB.BINARY, "d" + i + "_" + j);
                    expr.addTerm(-1.0, softVar);
                    model.addConstr(expr, GRB.LESS_EQUAL, n - 2, "d" + count);
                    count++;
                }
            }
        }
        if (additionalConstraints != null) {
            for (int i = 0; i < additionalConstraints.size(); i++) {
                ArrayList<Integer> current = additionalConstraints.get(i);
                int n = current.size();
                int last = current.get(n - 1);
                int k = 0;

                for (; k < n - 1; k++) {
                    if (!inverseDescMap.containsKey(current.get(k))) {
                        break;
                    }
                }
                if (k == n - 1) {
                    GRBLinExpr expr = new GRBLinExpr();
                    for (k = 0; k < n - 1; k++) {
                        int goalDes = inverseDescMap.get(current.get(k));
                        for (int l = 0; l < cs.goalXentity[goalDes].size(); l++) {
                            expr.addTerm(1.0, goalXentity[goalDes].get(l));
                        }
                    }
                    if (inverseDescMap.containsKey(last)) {
                        int goalDes = inverseDescMap.get(last);
                        for (int l = 0; l < cs.goalXentity[goalDes].size(); l++) {
                            expr.addTerm(-1.0, goalXentity[goalDes].get(l));
                        }
                    }
                    GRBVar softVar = model.addVar(0, 1, TaskManager.M, GRB.BINARY, "s" + i);
                    expr.addTerm(-1.0, softVar);
                    model.addConstr(expr, GRB.LESS_EQUAL, n - 2, "d" + count);
                    count++;
                }
            }
        }

        for (Color color : colors) {
            int c = Color.color2Int(color);
            int start = cs.agentColorMap[c][0];
            int end = cs.agentColorMap[c][1];
            int _start = cs.goalColorMap[c][0];
            int _end = cs.goalColorMap[c][1];
            char condA;
            char condG;
            if (end - start > _end - _start) {
                condG = GRB.EQUAL;
                condA = GRB.LESS_EQUAL;
            } else {
                condG = GRB.LESS_EQUAL;
                condA = GRB.EQUAL;
            }
            for (int i = start; i < end; i++) {
                GRBLinExpr expr = new GRBLinExpr();
                for (int j = 0; j < cs.agentXbox[i].size(); j++) {
                    expr.addTerm(1.0, agentXbox[i].get(j));
                }
                if (agentXgoal[i] != null) {
                    for (int j = 0; j < agentXgoal[i].size(); j++) {
                        expr.addTerm(1.0, agentXgoal[i].get(j));
                    }
                }
                model.addConstr(expr, condA, 1.0, "f" + i);
            }
            for (int i = _start; i < _end; i++) {
                GRBLinExpr expr = new GRBLinExpr();
                for (int j = 0; j < cs.goalXentity[i].size(); j++) {
                    expr.addTerm(1.0, goalXentity[i].get(j));
                }
                model.addConstr(expr, condG, 1.0, "c" + i);
            }
        }

        model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
        model.optimize();
        Box[] goalBoxMap = new Box[cs.boxDescription.size()];

        for (int i = 0; i < agentXbox.length; i++) {
            for (int j = 0; j < agentXbox[i].size(); j++) {
                if (agentXbox[i].get(j).get(GRB.DoubleAttr.X) == 1.0) {
                    int label = cs.agentDescription.get(i);
                    int index = label2index[label];
                    int box_position = cs.boxDescription.get(cs.agentXbox[i].get(j));
                    int agent_position = agentPosition[index];
                    int box_index = cs.agentXbox[i].get(j);
                    freeAgents.get(index).setIsFree(false);
                    char box_type = cs.s.node2box.get(box_position);
                    int boxNumber = getBoxNumber(cs.s, box_position, box_type);
                    Box b = boxes.get(box_type - 'A').get(boxNumber);
                    b.setPickupGuy(freeAgents.get(index));
                    // TODO: should we not set b.fat = true anytime we assign a pickupGuy?
                    goalBoxMap[box_index] = b;

                    if (isNeighbor(agent_position, box_position)) {
                        b.isFat = true;
                        freeAgents.get(index).setFat(true);
                        Goal g = new Goal(GoalType.MOVE_BOX, b);
                        freeAgents.get(index).setCurrentGoal(g);
                        // }
                    } else {
                        b.isFat = false;
                        freeAgents.get(index).setFat(false);
                        Goal g = new Goal(GoalType.REACH_BOX, b);
                        freeAgents.get(index).setCurrentGoal(g);
                    }
                    break;
                }
            }
        }

        for (int i = 0; i < goalXentity.length; i++) {
            for (int j = 0; j < goalXentity[i].size(); j++) {
                if (goalXentity[i].get(j).get(GRB.DoubleAttr.X) == 1.0) {
                    int goalPos = cs.goalDescription.get(i);
                    if (cs.goalType.get(i) > '9') {
                        goalBoxMap[cs.goalXentity[i].get(j)].setGoalNode(goalPos);
                    } else {
                        int label = cs.agentDescription.get(cs.goalXentity[i].get(j));
                        int index = label2index[label];
                        freeAgents.get(index).setFat(false);
                        freeAgents.get(index).setIsFree(false);
                        Goal g;
                        if (cs.goalType.get(i) == '-') {
                            assert (cs.s.node2box.containsKey(goalPos));
                            char box_type = cs.s.node2box.get(goalPos);
                            int boxNumber = getBoxNumber(cs.s, goalPos, box_type);
                            Box b = boxes.get(box_type - 'A').get(boxNumber);
                            g = new Goal(GoalType.REACH_BOX, b);
                            b.setPickupGuy(freeAgents.get(index));
                            int agent_position = agentPosition[index];
                            if (isNeighbor(agent_position, goalPos)) {
                                b.isFat = true;
                                freeAgents.get(index).setFat(true);
                            } else {
                                b.isFat = false;
                                freeAgents.get(index).setFat(false);
                            }
                        } else {
                            if (agentPosition[index] == goalPos) {
                                g = new Goal(GoalType.NO_GOAL);
                            } else {
                                g = new Goal(GoalType.REACH_AGENT_GOAL);
                            }
                        }
                        freeAgents.get(index).setCurrentGoal(g);
                    }
                    break;
                }
            }
        }

        if (debug) {

            for (int i = 0; i < agentXbox.length; i++) {
                for (int j = 0; j < agentXbox[i].size(); j++) {
                    Node x = GraphUtils.node_list[cs.boxDescription.get(cs.agentXbox[i].get(j))];
                    System.err.println(agentXbox[i].get(j).get(GRB.StringAttr.VarName) + " "
                            + agentXbox[i].get(j).get(GRB.DoubleAttr.X) + " " + x.row + ","
                            + x.col);
                }
            }

            for (int i = 0; i < agentXgoal.length; i++) {
                if (agentXgoal[i] == null)
                    continue;
                for (int j = 0; j < agentXgoal[i].size(); j++) {
                    Node x = GraphUtils.node_list[cs.goalDescription.get(cs.agentXgoal[i].get(j))];
                    System.err.println(agentXgoal[i].get(j).get(GRB.StringAttr.VarName) + " "
                            + agentXgoal[i].get(j).get(GRB.DoubleAttr.X) + " " + x.row + ","
                            + x.col);
                }
            }

            for (int i = 0; i < boxXgoal.length; i++) {
                for (int j = 0; j < boxXgoal[i].size(); j++) {
                    Node x = GraphUtils.node_list[cs.goalDescription.get(cs.boxXgoal[i].get(j))];
                    System.err.println(boxXgoal[i].get(j).get(GRB.StringAttr.VarName) + " "
                            + boxXgoal[i].get(j).get(GRB.DoubleAttr.X) + " " + x.row + "," + x.col);
                }
            }
        }

        model.dispose();
    }

    /*
     * Takes as input current state, list of all agents, tempGoalLocation :=
     * Node_id's where pickup
     * help is needed(a box must be present to be picked) tempGoalColor := The
     * corresponding team
     * color which can provide the help additionalConstraints := a list where its
     * element is of the
     * form [g1, g2, .. , gn] s.t constraint g1 & g2 ... & gn-1 => gn is enforced
     * and each gi is
     * either a node id corresponding to a state goal(box or agent) or is in
     * tempGoalLocation.
     */
    public static void assignTasks(State state, Agent[] agents, ArrayList<Integer> tempGoalLocation,
            ArrayList<Color> tempGoalColor, ArrayList<ArrayList<Integer>> additionalConstraints,
            HashMap<Integer, ArrayList<Box>> boxes) {

        ArrayList<Agent> freeAgents = new ArrayList<>();
        for (int i = 0; i < agents.length; i++) {
            Agent agent = agents[i];
            if (agent.isFree()) {
                freeAgents.add(agent);
            }
        }

        int[] tempAgentPosition = new int[freeAgents.size()];
        int[] tempLabel2Index = new int[State.maxNumAgents];
        int numColors = Color.values().length;
        ArrayList<Character>[] tempColor2Box = new ArrayList[numColors];
        Arrays.fill(tempLabel2Index, -1);
        for (int i = 0; i < numColors; i++) {
            tempColor2Box[i] = new ArrayList<>();
        }

        Color[] colors = getDistinctColors(freeAgents);
        for (int i = 0; i < freeAgents.size(); i++) {
            tempAgentPosition[i] = state.agentPosition[state.label2index[freeAgents.get(i).getAgentNumber()]];
            tempLabel2Index[freeAgents.get(i).getAgentNumber()] = i;
        }

        ArrayList<Integer>[] tempBoxMap = new ArrayList[State.CHARSET_ARRAY_SIZE];
        ArrayList<Integer>[] tempGoalMap = new ArrayList[State.GOAL_ARRAY_SIZE];
        for (int i = 0; i < tempBoxMap.length; i++) {
            tempBoxMap[i] = new ArrayList<>();
            if (contains(colors, State.boxColors[i])) {
                for (int j = 0; j < state.boxMap[i].size(); j++) {
                    int boxLoc = state.boxMap[i].get(j);
                    if (!isBoxSatisfied(state, boxLoc, (char) (i + 'A'))) {
                        tempBoxMap[i].add(boxLoc);
                    }
                }
            }
            if (tempBoxMap[i].size() > 0) {
                tempColor2Box[Color.color2Int(State.boxColors[i])].add((char) (i + 'A'));
            }
        }

        for (int i = 0; i < tempGoalMap.length; i++) {
            tempGoalMap[i] = new ArrayList<>();
            if (i + '0' > '9') {
                if (i - 'A' + '0' < 0)
                    continue;
                if (contains(colors, State.boxColors[i - 'A' + '0'])) {
                    for (int j = 0; j < state.goals[i].size(); j++) {
                        int goalLoc = state.goals[i].get(j);
                        if (!isGoalSatisfied(state, goalLoc, (char) (i + '0'))) {
                            tempGoalMap[i].add(goalLoc);
                        }
                    }
                }
            } else {
                if (contains(colors, State.agentColors[i])) {
                    if (state.goals[i].size() > 0)
                        tempGoalMap[i].add(state.goals[i].get(0));
                }
            }
        }

        HashMap<Color, ArrayList<Integer>> color2Location = new HashMap<>();
        if (tempGoalColor != null) {
            for (int i = 0; i < tempGoalColor.size(); i++) {
                if (!color2Location.containsKey(tempGoalColor.get(i))) {
                    color2Location.put(tempGoalColor.get(i), new ArrayList<>());
                }
                color2Location.get(tempGoalColor.get(i)).add(tempGoalLocation.get(i));
            }
        }

        CostStruct costStruct = new CostStruct(state);

        GraphUtils.get_distance_matrix(costStruct, colors, tempAgentPosition, tempLabel2Index,
                tempColor2Box, tempBoxMap, tempGoalMap, color2Location);

        ArrayList<ArrayList<Integer>>[] constraints = GraphUtils.get_disjoint_goal_ordering_constraints(state, true);
        try {
            ILPAssign(costStruct, constraints, colors, freeAgents, tempLabel2Index,
                    tempAgentPosition, additionalConstraints, boxes, false);
            // set any agent's that were not assigned a goal, NO_GOAL and not fat
            for (int i = 0; i < agents.length; i++) {
                Agent agent = agents[i];
                if (agent.isFree()) {
                    // set default goal for free agent as NO_GOAL
                    agent.setCurrentGoal(new Goal(GoalType.NO_GOAL));
                    agent.setFat(false);
                }
            }
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }

        // TODO: handle edge case where there are no goals for a team
    }

}
