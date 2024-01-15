#!/usr/bin/env python
# Created by Jonathan Mikler on 28/March/23

import os
from enum import Enum, auto
import pandas as pd
import os
import json


class BoxType(Enum):
    """
    Enum for box type A to Z
    """

    A = auto()
    B = auto()
    C = auto()
    D = auto()
    E = auto()
    F = auto()
    G = auto()
    H = auto()
    I = auto()
    J = auto()
    K = auto()
    L = auto()
    M = auto()
    N = auto()
    O = auto()
    P = auto()
    Q = auto()
    R = auto()
    S = auto()
    T = auto()
    U = auto()
    V = auto()
    W = auto()
    X = auto()
    Y = auto()
    Z = auto()


class AgentId(Enum):
    """
    Enum for agent id 0 to 9
    """

    ZERO = 0
    ONE = auto()
    TWO = auto()
    THREE = auto()
    FOUR = auto()
    FIVE = auto()
    SIX = auto()
    SEVEN = auto()
    EIGHT = auto()
    NINE = auto()


COLORS = {
    # make for 'blue', 'cyan', 'red', 'green', 'purple', 'orange', 'grey'
    "white": (255, 255, 255),
    "red": (255, 0, 0),
    "black": (0, 0, 0),
    "blue": (0, 100, 255),
    "cyan": (0, 255, 255),
    "green": (0, 255, 0),
    "purple": (255, 0, 255),
    "orange": (255, 165, 0),
    "grey": (128, 128, 128),
    "light_gray": (200, 200, 200),
    "yellow": (255, 255, 0),
}


def get_project_root():
    """Returns project root folder."""
    return os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


def read_level_file(lvl_path_):
    try:
        with open(lvl_path_, "r") as f:
            lines = f.readlines()
    except FileNotFoundError:
        print(f"ERROR: File {lvl_path_} not found")
        return None
    return lines


def categorize_level_elements(color_lines_):
    agents_dict = dict()  # key: color, value: list of agent IDs
    boxes_dict = dict()  # key: color, value: list of box IDs

    for color_line in color_lines_:
        color, color_elements = color_line.split(":")
        color_elements = color_elements.strip().split(",")

        for color_element in color_elements:
            color_element = color_element.strip()

            if color_element.isnumeric():
                agent_id = AgentId(int(color_element))
                if color in agents_dict:
                    agents_dict[color].append(agent_id.value)
                else:
                    agents_dict[color] = [agent_id.value]

            elif color_element.isalpha():
                box_id = BoxType[color_element]
                if color in boxes_dict:
                    boxes_dict[color].append(box_id.name)
                else:
                    boxes_dict[color] = [box_id.name]

    return agents_dict, boxes_dict


def count_level_elements(state_lines_):
    """
    Count the number of agents and boxes in a state
    """
    agent_count = 0
    box_count = 0

    for line in state_lines_:
        for char in line:
            if char.isnumeric():
                agent_count += 1
            elif char.isalpha():
                box_count += 1

    return agent_count, box_count


def get_level_info(lvlname_: str) -> dict:
    level_dict = dict()

    _lines = read_level_file(lvlname_)
    if len(_lines) < 3:
        print(f"WARNING LEVEL HAS SYNTACTIC ISSUE: {lvlname_}")
    level_dict["levelname"] = _lines[3].split("#")[-1].strip()

    # find the line number of the line starting with #initial
    for i, line in enumerate(_lines):
        if line.startswith("#colors"):
            colors_line = i
        if line.startswith("#initial"):
            initial_line = i
        if line.startswith("#goal"):
            goal_line = i

    level_dict["map_size"] = (
        len(_lines[initial_line + 1 : goal_line]),
        len(_lines[initial_line + 1].strip()),
    )

    _color_lines = _lines[colors_line + 1 : initial_line]

    _agents_dict, _boxes_dict = categorize_level_elements(_color_lines)

    # dicts need to be converted to json strings to be stored in the database
    level_dict["agents"] = json.dumps(_agents_dict)
    level_dict["boxes"] = json.dumps(_boxes_dict)

    _init_state_lines = _lines[initial_line + 1 : goal_line]

    agent_count, box_count = count_level_elements(_init_state_lines)
    level_dict["agent_count"] = agent_count
    level_dict["box_count"] = box_count

    return level_dict


def get_level_initial_state(lvl_lines_: list):
    state_lines = []
    _add_line = False

    for line in lvl_lines_:
        if line.startswith("#goal"):
            break
        if _add_line:
            state_lines.append(line[:-1])
        if line.startswith("#initial"):
            _add_line = True

    return state_lines


def get_level_goal_state(lvl_lines_: list):
    state_lines = []
    _add_line = False

    for line in lvl_lines_:
        if line.startswith("#end"):
            break
        if _add_line:
            state_lines.append(line[:-1])
        if line.startswith("#goal"):
            _add_line = True

    return state_lines


def get_levels_df():
    _LEVELS_PATH = f"{get_project_root()}/prog_proj/levels"

    levels_df = pd.DataFrame(columns=["levelname", "map_size", "agents", "boxes"])

    for level in os.listdir(_LEVELS_PATH):
        if level.endswith(".lvl"):
            level_dict = get_level_info(f"{_LEVELS_PATH}/{level}")
            _l_df = pd.DataFrame.from_dict(level_dict, orient="index").T
            levels_df = pd.concat([levels_df, _l_df])

    return levels_df


if __name__ == "__main__":
    pass
