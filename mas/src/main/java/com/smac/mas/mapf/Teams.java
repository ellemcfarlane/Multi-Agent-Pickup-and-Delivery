package com.smac.mas.mapf;

import java.util.HashMap;

import com.smac.mas.searchclient.Color;

public class Teams {

    // maps color to array of agents
    private static HashMap<Color, int[]> teams;
    private static Color[] agentColors; // maps agent to color

    public Teams(Color[] colors) {

        teams = new HashMap<Color, int[]>();
        // initialState.

        Teams.agentColors = colors;

        int colorOccurences[] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        // count number of colors per team
        for (Color color : Teams.agentColors) {
            if (color != null) {
                colorOccurences[Color.color2Int(color)]++;
            }
        }

        // create teams
        for (Color color : agentColors) {
            if (color != null && !teams.keySet().contains(color)) {
                teams.put(color, new int[colorOccurences[Color.color2Int(color)]]);
            }
        }

        // indices initialized to 0
        int indices[] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        // assign agents to teams
        for (int agent = 0; agent < agentColors.length; agent++) {

            Color colorOfAgent = agentColors[agent];
            if (colorOfAgent == null) {
                continue;
            }

            int colorInd = Color.color2Int(colorOfAgent);
            teams.get(colorOfAgent)[indices[colorInd]++] = agent;
        }

    }

    /*
     * Return the team of the given agent
     * 
     * @return integer array of agent numbers
     */
    public static int[] getTeam(Agent agent) {

        int agentNumber = agent.getAgentNumber();
        Color agentColor = agentColors[agentNumber];

        return teams.get(agentColor);

    }

    /*
     * Return the team of the given agent
     * 
     * @retrue integer array of agent numbers
     */
    public static int[] getTeam(int agentNumber) {

        Color agentColor = agentColors[agentNumber];
        return teams.get(agentColor);

    }

    /*
     * Print the team of the given agent
     */
    public static void printTeam(Agent agent) {

        int[] team = getTeam(agent);

        for (int a : team) {
            System.err.print(a);
        }

        System.err.println("----");

    }

    /*
     * Print the team of the given agent
     */
    public static void printTeam(int agent) {

        int[] team = getTeam(agent);

        for (int a : team) {
            System.err.print(a);
        }

        System.err.println("----");

    }

}
