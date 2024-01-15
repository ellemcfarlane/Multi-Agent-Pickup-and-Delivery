package com.smac.mas;

import org.junit.Test;

import com.smac.mas.mapf.*;
import com.smac.mas.mapf.Goal.GoalType;
import com.smac.mas.searchclient.*;

public class CTNodeTest {
    // tests the lazy case for getSmallestPlanSize
    @Test
    public void testGetSmallestPlanSize() {
        // create a CTNode
        // CTNode(int numAgents, Agent[] agents, HashMap<Integer, ArrayList<Box>> boxes)
        Agent[] agents = new Agent[2];
        agents[0] = new Agent(0);
        agents[1] = new Agent(1);
        agents[0].setCurrentGoal(new Goal(GoalType.NO_GOAL));
        agents[1].setCurrentGoal(new Goal(GoalType.REACH_AGENT_GOAL));

        CTNode ctNode = new CTNode(2, agents, null);
        // create solution for each agent
        DoubleLinkList<ActionWrapper>[] solution = new DoubleLinkList[2];
        solution[0] = new DoubleLinkList<ActionWrapper>();
        solution[1] = new DoubleLinkList<ActionWrapper>();
        // add actions to solution
        solution[0].add(new ActionWrapper(Action.Move0));
        solution[1].add(new ActionWrapper(Action.Move0));
        solution[1].add(new ActionWrapper(Action.Move0));

        ctNode.setSolution(solution);

        assert (ctNode.getSmallestPlanSize() == 1);

    }
}
