package com.smac.mas.searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.smac.mas.mapf.BDI;
import com.smac.mas.mapf.TaskManager;

public class SearchClient {
    public static State parseLevel(BufferedReader serverMessages) throws IOException {
        // We can assume that the level file is conforming to specification, since the
        // server
        // verifies this.
        // Read domain
        serverMessages.readLine(); // #domain
        serverMessages.readLine(); // hospital

        // Read Level name
        serverMessages.readLine(); // #levelname
        serverMessages.readLine(); // <name>

        // Read colors
        serverMessages.readLine(); // #colors
        Color[] agentColors = new Color[10];
        Color[] boxColors = new Color[26];
        String line = serverMessages.readLine();
        while (!line.startsWith("#")) {
            String[] split = line.split(":");
            Color color = Color.fromString(split[0].strip());
            String[] entities = split[1].split(",");
            for (String entity : entities) {
                char c = entity.strip().charAt(0);
                if ('0' <= c && c <= '9') {
                    // TODO/BUG: is it okay to do this if we don't know the number of agents yet?
                    agentColors[c - '0'] = color;
                } else if ('A' <= c && c <= 'Z') {
                    // TODO/BUG: is it okay to do this if we don't know the number of boxes yet?
                    boxColors[c - 'A'] = color;
                }
            }
            line = serverMessages.readLine();
        }

        // Read initial state
        // line is currently "#initial"
        int numRows = 0;
        int numCols = 0;
        ArrayList<String> levelLines = new ArrayList<>(64);
        line = serverMessages.readLine();
        while (!line.startsWith("#")) {
            levelLines.add(line);
            numCols = Math.max(numCols, line.length());
            ++numRows;
            line = serverMessages.readLine();
        }

        HashMap<Integer, ArrayList<Integer>> agentToPos = new HashMap<>();
        boolean[][] walls = new boolean[numRows][numCols];
        char[][] boxes = new char[numRows][numCols];
        int numNodes = 0;

        for (int row = 0; row < numRows; ++row) {
            line = levelLines.get(row);
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);
                if ('0' <= c && c <= '9') {
                    agentToPos.put(c - '0', new ArrayList<Integer>(Arrays.asList(row, col)));
                } else if ('A' <= c && c <= 'Z') {
                    boxes[row][col] = c;
                } else if (c == '+') {
                    walls[row][col] = true;
                }
            }
        }

        // doing this to avoid redoing our original implementation
        // of agentRows and agentCols
        // we can't do it before because we don't know numAgents
        // before iterating over level
        int numAgents = agentToPos.size();
        int[] agentRows = new int[numAgents];
        int[] agentCols = new int[numAgents];
        for (Map.Entry<Integer, ArrayList<Integer>> entry : agentToPos.entrySet()) {
            agentRows[entry.getKey()] = entry.getValue().get(0);
            agentCols[entry.getKey()] = entry.getValue().get(1);
        }

        for (int i = 1; i < numRows - 1; i++) {
            for (int j = 1; j < numCols - 1; j++) {
                if (!walls[i][j])
                    numNodes++;
            }
        }

        // Read goal state
        // line is currently "#goal"
        char[][] goals = new char[numRows][numCols];
        line = serverMessages.readLine();
        int row = 0;
        while (!line.startsWith("#")) {
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z')) {
                    goals[row][col] = c;
                }
            }

            ++row;
            line = serverMessages.readLine();
        }

        State initialState = null;

        try {
            initialState = new State(agentRows, agentCols, walls, boxes, goals, boxColors,
                    agentColors, numNodes);
        } catch (Exception e) {
            // System.err.println();
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }

        return initialState;
    }

    public static ActionWrapper[][] search(State initialState, Frontier frontier) {
        System.err.format("Starting %s.\n", frontier.getName());

        return GraphSearch.search(initialState, frontier);
    }

    public static void main(String[] args) throws IOException {
        // Use stderr to print to the console.
        System.err.println(
                "SearchClient initializing. I am sending this using the error output stream.");

        // Send client name to server.
        System.out.println("Goofy");

        // We can also print comments to stdout by prefixing with a #.
        System.out.println("#This is a comment.");

        // for debugging I would hardcode the level here
        // String filePath = "./levels/mutex.lvl";
        // BufferedReader serverMessages = new BufferedReader(
        // new InputStreamReader(
        // new FileInputStream(filePath),
        // StandardCharsets.US_ASCII
        // )
        // );

        // Parse the level.
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));

        State initialState = SearchClient.parseLevel(serverMessages);
        new TaskManager(); // initialize TaskManager

        // PositionTuple start1 = new PositionTuple(initialState.agentPosition[0],
        // initialState.boxMap[0].get(0));
        // PositionTuple start2 = new PositionTuple(initialState.agentPosition[1],
        // initialState.boxMap[1].get(0));
        // PositionTuple goal1 = new PositionTuple(initialState.goals[0].get(0),
        // initialState.goals['A' - '0'].get(0));
        // PositionTuple goal2 = new PositionTuple(initialState.goals[1].get(0),
        // initialState.goals['B' - '0'].get(0));

        // MDD mdd_1 = new MDD(start1, goal1, true, true);
        // MDD mdd_2 = new MDD(start2, goal2, true, true);
        // // mdd_1.unitExpandMDD(mdd_1);
        // // mdd_2.unitExpandMDD(mdd_2);
        // mdd_1.printMDD();
        // mdd_2.printMDD();

        // var constraint = mdd_1.generateConstraints(mdd_2);

        // for(var c : constraint.x) {
        // int h = GraphUtils.node_list[c.node_id].row;
        // int w = GraphUtils.node_list[c.node_id].col;
        // System.err.println(c.agent + " (" + h + "," + w + ") " + c.t);
        // }

        // for(var c : constraint.y) {
        // int h = GraphUtils.node_list[c.node_id].row;
        // int w = GraphUtils.node_list[c.node_id].col;
        // System.err.println(c.agent + " (" + h + "," + w + ") " + c.t);
        // }

        // Agent[] agents = new Agent[initialState.numAgents];
        // for (int i = 0; i < agents.length; i++) {
        // agents[i] = new Agent(initialState.agentLabel[i]);
        // }

        // HashMap<Integer, ArrayList<Box>> boxes = new HashMap<>();
        // for (int boxType = 0; boxType < initialState.boxMap.length; boxType++) {
        // for (int boxIndex = 0; boxIndex < initialState.boxMap[boxType].size();
        // boxIndex++) {

        // if (!boxes.containsKey(boxType)) {
        // boxes.put(boxType, new ArrayList<Box>());
        // }
        // boxes.get(boxType).add(new Box(boxType, boxIndex));

        // }
        // }

        // TaskManager.assignTasks(initialState, agents, null, null, null, boxes);

        // Select search strategy.
        Frontier frontier = null;
        String heuristicMethod = "";
        if (args.length > 0) {
            if (args.length > 1) {
                // remove first "-" e.g. -manhattan -> manhattan
                heuristicMethod = args[1].substring(1);
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "-bfs":
                    frontier = new FrontierBFS();
                    break;
                case "-dfs":
                    frontier = new FrontierDFS();
                    break;
                case "-astarGreedy":
                    frontier = new FrontierBestFirst(
                            new HeuristicAStarGreedy(initialState, heuristicMethod));
                    break;
                case "-astar":
                    frontier = new FrontierBestFirst(
                            new HeuristicAStar(initialState, heuristicMethod));
                    break;
                case "-wastar":
                    int w = 5;
                    if (args.length > 1) {
                        try {
                            w = Integer.parseUnsignedInt(args[1]);
                        } catch (NumberFormatException e) {
                            System.err.println(
                                    "Couldn't parse weight argument to -wastar as integer, using default.");
                        }
                    }
                    frontier = new FrontierBestFirst(
                            new HeuristicWeightedAStar(initialState, heuristicMethod, w));
                    break;
                case "-greedy":
                    frontier = new FrontierBestFirst(
                            new HeuristicGreedy(initialState, heuristicMethod));
                    break;
                default:
                    System.err.println(
                            "No algo specified. Use arguments -bfs, -dfs, -astar, -wastar, or "
                                    + "-greedy to set the search strategy.");
                    System.exit(1);
                    break;
            }
        } else {
            System.err.println(
                "No algo specified. Use arguments -bfs, -dfs, -astar, -wastar, or "
                        + "-greedy to set the search strategy.");
            System.exit(1);
        }

        // Search for a plan.
        ActionWrapper[][] plan;
        try {
            // plan = SearchClient.search(initialState, frontier);
            // plan = null;
            plan = BDI.search(initialState, frontier);

        } catch (OutOfMemoryError ex) {
            System.err.println("Maximum memory usage exceeded.");
            plan = null;
        }

        // Print plan to server.
        if (plan == null) {
            System.err.println("Unable to solve level.");
            System.exit(0);
        } else {
            System.err.format("Found solution of length %,d.\n", plan.length);
            int[] currentPosition = State.agentInit;
            String stepStr;
            for (ActionWrapper[] jointAction : plan) {
                stepStr = step(0, currentPosition, jointAction);
                System.out.print(stepStr);
                System.err.print(stepStr);
                for (int action = 1; action < jointAction.length; ++action) {
                    System.out.print("|");
                    System.err.print("|");
                    stepStr = step(action, currentPosition, jointAction);
                    System.out.print(stepStr);
                    System.err.print(stepStr);
                }
                System.out.println();
                System.err.println();
                serverMessages.readLine();
            }
        }
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

    public static String step(int i, int[] current_node, ActionWrapper[] jointAction) {
        String actionStr = jointAction[i].actionStr;
        Action action = jointAction[i].action;
        Node currentNode = GraphUtils.node_list[current_node[i]];
        if (actionStr == null) {
            actionStr = State.getActionStr(currentNode, action);
        }
        // update node if not a NoOp
        if (action.value != -1) {
            current_node[i] = currentNode.neighbor.get(action.agent_edge_id);
        }
        return actionStr;
    }

}
