package com.smac.mas.mapf;

import com.smac.mas.searchclient.ActionWrapper;

public class ConflictEstimator {
    public ConflictEstimator() {
    }

    public static int getNumConflictLowerBound(CTNode node) {

        try {
            int lsc = node.getLongestSolutionCost();
            int count = 0;

            ActionWrapper[] actions_t = new ActionWrapper[node.solution.length];
            for (int i = 0; i < node.solution.length; i++) {
                if (node.solution[i].hasFirst())
                    actions_t[i] = node.solution[i].getFirst();
            }

            for (int t = 1; t < lsc; t++) {

                // get next actions
                for (int i = 0; i < node.solution.length; i++) {
                    if (node.solution[i].hasNext())
                        actions_t[i] = node.solution[i].next();
                    else
                        actions_t[i] = null;
                }

                for (int i = 0; i < node.solution.length; i++) {
                    ActionWrapper act_i = actions_t[i];
                    if (act_i == null)
                        continue;
                    int currentNodeId_i = act_i.newAgentPos;
                    int prevNodeId_i = act_i.prevAgentPos;

                    for (int j = i + 1; j < node.solution.length; j++) {
                        ActionWrapper act_j = actions_t[j];
                        if (act_j == null)
                            continue;
                        int currentNodeId_j = act_j.newAgentPos;
                        int prevNodeId_j = act_j.prevAgentPos;

                        if (currentNodeId_i == currentNodeId_j) {
                            count++;
                        } else if (currentNodeId_i == prevNodeId_j || prevNodeId_i == currentNodeId_j) {
                            count++;
                        }
                    }
                }
            }

            return count;
        } catch (Exception e) {
            System.err.println("Error in getNumConflictLowerBound");
            System.err.println(e.getMessage());
            return 0;
        }
    }
}
