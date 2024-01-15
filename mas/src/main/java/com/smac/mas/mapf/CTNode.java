package com.smac.mas.mapf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.smac.mas.mapf.Goal.GoalType;
import com.smac.mas.searchclient.Action;
import com.smac.mas.searchclient.ActionWrapper;
import com.smac.mas.searchclient.Color;
import com.smac.mas.searchclient.State;

public class CTNode {

    public int cost;

    // variables for helping - should be stacked
    public ArrayList<Color> helpingColors = new ArrayList<Color>();
    public ArrayList<Integer> tempGoalLocation = new ArrayList<Integer>();
    public ArrayList<ArrayList<Integer>> additionalConstraints = new ArrayList<ArrayList<Integer>>();

    // public DoubleLinkList<Action>[] solution;

    public DoubleLinkList<ActionWrapper>[] solution;

    // public HashSet<Constraint> constraints;
    public Conflict conflict;
    public ActionWrapper[][] plan;
    // hashmap agent->time->place->constraints
    // TODO: do this for normal conflicts, too for efficiency
    // another todo: verify that the HashSet doesn't add same equivalent constraint
    // -- think we need
    // to define hash method to do so
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>> resourceConstraints;

    // agent -> time -> node -> set of constraints
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>> constraints;

    public Agent[] agents;
    public HashMap<Integer, ArrayList<Box>> boxes;
    public int depth;

    public CTNode(int numAgents, Agent[] agents, HashMap<Integer, ArrayList<Box>> boxes) {

        solution = new DoubleLinkList[numAgents];
        for (int i = 0; i < solution.length; i++) {
            solution[i] = new DoubleLinkList<>();
        }
        constraints = new HashMap<>();
        this.resourceConstraints = new HashMap<>();
        this.agents = agents;
        this.boxes = boxes;
        depth = 0;
    }

    public CTNode(int cost, DoubleLinkList<ActionWrapper>[] solution,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>> constraints,
            Conflict conflict, Agent[] agents, HashMap<Integer, ArrayList<Box>> boxes,
            ArrayList<Color> helpingColorsPrev, ArrayList<Integer> tempGoalLocationPrev,
            ArrayList<ArrayList<Integer>> additionalConstraintsPrev) {

        this.cost = cost;
        this.solution = new DoubleLinkList[solution.length];
        for (int i = 0; i < solution.length; i++) {
            this.solution[i] = solution[i].clone();
        }
        // this.constraints = (HashMap<Integer, HashMap<Integer, HashMap<Integer,
        // HashSet<Constraint>>>>) constraints.clone(); // TODO probably need to deep
        // copy
        this.constraints = copyConstr(constraints);

        if (this.resourceConstraints != null) {
            // this.resourceConstraints =
            // (HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>>)
            // resourceConstraints // TODO probably need to deep copy
            // .clone();
            this.resourceConstraints = copyConstr(resourceConstraints);
        }
        // TODO (elle): should conflict be cloned? only if we update the class with
        // fields that are not primitives
        this.conflict = conflict;
        if (agents != null) {
            this.agents = agents.clone();
        }
        this.boxes = boxes;

        // variables for helping - should be stacked
        helpingColors = helpingColorsPrev;
        tempGoalLocation = tempGoalLocationPrev;
        additionalConstraints = additionalConstraintsPrev;

    }

    public void setSolution(DoubleLinkList<ActionWrapper>[] solution) {
        this.solution = solution;
    }

    // shallow copy of all hashmaps in constraints
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>> copyConstr(
            HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>> oldConstr) {

        HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>> newConstr = (HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>>) oldConstr
                .clone(); // clone agent hashmap

        for (int agentID : oldConstr.keySet()) {

            HashMap<Integer, HashMap<Integer, HashSet<Constraint>>> newTimes = (HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>) oldConstr
                    .get(agentID)
                    .clone(); // clone time hashmaps

            newConstr.put(agentID, newTimes);

            for (int time : oldConstr.get(agentID).keySet()) {

                HashMap<Integer, HashSet<Constraint>> newNodes = (HashMap<Integer, HashSet<Constraint>>) oldConstr
                        .get(agentID).get(time)
                        .clone(); // clone node id hashmaps

                newConstr.get(agentID).put(time, newNodes);

                for (int nodeID : oldConstr.get(agentID).get(time).keySet()) {

                    HashSet<Constraint> newSetOfConstr = (HashSet<Constraint>) oldConstr
                            .get(agentID).get(time).get(nodeID).clone(); // clone set of constraints
                    newConstr.get(agentID).get(time).put(nodeID, newSetOfConstr);

                }
            }
        }

        return newConstr;

    }

    // utility fxn since we are using a hashmap for
    // this.resourceConstraints
    public void addResourceConstraint(Constraint constraint) {
        int agent = constraint.agent;
        int time = constraint.t;
        int place = constraint.node_id;
        if (this.resourceConstraints == null) {
            this.resourceConstraints = new HashMap<>();
        }
        if (!this.resourceConstraints.containsKey(agent)) {
            this.resourceConstraints.put(agent, new HashMap<>());
        }
        if (!this.resourceConstraints.get(agent).containsKey(time)) {
            this.resourceConstraints.get(agent).put(time, new HashMap<>());
        }
        if (!this.resourceConstraints.get(agent).get(time).containsKey(place)) {
            this.resourceConstraints.get(agent).get(time).put(place, new HashSet<>());
        }
        this.resourceConstraints.get(agent).get(time).get(place).add(constraint);
    }

    public void addNormalConstraint(Constraint constraint) {
        int agent = constraint.agent;
        int place = constraint.node_id;

        if (!this.constraints.containsKey(agent)) {
            this.constraints.put(agent, new HashMap<>());
        }

        int tStart;
        int tEnd;

        if (constraint instanceof IntervalConstraint) { // add all times

            tStart = constraint.tStart;
            tEnd = constraint.t;

        } else { // normal constraint

            tStart = constraint.t;
            tEnd = tStart + 1;

        }

        for (int time = tStart; time < tEnd; time++) {

            if (!this.constraints.get(agent).containsKey(time)) {
                this.constraints.get(agent).put(time, new HashMap<>());
            }
            if (!this.constraints.get(agent).get(time).containsKey(place)) {
                this.constraints.get(agent).get(time).put(place, new HashSet<>());
            }
            this.constraints.get(agent).get(time).get(place).add(constraint);

        }

    }

    public ActionWrapper[][] extractPlan() {
        plan = new ActionWrapper[getLongestSolutionCost()][solution.length];
        int numberOfAgents = solution.length;

        for (int step = 0; step < plan.length; step++) {
            for (int agent = 0; agent < numberOfAgents; agent++) {

                if (step < solution[agent].size()) {
                    plan[step][agent] = solution[agent].get(step);
                } else {
                    plan[step][agent] = new ActionWrapper(Action.NoOp);
                }
            }
        }
        return plan;
    }

    // longest solution cost
    public int getLongestSolutionCost() {
        int cost = 0;
        for (int i = 0; i < solution.length; i++) {
            if (cost < solution[i].size()) {
                cost = solution[i].size();
            }
        }
        return cost;
    }

    // return smallest plan size
    public int getSmallestPlanSize() {
        int cost = Integer.MAX_VALUE; // infinity
        int numberOfAgents = solution.length;

        for (int agent = 0; agent < numberOfAgents; agent++) {

            Goal currentGoal = this.agents[agent].getCurrentGoal();
            // if agent has no-goal, count it if it has a plan,
            // if agent has a goal, always count it
            if (currentGoal.getType() == GoalType.NO_GOAL && solution[agent].size() != 0
                    && cost > solution[agent].size()) {
                cost = solution[agent].size();
            } else if (currentGoal.getType() != GoalType.NO_GOAL && cost > solution[agent].size()) {
                cost = solution[agent].size();
            }
        }

        if (cost == Integer.MAX_VALUE) {
            return 0;
        } // no agent has a goal

        return cost;
    }

    public void setCost() {
        int cost = 0;
        for (int i = 0; i < solution.length; i++) {
            cost += solution[i].size();
        }
        this.cost = cost;
    }

    public void setCost(int bias) {
        int cost = 0;
        for (int i = 0; i < solution.length; i++) {
            cost += solution[i].size();
        }
        this.cost = cost + bias;
    }

    // Determines up to what timestep (smallestN, lsc, avg, etc) we validate paths,
    // if agent doesn't have an action at that timestep,
    // we create a NoOp for it in validate method.
    public int getValidateTime() {
        // WARNING: be careful with getSmallestPlanSize here,
        // current implementation leads to inf BDI loops with
        // levels like LazyTest2+
        // return getSmallestPlanSize();
        return getLongestSolutionCost();
    }

    /*
     * updates box simulation and returns true if no resource conflicts are found
     * 
     * @param timestep
     * 
     * @param boxType
     * 
     * @param nextBoxPosition
     * 
     * @param BoxSimulation
     * 
     * @return null if no resource conflicts are found, boxAction of conflicting
     * action if resource
     * conflicts are found
     * 
     */
    private SimulationAction updateBoxSimulation(int timestep, Agent agent, int[] boxData,
            int nextBoxPosition, int boxPosition,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            HashMap<Integer, HashMap<Integer, SimulationAction>> boxToLatestSimAction) {

        int boxType = boxData[0];
        int boxIdx = boxData[1];
        // add box into simulation
        if (!boxSimulation.containsKey(timestep + 1)) {
            boxSimulation.put(timestep + 1, new HashMap<>());
        }
        if (!boxSimulation.get(timestep + 1).containsKey(boxType)) {
            boxSimulation.get(timestep + 1).put(boxType, new HashMap<>());
        }
        if (boxToLatestSimAction != null && !boxToLatestSimAction.containsKey(boxType)) {
            boxToLatestSimAction.put(boxType, new HashMap<>());
        }
        // if not trying to move same box at same time at same place, then no rsc
        // conflict
        if (!boxSimulation.get(timestep + 1).get(boxType).containsKey(boxIdx) || (boxSimulation
                .get(timestep + 1).get(boxType).get(boxIdx).prevPos != boxPosition)) {
            Box box = this.boxes.get(boxType).get(boxIdx);
            SimulationAction boxAction = new SimulationAction(agent, box, nextBoxPosition, boxPosition, timestep + 1);
            boxSimulation.get(timestep + 1).get(boxType).put(boxIdx, boxAction);
            if (boxToLatestSimAction != null) {
                boxToLatestSimAction.get(boxType).put(boxIdx, boxAction);
            }
            // no conflict
            return null;
        } else {
            // resource conflict
            return boxSimulation.get(timestep + 1).get(boxType).get(boxIdx);
        }
    }

    boolean wasBoxMovedAtTime(
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            Box box, int timestep) {
        SimulationAction action = getBoxActionAtTime(boxSimulation, box, timestep);
        if (action == null) {
            return false;
        } else {
            return true;
        }
    }

    SimulationAction getLatestActionOnBox(Box box,
            HashMap<Integer, HashMap<Integer, SimulationAction>> boxToLatestSimAction) {
        if (boxToLatestSimAction.containsKey(box.boxType)) {
            if (boxToLatestSimAction.get(box.boxType).containsKey(box.boxIndex)) {
                return boxToLatestSimAction.get(box.boxType).get(box.boxIndex);
            }
        }
        return null;
    }

    // getBoxDetails: returns whether box was NOT just moved (isStatic) and the
    // "box-owner"
    // aka agent who last moved box or if box is fat, its
    // pickup guy
    // if no agent has ever touched box and box is not fat, returns true and null
    public ArrayList<Object> getBoxDetails(
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            HashMap<Integer, HashMap<Integer, SimulationAction>> boxToLatestSimAction, Box box,
            int timestep) {
        // to check if box was just moved, check if box is in boxSimulation at timestep

        // to check who last moved box, check if box is in boxToLatestSimAction
        // if not, set agent to null
        ArrayList<Object> boxActionDetails = new ArrayList<>();
        SimulationAction boxAction = getLatestActionOnBox(box, boxToLatestSimAction);
        boolean isStatic = true; // default
        Agent boxOwner = box.pickupGuy; // either null or if fat, pickupGuy
        // check if box is fat and if so, get the pickup guy
        // BUG: why is box not fat but has a pickup guy for case
        // when agent wants to move box to goal? see MATestFatSep3.lvl
        // if (box.isFat) {
        // boxOwner = box.pickupGuy;
        // }

        // if boxAction is null, it has never been touched so it is static
        if (boxAction == null) {
            boxActionDetails.add(isStatic);
            boxActionDetails.add(boxOwner);
            return boxActionDetails;
        }
        int timeBoxMoved = boxAction.time;
        isStatic = timeBoxMoved < timestep;
        boxOwner = boxAction.agent;

        boxActionDetails.add(isStatic);
        boxActionDetails.add(boxOwner);
        return boxActionDetails;
    }

    // time -> boxType -> boxIndex -> SimulationAction
    // returns
    public SimulationAction getBoxActionAtTime(
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            Box box, int timestep) {
        if (!boxSimulation.containsKey(timestep)) {
            return null;
        }
        if (!boxSimulation.get(timestep).containsKey(box.boxType)) {
            return null;
        }
        if (!boxSimulation.get(timestep).get(box.boxType).containsKey(box.boxIndex)) {
            return null;
        }
        return boxSimulation.get(timestep).get(box.boxType).get(box.boxIndex);
    }

    public ActionWrapper getAction(int agentID, int relativeTimestep, int[] agentToPosition) {
        if (relativeTimestep < solution[agentID].size()) {
            return solution[agentID].get(relativeTimestep);
        } else {
            int currPos = agentToPosition[agentID];
            return new ActionWrapper(Action.NoOp, currPos, currPos, "NoOp");
        }
    }

    public Integer[] getPositions(int agentID, ActionWrapper nextActionWrapper,
            int[] agentToPosition, int relativeTimestep) {
        // returns "current pos" and "new position" as result of this action for agent
        // and box
        // if not a box action, then box positions are null
        return new Integer[] { nextActionWrapper.prevAgentPos, nextActionWrapper.newAgentPos,
                nextActionWrapper.prevBoxPos, nextActionWrapper.newBoxPos };
    }

    // TODO: check if entity followed agent? (just for slight efficiency, we would
    // catch it later in
    // the loop anyway)
    public boolean agentFollowedEntity(int agentID, int agentPosition, int relativeTimestep,
            HashMap<Integer, Integer> prevPosition2AgentJustMoved,
            HashMap<Integer, SimulationAction> prevPosition2RecentBoxAction) {
        // agent followed agent
        if (prevPosition2AgentJustMoved.containsKey(agentPosition)) {
            int otherAgentID = prevPosition2AgentJustMoved.get(agentPosition);
            if (otherAgentID != agentID) {
                // TODO (fat): update AgentFollowedConflict to have null-null default for this
                conflict = new AgentFollowedConflict(agentID, otherAgentID, null, null,
                        agentPosition, relativeTimestep + 1);
                return true;
            }
        }

        // agent followed box
        if (prevPosition2RecentBoxAction.containsKey(agentPosition)) {
            SimulationAction boxAction = prevPosition2RecentBoxAction.get(agentPosition);
            int otherAgentID = boxAction.agent.getAgentNumber();
            Box box = boxAction.box;
            // create follow Conflict with box1=null for agent following and box2=box for
            // agent that moved box
            if (otherAgentID != agentID) {
                // first agent param followed second agent param
                conflict = new AgentFollowedConflict(agentID, otherAgentID, null, box,
                        agentPosition, relativeTimestep + 1);
                return true;
            }
        }
        return false;
    }

    // TODO: reduce code duplication between BoxFollowedEntity and
    // AgentFollowedEntity
    public boolean boxFollowedEntity(Box box, int boxPos,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            int relativeTimestep, HashMap<Integer, Integer> prevPosition2AgentJustMoved,
            HashMap<Integer, SimulationAction> prevPosition2RecentBoxAction) {
        // box followed agent
        if (prevPosition2AgentJustMoved.containsKey(boxPos)) {
            SimulationAction boxAction = getBoxActionAtTime(boxSimulation, box, relativeTimestep + 1);
            // boxAction should not be null here! If it is,
            // it may indicate an agent ran into a static box of diff color and we did not"
            // handle it above before we got here
            assert boxAction != null;
            int agentThatMovedBox = boxAction.agent.getAgentNumber();
            int otherAgentID = prevPosition2AgentJustMoved.get(boxPos);
            // TODO (fat): remove this if-check or make it an assert, it should never be
            // true for
            // this case
            if (otherAgentID != agentThatMovedBox) {
                // first agent param followed the second agent param
                // System.err.printf("BOX %s-%s FOLLOWED AGENT %s\n", box.boxType, box.boxIndex,
                // otherAgentID);
                // format string

                conflict = new BoxFollowedConflict(agentThatMovedBox, otherAgentID, box, null,
                        boxPos, relativeTimestep + 1);
                return true;
            }
        }
        // box just followed a box
        if (prevPosition2RecentBoxAction.containsKey(boxPos)) {
            SimulationAction otherBoxActio = prevPosition2RecentBoxAction.get(boxPos);
            int otherAgentID = otherBoxActio.agent.getAgentNumber();
            Box otherBox = otherBoxActio.box;

            SimulationAction boxAction = getBoxActionAtTime(boxSimulation, box, relativeTimestep + 1);
            int agentID = boxAction.agent.getAgentNumber();
            if (otherAgentID != agentID) {
                // first agent param follows second agent param
                conflict = new BoxFollowedConflict(agentID, otherAgentID, box, otherBox, boxPos,
                        relativeTimestep + 1);
                return true;
            }
        }
        return false;
    }

    String getBoxIDFromCharInt(Character boxType, int boxIndex) {
        return boxType + "_" + boxIndex;
    }

    String getBoxIDFromIntInt(int boxType, int boxIndex) {
        return (char) (boxType + 'A') + "_" + boxIndex;
    }

    public int[] getBoxData(String boxID) {
        // split string by "_" then parse first element as char and second as int
        String[] boxData = boxID.split("_");
        int boxType = boxData[0].charAt(0) - 'A';
        int boxIndex = Integer.parseInt(boxData[1]);
        return new int[] { boxType, boxIndex };
    }

    void addBoxToPos(String boxID, int boxPos, HashMap<Integer, HashSet<String>> posToBox) {
        if (!posToBox.containsKey(boxPos)) {
            posToBox.put(boxPos, new HashSet<>());
        }
        posToBox.get(boxPos).add(boxID);
    }

    void removeBoxFromPos(String boxID, int boxPos, HashMap<Integer, HashSet<String>> posToBox) {
        if (!posToBox.containsKey(boxPos)) {
            System.err.println("ERR: boxPos " + boxPos + " not in posToBox");
        }
        posToBox.get(boxPos).remove(boxID);
        if (posToBox.get(boxPos).size() == 0) {
            posToBox.remove(boxPos);
        }
    }

    boolean isBoxAtPosition(String boxID, int boxPos, HashMap<Integer, HashSet<String>> posToBox) {
        if (!posToBox.containsKey(boxPos)) {
            return false;
        }
        return posToBox.get(boxPos).contains(boxID);
    }

    boolean agentRanIntoAgent(int agentID, int agentPosition, int relativeTimestep,
            HashMap<Integer, Integer> position2Agent) {
        if (position2Agent.containsKey(agentPosition)) {
            int otherAgentID = position2Agent.get(agentPosition);
            // Can implement LazyConflicts here: increases efficiency by 10x+
            // on highly coupled levels with LazyConflicts (see LazyTest2Slower)
            // but leads to non-optimal solutions for, e.g. LazyTestEasy
            // see github history for how to implement
            if (otherAgentID != agentID) {
                conflict = new TimePlaceConflict(agentID, otherAgentID, agentPosition,
                        relativeTimestep + 1);
                return true;
            }
        }
        return false;
    }

    // 3 main cases:
    // 1. box is static and he has no pickup guy -> ask for help (diff col) or sim
    // (same col)
    // 2. box is static, diff col and DOES have a pickup guy -> so wait for him to
    // come and pick it
    // up
    // 3. else we have normal case of 2 agents to make a TimePlaceConflict with
    boolean agentRanIntoBox(int agentID, int agentPosition, int relativeTimestep,
            HashMap<Integer, HashSet<String>> posToBox,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            HashMap<Integer, HashMap<Integer, SimulationAction>> boxToLatestSimAction) {
        // BOX-AGENT TimePlaceConflict - dynamic (so far also static) boxes
        if (posToBox.containsKey(agentPosition)) {
            // TODO (fat): can now be multiple boxes in same position e.g. 2 agents push 2
            // boxes to same spot
            // and agent runs into them. So, how to choose which box to create conflict
            // with?
            // does it matter? maybe not

            // get any box randomly from the set of boxes at this position
            // TODO/BUG: are there issues with doing this?
            String boxID = posToBox.get(agentPosition).iterator().next();
            // get box type and idx
            int[] boxData = getBoxData(boxID);
            Box box = this.boxes.get(boxData[0]).get(boxData[1]);

            ArrayList<Object> boxDetails = getBoxDetails(boxSimulation, boxToLatestSimAction, box,
                    relativeTimestep + 1);
            boolean isBoxStatic = (boolean) boxDetails.get(0);
            // NOTE: otherAgent can be null if box is static and has no pickupGuy
            Agent otherAgent = (Agent) boxDetails.get(1);

            // create new conflict with box of the same color
            Color agentColor = State.agentColors[agentID];
            Color boxColor = State.boxColors[boxData[0]];

            // TODO: should we also include rest of box simulation after this point? probs
            // not right?
            // filter out unrelated box types in simulation
            boxSimulation = filterBoxSimulation(boxData, boxSimulation, relativeTimestep);

            // 1. if box is static and no pickup guy will move him, we need to
            // simulate (if same color) or ask for help (if diff color)
            if (isBoxStatic && box.pickupGuy == null) {
                if (agentColor == boxColor) {
                    ////// SCENARIO : movingagent-staticbox-notfat (SAME colors) //////////
                    // -> simulate aka SameColorStaticBoxAgentConflict (agent replans around it)
                    // -> ask for help if another agent free (TODO)

                    // System.err.println("box-conflict");
                    // System.err.println("time: " + (relativeTimestep+1));
                    // System.err.println("agent: " + agentID);
                    // TODO: implement second scenario handling in CBS where
                    // agent should not just work around box but also request help to move
                    // samecolor box
                    // if another agent is free to do so
                    conflict = new SameColorStaticBoxAgentConflict(agentID, box, agentPosition,
                            relativeTimestep + 1, boxSimulation);
                    return true;
                } else {
                    ////// SCENARIO : movingagent-staticbox (DIFF colors) //////////
                    // TODO both:
                    // -> simulate aka DiffColorStaticBoxConflict (plan around it if possible)
                    // -> ask for help to move
                    // conflict = new DiffColorStaticBoxAgentConflict(agentID, box, agentPosition,
                    // relativeTimestep + 1, boxSimulation);

                    conflict = new Call_For_Help_Conflict(agentID, box, agentPosition,
                            relativeTimestep + 1, boxSimulation);

                    return true;
                }
            } else {
                assert otherAgent != null;
                // 2. box is static and someone IS going to come move it, so wait for them
                // TODO/BUG: ask Adam is should only wait for diff color box case
                // interval constrain the box -- still did not get interval constraint while
                // somebody is going for the box
                if ((isBoxStatic && box.isFat != true)
                        && (otherAgent.currentGoal.type == GoalType.HELP_WITH_BOX
                                || otherAgent.currentGoal.type == GoalType.REACH_BOX)
                        && boxColor != agentColor) {

                    // compute expected time of pickup
                    int timeExpected = solution[box.pickupGuy.getAgentNumber()].size();

                    // try out both versions for improving
                    conflict = new Different_Color_Conflict_Wait(agentID, agentPosition, 0,
                            timeExpected, boxSimulation);

                    // from now on the pickupguy and box is fat
                    box.isFat = true;
                    box.pickupGuy.isFat = true;

                    return true;
                }
                // 3. otherwise box is dynamic and we can create a conflict with the other agent
                int otherAgentID = otherAgent.getAgentNumber();
                conflict = new TimePlaceBoxAgentConflict(agentID, otherAgentID, null, box,
                        agentPosition, relativeTimestep + 1);
                return true;
            }
        }
        return false;

    }

    // basic idea: if 2 agents are involved in some way -> TimePlaceAgentConflict
    // else: we need to simulate (to move same col box) or ask for help (to move
    // diff col box)

    // 3 main cases (marked in code comments with same numbers) handled here:
    // 1. if both moving -> normal conflict
    // 2. elif static owner fat -> ALSO normal conflict
    // 3. else: we need to simulate bc we ran into static box of same color OR we
    // need to call for help
    // because static box was not same color
    boolean boxRanIntoBox(Box box, String boxID, int boxPos, int relativeTimestep,
            HashMap<Integer, HashSet<String>> posToBox,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            HashMap<Integer, HashMap<Integer, SimulationAction>> boxToLatestSimAction, int[] agentToPosition) {

        if (posToBox.containsKey(boxPos)) {
            ArrayList<Object> boxDetails = getBoxDetails(boxSimulation, boxToLatestSimAction, box,
                    relativeTimestep + 1);
            boolean isBoxStatic = (boolean) boxDetails.get(0);
            // TODO IMPORTANT: does this cast fail if otherBoxAction is null?
            // and how to handle if it's null? something to handle with communication I
            // guess...?
            Agent agent = (Agent) boxDetails.get(1);
            // if more than one box at position, there is conflict between at least 2 boxes
            HashSet boxesAtPos = posToBox.get(boxPos);
            if (boxesAtPos.size() > 1) {
                HashSet<String> otherBoxes = (HashSet) boxesAtPos.clone();
                otherBoxes.remove(boxID);
                // TOOD: change to assert, should always be bigger than 0 if outer loop is > 1
                if (otherBoxes.size() > 0) {
                    // there is another box at this position
                    // get any box randomly from the set of other boxes at this position
                    String otherBoxID = otherBoxes.iterator().next();
                    // get box type and idx
                    int[] otherBoxData = getBoxData(otherBoxID);
                    Box otherBox = this.boxes.get(otherBoxData[0]).get(otherBoxData[1]);
                    ArrayList<Object> otherBoxDetails = getBoxDetails(boxSimulation,
                            boxToLatestSimAction, otherBox, relativeTimestep + 1);
                    boolean otherBoxIsStatic = (boolean) otherBoxDetails.get(0);
                    // TODO: does this cast fail if otherBoxAction is null?
                    Agent otherAgent = (Agent) otherBoxDetails.get(1);

                    // create new conflict with box of the same color
                    // TODO: why do we use State.boxColors and not CurrentBDIState?
                    Color boxColor = State.boxColors[box.boxType];
                    Color otherBoxColor = State.boxColors[otherBox.boxType];
                    boolean sameBoxType = box.boxType == otherBox.boxType;
                    // assert if sameBoxType then they have same color, otherwise a bug
                    assert (sameBoxType && (boxColor == otherBoxColor)) || !sameBoxType;

                    // 1. both are moving boxes, so it's like 2 fat-agents running into each other
                    // (even if they aren't fat) -> handle like normal TimePlace
                    if (!isBoxStatic && !otherBoxIsStatic) {
                        ///////////// movingbox-movingbox case //////////////////
                        // no simulation needed, standard Simultaneous conflict but involving a box
                        assert agent != null;
                        assert otherAgent != null;
                        int agentID = agent.getAgentNumber();
                        int otherAgentID = otherAgent.getAgentNumber();
                        conflict = new TimePlaceBoxBoxConflict(agentID, otherAgentID, box, otherBox,
                                boxPos, relativeTimestep + 1);
                        return true;
                    } else {
                        //////////////// movingbox-staticbox or staticbox-movingbox case ///////////

                        // NOTE: staticBoxOwner is confusing it doesn't mean he is a pickupguy
                        // just means he last touched the box
                        Agent staticBoxOwner = isBoxStatic ? agent : otherAgent;
                        Agent dynamicBoxOwner = isBoxStatic ? otherAgent : agent;
                        Box staticBox = isBoxStatic ? box : otherBox;
                        Box dynamicBox = isBoxStatic ? otherBox : box;
                        String id = getBoxIDFromIntInt(staticBox.boxType, staticBox.boxIndex);
                        int[] boxData = getBoxData(id);
                        boxSimulation = filterBoxSimulation(boxData, boxSimulation, relativeTimestep);
                        // NOTE: if box is static then the other agent is ALWAYS null UNLESS he is fat
                        // 2. TODO: static box has a pickupguy -> wait for him to move it
                        if (staticBoxOwner != null && staticBox.pickupGuy != null) {
                            // TODO: refactor to use static and dynamic agent?
                            int agentID = dynamicBoxOwner.getAgentNumber();
                            int agentPosition = agentToPosition[agentID];
                            assert agent != null;
                            // unlike other cases, here the otherAgent should never be null!
                            assert otherAgent != null;
                            if ((staticBox.isFat != true)
                                    && (staticBoxOwner.currentGoal.type == GoalType.HELP_WITH_BOX
                                            || staticBoxOwner.currentGoal.type == GoalType.REACH_BOX)
                                    && boxColor != otherBoxColor) {
                                // compute expected time of pickup
                                int timeExpected = solution[staticBox.pickupGuy.getAgentNumber()].size();

                                // try out both versions for improving
                                conflict = new Different_Color_Conflict_Wait(agentID, agentPosition, 0,
                                        timeExpected, boxSimulation);

                                // from now on the pickupguy and box is fat
                                staticBox.isFat = true;
                                staticBox.pickupGuy.isFat = true;

                                return true;
                            } else {
                                conflict = new TimePlaceBoxBoxConflict(dynamicBoxOwner.getAgentNumber(),
                                        staticBoxOwner.getAgentNumber(), dynamicBox,
                                        staticBox, boxPos, relativeTimestep + 1);
                                return true;
                            }
                        } else {
                            // String id = getBoxIDFromIntInt(staticBox.boxType, staticBox.boxIndex);
                            // int[] boxData = getBoxData(id);
                            // boxSimulation = filterBoxSimulation(boxData, boxSimulation,
                            // relativeTimestep);
                            // 3. no one is going to move the box, so we need to simulate or ask for
                            // help
                            if (boxColor == otherBoxColor) {
                                // simulate
                                // TODO: should we also include rest of box simulation after this point? probs
                                // not right?
                                conflict = new SameColorStaticBoxBoxConflict(
                                        dynamicBoxOwner.getAgentNumber(), staticBox, boxPos,
                                        relativeTimestep + 1, boxSimulation);
                                return true;

                            } else {
                                conflict = new Call_For_Help_Conflict(
                                        dynamicBoxOwner.getAgentNumber(), staticBox, boxPos,
                                        relativeTimestep + 1, boxSimulation);
                                return true;
                            }
                        }

                    }

                }
            }
            // only one box at this position, so no conflict
            return false;
        }
        return false;
    }

    boolean oldSimulationChanged(String boxID, int[] boxData, int boxPos,
            HashMap<Integer, HashSet<String>> posToBox, int relativeTimestep,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            HashMap<Integer, HashMap<Integer, SimulationAction>> boxToLatestSimAction) {
        boolean boxIsAtPosition = isBoxAtPosition(boxID, boxPos, posToBox);
        if (boxIsAtPosition) {
            return false;
        }
        if (!boxToLatestSimAction.containsKey(boxData[0])) {
            return true;
        } else if (!boxToLatestSimAction.get(boxData[0]).containsKey(boxData[1])) {
            return true;
        }
        return false;
    }

    boolean isBoxMissing(String boxID, int[] boxData, int boxPos,
            HashMap<Integer, HashSet<String>> posToBox, int relativeTimestep,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            HashMap<Integer, HashMap<Integer, SimulationAction>> boxToLatestSimAction) {
        boolean boxIsAtPosition = isBoxAtPosition(boxID, boxPos, posToBox);
        if (boxIsAtPosition) {
            return false;
        }
        // now check if box was moved before this timestep bc if it was moved at this
        // timestep,
        // it is a resource conflict and hasn't "gone missing", otherwise it was moved
        // before
        SimulationAction action = boxToLatestSimAction.get(boxData[0]).get(boxData[1]);
        int timeMoved = action.time;
        // box didn't just move from where agent thinks it is aka not a rsc conflict, so
        // must be missing
        return timeMoved != relativeTimestep + 1 || action.prevPos != boxPos;
    }

    // add any remaining box simulations until validateTime
    // -- NOTE: we do not handle RSC conflicts (or any conflicts) here,
    // so if there is a RSC conflict, we continue and the agent that moved the box
    // first will have his move in the box simulation whereas the other agent will
    // not
    // TODO: decide if it's okay to simulate all box actions or if we just want to
    // simulate
    // the consecutive actions on the box of interest
    // add rest of other agent's consecutive box actions on box
    // this filters unrelated (non-boxData) boxSimulations too
    HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> finishBoxSimulationForAgent(
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            Agent agent, int[] boxData, int startTimestep, int validationTime,
            int[] agentToPosition, HashSet<Integer> agentsAlreadyWent) {
        int agentID = agent.getAgentNumber();
        // get new boxSimualtion that only has boxData box actions up until timepoint
        boxSimulation = filterBoxSimulation(boxData, boxSimulation, startTimestep);
        // loop basically continues the simulation loop but just saves relevant box
        // actions
        for (int relativeTimestep = startTimestep; relativeTimestep < validationTime; relativeTimestep++) {
            boolean agentAlreadyExecutedAction = agentsAlreadyWent.contains(agentID)
                    && relativeTimestep == startTimestep;
            if (agentAlreadyExecutedAction) {
                continue;
            }
            ActionWrapper actionWrapper = getAction(agentID, relativeTimestep, agentToPosition);
            Integer[] positions = getPositions(agentID, actionWrapper, agentToPosition, relativeTimestep);
            Integer boxPosition = positions[2];
            Integer nextBoxPosition = positions[3];

            boolean triedToMoveBox = nextBoxPosition != null;

            boolean triedToMoveBoxOfInterest = triedToMoveBox && (actionWrapper.boxType - 'A') == boxData[0]
                    && actionWrapper.boxIdx == boxData[1];
            if (triedToMoveBoxOfInterest) {
                updateBoxSimulation(relativeTimestep, agent, boxData, nextBoxPosition, boxPosition,
                        boxSimulation, null);
            } else {
                // only update concurrent action on the box, so if not boxaction on box
                // of interest, return
                return boxSimulation;
            }
        }
        return boxSimulation;
    }

    private HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> filterBoxSimulation(int[] boxData,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation, int until) {
        // iterate over all timesteps up to until in boxSimulation and only take
        // boxData-related actions
        HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> newBoxSimulation = new HashMap<>();
        for (int timestep = 0; timestep <= until; timestep++) {
            if (boxSimulation.containsKey(timestep)) {
                for (int boxType : boxSimulation.get(timestep).keySet()) {
                    if (boxType == boxData[0]) {
                        if (!newBoxSimulation.containsKey(timestep)) {
                            newBoxSimulation.put(timestep, new HashMap<>());
                        }
                        // TODO/BUG: be careful here, is this logic okay??
                        // is this what we want?
                        newBoxSimulation.get(timestep).put(boxType, new HashMap<>());
                        for (int boxIdx : boxSimulation.get(timestep).get(boxType).keySet()) {
                            if (boxIdx == boxData[1]) {
                                // TODO: no need to copy the action we get from boxSimulation right? or... yes?
                                // boxAction should be read-only, so should be fine?
                                newBoxSimulation.get(timestep).get(boxType).put(boxIdx,
                                        boxSimulation.get(timestep).get(boxType).get(boxIdx));
                            }
                        }
                    }
                }
            }
        }
        return newBoxSimulation;
    }

    HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> finishBoxSimulationForAgentSet(
            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation,
            HashSet<Integer> agentSet, int[] boxData, int startTimestep, int validationTime,
            int[] agentToPosition, HashSet<Integer> agentsAlreadyWent) {
        for (Integer agentID : agentSet) {
            Agent agent = this.agents[agentID];
            boxSimulation = finishBoxSimulationForAgent(boxSimulation, agent, boxData, startTimestep,
                    validationTime, agentToPosition, agentsAlreadyWent);
        }
        return boxSimulation;
    }

    // refactor validate to use it
    public boolean validate(State[] partialStates, State BDI_current_state) {
        int validationTime = getValidateTime();
        // agentToPosition maps agentID->list of positions for given timestep
        // (fat-agents occupy 2
        // positions)
        int[] agentToPosition = new int[solution.length];
        // TODO: remove agentToPrevPosition, don't need since we only care about just
        // moved which we
        // track
        // with prevPosition2AgentJustMoved
        int[] agentToPrevPosition = new int[solution.length];
        // maps box.getID() string -> box position
        // HashMap of String to Set Integer

        HashMap<String, Integer> boxToPosition = new HashMap<>(); // map boxType to
        // list of positions
        // HashMap<Integer, int[]> posToBox = new HashMap<>(); // map position to
        // boxType and
        // boxIndex
        // maps to multiple boxIds (boxType-boxIdx strings) to avoid overriding when two
        // boxes are
        // in same position
        HashMap<Integer, HashSet<String>> posToBox = new HashMap<>();
        int numAgents = solution.length;

        // time -> boxType -> boxIndex -> SimulationAction; for quick checking of
        // resource conflicts
        HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulation = new HashMap<>();
        // boxType -> boxIndex -> SimulationAction; check who, where, and when given box
        // was moved
        // last
        HashMap<Integer, HashMap<Integer, SimulationAction>> boxToLatestSimAction = new HashMap<>();

        // save initial agent positions
        // TODO (refactor): pull out into own method
        for (int agentID = 0; agentID < numAgents; agentID++) {
            // get first agentToPosition[0] since only 1 agent in partialState
            // TODO: refactor for multiple agents in partialState

            agentToPosition[agentID] = partialStates[agentID].agentPosition[0];
            agentToPrevPosition[agentID] = -1;
        }

        // conflict with box of different color
        // else if (agentColor != boxColor && !box.isFat){

        // // somebody is going for the box - wait constraint
        // if (box.pickupGuy != null){

        // // compute expected time of pickup

        // int timeExpected = solution[box.pickupGuy.getAgentNumber()].size();

        // // try out both versions for improving
        // conflict = new Different_Color_Conflict_Wait(agentID, position, 0,
        // timeExpected,
        // boxSimulation);
        // // conflict = new Different_Color_Conflict_Wait(agentID, position, 0, 1,
        // boxSimulation);

        // // from now on the pickupguy and box is fat - to make the current BDI
        // non-conflicting
        // box.isFat = true;
        // box.pickupGuy.isFat = true;

        // return false;

        // }
        // // nobody is going for the box - assign a new pickup guy
        // else{

        // // return call for help conflict and solve that in the CBS
        // conflict = new Call_For_Help_Conflict(agentID, box, position,
        // relativeTimestep,
        // boxSimulation);
        // return false;

        // }

        // }

        // }

        // save initial box positions
        for (int boxType : this.boxes.keySet()) {
            for (Box box : this.boxes.get(boxType)) {
                int position = BDI_current_state.boxMap[boxType].get(box.boxIndex);
                String id = getBoxIDFromIntInt(boxType, box.boxIndex);
                // save both mappings from box2Pos and pos2Box
                boxToPosition.put(id, position);
                addBoxToPos(id, position, posToBox);
            }
        }

        // simulate for validation
        for (int relativeTimestep = 0; relativeTimestep < validationTime; relativeTimestep++) {
            HashMap<Integer, Integer> position2Agent = new HashMap<>();
            HashMap<Integer, Integer> prevPosition2AgentJustMoved = new HashMap<>();
            HashMap<Integer, SimulationAction> prevPosition2RecentBoxAction = new HashMap<>();
            HashSet<Integer> agentsAlreadyWent = new HashSet<>();

            // simulate agents
            for (int agentID = 0; agentID < numAgents; agentID++) {
                Agent agent = this.agents[agentID];
                ActionWrapper actionWrapper = getAction(agentID, relativeTimestep, agentToPosition);
                agentsAlreadyWent.add(agentID);
                Integer[] positions = getPositions(agentID, actionWrapper, agentToPosition, relativeTimestep);
                Integer agentPosition = positions[0];
                Integer nextAgentPosition = positions[1];
                Integer boxPosition = positions[2];
                Integer nextBoxPosition = positions[3];

                ///////////////////////////////// UPDATE BOX POSITION & PREV POSITION
                ///////////////////////////////// ///////////////////////////////////////

                boolean triedToMoveBox = nextBoxPosition != null;
                if (triedToMoveBox) {
                    Character boxType = actionWrapper.boxType;
                    Integer boxIdx = actionWrapper.boxIdx;
                    String boxID = getBoxIDFromCharInt(boxType, boxIdx);
                    // first check for rsc conflict
                    // then check if box is missing
                    // then add if everything looks good

                    int[] boxData = getBoxData(boxID);
                    // box missing if not a rsc conflict aka if the box was not moved from this
                    // position during this round
                    if (oldSimulationChanged(boxID, boxData, boxPosition, posToBox,
                            relativeTimestep, boxSimulation, boxToLatestSimAction)) {
                        conflict = new ResetPlanConflict(agentID, agentPosition, relativeTimestep + 1);
                        return false;
                    }
                    boolean boxMissing = isBoxMissing(boxID, boxData, boxPosition, posToBox,
                            relativeTimestep, boxSimulation, boxToLatestSimAction);

                    // need to check for this before rsc case
                    if (boxMissing) {
                        // we don't know who moved it, so simulate reamining box actions for all
                        // agents minus current one
                        // NOTE: we do not handle RSC conflicts (or any
                        // conflicts) here, so if there is a RSC conflict,
                        // we continue and the agent that moved the box
                        // first will have his move in the box simulation whereas the other agent
                        // will not
                        HashSet<Integer> agentSet = new HashSet<>();
                        for (int i = 0; i < numAgents; i++) {
                            if (i != agentID) {
                                agentSet.add(i);
                            }
                        }
                        // this filters unrelated (non-boxData) boxSimulations too
                        boxSimulation = finishBoxSimulationForAgentSet(boxSimulation, agentSet, boxData,
                                relativeTimestep, validationTime, agentToPosition,
                                agentsAlreadyWent);
                        Box box = this.boxes.get(boxData[0]).get(boxIdx);
                        conflict = new MissingRSCConflict(agentID, box, agentPosition,
                                relativeTimestep + 1, boxSimulation);
                        return false;
                    } else {
                        SimulationAction otherBoxAction = updateBoxSimulation(relativeTimestep,
                                agent, boxData, nextBoxPosition, boxPosition, boxSimulation,
                                boxToLatestSimAction);
                        boolean triedToMoveSameBoxAtSameTime = otherBoxAction != null;
                        if (triedToMoveSameBoxAtSameTime) {
                            int otherAgentID = otherBoxAction.agent.getAgentNumber();
                            int otherDest = otherBoxAction.newPos;
                            int dest = nextBoxPosition;
                            Agent otherAgent = this.agents[otherAgentID];
                            // add rest of other agent's consecutive box actions on box
                            // this filters unrelated (non-boxData) boxSimulations too
                            boxSimulation = finishBoxSimulationForAgent(boxSimulation, otherAgent, boxData,
                                    relativeTimestep, validationTime, agentToPosition,
                                    agentsAlreadyWent);
                            // create other box simulation for current agent to give to other
                            // agent's constraint
                            HashMap<Integer, HashMap<Integer, HashMap<Integer, SimulationAction>>> boxSimulationOfCurrentAgent = new HashMap<>();
                            updateBoxSimulation(relativeTimestep, agent, boxData, nextBoxPosition,
                                    boxPosition, boxSimulationOfCurrentAgent, null);
                            boxSimulation = finishBoxSimulationForAgent(boxSimulationOfCurrentAgent, agent, boxData,
                                    relativeTimestep, validationTime, agentToPosition,
                                    agentsAlreadyWent);
                            Box box = this.boxes.get(boxData[0]).get(boxData[1]);
                            conflict = new ResourceConflict(agentID, otherAgentID, box, boxPosition,
                                    otherDest, dest, relativeTimestep + 1, boxSimulation,
                                    boxSimulationOfCurrentAgent);
                            return false;
                        } else {
                            // no issues - update box position
                            boxToPosition.put(boxID, nextBoxPosition);
                            addBoxToPos(boxID, nextBoxPosition, posToBox);
                            removeBoxFromPos(boxID, boxPosition, posToBox);
                            SimulationAction action = boxToLatestSimAction.get(boxData[0]).get(boxIdx);
                            prevPosition2RecentBoxAction.put(action.prevPos, action);
                        }
                    }
                }

                ///////////////////////////////// UPDATE AGENT POSITION & PREV POSITION
                ///////////////////////////////// ///////////////////////////////////////
                agentToPrevPosition[agentID] = agentPosition;
                // only track last position if agent has moved this round
                // otherwise we can get follow conflicts even when they don't exist
                if (agentPosition != nextAgentPosition) {
                    prevPosition2AgentJustMoved.put(agentPosition, agentID);
                }
                agentToPosition[agentID] = nextAgentPosition;
                position2Agent.put(nextAgentPosition, agentID);
            }

            ///////////////////////////////// CHECK AGENTS-AGENTS / AGENTS-BOX SIMULTANEOUS
            ///////////////////////////////// CONFLICTS
            ///////////////////////////////// ///////////////////////////////////////
            // check for swap conflicts first (also includes normal agent-followed-box case)
            for (int agentID = 0; agentID < numAgents; agentID++) {
                int agentPosition = agentToPosition[agentID];
                // check for conflict with other agents at same position
                // int prevPosition = agentToPrevPosition[agentID];

                // loop should catch cases where:
                // * agent follows box/agent
                // * agent and agent/box swap
                // NOTE: there is no need to check the other direction, i.e. if an agent
                // followed
                // this agent
                // since we would catch it later in the loop
                if (agentFollowedEntity(agentID, agentPosition, relativeTimestep,
                        prevPosition2AgentJustMoved, prevPosition2RecentBoxAction)) {
                    return false;
                }
            }

            // now check for concurrent conflicts
            for (int agentID = 0; agentID < numAgents; agentID++) {
                int agentPosition = agentToPosition[agentID];

                // AGENT-AGENT TimePlaceConflict
                if (agentRanIntoAgent(agentID, agentPosition, relativeTimestep, position2Agent)) {
                    return false;
                }

                // or agentRanIntoBox or box ran into agent
                if (agentRanIntoBox(agentID, agentPosition, relativeTimestep, posToBox,
                        boxSimulation, boxToLatestSimAction)) {
                    return false;
                }
            }

            // TODO: maybe this box-swap check should come after agent swap check and before
            // the
            // agent concurrency check above
            // but I don't think so and haven't seen issues where it would be necessary

            // check for box-box swap conflicts first (also includes normal
            // box-followed-agent case)
            for (HashMap.Entry<String, Integer> entry : boxToPosition.entrySet()) {
                String boxID = entry.getKey();
                int boxPos = entry.getValue();
                // get box type and idx
                int[] boxData = getBoxData(boxID);
                Box box = this.boxes.get(boxData[0]).get(boxData[1]);

                // loop should catch cases where:
                // * box follows box/agent
                // * box and box swap
                // * BUT!! the case where box and agent swap gets detected above in
                // agentFollowedEntity -- it can be caught in this kind of loop but it's caught
                // first above
                if (boxFollowedEntity(box, boxPos, boxSimulation, relativeTimestep,
                        prevPosition2AgentJustMoved, prevPosition2RecentBoxAction)) {
                    return false;
                }
            }

            for (HashMap.Entry<String, Integer> entry : boxToPosition.entrySet()) {
                String boxID = entry.getKey();
                int boxPos = entry.getValue();
                // get box type and idx
                int[] boxData = getBoxData(boxID);
                Box box = this.boxes.get(boxData[0]).get(boxData[1]);
                // case where box-agent concurrent conflict is handled in above agent loop
                if (boxRanIntoBox(box, boxID, boxPos, relativeTimestep, posToBox, boxSimulation,
                        boxToLatestSimAction, agentToPosition)) {
                    return false;
                }
            }

        } // end for loop over timesteps

        return true;
    }// end validate method
}
// end class
