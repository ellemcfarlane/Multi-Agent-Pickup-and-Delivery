package com.smac.mas.localsearch;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import com.smac.mas.searchclient.GraphUtils;
import com.smac.mas.searchclient.Node;

public class Search {

    public static int[] getDistanceCost(LocalState initialState) {
        LocalState state = null;
        HashMap<Integer, Integer> fCost = new HashMap<>();
        fCost.put(initialState.currentNode, initialState.heuristic());
        HashSet<Integer> expanded = new HashSet<>();
        Comparator<LocalState> comparator = new Comparator<LocalState>() {
            @Override
            public int compare(LocalState t1, LocalState t2) {
                return fCost.get(t1.currentNode).compareTo(fCost.get(t2.currentNode));
            }
        };
        PriorityQueue<LocalState> openSet = new PriorityQueue<LocalState>(comparator);
        openSet.add(initialState);

        while (!openSet.isEmpty()) {

            state = openSet.remove();
            state.removeGoalState();

            if (state.isGoalState()) {
                int i = 0;
                int[] result = new int[LocalState.destination.length];
                for (int n : LocalState.destination) {
                    result[i] = fCost.get(n); // fCost == gCost at goal
                    i++;
                }
                // System.err.println("finished with " + fCost.size() + " nodes explored out of
                // " + GraphUtils.node_list.length
                // + " nodes");
                return result;
            }

            expanded.add(state.currentNode);
            Node current_node = GraphUtils.node_list[state.currentNode];

            for (int n : current_node.neighbor) {
                if (!expanded.contains(n)) {
                    int edgeCost = state.edgeCost(n);
                    int new_cost = state.currentCost + edgeCost + LocalState.heuristic(n);
                    int old_cost = fCost.getOrDefault(n, Integer.MAX_VALUE);

                    if (new_cost < old_cost) {
                        fCost.put(n, new_cost);
                        openSet.add(new LocalState(n, state.currentCost + edgeCost));
                    }
                }
            }
        }
        // failed to find solution
        return null;
    }
}
