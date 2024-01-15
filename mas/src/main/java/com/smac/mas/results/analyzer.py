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

    with open(log_file_name, "r") as f:
        # read every 5 lines in f:

        for i, line in enumerate(f):
            if ":" in line:
                key, value = line.split(": ")
                key = key.strip()
                value = value.strip()
                if key == "Level name":
                    curr_level = value
                    level_to_stats[curr_level] = {}
                else:
                    level_to_stats[curr_level][key] = value

    return level_to_stats


def length2int(sol_leng):
    # # remove any "." from the string
    # if "." in sol_leng:
    #     sol_leng = sol_leng.replace(".", "")
    # elif "," in sol_leng:
    #     sol_leng = sol_leng.replace(",", "")
    # # remove none digits
    sol_leng = "".join([i for i in sol_leng if i.isdigit()])
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

    level_to_stats = parse_log_file(read_file)

    WRITE_DIR = "tests/results/"
    now = datetime.datetime.now()
    write_file = f"{WRITE_DIR}{now.strftime('%Y-%m-%d_%H-%M-%S')}{'_comp' if args.comp else ''}.json"

    # baseline_results = {}
    # with open(args.baseline, "r") as f:
    #     baseline_results = json.load(f)

    # fails = 0
    # total_time = 0
    passed_levels = []
    for level in level_to_stats:
        stats = level_to_stats[level]
        if stats['Level solved'] == "Yes" or stats['Level solved'] == "Yes.":
            passed_levels.append(level)
    
    passed_levels = sorted(passed_levels, key=lambda x: length2int(level_to_stats[x]['Actions used']))
    print("Passed levels:", len(passed_levels))
    for level in passed_levels:
        print(f"{level} len: {level_to_stats[level]['Actions used']}, time: {level_to_stats[level]['Time to solve']}")
