package com.smac.mas.localsearch;

import java.util.ArrayList;
import java.util.HashMap;

import com.smac.mas.searchclient.Color;
import com.smac.mas.searchclient.GraphUtils;
import com.smac.mas.searchclient.State;

public class LocalState {
    public final int currentNode;
    public final int currentCost;
    private static Color color;
    public static int[] destination;
    private static HashMap<Integer, Boolean> destinationNodes;
    private static HashMap<Integer, Integer> box2agentMinDistance;
    public static State state;

    public LocalState(State state, int start, HashMap<Integer, Boolean> destinationNode, int[] destination,
            Color color) {
        this.currentNode = start;
        this.currentCost = 0;
        LocalState.state = state;
        LocalState.color = color;
        LocalState.destinationNodes = destinationNode;
        LocalState.destination = destination;
        compute_min_distance();
    }

    public LocalState(int start, Color color, HashMap<Integer, Boolean> destinationNode, int[] destination) {
        this.currentNode = start;
        this.currentCost = 0;
        LocalState.destination = destination;
        LocalState.destinationNodes = destinationNode;
        LocalState.color = color;
    }

    public LocalState(int u, int cost) {
        this.currentNode = u;
        this.currentCost = cost;
    }

    public void removeGoalState() {
        if (destinationNodes.containsKey(currentNode)) {
            destinationNodes.remove(currentNode);
        }
    }

    public boolean isGoalState() {
        return destinationNodes.size() == 0;
    }

    // for each box compute the min distance with agent
    private static void compute_min_distance() {
        LocalState.box2agentMinDistance = new HashMap<>();

        for (int i = 0; i < State.CHARSET_ARRAY_SIZE; i++) {
            for (int node_id : state.boxMap[i]) {
                int min_distance = Integer.MAX_VALUE;
                ArrayList<Integer> agent_labels = State.color2Agent[Color.color2Int(State.boxColors[i])];

                for (int l : agent_labels) {
                    int index = state.label2index[l];
                    if (index == -1)
                        continue;

                    // assuming connected components => always finite
                    int distance = GraphUtils.min_distance[node_id][state.agentPosition[index]];

                    if (distance < min_distance) {
                        min_distance = distance;
                    }
                }

                LocalState.box2agentMinDistance.put(node_id, min_distance);
            }
        }
    }

    public int edgeCost(int v) {
        if (state.node2box.containsKey(v)) {
            char box = state.node2box.get(v);

            if (State.boxColors[box - 'A'].equals(color))
                return 1;

            return 1 + LocalState.box2agentMinDistance.get(v);
        }
        return 1;
    }

    public int heuristic() {
        return LocalState.heuristic(currentNode);
    }

    public static int heuristic(int v) {
        int min_distance = Integer.MAX_VALUE;
        for (int d : destinationNodes.keySet()) {
            int distance = GraphUtils.min_distance[v][d];
            if (distance < min_distance) {
                min_distance = distance;
            }
        }
        return min_distance;
    }
}
