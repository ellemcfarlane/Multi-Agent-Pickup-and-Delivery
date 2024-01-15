import tester

# tests tester.py

if __name__ == "__main__":
    expected = {
        # solved
        "MAPF00": {
            "solution_length": "14",
            "solved": True,
            "time/s": "0,032",
            "heuristic": "manhattan",
            "strategy": "astar",
        },
        # client thinks it solved level but it didn't
        "Communication26": {
            "solution_length": "16",
            "solved": False,
            "time/s": "1,114",
        },
        # timeout
        "MATest3": {
            "solution_length": "-",
            "solved": False,
            "time/s": "-",
            "strategy": "astar",
        },
        "RSC1": {
            "solution_length": "-",
            "solved": False,
            "time/s": "-",
            "strategy": "astar",
            "exception": 'Exception in thread "main" java.lang.RuntimeException: Not yet implemented!\n',
        },
    }

    level_to_stats, strategy, heuristic = tester.parse_log_file("tests/test.log")
    for level in expected:
        if expected[level] != level_to_stats[level]:
            print(f"NOT EQUAL for level {level}")
            print("expected\n", expected[level])
            print("actual\n", level_to_stats[level])
            print()
