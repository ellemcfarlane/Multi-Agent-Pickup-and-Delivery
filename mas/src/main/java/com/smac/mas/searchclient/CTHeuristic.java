// package com.smac.mas.searchclient;

// import mapf.Agent;
// import java.util.*;

// public class CTHeuristic {

// class PositionTuple {
// public final int agent;
// public final int box;

// public PositionTuple(int x, int y) {
// this.agent = x;
// this.box = y;
// }

// @Override
// public boolean equals(Object obj) {
// PositionTuple other = (PositionTuple) obj;
// return this.agent == other.agent && this.box == other.box;
// }

// @Override
// public int hashCode() {
// return Objects.hash(this.agent, this.box);
// }
// }

// // TODO : Add NO-OP action
// PositionTuple[] expandState(PositionTuple t, boolean isFat) {

// Node agent = GraphUtils.node_list[t.agent];
// if (isFat) {
// Node box = GraphUtils.node_list[t.box];
// PositionTuple[] nextStates = new PositionTuple[agent.neighbor.size() +
// box.neighbor.size() - 2];
// int i = 0;

// // Pull action
// for (int n : agent.neighbor) {
// if (t.box != n) {
// nextStates[i] = new PositionTuple(n, t.agent);
// i++;
// }
// }

// // Push action
// for (int n : box.neighbor) {
// if (t.agent != n) {
// nextStates[i] = new PositionTuple(t.box, n);
// i++;
// }
// }

// return nextStates;
// } else {
// PositionTuple[] nextStates = new PositionTuple[agent.neighbor.size()];
// int i = 0;

// // move action
// for (int n : agent.neighbor) {
// nextStates[i] = new PositionTuple(n, n);
// i++;
// }

// return nextStates;
// }
// }

// /*
// * Algorithm to create all possible shortest path graph given consistent
// heuristic
// * Runs A* until we pop higher than optimal cost node from openSet
// * Proof : Assume there still remains a shortest path after above mentioned.
// That path must go thorugh
// * atleast one member in the openSet. Let x be the first such member. Since
// heuristic is consistent,
// * Then gCost of x in openSet is optimal, hence the the cost of the path from
// source to destination
// * through x would be atleast min{Openset} > optimalCost which is
// contradiction.
// */
// MDD createMDD(PositionTuple start, PositionTuple goalPosition, boolean isFat)
// {
// Queue<PositionTuple> openSet = new PriorityQueue<>();
// HashMap<PositionTuple, Integer> fCost = new HashMap<PositionTuple,
// Integer>();
// HashMap<PositionTuple, ArrayList<PositionTuple>> searchTree = new
// HashMap<>();
// searchTree.put(start, new ArrayList<>());
// openSet.add(start);
// fCost.put(start, GraphUtils.min_distance[start.box][goalPosition.box]);
// int optimalCost = Integer.MAX_VALUE;

// while (!openSet.isEmpty()) {
// PositionTuple current = openSet.poll();
// int gCost = fCost.get(current) -
// GraphUtils.min_distance[current.box][goalPosition.box];

// if (optimalCost < gCost) {
// // Tree expansion complete
// break;
// }

// if (current.equals(goalPosition)) {
// if (optimalCost != Integer.MAX_VALUE)
// optimalCost = gCost;
// }

// PositionTuple[] newStates = expandState(current, isFat);

// for (PositionTuple state : newStates) {
// int new_cost = gCost + 1 +
// GraphUtils.min_distance[state.box][goalPosition.box];
// int old_cost = fCost.getOrDefault(state, Integer.MAX_VALUE);

// if (new_cost < old_cost) {
// fCost.put(state, new_cost);
// openSet.add(state);
// ArrayList<PositionTuple> p = new ArrayList<>();
// p.add(current);
// searchTree.put(state, p);
// } else if (new_cost == old_cost) {
// searchTree.get(state).add(current);
// }
// }
// }

// /*
// * Construct a bfs tree from destination to start node
// * What if t=0
// */
// MDD M = new MDD(optimalCost + 1);
// int t = optimalCost;
// Queue<PositionTuple> bfsQueue = new LinkedList<>();
// M.position2Index[t].put(goalPosition, 0);
// bfsQueue.add(goalPosition);
// M.node_t[t].add(goalPosition);
// M.outgoing_t[t].add(new ArrayList<>());
// M.incoming_t[t].add(new ArrayList<>());
// int size = 0;

// while (!bfsQueue.isEmpty()) {
// PositionTuple current = bfsQueue.poll();
// int currentCost = fCost.get(current) -
// GraphUtils.min_distance[current.box][goalPosition.box];
// if (t == currentCost) {
// size = 0;
// t--;
// }

// ArrayList<PositionTuple> neighbor = searchTree.get(current);
// for (PositionTuple n : neighbor) {
// int gCost = fCost.get(n) - GraphUtils.min_distance[n.box][goalPosition.box];
// assert (gCost == t && currentCost == t + 1); // remove after testing

// if (!M.position2Index[t].containsKey(n)) {
// M.position2Index[t].put(n, size++);
// M.node_t[t].add(n);
// M.outgoing_t[t].add(new ArrayList<>());
// M.incoming_t[t].add(new ArrayList<>());
// }

// int from = M.position2Index[t].get(n);
// int to = M.position2Index[t + 1].get(current)
// M.outgoing_t[t].get(from).add(to);
// M.incoming_t[t+1].get(to).add(from);
// }
// }

// return M;
// }

// /*
// * Builds a MDD graph for the given agent
// * The agent must be present in the initial state
// */
// MDD buildGraph(State state) {
// int numAgents = state.numAgents;
// HashMap<Integer, Boolean> initialState = new HashMap<>();
// if (numAgents > 1) {
// System.err.println("CTHeuristic only supports single agent at the moment");
// System.exit(1);
// }

// for (int i = 0; i < state.boxMap.length; i++) {
// for (int j = 0; j < state.boxMap[i].size(); j++) {
// initialState.put(state.boxMap[i].get(j), true);
// }
// }

// // build graph
// int label = agent.getAgentNumber();
// int startPos = state.agentPosition[state.label2index[label]];
// HashMap<Integer, ArrayList<Integer>> position2node = new HashMap<>();

// /*
// * getExpandedStates like function
// * Iterate through all agent [usually one in my case]
// * Check applicable actions by looking at boxes in
// *
// *
// *
// * For each action type (Push | Pull | Move | NoOp) generate a set of literals
// * that
// * are applicable.
// *
// */

// return null;

// }
// }
