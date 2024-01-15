// package mapf;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.TreeSet;

// public class CTHeuristic {
//     /*
//      * Supposed to keep track of agent MDDs.
//      * Also useful to get next CTNode
//      */

//     FrontierCBS frontierA;
//     FrontierCBS frontierB;
//     TreeSet<CTNode> tracker;
//     int w;

//     public CTHeuristic(int w) {
//         frontierA = new FrontierCBS(new CTNodeComparator());
//         frontierB = new FrontierCBS(new SecondaryCTNodeComparator());
//         tracker = new TreeSet<>(new CTNodeComparator());
//         this.w = w;
//     }

//     public ArrayList<Constraint> getConstraints(CTNode N) {
//         return null;
//     }

//     public CTNode getNextNode() {
//         int bestCost = frontierA.peek().cost;

//         if (!tracker.isEmpty()) {
//             bestCost = tracker.first().cost;
//         }

//         while (!frontierA.isEmpty() && frontierA.peek().cost < this.w * bestCost) {
//             CTNode candidate = frontierA.pop();
//             frontierB.add(candidate);
//             tracker.add(candidate);
//         }

//         CTNode next = frontierB.pop();
//         tracker.remove(next);

//         return next;
//     }
// }
