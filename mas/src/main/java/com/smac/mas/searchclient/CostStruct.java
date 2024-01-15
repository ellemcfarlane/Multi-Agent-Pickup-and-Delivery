package com.smac.mas.searchclient;

import java.util.ArrayList;

public class CostStruct {
    public ArrayList<Integer>[] agentXbox;
    public ArrayList<Integer>[] boxXagent;
    public ArrayList<Integer>[] agentXboxCost;
    public ArrayList<Integer>[] agentXgoal;
    public ArrayList<Integer>[] boxXgoal;
    public ArrayList<Integer>[] goalXentity;
    public ArrayList<Integer>[] goalXentityCost;
    public ArrayList<Integer> agentDescription;
    public ArrayList<Integer> boxDescription;
    public ArrayList<Integer> goalDescription;
    public ArrayList<Character> goalType;
    public int[][] agentColorMap;
    public int[][] goalColorMap;
    public final State s;

    public CostStruct(State s) {
        agentDescription = new ArrayList<>();
        boxDescription = new ArrayList<>();
        goalDescription = new ArrayList<>();
        this.s = s;
    }

    public void initializeArrays() {
        agentXbox = new ArrayList[agentDescription.size()];
        boxXagent = new ArrayList[boxDescription.size()];
        agentXboxCost = new ArrayList[agentDescription.size()];
        agentXgoal = new ArrayList[agentDescription.size()];
        boxXgoal = new ArrayList[boxDescription.size()];
        goalXentity = new ArrayList[goalDescription.size()];
        goalXentityCost = new ArrayList[goalDescription.size()];
    }

    private String gridPos(int n) {
        return GraphUtils.node_list[n].row + "," + GraphUtils.node_list[n].col;
    }

    public void printCost() {
        System.err.println("AgentXbox");
        for (int i = 0; i < agentXbox.length; i++) {
            for (int j = 0; j < agentXbox[i].size(); j++) {
                System.err.println("Agent " + agentDescription.get(i) +
                        ", box: " + s.node2box.get(boxDescription.get(agentXbox[i].get(j))) +
                        ", cost: " + agentXboxCost[i].get(j));
            }
        }

        System.err.println("\nAgentXgoal");
        for (int i = 0; i < agentXgoal.length; i++) {
            if (agentXgoal[i] == null)
                continue;
            assert (agentXgoal[i].size() == 1);
            for (int j = 0; j < agentXgoal[i].size(); j++) {
                char c = s.node2goal.get(goalDescription.get(agentXgoal[i].get(j)));
                assert (goalXentityCost[goalDescription.get(agentXgoal[i].get(j))].size() == 1);
                System.err.println("Agent " + agentDescription.get(i) +
                        ", goal: " + c + ", cost: " + goalXentityCost[agentXgoal[i].get(j)].get(j));
            }
        }

        System.err.println("\nGoalXentity");
        for (int i = 0; i < goalXentity.length; i++) {
            for (int j = 0; j < goalXentity[i].size(); j++) {
                if (s.node2goal.get(goalDescription.get(i)) > '9') {
                    char g = s.node2goal.get(goalDescription.get(i));
                    System.err.println("Goal " + g + " at " + gridPos(goalDescription.get(i)) +
                            " boxAt " + gridPos(boxDescription.get(goalXentity[i].get(j))) + " cost "
                            + goalXentityCost[i].get(j));
                }
            }
        }
    }
}
