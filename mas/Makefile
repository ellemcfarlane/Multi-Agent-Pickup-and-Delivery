# source ../myvenv/bin/activate
# you need to create your own env_vars.mk file in same dir
# as the Makefile to include
# GUROBI_JAR, LD_LIBRARY_PATH, GRB_LICENSE_FILE
include env_vars.mk

SPEED := 150
TIMEOUT := 180
RAM := -Xmx8g
algo := astar
l := Goofy
heur := manhattan
MAIN_SUB_PATH := src/main/java
MAIN_DIR := src/main/java/com/smac/mas
TEST_DIR := ${MAIN_DIR}/tests
bench_levels := benchmarks/only_agents_levels.txt
test_levels_file := ${TEST_DIR}/test_levels.txt
log_file := benchmarks/raw/warmup.log
test_log_file := ${TEST_DIR}/test_levels.log
test_levels_dir := automatically_generated_test_levels
GUI := -g
LEVEL_DIR := complevels
SERVER := src/main/resources/server.jar
MVN_JAR := target/mas-1.0-SNAPSHOT.jar
SRC_CODE := .
JAVA_MVN_CMD := java -cp ${MAIN_SUB_PATH}:${GUROBI_JAR}:${MVN_JAR} $(RAM) com.smac.mas.searchclient.SearchClient -$(algo) -$(heur)
JAVA_CMD := java -cp ${MAIN_SUB_PATH}:${GUROBI_JAR} $(RAM) com.smac.mas.searchclient.SearchClient -$(algo) -$(heur)
DBG_CMD := java -cp ${MAIN_SUB_PATH}:${GUROBI_JAR} $(RAM) -agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=y com.smac.mas.searchclient.SearchClient -$(algo) -$(heur)
DBG_MVN_CMD := java -cp ${MAIN_SUB_PATH}:${GUROBI_JAR}:${MVN_JAR} $(RAM) -agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=y com.smac.mas.searchclient.SearchClient -$(algo) -$(heur)
SERVER_CMD := java -jar ${SERVER} -l ${MAIN_DIR}/$(LEVEL_DIR)/$(l).lvl
COMP_RESULTS := ${TEST_DIR}/results/comp_latest.json
TESTER_BIN := python3 ${TEST_DIR}/tester.py
PARSE_TEST_LOG_CMD := ${TESTER_BIN} -d -f "$(test_log_file)" -b ${TEST_DIR}/results/latest.json
PARSE_COMP_LOG_CMD := ${TESTER_BIN} -d -f "$(test_log_file)" -b ${COMP_RESULTS} -c

ec:
	echo ${LD_LIBRARY_PATH}
	ls src/lib/resources/gurobi.lic

# TODO: write install_gurobi.sh based on
# https://support.gurobi.com/hc/en-us/articles/4534161999889-How-do-I-install-Gurobi-Optimizer-
install-gurobi:
	chmod +x install_gurobi.sh
	./install_gurobi.sh
	cp src/lib/resources/gurobi.lic ${GRB_LICENSE_FILE}
	ls ${GRB_LICENSE_FILE}

# e.g. make run l=MAPF00 algo=astar
run: clean build
	java -jar ${SERVER} -l ${MAIN_DIR}/$(LEVEL_DIR)/$(l).lvl -c "${JAVA_CMD}" $(GUI) -s $(SPEED) -t $(TIMEOUT)

no-server: clean build
	"${JAVA_CMD}"

# NOTE: main diff is specifying the library path
run-ci: clean build
	java -jar ${SERVER} -l ${MAIN_DIR}/$(LEVEL_DIR)/$(l).lvl -c "java -Djava.library.path=${LD_LIBRARY_PATH} -cp ${MAIN_SUB_PATH}:${GUROBI_JAR} $(RAM) com.smac.mas.searchclient.SearchClient -$(algo) -$(heur)" $(GUI) -s $(SPEED) -t $(TIMEOUT)

build:
	javac ${MAIN_DIR}/searchclient/*.java ${MAIN_DIR}/mapf/*.java ${MAIN_DIR}/localsearch/*.java -cp ${GUROBI_JAR}

clean:
	rm -rf ${MAIN_DIR}/searchclient/*.class ${MAIN_DIR}/mapf/*.class ${MAIN_DIR}/localsearch/*.class

# see readme on how to use this
debug: build
	java -jar ${SERVER} -l ${MAIN_DIR}/$(LEVEL_DIR)/$(l).lvl -c "${DBG_CMD}" -s $(SPEED)

runm: cleanm buildm
	java -jar ${SERVER} -l ${MAIN_DIR}/$(LEVEL_DIR)/$(l).lvl -c "${JAVA_MVN_CMD}" $(GUI) -s $(SPEED) -t $(TIMEOUT)

buildm:
	mvn package

testm:
	mvn test

cleanm:
	mvn clean

# see readme on how to use this
debugm: build
	java -jar ${SERVER} -l ${MAIN_DIR}/$(LEVEL_DIR)/$(l).lvl -c "${DBG_MVN_CMD}" -s $(SPEED)

test: test_levels_file := ${TEST_DIR}/test_levels.txt
test: LEVEL_DIR := levels/
test: clean build test-prep
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)
	${TESTER_BIN} -d -f "$(test_log_file)" -b "${TEST_DIR}/results/latest.json" -c

perf-test: test_levels_file := ${TEST_DIR}/test_levels.txt
perf-test: LEVEL_DIR := levels/
perf-test: cleanm buildm test-prep
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_MVN_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)
	${TESTER_BIN} -d -f "$(test_log_file)" -b "${TEST_DIR}/results/latest.json" -c

test-parse:
	${PARSE_TEST_LOG_CMD}
	
test-parse-comp:
	${PARSE_COMP_LOG_CMD}

test-save:
	python3 tests/tester.py -f "$(test_log_file)"

test-save-comp:
	python3 tests/tester.py -f "$(test_log_file)" -b ${COMP_RESULTS} -c

test-comp: test_levels_file := ${TEST_DIR}/comp.txt
test-comp: LEVEL_DIR := complevels/
test-comp: clean build test-prep
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_MVN_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)
	${TESTER_BIN} -d -f "$(test_log_file)" -b "${TEST_DIR}/results/comp_latest.json" -c

test-compm: test_levels_file := ${TEST_DIR}/comp.txt
test-compm: LEVEL_DIR := complevels/
test-compm: clean build test-prep
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)
	${TESTER_BIN} -d -f "$(test_log_file)" -b "${TEST_DIR}/results/comp_latest.json" -c

cleanwin:
	DEL "searchclient\*.class" /S /Q

help:
	java -jar ${SERVER} -h

# benchmark-final-levels: build
# 	java -jar ${SERVER} -l complevels_new/ -c "java -cp ${GUROBI_JAR}:${MVN_JAR} $(RAM) searchclient.SearchClient -astar -$(heur)" -t 180 -o "Goofy.zip"

test-prep:
	rm -rf $(test_log_file)
	rm -rf $(test_levels_dir)
	mkdir -p $(test_levels_dir)
	cat $(test_levels_file) | xargs -I % sh -c 'cp ${MAIN_DIR}/${LEVEL_DIR}/$$(basename %) ${test_levels_dir}'

test-template: test-prep
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)

test-ben: test_levels_file := ${TEST_DIR}/san.txt
test-ben: LEVEL_DIR := complevels/
test-ben: test-prep
	java -jar ${SERVER} -l ${test_levels_dir} -c "java -cp ${SRC_CODE}:${GUROBI_JAR} $(RAM) searchclient.SearchClient -astar -$(heur)" -t 180 -o "Goofy.zip"

test-comp21: test_levels_file := ${TEST_DIR}/comp21.txt
test-comp21: LEVEL_DIR := complevels21
test-comp21: build
	rm -rf $(test_log_file)
	rm -rf $(test_levels_dir)
	mkdir -p $(test_levels_dir)
	cat $(test_levels_file) | xargs -I % cp % ${test_levels_dir}
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)
	python3 ${TEST_DIR}/tester.py -d -f "$(test_log_file)"

test-comm: test_levels_file := ${TEST_DIR}/comm.txt
test-comm: build
	rm -rf $(test_log_file)
	rm -rf $(test_levels_dir)
	mkdir -p $(test_levels_dir)
	cat $(test_levels_file) | xargs -I % cp % ${test_levels_dir}
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)
	python3 ${TEST_DIR}/tester.py -d -f "$(test_log_file)"

test-rsc: test_levels_file := ${TEST_DIR}/rsc.txt
test-rsc: build
	rm -rf $(test_log_file)
	rm -rf $(test_levels_dir)
	mkdir -p $(test_levels_dir)
	cat $(test_levels_file) | xargs -I % cp % ${test_levels_dir}
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)
	python3 ${TEST_DIR}/tester.py -d -f "$(test_log_file)"

test-follow: test_levels_file := ${TEST_DIR}/follow.txt
test-follow: build
	rm -rf $(test_log_file)
	rm -rf $(test_levels_dir)
	mkdir -p $(test_levels_dir)
	cat $(test_levels_file) | xargs -I % cp % ${test_levels_dir}
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)
	python3 ${TEST_DIR}/tester.py -d -f "$(test_log_file)"

test-fat: test_levels_file := tests/fat.txt
test-fat: build
	rm -rf $(test_log_file)
	rm -rf $(test_levels_dir)
	mkdir -p $(test_levels_dir)
	cat $(test_levels_file) | xargs -I % cp % ${test_levels_dir}
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)
	python3 tests/tester.py -d -f "$(test_log_file)"

test-masimple: test_levels_file := tests/masimple.txt
test-masimple: build
	rm -rf $(test_log_file)
	rm -rf $(test_levels_dir)
	mkdir -p $(test_levels_dir)
	cat $(test_levels_file) | xargs -I % cp % ${test_levels_dir}
	java -jar ${SERVER} -l ${test_levels_dir} -c "${JAVA_CMD}" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(test_log_file)
	python3 tests/tester.py -d -f "$(test_log_file)"

#### WARNING ALL OF THE BELOW COMMANDS HAVE NOT BEEN UPDATED, NO GAURANTEE THEY WORK ####

# bench_pre_step:
# 	rm -f $(log_file)

# # benchmark just one level -> warmup.log
# bench_one: build bench_pre_step
# 	@java -jar ${SERVER} -l $(LEVEL_DIR)/$(l).lvl -c "java $(RAM) searchclient.SearchClient -$(algo) -$(heur)" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(log_file)
# 	python3 benchmarks/stats_collector.py


# # benchmark all levels in benchmarklevels -> warmup.log
# # NOTE: GUI cannot run with level-directory as input
# bench: build bench_pre_step
# 	rm -rf benchmarklevels
# 	mkdir -p benchmarklevels
# 	cat $(bench_levels) | xargs -I % cp % benchmarklevels
# 	java -jar ${SERVER} -l benchmarklevels -c "java $(RAM) searchclient.SearchClient -$(algo) -$(heur)" -s $(SPEED) -t $(TIMEOUT) 2>&1 | tee -a $(log_file)
# 	python3 benchmarks/stats_collector.py $(CRATES)
# 	python3 benchmarks/stats_formatter.py $(CRATES) -s $(algo) -heur $(heur)


# bench_crates: bench_levels=benchmarks/crate_levels.txt
# bench_crates: CRATES=--crates
# bench_crates: bench

# # TODO add rule for benchmarking all levels, for now have to run bench then bench_crates

# test_tests:
# 	python3 tests/test_tester.py

# # TODO: fix this, doesn't work with multiple algos or heuristics
# write_stats:
# 	python3 benchmarks/stats_formatter.py -s $(algo) -heur $(heur)

# check_heur:
# 	python3 benchmarks/heuristic_checks.py -heur $(heur)

# test_stats:
# 	python3 benchmarks/test_stats_collector.py

# # build:
# # 	javac searchclient/*.java mapf/*.java localsearch/*.java -cp ${GUROBI_JAR}

