package com.smac.mas.mapf;

public class Box {

    /* This class saves information about a box */

    public int boxIndex; // box number in the boxMap[boxType]
    public int boxType; // integer corresponding to the box label
    public boolean isFat;
    public Agent pickupGuy;
    private int goalNode; // goal node

    public Box(int boxType, int boxIndex) {

        this.isFat = false;
        this.pickupGuy = null;
        this.boxIndex = boxIndex;
        this.boxType = boxType;
        this.goalNode = -1; 
    }

    public Box(Box box, Agent[] newAgents){

        this.isFat = box.isFat;
        
        if (box.pickupGuy == null)
            this.pickupGuy = null;
        else
            this.pickupGuy = newAgents[box.pickupGuy.getAgentNumber()];
        
        
        this.boxIndex = box.boxIndex;
        this.boxType = box.boxType;
        this.goalNode = box.getGoalNode(); 
    }

    public Box(int boxNumber, char boxLabel) {
        this.boxIndex = boxNumber;
        this.boxType = boxLabel - 'A';
        this.pickupGuy = null;
    }

    public String toString() {
        return "type-idx:" + this.boxType + "-" + this.boxIndex;
    }

    // should be unique
    public String getID() {
        return this.boxType + "_" + this.boxIndex;
    }

    public void setPickupGuy(Agent pickupGuy) {
        this.pickupGuy = pickupGuy;
    }

    public Agent getPickupGuy() {
        return this.pickupGuy;
    }

    public int getBoxIndex() {
        return this.boxIndex;
    }

    public int getBoxType() {
        return this.boxType;
    }

    public void setBoxIndex(int boxIndex) {
        this.boxIndex = boxIndex;
    }

    public void setGoalNode(int goalNode) {
        this.goalNode = goalNode;
    }

    public int getGoalNode() {
        return this.goalNode;
    }

    public void setBoxType(int boxType) {
        this.boxType = boxType;
    }

}
