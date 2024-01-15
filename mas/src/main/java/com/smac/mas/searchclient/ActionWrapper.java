package com.smac.mas.searchclient;

// wraps the Action enum class so we can add more properties to it
// like boxID associated with push/pull action
public class ActionWrapper {

    public final Action action;
    public final Character boxType;
    public final Integer boxIdx;
    public final String actionStr;
    public final Integer prevAgentPos;
    public final Integer prevBoxPos;
    public final Integer newAgentPos;
    public final Integer newBoxPos;

    public ActionWrapper(Action action, Integer prevAgentPos, Integer newAgentPos,
            Integer prevBoxPos, Integer newBoxPos, Character boxType, Integer boxIdx,
            String actionStr) {
        this.action = action;
        this.boxType = boxType;
        this.boxIdx = boxIdx;
        this.actionStr = actionStr;
        this.prevAgentPos = prevAgentPos;
        this.prevBoxPos = prevBoxPos;
        this.newAgentPos = newAgentPos;
        this.newBoxPos = newBoxPos;
    }

    public ActionWrapper(Action action, String actionStr) {
        this.action = action;
        this.boxType = null;
        this.boxIdx = null;
        this.actionStr = actionStr;
        this.prevAgentPos = null;
        this.prevBoxPos = null;
        this.newAgentPos = null;
        this.newBoxPos = null;
    }

    // wrapper for move actions - no box involved
    public ActionWrapper(Action action, Integer prevAgentPos, Integer newAgentPos,
            String actionStr) {
        this.action = action;
        this.boxType = null;
        this.boxIdx = null;
        this.actionStr = actionStr;
        this.prevAgentPos = prevAgentPos;
        this.prevBoxPos = null;
        this.newAgentPos = newAgentPos;
        this.newBoxPos = null;
    }

    public ActionWrapper(Action action) {
        this.action = action;
        this.boxType = null;
        this.boxIdx = null;
        this.actionStr = null;
        this.prevAgentPos = null;
        this.prevBoxPos = null;
        this.newAgentPos = null;
        this.newBoxPos = null;
    }

    public String toString() {
        String str = this.actionStr != null ? this.actionStr : action.toString();
        return str + " boxID:" + boxType + "_" + boxIdx;
    }
}
