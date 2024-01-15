package com.smac.mas.searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import com.smac.mas.localsearch.LocalState;
import com.smac.mas.localsearch.Search;
import java.util.Queue;
import java.util.LinkedList;

public class GraphUtils {

    public static int[][] coordinates_to_node;
    public static Node[] node_list;
    public static ArrayList<ArrayList<Integer>> bottleneck_nodes;
    public static HashMap<Integer, Integer> nodes2bottlneck;
    public static int[][] min_distance;

    public GraphUtils(boolean[][] walls, char[][] goals, int num_nodes) {
        coordinates_to_node = new int[walls.length][walls[0].length];
        int count = 0;
        node_list = new Node[num_nodes];
        nodes2bottlneck = new HashMap<>();
        for (int i = 0; i < walls.length; i++) {
            for (int j = 0; j < walls[i].length; j++) {
                if (i > 0 && j > 0 && i < walls.length - 1 && j < walls[i].length - 1) {
                    if (!walls[i][j]) {
                        char c = goals[i][j];
                        int goal_type = -1;

                        if ('0' <= c && c <= '9')
                            goal_type = 1;
                        else if ('A' <= c && c <= 'Z')
                            goal_type = 0;

                        node_list[count] = new Node(count, goal_type, c, i, j);
                        System.err.print(count + ":");
                        coordinates_to_node[i][j] = count++;
                    } else {
                        coordinates_to_node[i][j] = -1;
                        System.err.print("  ");
                    }
                } else {
                    coordinates_to_node[i][j] = -1;
                }
            }
            System.err.println("");
        }

        assert (count == num_nodes);

        for (Node node : node_list) {
            if (coordinates_to_node[node.row - 1][node.col] != -1) {
                node.neighbor.add(coordinates_to_node[node.row - 1][node.col]);
                node.edge2Dir.add('N');
            }
            if (coordinates_to_node[node.row + 1][node.col] != -1) {
                node.neighbor.add(coordinates_to_node[node.row + 1][node.col]);
                node.edge2Dir.add('S');
            }
            if (coordinates_to_node[node.row][node.col - 1] != -1) {
                node.neighbor.add(coordinates_to_node[node.row][node.col - 1]);
                node.edge2Dir.add('W');
            }
            if (coordinates_to_node[node.row][node.col + 1] != -1) {
                node.neighbor.add(coordinates_to_node[node.row][node.col + 1]);
                node.edge2Dir.add('E');
            }
        }

        compute_pairwise_min_distance();
    }

    private static void dfs(int node_id, boolean[] visited, ArrayList<Integer> component) {
        if (!check_validity(node_list[node_id]))
            return;

        visited[node_id] = true;
        component.add(node_id);
        Node node = node_list[node_id];
        for (int neighbor_id : node.neighbor) {
            if (!visited[neighbor_id]) {
                dfs(neighbor_id, visited, component);
            }
        }
    }

    private static boolean check_validity(Node node) {
        if (node.neighbor.size() != 2)
            return false;
        if (node.edge2Dir.get(0) == SearchClient.reverse(node.edge2Dir.get(1))) {
            return true;
        }

        Node neighbor1 = node_list[node.neighbor.get(0)];
        Node neighbor2 = node_list[node.neighbor.get(1)];

        for (int neighbor_id1 : neighbor1.neighbor) {
            for (int neighbor_id2 : neighbor2.neighbor) {
                if (neighbor_id1 == neighbor_id2 && neighbor_id1 != node.id)
                    return false;
            }
        }

        return true;
    }

    public static void filter_bottlenecks() {
        if (node_list == null)
            return;

        ArrayList<ArrayList<Integer>> components = new ArrayList<ArrayList<Integer>>();
        boolean[] visited = new boolean[node_list.length];
        for (int i = 0; i < node_list.length; i++) {
            Node node = node_list[i];
            if (check_validity(node)) {
                if (!visited[i]) {
                    ArrayList<Integer> component = new ArrayList<Integer>();
                    dfs(i, visited, component);
                    for (Integer node_id : component) {
                        nodes2bottlneck.put(node_id, components.size());
                    }
                    components.add(component);
                }
            }
        }

        bottleneck_nodes = components;
    }

    private static void dfs(int node_id, boolean[] visited, DisjointSet ds, State s) {
        visited[node_id] = true;
        Node node = node_list[node_id];
        for (int neighbor_id : node.neighbor) {
            if (!visited[neighbor_id] && !s.node2goal.containsKey(neighbor_id)) {
                ds.union(neighbor_id, node_id);
                dfs(neighbor_id, visited, ds, s);
            }
        }
    }

    public static DisjointSet get_connected_components(State s) {
        DisjointSet ds = new DisjointSet(node_list.length);
        boolean[] visited = new boolean[node_list.length];
        for (int i = 0; i < node_list.length; i++) {
            if (!visited[i] && !s.node2goal.containsKey(i)) {
                dfs(i, visited, ds, s);
            }
        }
        return ds;
    }

    private static void bfs(int source, int[][] distances, boolean[] visited) {
        Queue<Integer> queue = new LinkedList<>();

        queue.add(source);
        visited[source] = true;
        distances[source][source] = 0;

        while (!queue.isEmpty()) {
            int node = queue.poll();

            for (int n : GraphUtils.node_list[node].neighbor) {
                if (!visited[n]) {
                    visited[n] = true;
                    queue.add(n);
                    distances[source][n] = distances[source][node] + 1;
                    distances[n][source] = distances[source][n];
                }
            }
        }
    }

    private static void compute_pairwise_min_distance() {
        int n = node_list.length;
        min_distance = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                min_distance[i][j] = Integer.MAX_VALUE;
            }
        }

        boolean[] visited = new boolean[n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(visited, false);
            bfs(i, min_distance, visited);
        }
    }

    private static boolean is_feasable(State s,
            DisjointSet ds,
            int g,
            char g_type,
            boolean[] free,
            Stack<Operation> S,
            HashMap<Integer, Boolean> is_open) {
        int parent = ds.find_without_compression(g);
        if (g_type > '9') {
            Color c = State.boxColors[g_type - 'A'];
            ArrayList<Integer> positions = s.boxMap[g_type - 'A'];
            for (int p : positions) { // completed goal will always be in the seperate component
                if (ds.find_without_compression(p) == parent) {
                    for (int label : State.color2Agent[Color.color2Int(c)]) {
                        if (s.label2index[label] != -1) {
                            int agent_index = s.label2index[label];
                            if (free[agent_index]) {
                                // join_sets(s, ds, s.agentPosition[agent_index], S, is_open);
                                if (ds.find_without_compression(s.agentPosition[agent_index]) == parent) {
                                    // revert_last_union(ds, S);
                                    return true;
                                }
                                // revert_last_union(ds, S);
                            }
                        }
                    }
                    return false;
                }
            }
            return false;
        } else {
            int agent_index = s.label2index[g_type - '0'];
            // join_sets(s, ds, s.agentPosition[agent_index], S, is_open);
            boolean ret = ds.find_without_compression(s.agentPosition[agent_index]) == parent;
            // revert_last_union(ds, S);
            return ret;
        }
    }

    private static void join_sets(State s,
            DisjointSet ds,
            int node_id,
            Stack<Operation> S,
            HashMap<Integer, Boolean> is_open) {
        int this_id = 0;
        if (S.size() > 0)
            this_id = S.peek().id + 1;
        boolean has_pushed = false;

        for (int n : node_list[node_id].neighbor) {
            if (s.node2goal.containsKey(n) && !is_open.containsKey(n))
                continue;
            Operation op = ds.unionWithRetOps(node_id, n);
            op.id = this_id;
            S.push(op);
            has_pushed = true;
        }

        if (!has_pushed) {
            Operation op = new Operation();
            op.id = this_id;
            S.push(op);
        }
    }

    private static void revert_last_union(DisjointSet ds, Stack<Operation> S) {
        int id = S.peek().id;

        do {
            ds.revert(S.pop());
        } while (S.size() > 0 && S.peek().id == id);
    }

    private static void set_free_val(State s,
            char goal_type,
            boolean[] free,
            boolean value) {
        if (goal_type <= '9') {
            int label = goal_type - '0';
            int index = s.label2index[label];
            if (index == -1) {
                System.err.println("Level is not solvable!");
                return;
            }
            free[index] = value;
        }
    }

    public static ArrayList<Character> temp;

    private static ArrayList<ArrayList<Integer>> handle_goal_removal_case(State s,
            int g,
            ArrayList<Integer> remaining_goals,
            ArrayList<Integer> completed_goals,
            ArrayList<Character> rem_goal_type,
            ArrayList<Character> com_goal_type,
            boolean[] free,
            DisjointSet ds,
            HashMap<Integer, Boolean> is_open,
            Stack<Operation> operations,
            boolean[] processed,
            char current_goal_type,
            int count) {

        boolean[] temp_processed = new boolean[completed_goals.size()];
        Stack<Operation> temp_operations = new Stack<Operation>();
        ArrayList<ArrayList<Integer>> min_removal_constraints = generate_constraints(s,
                g,
                completed_goals,
                null,
                com_goal_type,
                null,
                free,
                ds,
                is_open,
                temp_operations,
                temp_processed,
                current_goal_type,
                false);

        if (min_removal_constraints == null)
            return null;

        while (count-- > 0) {
            revert_last_union(ds, operations);
        }

        for (int j = 0; j < remaining_goals.size(); j++) {
            if (processed[j]) {
                set_free_val(s, rem_goal_type.get(j), free, true);
                is_open.put(remaining_goals.get(j), true);
            } else {
                set_free_val(s, rem_goal_type.get(j), free, false);
                is_open.remove(remaining_goals.get(j));
            }
        }

        for (ArrayList<Integer> min_removal_constraint : min_removal_constraints) {
            for (int i = 0; i < min_removal_constraint.size(); i++) {
                int _g = min_removal_constraint.get(i);
                is_open.put(_g, true);
                join_sets(s, ds, _g, operations, is_open);
                char __g_type = s.node2goal.get(_g);
                set_free_val(s, __g_type, free, true);
            }
        }

        return generate_constraints(s,
                g,
                remaining_goals,
                null,
                rem_goal_type,
                null,
                free,
                ds,
                is_open,
                operations,
                processed,
                current_goal_type,
                true);
    }

    private static ArrayList<ArrayList<Integer>> generate_constraints(State s,
            int g,
            ArrayList<Integer> remaining_goals,
            ArrayList<Integer> completed_goals,
            ArrayList<Character> rem_goal_type,
            ArrayList<Character> com_goal_type,
            boolean[] free,
            DisjointSet ds,
            HashMap<Integer, Boolean> is_open,
            Stack<Operation> operations,
            boolean[] processed,
            char current_goal_type,
            boolean add_self) {

        ArrayList<ArrayList<Integer>> constraints = new ArrayList<ArrayList<Integer>>();
        while (!is_feasable(s, ds, g, current_goal_type, free, operations, is_open)) {
            ArrayList<Integer> constraint = new ArrayList<Integer>();
            int count = 0;

            for (int j = 0; j < remaining_goals.size(); j++) {
                if (processed[j])
                    continue;

                int _g = remaining_goals.get(j);
                is_open.put(_g, true);
                join_sets(s, ds, _g, operations, is_open);
                set_free_val(s, rem_goal_type.get(j), free, true);
                if (is_feasable(s, ds, g, current_goal_type, free, operations, is_open)) {
                    constraint.add(_g);
                    processed[j] = true;
                    is_open.remove(_g);
                    revert_last_union(ds, operations);
                    set_free_val(s, rem_goal_type.get(j), free, false);
                } else
                    count++;
            }

            if (constraint.size() == 0) {
                if (completed_goals == null) {
                    System.err.println("Level is not solvable");
                    return null;
                }
                assert (constraints.size() == 0);

                return handle_goal_removal_case(s,
                        g,
                        remaining_goals,
                        completed_goals,
                        rem_goal_type,
                        com_goal_type,
                        free,
                        ds,
                        is_open,
                        operations,
                        processed,
                        current_goal_type,
                        count);
            }

            // Revert last count # operations
            while (count-- > 0) {
                revert_last_union(ds, operations);
            }

            for (int j = 0; j < remaining_goals.size(); j++) {
                if (processed[j]) {
                    set_free_val(s, rem_goal_type.get(j), free, true);
                    is_open.put(remaining_goals.get(j), true);
                } else {
                    set_free_val(s, rem_goal_type.get(j), free, false);
                    is_open.remove(remaining_goals.get(j));
                }
            }

            for (int c : constraint) {
                join_sets(s, ds, c, operations, is_open);
            }

            if (add_self)
                constraint.add(g);
            constraints.add(constraint);
        }

        // reset again
        while (operations.size() > 0) {
            ds.revert(operations.pop());
        }

        for (int j = 0; j < remaining_goals.size(); j++) {
            set_free_val(s, rem_goal_type.get(j), free, false);
        }

        is_open.clear();

        return constraints;
    }

    /*
     * A basis condition is of the form g1 ^ g2 ... ^ gk => _gn where k > 1,
     * removing any gi for i < k makes _gn possible and gi = _gn iff i = k
     * If there doesn't exist a basis condition in satisfied goals then this
     * function returns a disjoint sets
     * of semi-basis conditions(might have neglected elements in completed goals)
     * among remaining goals. Otherwise it returns a disjoint sets of fully basis
     * conditions among
     * remaining goals. Note that returning all possible basis condition requires
     * EXPSPACE
     * Running Time : O(|Goals|^3 * (|Boxes| + |Agents|) * log(|V|))
     */
    public static ArrayList<ArrayList<Integer>>[] get_disjoint_goal_ordering_constraints(State s,
            boolean allAgentActive) {
        ArrayList<Integer> completed_goals = new ArrayList<>();
        ArrayList<Integer> remaining_goals = new ArrayList<>();
        ArrayList<Character> rem_goal_type = new ArrayList<>();
        ArrayList<Character> com_goal_type = new ArrayList<>();
        HashMap<Integer, Boolean> is_open = new HashMap<>();
        boolean[] free = new boolean[s.numAgents];
        Arrays.fill(free, true);

        for (int i = 0; i < State.GOAL_ARRAY_SIZE; i++) {
            for (int node_id : s.goals[i]) {
                if (i + '0' > '9') {
                    if (s.node2box.containsKey(node_id) && s.node2box.get(node_id) == (char) (i + '0')) {
                        completed_goals.add(node_id);
                        com_goal_type.add((char) (i + '0'));
                    } else {
                        remaining_goals.add(node_id);
                        rem_goal_type.add((char) (i + '0'));
                    }
                } else {
                    if (!allAgentActive && s.node2agent.containsKey(node_id)
                            && s.node2agent.get(node_id) == (char) (i + '0')) {
                        completed_goals.add(node_id);
                        com_goal_type.add((char) (i + '0'));
                    } else {
                        remaining_goals.add(node_id);
                        rem_goal_type.add((char) (i + '0'));
                    }
                    int index = s.label2index[i];
                    free[index] = false;
                }
            }
        }

        DisjointSet ds = State.connected_components;
        boolean[] processed = new boolean[remaining_goals.size()];
        Stack<Operation> operations = new Stack<Operation>();
        ArrayList<ArrayList<Integer>>[] result = new ArrayList[remaining_goals.size()];

        for (int i = 0; i < remaining_goals.size(); i++) {
            int g = remaining_goals.get(i);
            is_open.put(g, true);
            Arrays.fill(processed, false);
            processed[i] = true;
            join_sets(s, ds, g, operations, is_open);
            set_free_val(s, rem_goal_type.get(i), free, true);

            result[i] = generate_constraints(s,
                    g,
                    remaining_goals,
                    completed_goals,
                    rem_goal_type,
                    com_goal_type,
                    free,
                    ds,
                    is_open,
                    operations,
                    processed,
                    rem_goal_type.get(i),
                    true);

            if (result[i] == null) {
                while (operations.size() > 0) {
                    ds.revert(operations.pop());
                }
                while (i < remaining_goals.size()) {
                    result[i] = new ArrayList<>();
                    i++;
                }
                return result;
            }
        }

        temp = rem_goal_type;

        return result;
    }

    /*
     * Running Time : O((|G| + |A|) * |V| * log(|V|))
     */
    public static void get_distance_matrix(CostStruct costStruct,
            Color[] colors,
            int[] agentPosition,
            int[] label2index,
            ArrayList<Character>[] color2Box,
            ArrayList<Integer>[] state_boxMap,
            ArrayList<Integer>[] state_goals,
            HashMap<Color, ArrayList<Integer>> Color2SpecialGoal) {

        boolean initialized = false;
        int numColors = Color.values().length;
        int[][] goalColorMap = new int[numColors][2];
        int[][] boxColorMap = new int[numColors][2];
        int[][] agentColorMap = new int[numColors][2];
        int[][] specialGoalMap = new int[State.maxNumAgents][2];
        int[][] goalMap = new int[State.GOAL_ARRAY_SIZE][2];
        int[][] boxMap = new int[State.CHARSET_ARRAY_SIZE][2];
        ArrayList<Character> goalType = new ArrayList<>();

        for (Color color : colors) {
            boxColorMap[Color.color2Int(color)][0] = costStruct.boxDescription.size();
            goalColorMap[Color.color2Int(color)][0] = costStruct.goalDescription.size();
            for (char c : color2Box[Color.color2Int(color)]) {
                int _c = c - 'A';
                int _d = c - '0';
                boxMap[_c][0] = costStruct.boxDescription.size();
                for (int position : state_boxMap[_c]) {
                    costStruct.boxDescription.add(position);
                }
                boxMap[_c][1] = costStruct.boxDescription.size();

                goalMap[_d][0] = costStruct.goalDescription.size();
                for (int g : state_goals[_d]) {
                    costStruct.goalDescription.add(g);
                    goalType.add(c);
                }
                goalMap[_d][1] = costStruct.goalDescription.size();
            }
            boxColorMap[Color.color2Int(color)][1] = costStruct.boxDescription.size();
            int hasSpecialStart = -1;
            int hasSpecialEnd = 0;
            if (Color2SpecialGoal.containsKey(color)) {
                hasSpecialStart = costStruct.goalDescription.size();
                ArrayList<Integer> specialGoal = Color2SpecialGoal.get(color);
                for (int g : specialGoal) {
                    costStruct.goalDescription.add(g);
                    goalType.add('-');
                }
                hasSpecialEnd = costStruct.goalDescription.size();
            }

            agentColorMap[Color.color2Int(color)][0] = costStruct.agentDescription.size();
            for (int l : State.color2Agent[Color.color2Int(color)]) {
                int index = label2index[l];

                if (index != -1) {
                    costStruct.agentDescription.add(l);
                    if (state_goals[l].size() > 0) {
                        goalMap[l][0] = costStruct.goalDescription.size();
                        costStruct.goalDescription.add(state_goals[l].get(0));
                        goalType.add((char) (l + '0'));
                        goalMap[l][1] = goalMap[l][0] + 1;
                    }

                    if (hasSpecialStart != -1) {
                        specialGoalMap[l][0] = hasSpecialStart;
                        specialGoalMap[l][1] = hasSpecialEnd;
                    }
                }
            }
            agentColorMap[Color.color2Int(color)][1] = costStruct.agentDescription.size();
            goalColorMap[Color.color2Int(color)][1] = costStruct.goalDescription.size();
        }

        costStruct.initializeArrays();
        costStruct.agentColorMap = agentColorMap;
        costStruct.goalColorMap = goalColorMap;

        for (int i = 0; i < costStruct.boxXagent.length; i++) {
            costStruct.boxXagent[i] = new ArrayList<>();
            costStruct.boxXgoal[i] = new ArrayList<>();
        }

        // get agent to box cost and agent to goal cost
        for (int i = 0; i < costStruct.agentDescription.size(); i++) {
            int label = costStruct.agentDescription.get(i);
            int index = label2index[label];
            Color color = State.agentColors[label];
            HashMap<Integer, Boolean> destinationNodes = new HashMap<>();
            int start = boxColorMap[Color.color2Int(color)][0];
            int end = boxColorMap[Color.color2Int(color)][1];
            int _start = goalMap[label][0];
            int _end = goalMap[label][1];
            int __start = specialGoalMap[label][0];
            int __end = specialGoalMap[label][1];
            int size = end - start + _end - _start + __end - __start;
            int[] keys = new int[size];
            costStruct.agentXbox[i] = new ArrayList<>(end - start);
            costStruct.agentXboxCost[i] = new ArrayList<>(end - start);
            costStruct.agentXgoal[i] = new ArrayList<>(Math.max(1, __end - __start));
            int j = 0;

            while (start + j < end) {
                costStruct.agentXbox[i].add(start + j);
                costStruct.boxXagent[start + j].add(i);
                keys[j] = costStruct.boxDescription.get(start + j);
                destinationNodes.put(keys[j], true);
                j++;
            }

            j = 0;
            while (__start + j < __end) {
                costStruct.agentXgoal[i].add(__start + j);
                if (costStruct.goalXentity[__start + j] == null) {
                    costStruct.goalXentity[__start + j] = new ArrayList<>();
                    costStruct.goalXentityCost[__start + j] = new ArrayList<>();
                }
                costStruct.goalXentity[__start + j].add(i);
                keys[j + end - start] = costStruct.goalDescription.get(__start + j);
                destinationNodes.put(keys[j + end - start], true);
                j++;
            }

            if (_end != _start) { // changes required
                costStruct.goalXentity[_start] = new ArrayList<>(1);
                costStruct.goalXentityCost[_start] = new ArrayList<>(1);
                costStruct.agentXgoal[i] = new ArrayList<>(1);
                costStruct.agentXgoal[i].add(_start);
                costStruct.goalXentity[_start].add(i);
                keys[size - 1] = costStruct.goalDescription.get(_start);
                destinationNodes.put(keys[size - 1], true);
            }

            LocalState ls;

            if (initialized) {
                ls = new LocalState(agentPosition[index], color, destinationNodes, keys);
            } else {
                ls = new LocalState(costStruct.s, agentPosition[index], destinationNodes, keys, color);
                initialized = true;
            }

            int[] cost = Search.getDistanceCost(ls);
            j = 0;
            while (start + j < end) {
                costStruct.agentXboxCost[i].add(cost[j]);
                j++;
            }
            int __j = 0;
            while (__start + __j < __end) {
                costStruct.goalXentityCost[__start + __j].add(cost[j + __j]);
                __j++;
            }

            if (_start != _end) {
                costStruct.goalXentityCost[_start].add(cost[size - 1]);
            }
        }

        for (int i = 0; i < costStruct.goalDescription.size(); i++) {
            char goal = goalType.get(i);

            if ('9' < goal) {
                int g = goal - 'A';
                int start = boxMap[g][0];
                int end = boxMap[g][1];
                Color color = State.boxColors[g];
                HashMap<Integer, Boolean> destinationNodes = new HashMap<>();
                costStruct.goalXentity[i] = new ArrayList<>(end - start);
                costStruct.goalXentityCost[i] = new ArrayList<>(end - start);
                int[] keys = new int[end - start];
                int j = 0;

                while (start + j < end) {
                    costStruct.goalXentity[i].add(start + j);
                    costStruct.boxXgoal[start + j].add(i);
                    keys[j] = costStruct.boxDescription.get(start + j);
                    destinationNodes.put(keys[j], true);
                    j++;
                }

                LocalState ls;
                ls = new LocalState(costStruct.goalDescription.get(i), color, destinationNodes, keys);
                int[] cost = Search.getDistanceCost(ls);
                j = 0;
                while (start + j < end) {
                    costStruct.goalXentityCost[i].add(cost[j]);
                    j++;
                }
            }
        }
        costStruct.goalType = goalType;

        LocalState.state = null;
        LocalState.destination = null;
    }
};
