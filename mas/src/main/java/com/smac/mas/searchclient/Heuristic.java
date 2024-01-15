package com.smac.mas.searchclient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.smac.mas.mapf.Agent;
import com.smac.mas.mapf.SimulationAction;
import com.smac.mas.mapf.Goal.GoalType;

public abstract class Heuristic implements Comparator<State> {
    public static HashMap<Character, List<Integer[]>> goalsToPos;
    public static final int constant = 5;
    public static final int scale = 1;
    public static int M = 0;
    public static int ww = 0;
    public String heuristicMethod;
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation;

    boolean crateScenario;

    public Heuristic(State initialState) {
        // add empty heuristic when no heuristic is specified
        this(initialState, "");
    }

    public Heuristic(State initialState, String heuristicMethod) {

        heuristicMethod = heuristicMethod;

    }

    public Heuristic(State initialState, String heuristicMethod,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation) {

    }

    // create public hashmap
    // public Heuristic(State initialState, String heuristicMethod) {
    // this.heuristicMethod = heuristicMethod;
    // ArrayList<String> validHeuristics = new ArrayList<String>();
    // validHeuristics.add("manhattan");
    // validHeuristics.add("goalcount");
    // validHeuristics.add("mean");

    // if (!validHeuristics.contains(heuristicMethod)) {
    // System.err.format("invalid heuristic method: %s, valid options %s; exiting
    // 1\n",
    // heuristicMethod, validHeuristics);
    // System.exit(1);
    // } else {
    // System.err.format("heuristic method: %s\n", heuristicMethod);
    // }
    // this.goalsToPos = new HashMap<>();

    // int M = initialState.boxes.length;
    // int count = 0;
    // for (int i = 0; i < M; i++) {
    // count = Math.max(count, initialState.boxes[i].length);
    // }
    // this.M = M + count;
    // // Here's a chance to pre-process the static parts of the level.

    // // create map from intialState.goals char to tuple of row,column of goal
    // for (int row = 1; row < initialState.goals.length - 1; row++) {
    // for (int col = 1; col < initialState.goals[row].length - 1; col++) {
    // char goal = initialState.goals[row][col];
    // // add to map if box or agent goal exists
    // if (('A' <= goal && goal <= 'Z') || ('0' <= goal && goal <= '9')) {
    // // if goal already exists in map, add to list of positions
    // // else create new list of positions
    // if (goalsToPos.containsKey(goal)) {
    // goalsToPos.get(goal).add(new Integer[] {row, col});
    // } else {
    // List<Integer[]> positions = new ArrayList<Integer[]>();
    // positions.add(new Integer[] {row, col});
    // goalsToPos.put(goal, positions);
    // }
    // }

    // if ('A' <= goal && goal <= 'Z') {
    // crateScenario = true;
    // }
    // if ('0' <= goal && goal <= '9') {
    // crateScenario = false;
    // }

    // }
    // }

    // }

    // // function that computes manhattan distance
    // private static int manhattanDistance(int x1, int x2, int y1, int y2) {

    // return Math.abs((x2 - x1)) + Math.abs((y2 - y1));

    // }

    // public int manhattanHeuristic(State s) {
    // float sumDistances = 0;
    // List<Integer[]> positions;
    // int rowGoal, colGoal;
    // int rowCurrent, colCurrent;

    // float num_agents = 0;

    // if (!this.crateScenario) {

    // for (char goal : this.goalsToPos.keySet()) {

    // positions = this.goalsToPos.get(goal);

    // // increase number of agents
    // if ('0' <= goal && goal <= '9' && s.agentRows.containsKey(goal - '0') &&
    // s.agentCols.containsKey(goal - '0')) {
    // ++num_agents;
    // }

    // for (Integer[] pos : positions) {
    // rowGoal = pos[0];
    // colGoal = pos[1];

    // // get corresponding agent
    // int agent_index = (int) goal - '0';

    // // compute M. distance to for 1 agent
    // if ('0' <= goal && goal <= '9' && s.agentRows.containsKey(goal - '0') &&
    // s.agentCols.containsKey(goal - '0')) {
    // sumDistances += manhattanDistance(s.agentRows.get(agent_index), rowGoal,
    // s.agentCols.get(agent_index), colGoal);
    // }
    // }
    // }
    // }

    // if (this.crateScenario) {
    // int agentRow = s.agentRows.get(0);
    // int agentCol = s.agentCols.get(0);
    // int distToBox = 0;

    // for (char goal : this.goalsToPos.keySet()) {
    // positions = this.goalsToPos.get(goal);
    // for (Integer[] pos : positions) {
    // rowGoal = pos[0];
    // colGoal = pos[1];

    // float minValue = s.goals.length * s.goals[0].length * 2;
    // int bestRow = 0;
    // int bestsCol = 0;

    // if (s.boxPositions.containsKey(goal)) {
    // int[][] arr = s.boxPositions.get(goal);
    // for (int crate = 0; crate < arr.length; ++crate) {
    // rowCurrent = arr[crate][0];
    // colCurrent = arr[crate][1];

    // float newMin = Math.min(minValue,
    // manhattanDistance(rowCurrent, rowGoal, colCurrent, colGoal));

    // if (newMin < minValue) {
    // minValue = newMin;
    // bestRow = rowCurrent;
    // bestsCol = colCurrent;

    // }

    // }
    // }
    // distToBox = manhattanDistance(agentRow, bestRow, agentCol, bestsCol);
    // sumDistances += minValue + distToBox;
    // }
    // }
    // }

    // // normalize the distance
    // if (!this.crateScenario) {
    // sumDistances = sumDistances / num_agents;
    // }

    // return (int) Math.ceil(sumDistances);
    // }

    // public int goalCountHeuristic(State s) {
    // int unmet_goal = 0;
    // List<Integer[]> positions;
    // int row, col;
    // for (char goal : this.goalsToPos.keySet()) {
    // positions = this.goalsToPos.get(goal);
    // for (Integer[] pos : positions) {
    // row = pos[0];
    // col = pos[1];
    // if ('A' <= goal && goal <= 'Z' && s.boxes[row][col] != goal) {
    // unmet_goal++;
    // }
    // // If the value of 'goal' is part of integers from 0 to 9, and if there is no
    // // corresponding agent with the same letter at goal's postion, then return
    // // false.
    // if ('0' <= goal && goal <= '9'
    // && !(s.agentRows.get(goal - '0') == row && s.agentCols.get(goal - '0') ==
    // col)) {
    // unmet_goal++;
    // }
    // }
    // }
    // return unmet_goal;
    // }

    // public static float getDistanceCost(List<int[]> coords, int x, int y, int
    // offset,
    // boolean gmean) {
    // int n = coords.size();
    // float result = 0;

    // for (int i = 0; i < n; i++) {
    // float target = (manhattanDistance(coords.get(i)[0], x, coords.get(i)[1], y) -
    // offset);
    // if (target < 1) {
    // if (gmean) {
    // return 0;
    // } else {
    // return -1;
    // }
    // }

    // result += Math.log(target);
    // }

    // if (gmean) {
    // return (int) Math.exp(result / ((float) n));
    // } else {
    // return result;
    // }
    // }

    // public static int compute_unmet_crate_positions(State s,
    // HashMap<Integer, List<int[]>> crate_position,
    // HashMap<Integer, List<int[]>> unmet_crate_goals) {

    // int unmet_goals = 0;
    // for (int i = 0; i < s.boxes.length; i++) {
    // for (int j = 0; j < s.boxes[i].length; j++) {
    // char goal = s.goals[i][j];
    // char box = s.boxes[i][j];

    // if ('A' <= box && box <= 'Z' && box != goal) {
    // int index = (int) (box - 'A');
    // if (!crate_position.containsKey(index)) {
    // crate_position.put(index, new ArrayList<int[]>());
    // }

    // var val = crate_position.get(index);
    // val.add(new int[] {i, j});
    // }

    // if ('A' <= goal && goal <= 'Z' && box != goal) {
    // int index = (int) (goal - 'A');
    // if (!unmet_crate_goals.containsKey(index)) {
    // unmet_crate_goals.put(index, new ArrayList<int[]>());
    // }

    // var val = unmet_crate_goals.get(index);
    // val.add(new int[] {i, j});
    // unmet_goals++;
    // }
    // }
    // }

    // return unmet_goals;
    // }

    // /*
    // * This heuristic should only works well in single agent case h = for each
    // crate calculate gm
    // * distance with corresponding goal positions + for each agent calculate
    // calculate the gm
    // * distance with unmet crates,\ + constant if unmet crates > 0 else constant -
    // constant / sqrt(1
    // * + mean distance with final agent position if exist)
    // */

    // public static int meanHeuristic(State s) {
    // int ret = constant * 0;
    // HashMap<Integer, List<int[]>> unmet_crate_position = new HashMap<Integer,
    // List<int[]>>();
    // HashMap<Integer, List<int[]>> unmet_crate_goals = new HashMap<Integer,
    // List<int[]>>();
    // int total_unmet_goals =
    // compute_unmet_crate_positions(s, unmet_crate_position, unmet_crate_goals);
    // int num_agents = s.agentRows.size();
    // float[] agent_log_cost = new float[num_agents];
    // int[] count = new int[num_agents];

    // for (int i = 0; i < num_agents; i++) {
    // agent_log_cost[i] = 0;
    // count[i] = 0;
    // }

    // for (int c : unmet_crate_position.keySet()) {
    // if (unmet_crate_goals.containsKey(c)) {
    // List<int[]> position = unmet_crate_position.get(c);
    // List<int[]> coords = unmet_crate_goals.get(c);

    // for (int i = 0; i < position.size(); i++) {
    // ret += getDistanceCost(coords, position.get(i)[0], position.get(i)[1], 0,
    // true);
    // }

    // for (int i = 0; i < num_agents; i++) {
    // if (s.agentColors[i].equals(s.boxColors[c]) && agent_log_cost[i] >= 0) {
    // var val =
    // getDistanceCost(position, s.agentRows.get(i), s.agentCols.get(i), 1, false);
    // if (((int) val) == -1) {
    // agent_log_cost[i] = -1;
    // } else {
    // agent_log_cost[i] += val;
    // }
    // count[i] += coords.size();
    // }
    // }
    // }
    // }

    // for (int i = 0; i < num_agents; i++) {
    // if (agent_log_cost[i] > 0) {
    // ret += (int) Math.exp(agent_log_cost[i] / ((float) count[i]));
    // }
    // }

    // ret += (M * total_unmet_goals);

    // if (total_unmet_goals == 0) {
    // int sum_distances = 0;

    // for (char goal : goalsToPos.keySet()) {
    // var positions = goalsToPos.get(goal);
    // for (Integer[] pos : positions) {
    // int row = pos[0];
    // int col = pos[1];

    // if ('0' <= goal && goal <= '9') {
    // int agent_index = (int) goal - '0';
    // sum_distances += manhattanDistance(s.agentRows.get(agent_index), row,
    // s.agentCols.get(agent_index), col);
    // }
    // }
    // }

    // float mean_distance = ((float) sum_distances) / num_agents;
    // ret -= (int) (((float) constant) / Math.sqrt(1 + mean_distance));
    // }

    // return scale * ret;
    // }

    public int h(State s) { // just 1 heuristic for everything - s is considered to be a partial state

        int h_val = 0;

        int agentPosition = s.agentPosition[0];

        Agent agent = State.agentInPartialState; // set in the GraphSearch class

        if (agent.currentGoal.type == GoalType.REACH_AGENT_GOAL) {

            // agent to goal dist
            int goalNode = s.goals[agent.agentNumber].get(0);
            h_val = GraphUtils.min_distance[agentPosition][goalNode];

        } else if (agent.currentGoal.type == GoalType.REACH_BOX || agent.currentGoal.type == GoalType.HELP_WITH_BOX) {

            // agent to box dist
            int boxType = agent.currentGoal.box.boxType;
            int boxIndex = agent.currentGoal.box.boxIndex;

            int boxPosition = s.boxMap[boxType].get(boxIndex);

            h_val = GraphUtils.min_distance[agentPosition][boxPosition];

        } else if (agent.currentGoal.type == GoalType.MOVE_BOX) {

            int boxType = agent.currentGoal.box.boxType;
            int boxIndex = agent.currentGoal.box.boxIndex;

            // compute dist to box - for the case of separation
            int boxPosition = s.boxMap[boxType].get(boxIndex);
            int distToBox = GraphUtils.min_distance[agentPosition][boxPosition];

            // compute dist from box to its destination
            int boxGoal = agent.currentGoal.box.getGoalNode();
            int boxToGoalDistance = GraphUtils.min_distance[boxPosition][boxGoal];

            h_val = distToBox + boxToGoalDistance;

        } else if (agent.currentGoal.type == GoalType.NO_GOAL) {

            h_val = -2 * s.g(); // to get heuristically better in each timestep
        }

        // add cost for move with a box unless it is a move box
        if (s.wasGeneratedUsingMovingBox &&
                (agent.currentGoal.type == GoalType.REACH_AGENT_GOAL ||
                        agent.currentGoal.type == GoalType.REACH_BOX ||
                        // agent.currentGoal.type == GoalType.HELP_WITH_BOX ||
                        agent.currentGoal.type == GoalType.NO_GOAL)) {

            h_val += 1;

        }

        return h_val;
    }

    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2) {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicAStarGreedy extends Heuristic {
    public HeuristicAStarGreedy(State initialState, String heuristicMethod) {
        super(initialState, heuristicMethod);
    }

    @Override
    public int f(State s) {
        return this.h(s);
    }

    @Override
    public String toString() {
        return "A* greedy evaluation";
    }
}

class HeuristicAStar extends Heuristic {
    public HeuristicAStar(State initialState, String heuristicMethod) {
        super(initialState, heuristicMethod);
    }

    @Override
    public int f(State s) {
        return s.g() + this.h(s);
    }

    @Override
    public String toString() {
        return "A* pairwise min evaluation";
    }
}

class HeuristicWeightedAStar extends Heuristic {
    private int w;

    public HeuristicWeightedAStar(State initialState, String heuristicMethod, int w) {
        super(initialState, heuristicMethod);
        this.w = w;
    }

    @Override
    public int f(State s) {
        return s.g() + this.w * this.h(s);
    }

    @Override
    public String toString() {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy extends Heuristic {
    public HeuristicGreedy(State initialState, String heuristicMethod) {
        super(initialState, heuristicMethod);
    }

    @Override
    public int f(State s) {
        return this.h(s);
    }

    @Override
    public String toString() {
        return "greedy evaluation";
    }
}
