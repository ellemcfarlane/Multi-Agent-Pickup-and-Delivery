import json
import argparse
import datetime

# don't use this script directly, run 'make test' and 'make test-save' to save results before merging
# the make commands will run levels in test_levels.txt and then run this script to parse the log file
# and make sure the results are as expected

# TODO (emcfarlane): update to use regex instead of string parsing


# NOTE: for now assumes strategy and heuristic are constant throughout log file but level changes
# detects strategy and heuristic from log file
def parse_log_file(log_file_name):
    # Read the input file
    level_to_stats = {}
    curr_level = None
    heuristic = None
    strategy = None
    with open(log_file_name, "r") as f:
        for line in f:
            if "levels/" in line and "cp" not in line and "rm" not in line:
                curr_level = line.split("levels/")[1].strip().split(".")[0]
                level_to_stats[curr_level] = {}

            if "evaluation." in line:
                strategy = line.split("evaluation.")[0].strip().split(" ")[-1]
            elif "search." in line:
                strategy = line.strip().split(" ")[1]
            if strategy:
                if strategy == "breadth-first":
                    strategy = "bfs"
                elif strategy == "depth-first":
                    strategy = "dfs"
                elif strategy == "A*":
                    strategy = "astar"
                level_to_stats[curr_level]["strategy"] = strategy

            if "heuristic method:" in line:
                if "invalid" in line:
                    heuristic = "invalid"
                else:
                    heuristic = line.split("heuristic method:")[1].strip()
                    level_to_stats[curr_level]["heuristic"] = heuristic

            # no longer indicates anything useful for us unless we sum Generated
            # if "#Generated:" in line:
            #     num_gen = line.split("#Generated:")[1].strip().split(", ")[0]
            #     level_to_stats[curr_level]["generated"] = num_gen
            if "Found solution of length" in line:
                sol_length = line.split("Found solution of length")[1].strip()[:-1]
                level_to_stats[curr_level]["solution_length"] = sol_length

            if "Time to solve:" in line:
                time_to_solve = line.split("Time to solve:")[1].strip().split(" ")[0]
                if time_to_solve == "0,000":
                    time_to_solve = "-"
                    level_to_stats[curr_level]["solution_length"] = "-"
                level_to_stats[curr_level]["time/s"] = time_to_solve
            # NOTE: I'm defining a timeout as level not solved but this could
            # also simply be that our client did not solve the level but thought it did!
            if "Level solved:" in line and "Yes" not in line:
                level_to_stats[curr_level]["solved"] = False
            elif "Level solved:" in line and "Yes" in line:
                level_to_stats[curr_level]["solved"] = True

            if "Exception" in line or "exception" in line:
                level_to_stats[curr_level]["exception"] = line

            if "Unable to solve level" in line:
                level_to_stats[curr_level]["failed_to_find_sol"] = True

    return level_to_stats, strategy, heuristic


def length2int(sol_leng):
    # remove any "." from the string
    sol_leng = sol_leng.replace(".", "")
    return int(sol_leng)


if "__main__" == __name__:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "-f",
        "--file",
        default="tests/test.log",
        help="name of the log file to read from, defaults to raw/warmup.log",
    )
    parser.add_argument(
        "-b",
        "--baseline",
        default="tests/results/latest.json",
        help="file to compare solution length results to",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="default is test_<timestamp>.json",
    )
    parser.add_argument(
        "-d",
        "--dry_run",
        action="store_true",
        help="doesn't write to file",
    )
    parser.add_argument(
        "-v",
        "--verbose",
        action="store_true",
        help="prints results to console",
    )
    parser.add_argument(
        "-c",
        "--comp",
        action="store_true",
        help="prints results to console",
    )
    args = parser.parse_args()
    read_file = args.file

    level_to_stats, strategy, heuristic = parse_log_file(read_file)

    WRITE_DIR = "tests/results/"
    now = datetime.datetime.now()
    write_file = f"{WRITE_DIR}{now.strftime('%Y-%m-%d_%H-%M-%S')}{'_comp' if args.comp else ''}.json"

    if args.verbose:
        for level, stats in level_to_stats.items():
            print(level)
            for key, val in stats.items():
                print(f"{key}: {val}")
            print()

    baseline_results = {}
    with open(args.baseline, "r") as f:
        baseline_results = json.load(f)

    fails = 0
    total_time = 0
    passed_levels = []
    longer_sol = set()
    shorter_sol = set()
    for level in level_to_stats:
        stats = level_to_stats[level]
        if stats["time/s"] != "-":
            non_eur_str_time = stats["time/s"].replace(",", ".")
            total_time += float(non_eur_str_time)

        if "failed_to_find_sol" in stats:
            print(f"ERROR {level}: client failed to find a solution for level")
            fails += 1
        elif "exception" in stats:
            print(f"ERROR {level}: exception thrown: {stats['exception']}")
            fails += 1
        elif stats["solution_length"] != "-" and not stats["solved"]:
            sol_len = stats["solution_length"]
            print(
                f"ERROR {level}: client thinks it found sol. length {sol_len} but it failed to solve!"
            )
            fails += 1
        elif stats["time/s"] == "-":
            print(f"ERROR {level}: timeout for level {level}")
            fails += 1
        elif stats["solved"] == "no":
            print(f"ERROR {level}: failed to solve level {level}")
            fails += 1
        else:
            if level in baseline_results:
                prev_sol = baseline_results[level]["solution_length"]
                curr_sol = stats["solution_length"]
                if prev_sol == "-" and curr_sol != "-":
                    shorter_sol.add(level)
                elif prev_sol != "-" and curr_sol == "-":
                    longer_sol.add(level)
                elif length2int(stats["solution_length"]) > length2int(
                    baseline_results[level]["solution_length"]
                ):
                    longer_sol.add(level)
                elif length2int(stats["solution_length"]) < length2int(
                    baseline_results[level]["solution_length"]
                ):
                    shorter_sol.add(level)
            passed_levels.append(level)

    print(
        f"\ntotal time for client to find what it thinks is a solution for all levels: {total_time}"
    )
    passed_levels = sorted(passed_levels)
    n_levels = len(level_to_stats)

    if passed_levels:
        print(
            """
######################################
########### PASSED LEVELS ############
######################################
            """
        )
        for level in passed_levels:
            str = f"{level}"
            prev_sol_len = (
                baseline_results[level]["solution_length"]
                if level in baseline_results
                else None
            )
            curr_sol_len = level_to_stats[level]["solution_length"]
            if level in longer_sol:
                str += f" WARNING, now have longer solution than results in {args.baseline}. Was: {prev_sol_len}, now {curr_sol_len}"
            elif level in shorter_sol:
                str += f" Nice!! now have shorter solution than results in {args.baseline}. Was: {prev_sol_len}, now {curr_sol_len}"
            print(str)
        print(
            f"""
######################################
####### ^^^ {n_levels-fails}/{n_levels} PASSED ^^^ #########
######################################
            """
        )
    if fails == 0:
        print(f"\nAll {len(level_to_stats)} tests PASSED")
        if args.dry_run:
            print("before merging, please save results with 'make test-save'!")
    else:
        print(
            f"""\n\n\n
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%% {fails}/{n_levels} !!!FAILED!!! %%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            """
        )

    if longer_sol:
        print(
            f"\nWARNING: {len(longer_sol)} levels now have longer solution than {args.baseline}"
        )
    if shorter_sol:
        print(
            f"Positive updates: {len(shorter_sol)} levels now have shorter solution than {args.baseline}"
        )
    # unions between baseline and level_to_stats
    baseline_levels = set(baseline_results.keys())
    new_levels = set(level_to_stats.keys())
    new_levels_not_in_baseline = new_levels - baseline_levels
    if len(new_levels_not_in_baseline) > 0:
        print(
            f"\n{len(new_levels_not_in_baseline)} new levels not in old baseline {args.baseline}: {new_levels_not_in_baseline}"
        )
    print("compared results to: ", args.baseline)
    # WARNING: OVERRIDES LATEST IF NOT DRY RUN
    latest_file = (
        "tests/results/latest.json"
        if not args.comp
        else "tests/results/comp_latest.json"
    )
    if not args.dry_run:
        with open(write_file, "w") as f:
            json.dump(level_to_stats, f)
        with open(latest_file, "w") as f:
            json.dump(level_to_stats, f)
        print(f"Test results written to {write_file} and {latest_file}")
    assert fails == 0
