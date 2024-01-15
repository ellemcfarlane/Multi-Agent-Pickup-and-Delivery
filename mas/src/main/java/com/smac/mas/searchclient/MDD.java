// package searchclient;

// import java.util.*;
// import mapf.Constraint;
// import mapf.IntervalConstraint;

// class QuadStruct {
//     int from_i;
//     int to_i;
//     int from_j;
//     int to_j;

//     QuadStruct(int i, int j, int k, int l) {
//         from_i = i;
//         to_i = j;
//         from_j = k;
//         to_j = l;
//     }

//     @Override
//     public boolean equals(Object o) {
//         if (o == this) {
//             return true;
//         }
//         if (!(o instanceof QuadStruct)) {
//             return false;
//         }
//         QuadStruct q = (QuadStruct) o;
//         return from_i == q.from_i &&
//                 to_i == q.to_i &&
//                 from_j == q.from_j &&
//                 to_j == q.to_j;
//     }

//     @Override
//     public int hashCode() {
//         return Objects.hash(this.from_i, this.to_i, this.from_j, this.to_j);
//     }
// }

// class ArrayKey {
//     private final int[] arr;

//     public ArrayKey(int[] arr) {
//         this.arr = arr.clone();
//     }

//     @Override
//     public boolean equals(Object o) {
//         if (this == o)
//             return true;
//         if (o == null || getClass() != o.getClass())
//             return false;
//         ArrayKey arrayKey = (ArrayKey) o;
//         return Arrays.equals(arr, arrayKey.arr);
//     }

//     @Override
//     public int hashCode() {
//         return Arrays.hashCode(arr);
//     }

//     @Override
//     public String toString() {
//         return Arrays.toString(arr);
//     }
// }

// class PositionTuple {
//     public final int agent;
//     public final int box;
//     public final int time;

//     public PositionTuple(int x) {
//         this.agent = x;
//         this.box = x;
//         this.time = 0;
//     }

//     public PositionTuple(int x, int y) {
//         this.agent = x;
//         this.box = y;
//         this.time = 0;
//     }

//     public PositionTuple(int x, int y, int t) {
//         this.agent = x;
//         this.box = y;
//         this.time = t;
//     }

//     public PositionTuple(PositionTuple t, int shift) {
//         this.agent = t.agent;
//         this.box = t.box;
//         this.time = t.time + shift;
//     }

//     public boolean positionEquals(PositionTuple other) {
//         return this.box == other.box;
//     }

//     @Override
//     public String toString() {
//         return "(" + agent + ", " + box + ", " + time + ")";
//     }

//     @Override
//     public boolean equals(Object obj) {
//         PositionTuple other = (PositionTuple) obj;
//         return this.agent == other.agent && this.box == other.box && this.time == other.time;
//     }

//     @Override
//     public int hashCode() {
//         return Objects.hash(this.agent, this.box, this.time);
//     }
// }

// public class MDD {

//     public ArrayList<ArrayList<PositionTuple>> node_t;
//     public ArrayList<ArrayList<ArrayList<Integer>>> outgoing_t;
//     public ArrayList<ArrayList<ArrayList<Integer>>> incoming_t;
//     public ArrayList<HashMap<PositionTuple, Integer>> position2Index;
//     public ArrayList<HashMap<Integer, ArrayList<Integer>>> agentPosition2Index;
//     public ArrayList<HashMap<Integer, ArrayList<Integer>>> boxPosition2Index;

//     public boolean isFat;
//     public PositionTuple goalPosition;
//     public PositionTuple start;
//     public int optimalTime;
//     public int currentExpansionTime;
//     private Queue<PositionTuple> openSet;
//     private HashMap<PositionTuple, Integer> fCost;
//     private HashMap<PositionTuple, ArrayList<PositionTuple>> searchTree;

//     public MDD(PositionTuple start, PositionTuple goalPosition, boolean isFat, boolean initialize) {
//         this.isFat = isFat;
//         this.goalPosition = new PositionTuple(goalPosition, 0);
//         this.start = new PositionTuple(start, 0);

//         Comparator<PositionTuple> comparator = new Comparator<PositionTuple>() {
//             @Override
//             public int compare(PositionTuple t1, PositionTuple t2) {
//                 int c1 = t1.time + heuristic(start);
//                 int c2 = t2.time + heuristic(start);
//                 return c1 - c2;
//             }
//         };

//         openSet = new PriorityQueue<>(comparator);
//         fCost = new HashMap<PositionTuple, Integer>();
//         searchTree = new HashMap<>();
//         if (initialize)
//             createMDD();
//     }

//     int expandSearchTree(int cost) {
//         if (openSet.isEmpty()) {
//             System.err.println("Error : Open set is empty");
//             return -1;
//         }

//         while (!openSet.isEmpty()) {
//             PositionTuple current = openSet.poll();

//             int gCost = fCost.get(current) - heuristic(current);
//             assert (gCost == current.time); // Remove this later

//             if (cost < current.time) {
//                 // Tree expansion complete
//                 openSet.add(current);
//                 break;
//             }

//             if (cost == Integer.MAX_VALUE) {
//                 if (current.positionEquals(goalPosition)) {
//                     cost = current.time;
//                 }
//             }

//             PositionTuple[] newStates = expandState(current, current.time + 1);

//             for (PositionTuple state : newStates) {
//                 int new_cost = state.time + heuristic(state);
//                 int old_cost = fCost.getOrDefault(state, Integer.MAX_VALUE);

//                 if (old_cost != Integer.MAX_VALUE && new_cost < old_cost) {
//                     System.err.println("New cost is less than old cost. This should not happen");
//                     fCost.put(state, new_cost);
//                     openSet.add(state);
//                     ArrayList<PositionTuple> p = new ArrayList<>();
//                     p.add(current);
//                     searchTree.put(state, p);
//                 }

//                 if (searchTree.containsKey(state)) {
//                     searchTree.get(state).add(current);
//                 } else {
//                     ArrayList<PositionTuple> p = new ArrayList<>();
//                     p.add(current);
//                     searchTree.put(state, p);
//                     openSet.add(state);
//                     fCost.put(state, new_cost);
//                 }
//             }
//         }

//         return cost;
//     }

//     private static void fillFromTree(MDD mdd1, MDD mdd2) {
//         int t = mdd2.currentExpansionTime;
//         Queue<PositionTuple> bfsQueue = new LinkedList<>();
//         bfsQueue.add(mdd2.goalPosition);
//         int size = 0;

//         while (!bfsQueue.isEmpty()) {
//             PositionTuple current = bfsQueue.poll();
//             int currentCost = current.time;

//             if (t == currentCost) {
//                 if (--t >= 0)
//                     size = mdd2.node_t.get(t).size();
//             }

//             ArrayList<PositionTuple> neighbor = mdd1.searchTree.get(current);
//             for (PositionTuple n : neighbor) {

//                 if (!mdd2.position2Index.get(t).containsKey(n)) {
//                     mdd2.position2Index.get(t).put(n, size);

//                     if (!mdd2.agentPosition2Index.get(t).containsKey(n.agent)) {
//                         ArrayList<Integer> list = new ArrayList<>();
//                         list.add(size);
//                         mdd2.agentPosition2Index.get(t).put(n.agent, list);
//                     } else {
//                         mdd2.agentPosition2Index.get(t).get(n.agent).add(size);
//                     }

//                     if (!mdd2.boxPosition2Index.get(t).containsKey(n.box)) {
//                         ArrayList<Integer> list = new ArrayList<>();
//                         list.add(size);
//                         mdd2.boxPosition2Index.get(t).put(n.box, list);
//                     } else {
//                         mdd2.boxPosition2Index.get(t).get(n.box).add(size);
//                     }

//                     mdd2.node_t.get(t).add(n);
//                     mdd2.outgoing_t.get(t).add(new ArrayList<>());
//                     mdd2.incoming_t.get(t).add(new ArrayList<>());
//                     bfsQueue.add(n);
//                     size++;
//                 }

//                 int from = mdd2.position2Index.get(t).get(n);
//                 int to = mdd2.position2Index.get(t + 1).get(current);
//                 mdd2.outgoing_t.get(t).get(from).add(to);
//                 mdd2.incoming_t.get(t + 1).get(to).add(from);
//             }
//         }
//     }

//     /*
//      * Algorithm to create all possible shortest path graph given consistent
//      * heuristic
//      * Runs A* until we pop higher than optimal cost node from openSet
//      * Proof : Assume there still remains a shortest path after above mentioned
//      * That path must go thorugh atleast one member in the openSet. Let x be the
//      * first such member
//      * Since heuristic is consistent, Then gCost of x in openSet is optimal, hence
//      * the the cost of the path from
//      * source to destination through x would be atleast min{Openset} > optimalCost
//      * which is contradiction
//      */
//     private void createMDD() {
//         searchTree.put(start, new ArrayList<>());
//         openSet.add(start);
//         fCost.put(start, heuristic(start));
//         optimalTime = Integer.MAX_VALUE;
//         currentExpansionTime = -1;

//         optimalTime = expandSearchTree(optimalTime);
//         goalPosition = new PositionTuple(goalPosition, optimalTime);

//         if (optimalTime == Integer.MAX_VALUE) {
//             System.err.println("Error : Optimal time is MAX_VALUE");
//             return;
//         }

//         currentExpansionTime = optimalTime;
//         initializeArray();
//         ArrayList<Integer> list = new ArrayList<>();
//         list.add(0);
//         node_t.get(optimalTime).add(goalPosition);
//         outgoing_t.get(optimalTime).add(new ArrayList<>());
//         incoming_t.get(optimalTime).add(new ArrayList<>());
//         position2Index.get(optimalTime).put(goalPosition, 0);
//         agentPosition2Index.get(optimalTime).put(goalPosition.agent, list);
//         boxPosition2Index.get(optimalTime).put(goalPosition.box, (ArrayList<Integer>) list.clone());
//         fillFromTree(this, this);
//     }

//     PositionTuple[] expandState(PositionTuple t, int time) {

//         Node agent = GraphUtils.node_list[t.agent];
//         if (isFat) {
//             Node box = GraphUtils.node_list[t.box];
//             PositionTuple[] nextStates = new PositionTuple[agent.neighbor.size() + box.neighbor.size() - 1];
//             int i = 0;

//             // NO-OP action
//             nextStates[i++] = new PositionTuple(t.agent, t.box, time);

//             // Pull action
//             for (int n : agent.neighbor) {
//                 if (t.box != n) {
//                     nextStates[i] = new PositionTuple(n, t.agent, time);
//                     i++;
//                 }
//             }

//             // Push action
//             for (int n : box.neighbor) {
//                 if (t.agent != n) {
//                     nextStates[i] = new PositionTuple(t.box, n, time);
//                     i++;
//                 }
//             }

//             return nextStates;
//         } else {
//             PositionTuple[] nextStates = new PositionTuple[agent.neighbor.size() + 1];
//             int i = 0;

//             // NO-OP action
//             nextStates[i++] = new PositionTuple(t.agent, t.box, time);

//             // move action
//             for (int n : agent.neighbor) {
//                 nextStates[i] = new PositionTuple(n, n, time);
//                 i++;
//             }

//             return nextStates;
//         }
//     }

//     void expandDataStructures() {
//         node_t.add(new ArrayList<>());
//         outgoing_t.add(new ArrayList<>());
//         incoming_t.add(new ArrayList<>());
//         position2Index.add(new HashMap<>());
//         agentPosition2Index.add(new HashMap<>());
//         boxPosition2Index.add(new HashMap<>());
//         this.goalPosition = new PositionTuple(goalPosition, 1);
//         int size = node_t.size();
//         this.node_t.get(size - 1).add(this.goalPosition);
//         this.position2Index.get(size - 1).put(this.goalPosition, 0);
//         ArrayList<Integer> list = new ArrayList<>();
//         list.add(0);
//         this.agentPosition2Index.get(size - 1).put(this.goalPosition.agent, list);
//         this.boxPosition2Index.get(size - 1).put(this.goalPosition.box, (ArrayList<Integer>) list.clone());
//         this.outgoing_t.get(size - 1).add(new ArrayList<>());
//         this.incoming_t.get(size - 1).add(new ArrayList<>());
//         currentExpansionTime++;
//         assert (this.node_t.size() == currentExpansionTime + 1); // remove later
//     }

//     private int heuristic(PositionTuple current) {
//         return GraphUtils.min_distance[current.box][goalPosition.box];
//     }

//     public void unitExpandMDD(MDD mdd) {
//         if (currentExpansionTime == 0) {
//             System.err.println("MDD not initialized");
//             return;
//         }

//         if (this != mdd) {
//             while (mdd.currentExpansionTime < currentExpansionTime + 1)
//                 mdd.unitExpandMDD(mdd);
//         } else {
//             expandSearchTree(currentExpansionTime + 1);
//         }

//         expandDataStructures();

//         assert (currentExpansionTime == goalPosition.time); // Remove this later
//         fillFromTree(mdd, this);
//     }

//     private void initializeArray() {
//         int t = currentExpansionTime + 1;
//         node_t = new ArrayList<>(t);
//         for (int i = 0; i < t; i++) {
//             node_t.add(new ArrayList<>());
//         }
//         outgoing_t = new ArrayList<>(t);
//         for (int i = 0; i < t; i++) {
//             outgoing_t.add(new ArrayList<>());
//         }
//         incoming_t = new ArrayList<>(t);
//         for (int i = 0; i < t; i++) {
//             incoming_t.add(new ArrayList<>());
//         }
//         position2Index = new ArrayList<>(t);
//         for (int i = 0; i < t; i++) {
//             position2Index.add(new HashMap<>());
//         }
//         agentPosition2Index = new ArrayList<>(t);
//         for (int i = 0; i < t; i++) {
//             agentPosition2Index.add(new HashMap<>());
//         }
//         boxPosition2Index = new ArrayList<>(t);
//         for (int i = 0; i < t; i++) {
//             boxPosition2Index.add(new HashMap<>());
//         }
//     }

//     public MDD view(int time) {
//         assert (optimalTime <= time && time <= currentExpansionTime);
//         int delta = time - currentExpansionTime;
//         PositionTuple newGoalPosition = new PositionTuple(goalPosition, delta);
//         MDD newView = new MDD(start, newGoalPosition, isFat, false);
//         newView.currentExpansionTime = time;
//         newView.optimalTime = optimalTime;
//         newView.initializeArray();
//         int size = newView.node_t.size();
//         newView.node_t.get(size - 1).add(newView.goalPosition);
//         newView.position2Index.get(size - 1).put(newView.goalPosition, 0);
//         ArrayList<Integer> list = new ArrayList<>();
//         list.add(0);
//         newView.agentPosition2Index.get(size - 1).put(newView.goalPosition.agent, list);
//         newView.boxPosition2Index.get(size - 1).put(newView.goalPosition.box, (ArrayList<Integer>) list.clone());
//         newView.outgoing_t.get(size - 1).add(new ArrayList<>());
//         newView.incoming_t.get(size - 1).add(new ArrayList<>());
//         fillFromTree(this, newView);
//         assert (newView.currentExpansionTime == size - 1); // Remove this later

//         return newView;
//     }

//     private static boolean hasNonEmptyIntersection(PositionTuple t1, PositionTuple t2) {
//         return t1.agent == t2.agent ||
//                 t1.agent == t2.box ||
//                 t1.box == t2.agent ||
//                 t1.box == t2.box;
//     }

//     private static boolean reverseDFS(int currentLevel,
//             int currentNode,
//             MDD M,
//             int targetLevel,
//             PositionTuple notInclude,
//             boolean[] notActive) {

//         if (currentLevel == targetLevel) {
//             return !notActive[currentNode];
//         }

//         for (int nextNode : M.incoming_t.get(currentLevel).get(currentNode)) {
//             if (hasNonEmptyIntersection(M.node_t.get(currentLevel - 1).get(nextNode), notInclude))
//                 continue;

//             if (reverseDFS(currentLevel - 1, nextNode, M, targetLevel, notInclude, notActive)) {
//                 return true;
//             }
//         }

//         return false;
//     }

//     private static int classifyConflict(MDD M1,
//             MDD M2,
//             Tuple<ArrayList<int[]>[], HashSet<QuadStruct>[]> MDDMutexes) {

//         if (M1.node_t.size() > M2.node_t.size()) {
//             return classifyConflict(M2, M1, MDDMutexes);
//         }

//         ArrayList<int[]>[] node_mutex = MDDMutexes.x;
//         int size = node_mutex.length;
//         int numNodes = M2.node_t.get(size - 1).size();
//         // System.err.println("Size: " + (M1.node_t.size() - 1) + " " +
//         // (M2.node_t.size() - 1));
//         // System.err.println("Comp: " + node_mutex[size - 1].size() + " " + numNodes);

//         if (node_mutex[size - 1].size() == numNodes) {
//             return -1; // PC
//         }

//         boolean[] notActive = new boolean[numNodes];
//         for (int[] mutex : node_mutex[size - 1]) {
//             notActive[mutex[1]] = true;
//         }

//         if (reverseDFS(M2.node_t.size() - 1,
//                 0,
//                 M2,
//                 size - 1,
//                 M1.node_t.get(size - 1).get(0),
//                 notActive)) {
//             // NC
//             return 0;
//         } else {
//             // AC
//             return -1;
//         }
//     }

//     private static int propogateAndFind(MDD M1, MDD M2) {
//         var mutex = proprogateMutex(M1, M2, false);
//         return classifyConflict(M1, M2, mutex);
//     }

//     private static ArrayList<Integer> getUnionPositions(PositionTuple P, MDD mdd, int time) {
//         ArrayList<Integer> arr1 = mdd.agentPosition2Index.get(time).get(P.agent);
//         ArrayList<Integer> arr2 = mdd.boxPosition2Index.get(time).get(P.box);

//         if (arr1 == null && arr2 == null) {
//             return new ArrayList<>();
//         } else if (arr1 == null) {
//             return arr2;
//         } else if (arr2 == null) {
//             return arr1;
//         }

//         ArrayList<Integer> unionPositions = new ArrayList<>();
//         // Remove this later
//         // check if arr1 and arr2 are sorted
//         for (int i = 1; i < arr1.size(); i++) {
//             if (arr1.get(i) < arr1.get(i - 1)) {
//                 System.err.println("array not sorted!");
//                 return null;
//             }
//         }
//         // Remove this later
//         // check if arr1 and arr2 are sorted
//         for (int i = 1; i < arr2.size(); i++) {
//             if (arr2.get(i) < arr2.get(i - 1)) {
//                 System.err.println("array not sorted!");
//                 return null;
//             }
//         }

//         // merge two sorted arrays into unionPositions
//         int i = 0, j = 0;
//         while (i < arr1.size() && j < arr2.size()) {
//             if (arr1.get(i) < arr2.get(j)) {
//                 unionPositions.add(arr1.get(i));
//                 i++;
//             } else if (arr1.get(i) > arr2.get(j)) {
//                 unionPositions.add(arr2.get(j));
//                 j++;
//             } else {
//                 unionPositions.add(arr1.get(i));
//                 i++;
//                 j++;
//             }
//         }

//         while (i < arr1.size()) {
//             unionPositions.add(arr1.get(i++));
//         }

//         while (j < arr2.size()) {
//             unionPositions.add(arr2.get(j++));
//         }

//         return unionPositions;
//     }

//     private static Tuple<ArrayList<int[]>[], HashSet<QuadStruct>[]> proprogateMutex(MDD M1, MDD M2, boolean allMutex) {
//         int n = M1.node_t.size();
//         int m = M2.node_t.size();

//         System.err.println((n - 1) + "," + (m - 1));

//         if (n > m) {
//             return proprogateMutex(M2, M1, allMutex);
//         }

//         HashMap<Tuple<Integer, Integer>, Integer>[] count = null;
//         if (allMutex) {
//             count = new HashMap[n];
//             for (int i = 0; i < n; i++)
//                 count[i] = new HashMap<>();
//         }

//         ArrayList<int[]>[] node_mutex = new ArrayList[n];
//         HashSet<QuadStruct>[] edge_mutex = new HashSet[n];
//         HashSet<ArrayKey>[] hasNodeMutex = new HashSet[n];
//         for (int i = 0; i < n; i++) {
//             node_mutex[i] = new ArrayList<>();
//             edge_mutex[i] = new HashSet<>();
//             hasNodeMutex[i] = new HashSet<>();
//         }

//         for (int t = 0; t < n; t++) {
//             for (int i = 0; i < M1.node_t.get(t).size(); i++) {
//                 PositionTuple pT = M1.node_t.get(t).get(i);
//                 assert (pT.time == t); // Remove this later
//                 ArrayList<Integer> union = getUnionPositions(pT, M2, t);
//                 for (int u : union) {
//                     node_mutex[t].add(new int[] { i, u });
//                     hasNodeMutex[t].add(new ArrayKey(node_mutex[t].get(node_mutex[t].size() - 1)));
//                 }
//                 if (t + 1 < m) {
//                     ArrayList<Integer> union2 = getUnionPositions(pT, M2, t + 1);
//                     for (int _i : union2) {
//                         for (int j : M1.outgoing_t.get(t).get(i)) {
//                             for (int k : M2.incoming_t.get(t + 1).get(_i)) {
//                                 if (allMutex) {
//                                     if (!edge_mutex[t].contains(new QuadStruct(i, j, k, _i))) {
//                                         edge_mutex[t].add(new QuadStruct(i, j, k, _i));
//                                         Tuple<Integer, Integer> key = new Tuple<>(i, k);
//                                         if (count[t].containsKey(key)) {
//                                             count[t].put(key, count[t].get(key) + 1);
//                                         } else {
//                                             count[t].put(key, 1);
//                                         }
//                                     }
//                                 } else {
//                                     edge_mutex[t].add(new QuadStruct(i, j, k, _i));
//                                 }
//                             }
//                         }
//                     }
//                 }
//             }

//             for (int i = 0; i < M2.node_t.get(t).size(); i++) {
//                 PositionTuple pT = M2.node_t.get(t).get(i);
//                 if (t + 1 < n) {
//                     ArrayList<Integer> union2 = getUnionPositions(pT, M1, t + 1);
//                     for (int _i : union2) {
//                         for (int j : M2.outgoing_t.get(t).get(i)) {
//                             for (int k : M1.incoming_t.get(t + 1).get(_i)) {
//                                 if (allMutex) {
//                                     if (!edge_mutex[t].contains(new QuadStruct(k, _i, i, j))) {
//                                         Tuple<Integer, Integer> key = new Tuple<>(k, i);
//                                         if (count[t].containsKey(key)) {
//                                             count[t].put(key, count[t].get(key) + 1);
//                                         } else {
//                                             count[t].put(key, 1);
//                                         }
//                                         edge_mutex[t].add(new QuadStruct(k, _i, i, j));
//                                     }
//                                 } else {
//                                     edge_mutex[t].add(new QuadStruct(k, _i, i, j));
//                                 }
//                             }
//                         }
//                     }
//                 }
//             }
//         }

//         for (int t = 0; t < n; t++) {

//             if (allMutex) {

//                 for (int[] mutex : node_mutex[t]) {
//                     int ni = mutex[0];
//                     int nj = mutex[1];

//                     for (int oi : M1.outgoing_t.get(t).get(ni)) {
//                         for (int oj : M2.outgoing_t.get(t).get(nj)) {
//                             if (!edge_mutex[t].contains(new QuadStruct(ni, oi, nj, oj))) {
//                                 edge_mutex[t].add(new QuadStruct(ni, oi, nj, oj));
//                                 Tuple<Integer, Integer> key = new Tuple<>(ni, nj);
//                                 if (count[t].containsKey(key)) {
//                                     count[t].put(key, count[t].get(key) + 1);
//                                 } else {
//                                     count[t].put(key, 1);
//                                 }
//                             }
//                         }
//                     }
//                 }

//                 for (Tuple<Integer, Integer> T : count[t].keySet()) {
//                     int n1 = M1.outgoing_t.get(t).get(T.x).size();
//                     int n2 = M2.outgoing_t.get(t).get(T.y).size();
//                     int val = count[t].get(T);
//                     assert (val <= n1 * n2); // Remove later

//                     if (val == n1 * n2) {
//                         int[] key = new int[] { T.x, T.y };
//                         ArrayKey K = new ArrayKey(key);
//                         if (!hasNodeMutex[t].contains(K)) {
//                             node_mutex[t].add(key);
//                             hasNodeMutex[t].add(K);
//                         }
//                     }
//                 }
//             } else {
//                 for (int[] mutex : node_mutex[t]) {
//                     int ni = mutex[0];
//                     int nj = mutex[1];

//                     for (int oi : M1.outgoing_t.get(t).get(ni)) {
//                         for (int oj : M2.outgoing_t.get(t).get(nj)) {
//                             edge_mutex[t].add(new QuadStruct(ni, oi, nj, oj));
//                         }
//                     }
//                 }
//             }

//             for (QuadStruct mutex : edge_mutex[t]) {
//                 int toi = mutex.to_i;
//                 int toj = mutex.to_j;
//                 boolean isPropogated = true;

//                 for (int fromi : M1.incoming_t.get(t + 1).get(toi)) {
//                     for (int fromj : M2.incoming_t.get(t + 1).get(toj)) {
//                         if (!edge_mutex[t].contains(new QuadStruct(fromi, toi, fromj, toj))) {
//                             isPropogated = false;
//                         }
//                     }
//                 }

//                 if (isPropogated) {
//                     int[] key = new int[] { toi, toj };
//                     ArrayKey K = new ArrayKey(key);
//                     if (!hasNodeMutex[t + 1].contains(K)) {
//                         node_mutex[t + 1].add(key);
//                         hasNodeMutex[t + 1].add(K);
//                     }
//                 }
//             }
//         }

//         return new Tuple<ArrayList<int[]>[], HashSet<QuadStruct>[]>(node_mutex, edge_mutex);
//     }

//     private static Tuple<ArrayList<Constraint>, ArrayList<Constraint>> getPreGoalConstraints(MDD M1,
//             MDD M2,
//             Tuple<ArrayList<int[]>[], HashSet<QuadStruct>[]> mutex_,
//             int agentA,
//             int agentB,
//             boolean reverse) {

//         var node_mutex = mutex_.x;
//         ArrayList<Constraint> constraintsA = new ArrayList<>();
//         ArrayList<Constraint> constraintsB = new ArrayList<>();

//         for (int t = 0; t < node_mutex.length; t++) {
//             int[] countA = new int[M1.node_t.get(t).size()];
//             int[] countB = new int[M2.node_t.get(t).size()];
//             for (int[] mutex : node_mutex[t]) {
//                 countA[mutex[0]]++;
//                 countB[mutex[1]]++;
//             }

//             for (int i = 0; i < countA.length; i++) {
//                 assert (countA[i] <= M2.node_t.get(t).size()); // sanity check
//                 if (countA[i] == M2.node_t.get(t).size()) {
//                     constraintsA.add(new Constraint(agentA, M1.node_t.get(t).get(i).box, M1.node_t.get(t).get(i).time));
//                     if (M1.node_t.get(t).get(i).box != M1.node_t.get(t).get(i).agent) { // extra-over constrained
//                         constraintsA.add(
//                                 new Constraint(agentA, M1.node_t.get(t).get(i).agent, M1.node_t.get(t).get(i).time));
//                     }
//                 }
//             }

//             for (int i = 0; i < countB.length; i++) {
//                 assert (countB[i] <= M1.node_t.get(t).size()); // sanity check

//                 if (countB[i] == M1.node_t.get(t).size()) {
//                     constraintsB.add(new Constraint(agentB, M2.node_t.get(t).get(i).box, M2.node_t.get(t).get(i).time));
//                     if (M2.node_t.get(t).get(i).box != M2.node_t.get(t).get(i).agent) { // extra-over constrained
//                         constraintsB.add(
//                                 new Constraint(agentB, M2.node_t.get(t).get(i).agent, M2.node_t.get(t).get(i).time));
//                     }
//                 }
//             }
//         }

//         if (reverse)
//             return new Tuple<ArrayList<Constraint>, ArrayList<Constraint>>(constraintsB, constraintsA);
//         else
//             return new Tuple<ArrayList<Constraint>, ArrayList<Constraint>>(constraintsA, constraintsB);
//     }

//     private static Tuple<ArrayList<Constraint>, ArrayList<Constraint>> getAfterGoalConstraints(MDD M1,
//             MDD M2,
//             Tuple<ArrayList<int[]>[], HashSet<QuadStruct>[]> mutex_,
//             int agentA,
//             int agentB,
//             boolean reverse) {

//         var node_mutex = mutex_.x;
//         ArrayList<Constraint> constraintsA = new ArrayList<>();
//         ArrayList<Constraint> constraintsB = new ArrayList<>();
//         int last = node_mutex.length - 1;

//         constraintsA.add(new IntervalConstraint(agentA, M1.node_t.get(last).get(0).box, 0, last)); // Add until
//                                                                                                    // constraint till
//                                                                                                    // t+1
//         if (M1.node_t.get(last).get(0).box != M1.node_t.get(last).get(0).agent) { // extra-over constrained
//             constraintsA.add(new IntervalConstraint(agentA, M1.node_t.get(last).get(0).agent, 0, last));
//         }

//         for (int[] mutex : node_mutex[last]) {
//             int i = mutex[1];
//             constraintsB.add(new Constraint(agentB, M2.node_t.get(last).get(i).box, M2.node_t.get(last).get(i).time));
//             if (M2.node_t.get(last).get(i).box != M2.node_t.get(last).get(i).agent) { // extra-over constrained
//                 constraintsB
//                         .add(new Constraint(agentB, M2.node_t.get(last).get(i).agent, M2.node_t.get(last).get(i).time));
//             }
//         }

//         for (int t = last + 1; t < M2.node_t.size(); t++) {
//             for (PositionTuple pT : M2.node_t.get(t)) {
//                 assert (pT.time == t); // Remove this later
//                 if (hasNonEmptyIntersection(pT, M1.node_t.get(last).get(0))) {
//                     constraintsB.add(new Constraint(agentB, pT.box, pT.time));
//                     if (pT.box != pT.agent) { // extra-over constrained
//                         constraintsB.add(new Constraint(agentB, pT.agent, pT.time));
//                     }
//                 }
//             }
//         }

//         if (reverse)
//             return new Tuple<ArrayList<Constraint>, ArrayList<Constraint>>(constraintsB, constraintsA);
//         else
//             return new Tuple<ArrayList<Constraint>, ArrayList<Constraint>>(constraintsA, constraintsB);
//     }

//     private static Tuple<ArrayList<Constraint>, ArrayList<Constraint>> generateConstraints(MDD m1, MDD m2, MDD M1,
//             MDD M2) {
//         int conflictType = propogateAndFind(m1, m2);

//         if (conflictType == 0) {
//             return null;
//         }

//         m1.unitExpandMDD(M1);
//         m2.unitExpandMDD(M2);

//         while (propogateAndFind(m1, m2) != 0) {
//             m1.unitExpandMDD(M1);
//             m2.unitExpandMDD(M2);
//         }

//         MDD m2_hat = M2.view(m2.currentExpansionTime - 1);
//         while (propogateAndFind(m1, m2_hat) != 0) {
//             m1.unitExpandMDD(M1);
//         }

//         MDD m1_hat = M1.view(m1.currentExpansionTime - 1);
//         var mutex = proprogateMutex(m1_hat, m2_hat, true);
//         conflictType = classifyConflict(m1_hat, m2_hat, mutex);

//         assert (conflictType != 0);

//         if (conflictType == -1) {
//             // generate PC constraints
//             System.err.println("PC");
//             if (m1_hat.currentExpansionTime <= m2_hat.currentExpansionTime)
//                 return getPreGoalConstraints(m1_hat, m2_hat, mutex, 0, 1, false);
//             else
//                 return getPreGoalConstraints(m2_hat, m1_hat, mutex, 1, 0, true);
//         } else {
//             // generate AC constraints
//             System.err.println("AC");
//             if (m1_hat.currentExpansionTime <= m2_hat.currentExpansionTime)
//                 return getAfterGoalConstraints(m1_hat, m2_hat, mutex, 0, 1, false);
//             else
//                 return getAfterGoalConstraints(m2_hat, m1_hat, mutex, 1, 0, true);
//         }
//     }

//     public Tuple<ArrayList<Constraint>, ArrayList<Constraint>> generateConstraints(MDD M2) {
//         MDD M1 = this;
//         MDD M1_hat = this;
//         MDD M2_hat = M2;

//         if (M1.optimalTime != M1.currentExpansionTime) {
//             assert (M1.optimalTime < M1.currentExpansionTime);
//             M1_hat = M1.view(M1.optimalTime);
//         }

//         if (M2.optimalTime != M2.currentExpansionTime) {
//             assert (M2.optimalTime < M2.currentExpansionTime);
//             M2_hat = M2.view(M2.optimalTime);
//         }

//         return generateConstraints(M1_hat, M2_hat, M1, M2);
//     }

//     private static String getNode(PositionTuple t) {
//         return "(" + GraphUtils.node_list[t.box].row + ", " + GraphUtils.node_list[t.box].col + ")";
//     }

//     public void printMDD() {
//         System.err.println("MDD: ");
//         for (int t = 0; t < node_t.size(); t++) {
//             System.err.println("t = " + t);
//             for (int i = 0; i < node_t.get(t).size(); i++) {
//                 System.err.print("node " + getNode(node_t.get(t).get(i)) + " " + ": ");
//                 for (int j : outgoing_t.get(t).get(i)) {
//                     System.err.print(getNode(node_t.get(t + 1).get(j)) + " ");
//                 }
//                 System.err.println();
//             }
//         }
//     }
// }
