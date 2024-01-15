package com.smac.mas.searchclient;

enum ActionType {
    NoOp,
    Move,
    Push,
    Pull
}

public enum Action {
    NoOp("NoOp", ActionType.NoOp, -1, -1, -1, -1),

    Move0("Move(0)", ActionType.Move, 0, -1, 0, -1),
    Move1("Move(1)", ActionType.Move, 1, -1, 0, -1),
    Move2("Move(2)", ActionType.Move, 2, -1, 0, -1),
    Move3("Move(3)", ActionType.Move, 3, -1, 0, -1),

    Push00("Push(0,0)", ActionType.Push, 0, 0, 1, -1),
    Push01("Push(0,1)", ActionType.Push, 0, 1, 1, -1),
    Push02("Push(0,2)", ActionType.Push, 0, 2, 1, -1),
    Push03("Push(0,3)", ActionType.Push, 0, 3, 1, -1),
    Push10("Push(1,0)", ActionType.Push, 1, 0, 1, -1),
    Push11("Push(1,1)", ActionType.Push, 1, 1, 1, -1),
    Push12("Push(1,2)", ActionType.Push, 1, 2, 1, -1),
    Push13("Push(1,3)", ActionType.Push, 1, 3, 1, -1),
    Push20("Push(2,0)", ActionType.Push, 2, 0, 1, -1),
    Push21("Push(2,1)", ActionType.Push, 2, 1, 1, -1),
    Push22("Push(2,2)", ActionType.Push, 2, 2, 1, -1),
    Push23("Push(2,3)", ActionType.Push, 2, 3, 1, -1),
    Push30("Push(3,0)", ActionType.Push, 3, 0, 1, -1),
    Push31("Push(3,1)", ActionType.Push, 3, 1, 1, -1),
    Push32("Push(3,2)", ActionType.Push, 3, 2, 1, -1),
    Push33("Push(3,3)", ActionType.Push, 3, 3, 1, -1),

    Pull00("Pull(0,0)", ActionType.Pull, 0, 0, 2, 0),
    Pull01("Pull(0,1)", ActionType.Pull, 0, 1, 2, 1),
    Pull02("Pull(0,2)", ActionType.Pull, 0, 2, 2, 2),
    Pull03("Pull(0,3)", ActionType.Pull, 0, 3, 2, 3),
    Pull10("Pull(1,0)", ActionType.Pull, 1, 0, 2, 0),
    Pull11("Pull(1,1)", ActionType.Pull, 1, 1, 2, 1),
    Pull12("Pull(1,2)", ActionType.Pull, 1, 2, 2, 2),
    Pull13("Pull(1,3)", ActionType.Pull, 1, 3, 2, 3),
    Pull20("Pull(2,0)", ActionType.Pull, 2, 0, 2, 0),
    Pull21("Pull(2,1)", ActionType.Pull, 2, 1, 2, 1),
    Pull22("Pull(2,2)", ActionType.Pull, 2, 2, 2, 2),
    Pull23("Pull(2,3)", ActionType.Pull, 2, 3, 2, 3),
    Pull30("Pull(3,0)", ActionType.Pull, 3, 0, 2, 0),
    Pull31("Pull(3,1)", ActionType.Pull, 3, 1, 2, 1),
    Pull32("Pull(3,2)", ActionType.Pull, 3, 2, 2, 2),
    Pull33("Pull(3,3)", ActionType.Pull, 3, 3, 2, 3);

    public final String name;
    public final ActionType type;
    public final int agent_edge_id;
    public final int box_edge_id;
    public final int value;
    public final int pull_type;

    Action(String name, ActionType type, int agent_edge_id, int box_edge_id, int value, int pull_type) {
        this.name = name;
        this.type = type;
        this.agent_edge_id = agent_edge_id;
        this.box_edge_id = box_edge_id;
        this.value = value;
        this.pull_type = pull_type;
    }
}
