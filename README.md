
# Multi-Agent-Pickup-and-Delivery
This is a multi-agent pickup and delivery solver for a custom domain with the following architecture: 
<p align="center">
  <img src="mas/media/architecture_black.png?raw=true" alt="MAS architecture"/>
</p>

uses:
* modified version of conflict-based search (CBS) with FOCAL search to handle resource conflicts and communication
* integer linear programming to assign packages to teams based on color, distance, and ordering constraints

# Installation
* install gurobi optimizer at https://support.gurobi.com/hc/en-us/articles/4534161999889-How-do-I-install-Gurobi-Optimizer-
* create env_vars.mk file in top level directory with the following contents:
```
GUROBI_HOME := /Library/gurobi1001/macos_universal2
PATH := "${PATH}:${GUROBI_HOME}/bin"
GUROBI_JAR := ${GUROBI_HOME}/lib/gurobi.jar
LD_LIBRARY_PATH := "${LD_LIBRARY_PATH}:${GUROBI_HOME}/lib" # note: you may have to change : -> ; if on Windows
GRB_LICENSE_FILE := /Library/gurobi1001/gurobi.lic
```

# Running
* `make run` with defaults
* or e.g. `make run l=MAPF00 algo=astar heur=manhattan` where l is level in mas/src/main/java/com/smac/mas/levels

# Testing
* edit mas/src/main/java/com/smac/mas/tests/test_levels.txt to inclue the levels you want to test
* run `make test`

Before merging new code, please:
1. add whatever new test level files to test_levels.txt
2. `make test TIMEOUT=1` to see everything is good
3. `make test-save TIMEOUT=1` which will override latest.json and add a copy as datetime.json in results/ so be careful!

If you are getting timeouts for slow levels, either remove them bc they are probably not unit-test like or bump TIMEOUT.

If you want to test your own custom levels, create a file in tests/ and then run:
`make test test_levels_file=tests/<your file>`

# Debugging (from cli)
* after running jdb -attach, should see "VM Started: > No frames ... main[1]", then you can run:
* > `stop at searchclient.SearchClient:<line-number>`, e.g. stop at searchclient.SearchClient:111
* > `run`
* to debug other classes called by SearchClient, you will have to step into the line called within SearchClient.main and then, e.g.:
* `stop in searchclient.Heuristic.h` to debug the h method in this class
* you can double check jdb has loaded the class you want to debug
* by running `classes` and ensuring it shows at the end of the output
* note: client never times out in debug mode
* `make debug`, then either in another terminal, run "jdb -attach 5005" (see notes below for tips on debugging from command-line)
* or create a debugging agent in an IDE, e.g. intellij and set the port to 5005 and debug from there

# Development:
```
cd Multi-Agent-Pickup-and-Delivery (aka top level git dir)
mvn archetype:generate -DgroupId=com.smac.mas -DartifactId=mas -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
```

# Credits
Team members: Elle McFarlane, Adam Bosák, Harsh Vardhan Rai, Alan Mansour
