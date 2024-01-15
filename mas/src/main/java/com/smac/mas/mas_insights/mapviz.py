#!/usr/bin/env python
# Created by Jonathan Mikler on 21/April/23
import sys
import pygame
import json

# local
from mas_insights import AgentId, BoxType, COLORS, read_level_file,get_project_root,get_level_goal_state, get_level_initial_state, get_level_info


def main():
    print("Welcome to ChickenViz \U0001F423, your chicken MAS visualizer!")

    _root = get_project_root()


    # Get the level file name from the command line argument
    if len(sys.argv) > 1:
        _levelname = sys.argv[1]
        _filepath = f"{_root}/prog_proj/levels/{_levelname}"
    else:
        print("Please provide a level file name as an argument")
        return
    
    _level = read_level_file(_filepath)
    if not _level:
        print(f"Could not read level file: {_filepath}")
        return
    # Extract the initial and goal states
    initial_state = get_level_initial_state(_level)
    goal_state = get_level_goal_state(_level)

    # Define the size of the screen based on the size of the initial state
    num_rows = len(initial_state)
    num_cols = len(initial_state[0])
    cell_size = 30
    screen_width = num_cols * cell_size
    screen_height = num_rows * cell_size

    # print details of screen and level
    print("\nLevel details:")
    print(f"Level: {_levelname}")
    print(f"num_rows: {num_rows}, num_cols: {num_cols}")
    print(f"screen_width: {screen_width}, screen_height: {screen_height}")

    _game = True

    if _game:
        pygame.init()
        screen = pygame.display.set_mode((screen_width, screen_height))
        screen.fill(COLORS.get("white"))
        # Create a font object
        font = pygame.font.Font(None, 25)
        # set the title of the window
        pygame.display.set_caption(f"ChickenViz \U0001F423 | Level: {_levelname}")

    level_info = get_level_info(_filepath)
    _agents_dict = json.loads(level_info["agents"])
    _boxes_dict = json.loads(level_info["boxes"])

    def get_color(element_dict_:dict, element_id_:int)->str:
        # get color of given agent or box by id
        for color, elements_list in element_dict_.items():
            if element_id_ in elements_list:
                return color
    
    def get_agent_text(c_:str, init_state_:bool):
        if init_state_:
            _ac = get_color(element_dict_=_agents_dict, element_id_=int(c_))
            text = font.render(initial_state[row][col], True, COLORS.get('black') , COLORS.get(_ac))
        else:
            text = font.render(c_, True, COLORS.get('black'))
        return text

    # Draw the initial state on the screen
    for row in range(num_rows):
        for col in range(num_cols):
            _c_init = initial_state[row][col]
            _c_goal = goal_state[row][col]

            if _c_init == "+":
                pygame.draw.rect(screen, COLORS.get('light_gray'), [col*cell_size, row*cell_size, cell_size, cell_size],)
            elif _c_init.isnumeric(): # agents
                _ac = get_color(element_dict_=_agents_dict, element_id_=int(_c_init))
                # draw circle with agent id written in it
                pygame.draw.circle(screen, COLORS.get(_ac), (col*cell_size + cell_size//2, row*cell_size + cell_size//2), cell_size//2)
                text = get_agent_text(c_=_c_init, init_state_=True)
                screen.blit(text, (col*cell_size + 5, row*cell_size + 5))
            elif _c_init in BoxType._member_names_: # boxes
                _bc = get_color(element_dict_=_boxes_dict, element_id_=_c_init)
                # draw rectangle with boxtype written in it
                pygame.draw.rect(screen, COLORS.get(_bc), [col*cell_size, row*cell_size, cell_size, cell_size])
                text = font.render(initial_state[row][col], True, COLORS.get("black"))
                screen.blit(text, (col*cell_size + 5, row*cell_size + 5))
            else:
                pygame.draw.rect(screen, COLORS.get('black'), [col*cell_size, row*cell_size, cell_size, cell_size],1)

            if _c_goal.isnumeric(): # agents
                text = get_agent_text(c_=_c_goal, init_state_=False)
                pygame.draw.rect(screen, COLORS.get('yellow'), [col*cell_size, row*cell_size, cell_size, cell_size])
                screen.blit(text, (col*cell_size + 5, row*cell_size + 5))
            elif _c_goal in BoxType._member_names_:
                pygame.draw.rect(screen, COLORS.get('yellow'), [col*cell_size, row*cell_size, cell_size, cell_size])
                text = font.render(goal_state[row][col], True, COLORS.get("black"))
                screen.blit(text, (col*cell_size + 5, row*cell_size + 5))

    # # Update the display
    pygame.display.update()

    # Wait for the user to close the window
    _finish = False
    while not _finish:
        try:
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    pygame.quit()
                    quit()
        except:
            _finish = True
            pygame.quit()
            quit()

if __name__ == '__main__':
    main()
