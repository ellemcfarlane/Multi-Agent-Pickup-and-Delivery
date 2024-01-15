package com.smac.mas.searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.smac.mas.mapf.Agent;
import com.smac.mas.mapf.Box;
import com.smac.mas.mapf.Constraint;
import com.smac.mas.mapf.Goal;
import com.smac.mas.mapf.Goal.GoalType;
import com.smac.mas.mapf.Teams;

import com.smac.mas.mapf.*;

public class State {
    /*
     * array of size numAgents consisting of node id where an agent is located
     */
    public final int[] agentPosition;

    public static Agent agentInPartialState = null; // is set only in low level partial state search
    public boolean wasGeneratedUsingMovingBox = false; // just a marker if a state was generated using push / pull
                                                       // actions - just for heuristics

    /*
     * array of size 10 where label2index[i] is the index of agent with label i in
     * agentPosition
     * label2index[i] is -1 when no agent of label i is present in this state
     */
    public final int[] label2index;

    /*
     * agentLabel[i] is the label of the agent at agentPosition[i]
     */
    public final int[] agentLabel;

    /*
     * maps box type to list of corresponding node id for example, boxMap[0]
     * contains the list of
     * node ids where box type 'A' is located
     */
    public ArrayList<Integer>[] boxMap;

    /*
     * maps node id to box type at node
     */
    public final HashMap<Integer, Character> node2box;
    public final HashMap<Integer, Integer> node2boxIdx;

    /*
     * maps node id to agent type at node
     */
    public final HashMap<Integer, Character> node2agent;

    /*
     * maps node id to goal type at node
     */
    public final HashMap<Integer, Character> node2goal;

    /*
     * array of size GOAL_ARRAY_SIZE=43 where goals[i] is the list of node ids where
     * goal type
     * (char)(i + '0') is located for example goals[17] contains the list of node
     * ids where goal
     * type 'A' is located
     */
    public final ArrayList<Integer>[] goals;

    /*
     * connected components of the underlying graph where all goal cells are assumed
     * to be
     * walls(used in goal ordering)
     */
    public static DisjointSet connected_components;

    public final State parent;

    public static Teams teams;

    public final ActionWrapper[] jointAction;

    private final int g;

    public final int numAgents; // num of agents present in this state

    private int hash = 0;

    public static boolean[][] walls;

    public static int[] agentInit; // maps agent to initial position

    public static ArrayList<Integer>[] color2Agent; // maps color to agent

    public static Color[] agentColors; // maps agent to color

    public static Color[] boxColors; // maps box to color

    public static ArrayList<Character>[] color2Box;

    public static GraphUtils graph; // stores the graph

    private static final Random RNG = new Random(1);

    // public static HashSet<Constraint> constraints = null;

    // agentID -> time -> nodeID -> set of constraints
    public static HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>> constraints = null;
    // maps agent->time->place->constraints
    public static HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Constraint>>>> resourceConstraints = null;
    public static final int GOAL_ARRAY_SIZE = 43;

    public static final int CHARSET_ARRAY_SIZE = 26;

    public static final int maxNumAgents = 10;

    /*
     * MoveActions[i] action applied at a vertex v(agent location) moves agent along
     * the edge (v,
     * v.neighbors[i])
     */
    public static final Action MoveActions[] = { Action.Move0, Action.Move1, Action.Move2, Action.Move3 };

    /*
     * PushActions[i][j] action applied at a vertex v(agent location) moves agent
     * along the edge (v,
     * v.neighbors[i]) and pushes box along the edge (v.neighbors[i],
     * v.neighbors[i].neighbors[j])
     */
    public static final Action PushActions[][] = { { Action.Push00, Action.Push01, Action.Push02, Action.Push03 },
            { Action.Push10, Action.Push11, Action.Push12, Action.Push13 },
            { Action.Push20, Action.Push21, Action.Push22, Action.Push23 },
            { Action.Push30, Action.Push31, Action.Push32, Action.Push33 } };

    /*
     * PullActions[i][j] action applied at a vertex v(agent location) moves agent
     * along the edge (v,
     * v.neighbors[i]) and pulls the box from v.neighbors[j] to v
     */
    public static final Action PullActions[][] = { { Action.Pull00, Action.Pull01, Action.Pull02, Action.Pull03 },
            { Action.Pull10, Action.Pull11, Action.Pull12, Action.Pull13 },
            { Action.Pull20, Action.Pull21, Action.Pull22, Action.Pull23 },
            { Action.Pull30, Action.Pull31, Action.Pull32, Action.Pull33 } };

    public State(int[] agentRows, int[] agentCols, boolean[][] walls, char[][] boxes,
            char[][] goals, Color[] boxColors, Color[] agentColors, int numNodes) throws Exception {

        this.numAgents = agentRows.length;
        State.graph = new GraphUtils(walls, goals, numNodes);
        GraphUtils.filter_bottlenecks();
        agentPosition = new int[numAgents];
        agentInit = new int[numAgents];
        this.jointAction = null;
        this.g = 0;
        this.parent = null;
        this.node2agent = new HashMap<Integer, Character>();

        for (int i = 0; i < numAgents; ++i) {
            int node_id = GraphUtils.coordinates_to_node[agentRows[i]][agentCols[i]];
            agentPosition[i] = node_id;
            agentInit[i] = node_id;
            this.node2agent.put(node_id, (char) ('0' + i));
        }

        State.agentColors = agentColors;
        int numColors = Color.values().length;
        State.color2Agent = new ArrayList[numColors];
        State.color2Box = new ArrayList[numColors];
        for (int i = 0; i < numColors; i++) {
            State.color2Agent[i] = new ArrayList<Integer>();
            State.color2Box[i] = new ArrayList<Character>();
        }

        for (int i = 0; i < numAgents; i++) {
            State.color2Agent[Color.color2Int(agentColors[i])].add(i);
        }

        State.walls = walls;
        State.boxColors = boxColors;
        this.agentLabel = new int[numAgents];
        this.label2index = new int[maxNumAgents];
        for (int i = 0; i < maxNumAgents; i++)
            this.label2index[i] = -1;

        for (int i = 0; i < numAgents; i++) { // assume that labels are 0,1,2,3,...
            this.agentLabel[i] = i;
            this.label2index[i] = i;
        }
        this.node2box = new HashMap<Integer, Character>();
        this.node2boxIdx = new HashMap<Integer, Integer>();
        this.boxMap = (ArrayList<Integer>[]) new ArrayList[CHARSET_ARRAY_SIZE];
        for (int i = 0; i < CHARSET_ARRAY_SIZE; i++) {
            this.boxMap[i] = new ArrayList<Integer>();
        }

        this.goals = (ArrayList<Integer>[]) new ArrayList[GOAL_ARRAY_SIZE];
        this.node2goal = new HashMap<>();
        for (int i = 0; i < GOAL_ARRAY_SIZE; i++) {
            this.goals[i] = new ArrayList<Integer>();
        }

        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes[0].length; j++) {
                if ('A' <= boxes[i][j] && boxes[i][j] <= 'Z') {
                    if (GraphUtils.coordinates_to_node[i][j] == -1) {
                        throw new Exception("Error parsing data");
                    }
                    var boxPosition = GraphUtils.coordinates_to_node[i][j];
                    var boxType = boxes[i][j];
                    this.boxMap[boxType - 'A'].add(boxPosition);
                    this.node2box.put(boxPosition, boxType);
                    int boxIdx = this.boxMap[boxType - 'A'].size() - 1;
                    this.node2boxIdx.put(boxPosition, boxIdx);
                    int index = Color.color2Int(State.boxColors[boxType - 'A']);
                    boolean hasBox = false;
                    for (char box : State.color2Box[index]) {
                        if (box == boxType) {
                            hasBox = true;
                            break;
                        }
                    }
                    if (!hasBox)
                        State.color2Box[index].add(boxType);
                }

                if (('0' <= goals[i][j] && goals[i][j] <= '9')
                        || ('A' <= goals[i][j] && goals[i][j] <= 'Z')) {
                    this.goals[goals[i][j] - '0'].add(GraphUtils.coordinates_to_node[i][j]);
                    this.node2goal.put(GraphUtils.coordinates_to_node[i][j], goals[i][j]);
                }
            }
        }

        State.connected_components = GraphUtils.get_connected_components(this);
        State.teams = new Teams(agentColors);
        // ArrayList<ArrayList<Integer>>[] X =
        // GraphUtils.get_disjoint_goal_ordering_constraints(this, true);
        // for (int i = 0; i < X.length; i++) {
        // System.err.println("goal: " + GraphUtils.temp.get(i));
        // for (int j = 0; j < X[i].size(); j++) {
        // for (int k = 0; k < X[i].get(j).size(); k++) {
        // System.err.print(this.node2goal.get(X[i].get(j).get(k)) + " ");
        // }
        // System.err.println();
        // }
        // System.err.println("............");
        // }

        // this.printConnectedComponents();

        // for(int i=0; i < this.connected_components.size(); i++){
        // System.err.println("connected component[" + i + "]");
        // for(int j=0; j < this.connected_components.get(i).size(); j++){
        // System.err.print(this.connected_components.get(i).get(j) + " ");
        // }
        // System.err.println();
        // }

        // this.printBottlenecks();
        // HashSet<Color> colors = new HashSet<Color>(Arrays.asList(agentColors));
        // Color[] array = colors.toArray(new Color[colors.size()]);
        // for(Color c : array){
        // System.err.println(c);
        // }
        // Color[] array = {Color.Blue, Color.Red, Color.Purple};
        // Color[] array = {Color.Blue};
        // CostStruct c = GraphUtils.get_distance_matrix(this, array);
        // c.printCost();
    }

    /*
     * copy constructor
     */
    public State(State state) {
        this.numAgents = state.numAgents;
        this.agentPosition = state.agentPosition.clone();
        this.node2agent = new HashMap<Integer, Character>(state.node2agent);
        this.node2box = new HashMap<Integer, Character>(state.node2box);
        this.node2boxIdx = new HashMap<Integer, Integer>(state.node2boxIdx);
        this.node2goal = new HashMap<Integer, Character>(state.node2goal);
        this.boxMap = new ArrayList[CHARSET_ARRAY_SIZE];
        this.label2index = state.label2index.clone();
        this.agentLabel = state.agentLabel.clone();
        for (int i = 0; i < CHARSET_ARRAY_SIZE; i++) {
            this.boxMap[i] = new ArrayList<>(state.boxMap[i]);
        }
        // this.connected_components = new DisjointSet(state.connected_components);
        this.goals = state.goals.clone();
        this.parent = state.parent;
        if (state.jointAction == null)
            this.jointAction = null;
        else
            this.jointAction = state.jointAction.clone();
        this.g = state.g;
    }

    // creates state with updated agent and box positions due to jointAction
    private State(State parent, ActionWrapper[] jointAction, int[] agentDestination,
            int[] boxDestination) {

        this.parent = parent;
        this.jointAction = jointAction.clone();
        this.g = parent.g + 1;
        this.numAgents = parent.numAgents;
        this.goals = parent.goals; // no need to clone as goalofParent == goalofChild
        this.node2goal = parent.node2goal;
        this.connected_components = parent.connected_components;
        this.agentLabel = parent.agentLabel; // no need to clone as labelofParent == labelofChild
        this.label2index = parent.label2index;
        this.agentPosition = new int[numAgents];
        this.node2agent = new HashMap<Integer, Character>();
        this.node2box = new HashMap<Integer, Character>(parent.node2box);
        this.node2boxIdx = new HashMap<Integer, Integer>(parent.node2boxIdx);

        for (int i = 0; i < numAgents; i++) {
            this.agentPosition[i] = agentDestination[i];
            this.node2agent.put(this.agentPosition[i], (char) (this.agentLabel[i] + '0'));
        }

        this.boxMap = new ArrayList[CHARSET_ARRAY_SIZE];
        for (int i = 0; i < CHARSET_ARRAY_SIZE; i++) {
            this.boxMap[i] = new ArrayList<>(parent.boxMap[i]);
        }

        for (int i = 0; i < numAgents; i++) {
            Action action = jointAction[i].action;
            if (action.value >= 1) {
                int from, to;
                if (action.value == 2) {
                    int neighbor = action.pull_type;
                    from = GraphUtils.node_list[boxDestination[i]].neighbor.get(neighbor);
                    to = boxDestination[i];
                } else {
                    from = agentDestination[i];
                    to = boxDestination[i];
                }

                char box_type = parent.node2box.get(from);
                int box_idx = parent.node2boxIdx.get(from);

                this.node2box.remove(from);
                this.node2boxIdx.remove(from);
                this.node2box.put(to, box_type);
                this.node2boxIdx.put(to, box_idx);

                var temp = this.boxMap[box_type - 'A'];
                for (int j = 0; j < temp.size(); j++)
                    if (temp.get(j) == from) {
                        temp.set(j, to);
                        break;
                    }
            }
        }
    }

    private State(int[] agentPosition, HashMap<Integer, Character> node2agent,
            ArrayList<Integer>[] boxMap, HashMap<Integer, Character> node2box,
            HashMap<Integer, Integer> node2boxIdx, ArrayList<Integer>[] goals,
            HashMap<Integer, Character> node2goal, int[] agentLabel, int[] label2index) {
        this.numAgents = agentPosition.length;
        this.agentPosition = agentPosition;
        this.boxMap = boxMap;
        this.node2box = node2box;
        this.node2boxIdx = node2boxIdx;
        this.node2agent = node2agent;
        this.node2goal = node2goal;
        this.agentLabel = agentLabel;
        this.label2index = label2index;
        this.goals = goals;
        this.jointAction = null;
        this.parent = null;
        this.g = 0;
        // this.connected_components = GraphUtils.get_connected_components(this);
    }

    private static void step(int i, int current_node, ActionWrapper actionWrapper,
            int[] agentDestination, int[] boxDestination) {

        Action action = actionWrapper.action;
        switch (action.value) {
            case -1:
                agentDestination[i] = current_node;
                boxDestination[i] = -1;
                break;
            case 0:
                agentDestination[i] = GraphUtils.node_list[current_node].neighbor.get(action.agent_edge_id);
                boxDestination[i] = -1;
                break;
            case 1:
                int next_neighbor = GraphUtils.node_list[current_node].neighbor.get(action.agent_edge_id);
                agentDestination[i] = next_neighbor;
                boxDestination[i] = GraphUtils.node_list[next_neighbor].neighbor.get(action.box_edge_id);
                break;
            case 2:
                boxDestination[i] = current_node;
                agentDestination[i] = GraphUtils.node_list[current_node].neighbor.get(action.agent_edge_id);
                break;
            default:
                break;
        }
    }

    // Note that duplicate agents are not allowed
    public State[] partialStates(Agent[] agents, HashMap<Integer, ArrayList<Box>> boxes) {
        State[] partialStates = new State[numAgents];

        // set goals for all agents in 'agents'
        for (Agent agent : agents) {
            int i = agent.getAgentNumber();

            // create new arrays and set agents to his position
            HashMap<Integer, Character> new_node2box = new HashMap<>();
            HashMap<Integer, Integer> new_node2boxIdx = new HashMap<>();
            HashMap<Integer, Character> new_node2agent = new HashMap<>();
            HashMap<Integer, Character> new_node2goal = new HashMap<>();
            new_node2agent.put(this.agentPosition[i], (char) (i + '0'));
            int[] new_agentPosition = { this.agentPosition[i] };
            int[] new_agentLabel = { this.agentLabel[i] };
            int[] new_label2index = new int[maxNumAgents];
            for (int j = 0; j < maxNumAgents; j++)
                new_label2index[j] = -1;
            new_label2index[this.agentLabel[i]] = 0;
            ArrayList<Integer>[] new_boxMap = new ArrayList[CHARSET_ARRAY_SIZE];
            for (int j = 0; j < CHARSET_ARRAY_SIZE; j++)
                new_boxMap[j] = new ArrayList<Integer>();
            ArrayList<Integer>[] new_goals = new ArrayList[GOAL_ARRAY_SIZE];
            for (int j = 0; j < GOAL_ARRAY_SIZE; j++)
                new_goals[j] = new ArrayList<Integer>();
            // place boxes of the same color
            Color colorOfAgent = State.agentColors[i];
            int intColor = Color.color2Int(colorOfAgent);
            for (char boxChar : color2Box[intColor]) {
                ArrayList<Box> boxesOfType = boxes.get(boxChar - 'A');
                for (Box box : boxesOfType) {
                    // TODO: should not put box that are FAT - we dont want
                    // other agents to move them
                    // if (box.pickupGuy == null
                    // || box.pickupGuy.getAgentNumber() == agent.getAgentNumber()) {
                    int currentBoxPosition = this.boxMap[box.boxType].get(box.boxIndex);
                    new_boxMap[box.boxType].add(currentBoxPosition);
                    new_node2box.put(currentBoxPosition, (char) (box.boxType + 'A'));
                    new_node2boxIdx.put(currentBoxPosition, box.boxIndex);
                    // node2
                    // }
                }
            }

            // set goals
            Goal agentGoal = agent.getCurrentGoal();

            if (agentGoal.type == GoalType.REACH_AGENT_GOAL) {
                new_goals[agentLabel[i]].add(this.goals[agentLabel[i]].get(0));
                new_node2goal.put(this.goals[agentLabel[i]].get(0), (char) (agentLabel[i] + '0'));
            } else if (agentGoal.type == GoalType.NO_GOAL) {
                // do nothing
            } else if (agentGoal.type == GoalType.MOVE_BOX) {
                Box box = agentGoal.box;
                new_goals[box.boxType + ('A' - '0')].add(box.getGoalNode()); // set its box to the
                                                                             // goal node
                new_node2goal.put(box.getGoalNode(), (char) (box.boxType + 'A')); // set its box to
                                                                                  // the goal node
                // int currentBoxPosition = this.boxMap[box.boxType].get(box.boxIndex);
                // new_boxMap[box.boxType].add(currentBoxPosition);
                // new_node2box.put(currentBoxPosition, (char) (box.boxType + 'A'));
            } else if (agentGoal.type == GoalType.REACH_BOX) { // probably add here (||
                                                               // agentGoal.type ==
                                                               // GoalType.HELP_WITH_BOX) + in the
                                                               // lowlevel doesnt need the reached
                                                               // box special case
                // Box box = agentGoal.box;
                // int currentBoxPosition = this.boxMap[box.boxType].get(box.boxIndex);
                // new_boxMap[box.boxType].add(currentBoxPosition);
                // new_node2box.put(currentBoxPosition, (char) (box.boxType + 'A'));
            } else if (agentGoal.type == GoalType.HELP_WITH_BOX) {
                // TODO
            }

            partialStates[i] = new State(new_agentPosition, new_node2agent, new_boxMap,
                    new_node2box, new_node2boxIdx, new_goals, new_node2goal, new_agentLabel,
                    new_label2index);
        }

        return partialStates;
    }

    public int g() {
        return this.g;
    }

    public boolean isGoalState() {

        for (int goalLabel = 0; goalLabel < GOAL_ARRAY_SIZE; goalLabel++) {
            for (int node_id : this.goals[goalLabel]) {

                // box goal
                if (goalLabel + '0' > '9') {
                    if (!node2box.containsKey(node_id)
                            || node2box.get(node_id) != (char) (goalLabel + '0')) {
                        return false;
                    }

                    // agent goal
                } else {
                    if (!node2agent.containsKey(node_id)
                            || node2agent.get(node_id) != (char) (goalLabel + '0')) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /*
     * Overridden method isGoalState with a partial state - only 1 agent -
     * currentAgent Check if his
     * goal is satisfied
     * 
     */
    public boolean isGoalState(Agent currentAgent) {

        // goal for partial state
        Goal goal = currentAgent.getCurrentGoal();
        int node_id;

        /* GET NODE ID OF THE GOAL */
        if (goal.type == GoalType.MOVE_BOX) {
            int boxLabel = goal.box.boxType + ('A' - '0');
            // BUG: question by Elle: should this always be index 0??? PartialState can have
            // many
            // boxes of same type
            // no?
            node_id = this.goals[boxLabel].get(0);
        } else if (goal.type == GoalType.REACH_BOX || goal.type == GoalType.HELP_WITH_BOX) {

            Box box = goal.box;
            // BUG: question by Elle: why/how is it that when we have a boxgoal sometimes
            // this.goals is completely empty????
            node_id = this.boxMap[box.boxType].get(box.boxIndex);

        } else if (goal.type == GoalType.REACH_AGENT_GOAL) {
            int agentNum = currentAgent.getAgentNumber();
            node_id = this.goals[agentNum].get(0);
        }
        // no goal
        else {
            node_id = -1;
        }

        /* ACT BASED ON GOAL TYPE */

        if (goal.type == GoalType.REACH_BOX || goal.type == GoalType.HELP_WITH_BOX) {

            ArrayList<Integer> neighbors = GraphUtils.node_list[node_id].neighbor;

            // just check the neighboring positions
            if (isInNeighborHood(neighbors, currentAgent.getAgentNumber())) {
                return true;
            }

        }

        else if (goal.type == GoalType.NO_GOAL) {
            // elle write code here
            // help with box will basicly be reach box but 'reaching' doesnt need to be
            // satisfied -
            // you can ignore it for now if you want
            // (first need to add validation with fat agent to make this work + isConstraint
            // with
            // fat agent)
            // NOTE: to make it work with lsc validation you need to check for the time
            // constraint
            // in all functions - not just this one
            return true;
        }

        // move box
        else if (goal.type == GoalType.MOVE_BOX) {

            int label = goal.box.boxType + 'A';
            if (node2box.containsKey(node_id) && node2box.get(node_id) == (char) label) {
                return true;
            }
        }

        // reach agent goal
        else if (goal.type == GoalType.REACH_AGENT_GOAL) {
            if (node2agent.containsKey(node_id)
                    && node2agent.get(node_id) == (char) (currentAgent.getAgentNumber() + '0')) {
                return true;
            }
        }

        return false;
    }

    /*
     * Checks if agent (for example 0 is for agent '0') is in the neighborhood nodes
     */
    private boolean isInNeighborHood(ArrayList<Integer> neighbors, int agent) {

        for (int neighborID : neighbors) {

            if (!node2agent.containsKey(neighborID)) { // nobody at this node
                continue;
            }

            if (node2agent.get(neighborID) == (char) (agent + '0')) { // is correct agent around the
                                                                      // node
                return true;
            }
        }

        return false;
    }

    public State step(ActionWrapper[] jointAction) {
        int[] agentDestination = new int[numAgents];
        int[] boxDestination = new int[numAgents];
        for (int i = 0; i < numAgents; i++) {
            int current_node = this.agentPosition[i];
            step(i, current_node, jointAction[i], agentDestination, boxDestination);
        }
        return new State(this, jointAction, agentDestination, boxDestination);
    }

    // checks if any agent is constrained at its current time-space (and therefore
    // should not be
    // there)
    public boolean isValid() {
        Node currentNode = null;
        int agentID = -1;
        for (int i = 0; i < numAgents; i++) {
            currentNode = GraphUtils.node_list[agentPosition[i]];
            agentID = this.agentLabel[i];
            if (isConstrainedNow(agentID, currentNode.id)) {
                return false;
            }
        }
        return true;
    }

    public String getBoxIDAtNode(int node) {
        if (node2box.containsKey(node)) {
            return node2box.get(node) + "_" + node2boxIdx.get(node);
        }
        return null;
    }

    // TODO (fac): update isValid to handle more than one Agent object for hybrid
    // CBS!
    // returns false if agent or box starts in constrained spot in initial state
    public boolean isValid(Agent currentAgent) {
        int agentID = currentAgent.getAgentNumber();
        // only 1 agent in partial state (for now) so index == 0
        Node currentNode = GraphUtils.node_list[agentPosition[0]];
        // agent or box started at constrained spot
        if (isConstrainedNow(agentID, currentNode.id) || isConstrainedBoxAtConstraintNow(agentID)) {
            return false;
        }
        return true;
    }

    // DO NOT USE ANYMORE! Does not check for fat-agent conflicts
    public ArrayList<State> getExpandedStates() {
        ArrayList<ActionWrapper>[] agentActions = new ArrayList[numAgents];
        ArrayList<Integer>[] agentDestination = new ArrayList[numAgents];
        ArrayList<Integer>[] boxDestination = new ArrayList[numAgents];
        ArrayList<Integer>[] boxPosition = new ArrayList[numAgents];

        for (int i = 0; i < numAgents; i++) {
            Node current_node = GraphUtils.node_list[agentPosition[i]];
            int agentID = this.agentLabel[i];
            agentActions[i] = new ArrayList<ActionWrapper>();
            agentDestination[i] = new ArrayList<Integer>();
            boxDestination[i] = new ArrayList<Integer>();
            boxPosition[i] = new ArrayList<Integer>();

            // generate NoOp if agent allowed to be in same space at next time step
            if (!isConstrainedNext(agentID, current_node.id)) {
                agentActions[i].add(new ActionWrapper(Action.NoOp));
                agentDestination[i].add(current_node.id);
                boxDestination[i].add(-1);
                boxPosition[i].add(-1);
            }

            // generate agent move
            for (int move = 0; move < current_node.neighbor.size(); move++) {
                // check if neighbor node is free to move to
                if (!node2box.containsKey(current_node.neighbor.get(move))
                        && !node2agent.containsKey(current_node.neighbor.get(move))
                        && !isConstrainedNext(agentID, current_node.neighbor.get(move))) {
                    agentActions[i].add(new ActionWrapper(MoveActions[move]));
                    agentDestination[i].add(current_node.neighbor.get(move));
                    boxDestination[i].add(-1);
                    boxPosition[i].add(-1);
                }
            }
            // generate pushes
            for (int agent_move = 0; agent_move < current_node.neighbor.size(); agent_move++) {
                int next = current_node.neighbor.get(agent_move);
                if (node2box.containsKey(next)) {
                    int box = node2box.get(next) - 'A';
                    if (boxColors[box] == agentColors[i]) {
                        Node next_node = GraphUtils.node_list[next];
                        for (int box_move = 0; box_move < next_node.neighbor.size(); box_move++) {
                            int next2next = next_node.neighbor.get(box_move);
                            if (!node2agent.containsKey(next2next)
                                    && !node2box.containsKey(next2next)
                                    && !isConstrainedNext(agentID, next2next)) {
                                agentActions[i]
                                        .add(new ActionWrapper(PushActions[agent_move][box_move]));
                                agentDestination[i].add(next);
                                boxDestination[i].add(next2next);
                                boxPosition[i].add(next);
                            }
                        }
                    }
                }
            }

            // generate pulls
            for (int box_move = 0; box_move < current_node.neighbor.size(); box_move++) {
                int next = current_node.neighbor.get(box_move);
                if (node2box.containsKey(next)) {
                    int box = node2box.get(next) - 'A';
                    if (boxColors[box] == agentColors[i]) {
                        for (int agent_move = 0; agent_move < current_node.neighbor
                                .size(); agent_move++) {
                            int second_next = current_node.neighbor.get(agent_move);
                            if (!node2box.containsKey(second_next)
                                    && !node2agent.containsKey(second_next)
                                    && !isConstrainedNext(agentID, second_next)) {
                                agentActions[i]
                                        .add(new ActionWrapper(PullActions[agent_move][box_move]));
                                agentDestination[i].add(second_next);
                                boxDestination[i].add(current_node.id);
                                boxPosition[i].add(next);
                            }
                        }
                    }
                }
            }
        }

        ActionWrapper[] jointAction = new ActionWrapper[numAgents];
        int[] jointAgentDestination = new int[numAgents];
        int[] jointBoxDestination = new int[numAgents];
        int[] jointBoxPosition = new int[numAgents];
        int[] actionsPermutation = new int[numAgents];
        ArrayList<State> expandedStates = new ArrayList<>(16);

        for (int i = 0; i < numAgents; i++) {
            actionsPermutation[i] = 0;
        }

        // do not return any expanded states if
        // any agent has no possible moves (cannot even NoOp)
        // because then any jointAction will be invalid
        boolean allAgentsHaveOneMove = false;
        for (int i = 0; i < numAgents; i++) {
            if (agentActions[i].size() >= 1) {
                allAgentsHaveOneMove = true;
            } else {
                allAgentsHaveOneMove = false;
                break;
            }
        }

        while (allAgentsHaveOneMove) {
            for (int agent = 0; agent < numAgents; agent++) {
                jointAction[agent] = agentActions[agent].get(actionsPermutation[agent]);
                jointAgentDestination[agent] = agentDestination[agent].get(actionsPermutation[agent]);
                jointBoxDestination[agent] = boxDestination[agent].get(actionsPermutation[agent]);
                jointBoxPosition[agent] = boxPosition[agent].get(actionsPermutation[agent]);
            }

            // isConflicting from original server definition (aka 2 agents moving into same
            // cell)
            // TODO (elle): redundant if using CBS, so maybe we can remove check for
            // CBS-case?
            if (!this.isConflicting(jointAction, jointAgentDestination, jointBoxDestination,
                    jointBoxPosition)) {
                expandedStates.add(
                        new State(this, jointAction, jointAgentDestination, jointBoxDestination));
            }

            // Advance permutation
            boolean done = false;
            for (int agent = 0; agent < numAgents; agent++) {
                if (actionsPermutation[agent] < agentActions[agent].size() - 1) {
                    ++actionsPermutation[agent];
                    break;
                } else {
                    actionsPermutation[agent] = 0;
                    if (agent == numAgents - 1) {
                        done = true;
                    }
                }
            }
            // Last permutation?
            if (done) {
                break;
            }
        }
        Collections.shuffle(expandedStates, State.RNG);
        return expandedStates;
    }

    public static char reverse(char c) {
        switch (c) {
            case 'E':
                return 'W';
            case 'W':
                return 'E';
            case 'N':
                return 'S';
            case 'S':
                return 'N';
            default:
                break;
        }
        return ' ';
    }

    public static String getActionStr(Node current_node, Action action) {
        String actionStr = "NoOp";
        switch (action.value) {
            case -1:
                break;
            case 0:
                char direction = current_node.edge2Dir.get(action.agent_edge_id);
                actionStr = "Move(" + direction + ")";
                break;
            case 1:
                char direction_1 = current_node.edge2Dir.get(action.agent_edge_id);
                Node next_neighbor = GraphUtils.node_list[current_node.neighbor.get(action.agent_edge_id)];
                char direction_2 = next_neighbor.edge2Dir.get(action.box_edge_id);
                actionStr = "Push(" + direction_1 + "," + direction_2 + ")";
                break;
            case 2:
                char direction_1_ = current_node.edge2Dir.get(action.agent_edge_id);
                char direction_2_ = reverse(current_node.edge2Dir.get(action.box_edge_id));
                actionStr = "Pull(" + direction_1_ + "," + direction_2_ + ")";
                break;
            default:
                break;
        }
        return actionStr;
    }

    // LATEST VERSION: this is latest version of getExpandedStates that checks for
    // fat-agent
    // constraints
    // so it should be the only one used now
    public ArrayList<State> getExpandedStates(Agent currentAgent) {
        ArrayList<ActionWrapper>[] agentActions = new ArrayList[numAgents];
        ArrayList<Integer>[] agentDestination = new ArrayList[numAgents];
        ArrayList<Integer>[] boxDestination = new ArrayList[numAgents];
        ArrayList<Integer>[] boxPosition = new ArrayList[numAgents];

        for (int i = 0; i < numAgents; i++) {
            Node current_node = GraphUtils.node_list[agentPosition[i]];
            int agentID = this.agentLabel[i];
            int agentPos = current_node.id;
            agentActions[i] = new ArrayList<ActionWrapper>();
            agentDestination[i] = new ArrayList<Integer>();
            boxDestination[i] = new ArrayList<Integer>();
            boxPosition[i] = new ArrayList<Integer>();

            // generate NoOp if agent and any box allowed to be in same space at next time
            // step
            // this just checks if any of the boxes is sitting at a constrained spot and
            // will
            // violate a constraint
            // at next timestep unless moved
            boolean hasConstrainedBoxNext = isConstrainedBoxAtConstraintNext(agentID);
            if (!isConstrainedNext(agentID, current_node.id) && !hasConstrainedBoxNext) {
                Action action = Action.NoOp;
                agentActions[i].add(new ActionWrapper(action, agentPos, agentPos,
                        getActionStr(current_node, action)));
                agentDestination[i].add(agentPos);
                boxDestination[i].add(-1);
                boxPosition[i].add(-1);
            }

            // generate agent move, but don't let agent move away from box if it is
            // constrained at
            // it's current position
            // at the next timestep, i.e. force the agent to do push/pull
            for (int move = 0; move < current_node.neighbor.size(); move++) {
                int agentDest = current_node.neighbor.get(move);
                // check if neighbor node is free to move to
                if (!node2box.containsKey(current_node.neighbor.get(move))
                        && !node2agent.containsKey(current_node.neighbor.get(move))
                        && !isConstrainedNext(agentID, current_node.neighbor.get(move))
                        && !hasConstrainedBoxNext) {
                    Action action = MoveActions[move];
                    agentActions[i].add(new ActionWrapper(action, agentPos, agentDest,
                            getActionStr(current_node, action)));
                    agentDestination[i].add(agentDest);
                    boxDestination[i].add(-1);
                    boxPosition[i].add(-1);
                }
            }
            // TODO IMPORTANT: for box constraints check if box constraint matches given box
            // trying
            // to push/pull OR does it not matter bc if any box is constrained at this
            // position, no
            // box should be moved here?

            // generate pushes
            for (int agent_move = 0; agent_move < current_node.neighbor.size(); agent_move++) {
                int boxPos = current_node.neighbor.get(agent_move);
                int agentDest = boxPos;
                if (!isConstrainedNext(agentID, agentDest)) {
                    if (node2box.containsKey(boxPos)) {
                        Character boxType = node2box.get(boxPos);
                        int boxIdx = node2boxIdx.get(boxPos);
                        int normalizedType = node2box.get(boxPos) - 'A';
                        boolean hasRscConstraint = hasRSCConstraint(agentID, boxPos, this.g() + 1,
                                normalizedType, boxIdx);
                        if (!hasRscConstraint
                                && boxColors[normalizedType] == agentColors[agentID]) {
                            Node next_node = GraphUtils.node_list[agentDest];
                            for (int box_move = 0; box_move < next_node.neighbor
                                    .size(); box_move++) {
                                int boxDest = next_node.neighbor.get(box_move);
                                if (!node2agent.containsKey(boxDest)
                                        && !node2box.containsKey(boxDest)
                                        && !isConstrainedNext(agentID, boxDest)) {
                                    Action action = PushActions[agent_move][box_move];
                                    agentActions[i].add(new ActionWrapper(action, agentPos,
                                            agentDest, boxPos, boxDest, boxType, boxIdx,
                                            getActionStr(current_node, action)));
                                    agentDestination[i].add(agentDest);
                                    boxDestination[i].add(boxDest);
                                    boxPosition[i].add(boxPos);
                                }
                            }
                        }
                    }
                }
            }

            // generate pulls
            for (int box_move = 0; box_move < current_node.neighbor.size(); box_move++) {
                int boxPos = current_node.neighbor.get(box_move);
                int boxDest = agentPos;
                if (!isConstrainedNext(agentID, boxDest)) {
                    if (node2box.containsKey(boxPos)) {
                        Character boxType = node2box.get(boxPos);
                        int boxIdx = node2boxIdx.get(boxPos);
                        int normalizedType = node2box.get(boxPos) - 'A';
                        boolean hasRscConstraint = hasRSCConstraint(agentID, boxPos, this.g() + 1,
                                normalizedType, boxIdx);
                        if (!hasRscConstraint
                                && boxColors[normalizedType] == agentColors[agentID]) {
                            for (int agent_move = 0; agent_move < current_node.neighbor
                                    .size(); agent_move++) {
                                int agentDest = current_node.neighbor.get(agent_move);
                                if (!node2box.containsKey(agentDest)
                                        && !node2agent.containsKey(agentDest)
                                        && !isConstrainedNext(agentID, agentDest)) {
                                    Action action = PullActions[agent_move][box_move];
                                    agentActions[i].add(new ActionWrapper(action, agentPos,
                                            agentDest, boxPos, boxDest, boxType, boxIdx,
                                            getActionStr(current_node, action)));
                                    agentDestination[i].add(agentDest);
                                    boxDestination[i].add(boxDest);
                                    boxPosition[i].add(boxPos);
                                }
                            }
                        }
                    }
                }
            }
        }

        ActionWrapper[] jointAction = new ActionWrapper[numAgents];
        int[] jointAgentDestination = new int[numAgents];
        int[] jointBoxDestination = new int[numAgents];
        int[] jointBoxPosition = new int[numAgents];
        int[] actionsPermutation = new int[numAgents];
        ArrayList<State> expandedStates = new ArrayList<>(16);

        for (int i = 0; i < numAgents; i++) {
            actionsPermutation[i] = 0;
        }

        // do not return any expanded states if
        // any agent has no possible moves (cannot even NoOp)
        // because then any jointAction will be invalid
        boolean allAgentsHaveOneMove = false;
        for (int i = 0; i < numAgents; i++) {
            if (agentActions[i].size() >= 1) {
                allAgentsHaveOneMove = true;
            } else {
                allAgentsHaveOneMove = false;
                break;
            }
        }

        boolean moveBoxState;
        while (allAgentsHaveOneMove) {

            moveBoxState = false;

            for (int agent = 0; agent < numAgents; agent++) {
                jointAction[agent] = agentActions[agent].get(actionsPermutation[agent]);

                // mark the state if it was generated using push / pull
                if (jointAction[agent].action.type == ActionType.Pull
                        || jointAction[agent].action.type == ActionType.Push) {
                    moveBoxState = true;
                }

                jointAgentDestination[agent] = agentDestination[agent].get(actionsPermutation[agent]);
                jointBoxDestination[agent] = boxDestination[agent].get(actionsPermutation[agent]);
                jointBoxPosition[agent] = boxPosition[agent].get(actionsPermutation[agent]);
            }

            if (!this.isConflicting(jointAction, jointAgentDestination, jointBoxDestination,
                    jointBoxPosition)) {

                State newState = new State(this, jointAction, jointAgentDestination, jointBoxDestination);
                if (moveBoxState)
                    newState.wasGeneratedUsingMovingBox = true;

                expandedStates.add(newState);
            }

            // Advance permutation
            boolean done = false;
            for (int agent = 0; agent < numAgents; agent++) {
                if (actionsPermutation[agent] < agentActions[agent].size() - 1) {
                    ++actionsPermutation[agent];
                    break;
                } else {
                    actionsPermutation[agent] = 0;
                    if (agent == numAgents - 1) {
                        done = true;
                    }
                }
            }
            // Last permutation?
            if (done) {
                break;
            }
        }
        Collections.shuffle(expandedStates, State.RNG);
        return expandedStates;
    }// latest version

    private boolean isConflicting(ActionWrapper[] jointAction, int[] agentDestination,
            int[] boxDestination, int[] boxPosition) {
        for (int a1 = 0; a1 < numAgents; ++a1) {
            Action action1 = jointAction[a1].action;
            if (action1 == Action.NoOp) {
                continue;
            }

            for (int a2 = a1 + 1; a2 < numAgents; ++a2) {
                Action action2 = jointAction[a2].action;
                if (action2 == Action.NoOp) {
                    continue;
                }

                if (action1.value != 0 && action2.value != 0) {
                    if (boxPosition[a1] == boxPosition[a2])
                        return true;
                }

                // TOASK (elle): shouldn't we check for if 2-agents trying to move same box,
                // too?
                if (action1.value == 1) { // if push then only check collision with box
                    if (boxDestination[a1] == agentDestination[a2]
                            || boxDestination[a1] == boxDestination[a2])
                        return true;
                } // if move or pull then check collision with agent
                else if (agentDestination[a1] == agentDestination[a2]
                        || agentDestination[a1] == boxDestination[a2]) {
                    return true;
                }
            }
        }

        return false;
    }

    // this is for resource constraints i.e. agent cannot move given box at this
    // place at this time
    private boolean hasRSCConstraint(int agent, int node_id, int time, int boxType, int boxIdx) {
        if (this.resourceConstraints == null) {
            return false;
        } else if (!this.resourceConstraints.containsKey(agent)) {
            return false;
        } else if (!this.resourceConstraints.get(agent).containsKey(time)) {
            return false;
        } else if (!this.resourceConstraints.get(agent).get(time).containsKey(node_id)) {
            return false;
        } else {
            HashSet<Constraint> constraints = this.resourceConstraints.get(agent).get(time).get(node_id);
            for (Constraint constraint : constraints) {
                if (constraint.box.boxType == boxType && constraint.box.boxIndex == boxIdx) {
                    return true;
                }
            }
            return false;
        }
    }

    // TODO (efficiency): make check more efficient aka make hashmap of
    // agentID->nodeID->constraints
    private boolean isConstrained(int agent, int node_id, int time) {
        if (State.constraints == null) {
            return false;
        } else {

            if (!State.constraints.containsKey(agent))
                return false;

            if (!State.constraints.get(agent).containsKey(time))
                return false;

            if (!State.constraints.get(agent).get(time).containsKey(node_id))
                return false;

            // all conditions above are true
            return true;
        }

    }

    // NOTE: this is NOT for checking resource-conflicts but rather
    // fat-agent/temporarily moving a
    // box agents
    // i.e. if a box is constrained it means it is not allowed to be at a place at a
    // certain time
    // whereas a resource constraint is an agent is not allowed to touch a box at a
    // certain
    // time/place; for resource constraints, see hasRSCConstraint
    private boolean isConstrainedBoxAtConstraint(int agent, int time) {
        if (State.constraints == null) {
            return false;
        } else {

            if (!State.constraints.containsKey(agent))
                return false;

            if (!State.constraints.get(agent).containsKey(time))
                return false;

            for (int nodeID : State.constraints.get(agent).get(time).keySet()) {
                for (Constraint constraint : State.constraints.get(agent).get(time).get(nodeID)) {

                    if (agent == constraint.agent && time == constraint.t
                            && constraint.box != null) {
                        Box constrainedBox = constraint.box;
                        int currPosOfConstrainedBox = this.boxMap[constrainedBox.boxType].get(constrainedBox.boxIndex);
                        // BUG?: should we change this so that we check if *any* box is at any
                        // other box
                        // constraint?
                        // might speed up things but also may have unwanted side-effects
                        if (currPosOfConstrainedBox == constraint.node_id) {
                            // constrained box is at a constraint
                            return true;
                        }
                    }

                }
            }
            return false;
        }
    }

    private boolean isConstrainedNext(int agent, int node_id) {
        State state = this;
        return isConstrained(agent, node_id, state.g + 1);
    }

    private boolean isConstrainedBoxAtConstraintNext(int agent) {
        return isConstrainedBoxAtConstraint(agent, this.g + 1);
    }

    private boolean isConstrainedNow(int agent, int node_id) {
        State state = this;
        return isConstrained(agent, node_id, state.g);
    }

    private boolean isConstrainedBoxAtConstraintNow(int agent) {
        return isConstrainedBoxAtConstraint(agent, this.g);
    }

    public ActionWrapper[][] extractPlan() {
        ActionWrapper[][] plan = new ActionWrapper[this.g][];
        State state = this;
        while (state.jointAction != null) {
            plan[state.g - 1] = state.jointAction;
            state = state.parent;
        }
        return plan;
    }

    public void printConnectedComponents() {
        StringBuilder s = new StringBuilder();
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int row = 0; row < this.walls.length; row++) {
            for (int col = 0; col < this.walls[row].length; col++) {
                int id = GraphUtils.coordinates_to_node[row][col];
                if (id != -1) {
                    int parent = this.connected_components.find_without_compression(id);
                    if (map.containsKey(parent)) {
                        s.append(map.get(parent));
                    } else {
                        map.put(parent, map.size());
                        s.append(map.get(parent));
                    }
                } else {
                    s.append("+");
                }
            }
            s.append("\n");
        }

        System.err.println(s.toString());
    }

    public void printBottlenecks() {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < this.walls.length; row++) {
            for (int col = 0; col < this.walls[row].length; col++) {
                int id = GraphUtils.coordinates_to_node[row][col];
                if (id != -1) {
                    if (this.node2agent.containsKey(id)) {
                        s.append(this.node2agent.get(id));
                    } else if (this.node2box.containsKey(id)) {
                        s.append(this.node2box.get(id));
                    } else {
                        if (GraphUtils.nodes2bottlneck.containsKey(id)) {
                            s.append("x");
                        } else {
                            s.append(" ");
                        }
                    }
                } else {
                    s.append("+");
                }
            }
            s.append("\n");
        }

        System.err.println(s.toString());
    }

    @Override
    public int hashCode() {
        if (this.hash == 0) {
            Comparator<Tuple<Integer, Character>> tupleComparator = new Comparator<Tuple<Integer, Character>>() {
                @Override
                public int compare(Tuple<Integer, Character> t1,
                        Tuple<Integer, Character> t2) {
                    return t1.x.compareTo(t2.x);
                }
            };

            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.agentColors);
            result = prime * result + Arrays.hashCode(this.boxColors);
            result = prime * result + Arrays.deepHashCode(this.walls);
            result = prime * result + Arrays.deepHashCode(this.goals);
            result = prime * result + Arrays.hashCode(agentPosition);
            result = prime * result + Arrays.hashCode(agentLabel);
            result = prime * result + this.g; // should only be added if astar through space-time is
                                              // performed
            ArrayList<Tuple<Integer, Character>> L = new ArrayList<Tuple<Integer, Character>>();

            for (ArrayList<Integer> l : boxMap) {
                for (int d : l) {
                    L.add(new Tuple<Integer, Character>(d, this.node2box.get(d)));
                }
            }

            Collections.sort(L, tupleComparator);
            for (int i = 0; i < L.size(); i++) {
                char c = L.get(i).y;
                result = prime * result + L.get(i).x * c;
            }

            this.hash = result;
        }
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        State other = (State) obj;

        return Arrays.equals(this.agentPosition, other.agentPosition)
                && Arrays.equals(this.agentLabel, other.agentLabel)
                && this.node2box.equals(other.node2box)
                && Arrays.equals(this.agentColors, other.agentColors)
                && Arrays.equals(this.boxColors, other.boxColors)
                && Arrays.deepEquals(this.goals, other.goals);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < this.walls.length; row++) {
            for (int col = 0; col < this.walls[row].length; col++) {
                int id = GraphUtils.coordinates_to_node[row][col];
                if (id != -1) {
                    if (this.node2agent.containsKey(id)) {
                        s.append(this.node2agent.get(id));
                    } else if (this.node2box.containsKey(id)) {
                        s.append(this.node2box.get(id));
                    } else if (this.node2goal.containsKey(id)) {
                        s.append(this.node2goal.get(id));
                    } else {
                        s.append(" ");
                    }
                } else {
                    s.append("+");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}
