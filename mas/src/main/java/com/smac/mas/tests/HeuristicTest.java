package tests;
// package test;


// import org.junit.Test;

// import searchclient.*;

// import static org.junit.Assert.*;

// // SA = single agent, MA = multi agent
// public class HeuristicTest {

//   @Test
//   public void testgoalCountHeuristicSA() {
//     /**
//      * starting state: ++++ +0 + + + ++++
//      */
//     char[][] boxes =
//         {{' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' '}};
//     char[][] goals =
//         {{' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' '}, {' ', ' ', '0', ' '}, {' ', ' ', ' ', ' '}};
//     boolean[][] walls = {{true, true, true, true}, {true, false, false, true},
//         {true, false, false, true}, {true, true, true, true}};
//     State s = new State(new int[] {1}, new int[] {1}, new Color[] {Color.Red}, walls, boxes,
//         new Color[] {}, goals);
//     System.err.println(s);
//     int expected_h_val = 1;
//     Heuristic heur = new HeuristicPlaceholder(s);
//     int actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);

//     /**
//      * starting state: ++++ + + + 0+ ++++
//      */
//     s = new State(new int[] {2}, new int[] {2}, new Color[] {Color.Red}, walls, boxes,
//         new Color[] {}, goals);
//     System.err.println(s);
//     expected_h_val = 0;
//     actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);
//   }

//   @Test
//   public void testgoalCountHeuristicMA() {
//     /**
//      * starting state: ++++ +01+ + + ++++
//      */
//     char[][] boxes =
//         {{' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' '}};
//     char[][] goals =
//         {{' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' '}, {' ', '0', '1', ' '}, {' ', ' ', ' ', ' '}};
//     boolean[][] walls = {{true, true, true, true}, {true, false, false, true},
//         {true, false, false, true}, {true, true, true, true}};
//     State s = new State(new int[] {1, 1}, new int[] {1, 2}, new Color[] {Color.Red, Color.Blue},
//         walls, boxes, new Color[] {}, goals);
//     System.err.println(s);
//     int expected_h_val = 2;
//     Heuristic heur = new HeuristicPlaceholder(s);
//     int actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);

//     s = new State(new int[] {1, 2}, new int[] {1, 2}, new Color[] {Color.Red}, walls, boxes,
//         new Color[] {}, goals);
//     System.err.println(s);
//     expected_h_val = 1;
//     actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);

//     s = new State(new int[] {2, 2}, new int[] {1, 2}, new Color[] {Color.Red}, walls, boxes,
//         new Color[] {}, goals);
//     System.err.println(s);
//     expected_h_val = 0;
//     actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);
//   }

//   @Test
//   public void testgoalCountHeuristicBoxes() {
//     /**
//      * starting state: ++++ +01+ +A + ++++
//      */
//     char[][] boxes =
//         {{' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' '}, {' ', 'A', ' ', ' '}, {' ', ' ', ' ', ' '}};
//     char[][] goals =
//         {{' ', ' ', ' ', ' '}, {' ', 'A', ' ', ' '}, {' ', '0', '1', ' '}, {' ', ' ', ' ', ' '}};
//     boolean[][] walls = {{true, true, true, true}, {true, false, false, true},
//         {true, false, false, true}, {true, true, true, true}};
//     State s = new State(new int[] {1, 1}, new int[] {1, 2}, new Color[] {Color.Red, Color.Blue},
//         walls, boxes, new Color[] {}, goals);
//     System.err.println(s);
//     int expected_h_val = 3;
//     Heuristic heur = new HeuristicPlaceholder(s);
//     int actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);

//     boxes = new char[][] {{' ', ' ', ' ', ' '}, {' ', 'A', ' ', ' '}, {' ', ' ', ' ', ' '},
//         {' ', ' ', ' ', ' '}};
//     s = new State(new int[] {1, 1}, new int[] {1, 2}, new Color[] {Color.Red}, walls, boxes,
//         new Color[] {}, goals);
//     System.err.println(s);
//     expected_h_val = 2;
//     actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);

//     s = new State(new int[] {2, 2}, new int[] {1, 2}, new Color[] {Color.Red}, walls, boxes,
//         new Color[] {}, goals);
//     System.err.println(s);
//     expected_h_val = 0;
//     actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);
//   }

//   @Test
//   public void testgoalCountHeuristicDuplicateBoxType() {
//     /**
//      * starting state: ++++ +01+ +AA+ ++++
//      */
//     char[][] boxes =
//         {{' ', ' ', ' ', ' '}, {' ', ' ', ' ', ' '}, {' ', 'A', 'A', ' '}, {' ', ' ', ' ', ' '}};
//     char[][] goals =
//         {{' ', ' ', ' ', ' '}, {' ', 'A', 'A', ' '}, {' ', '0', '1', ' '}, {' ', ' ', ' ', ' '}};
//     boolean[][] walls = {{true, true, true, true}, {true, false, false, true},
//         {true, false, false, true}, {true, true, true, true}};
//     State s = new State(new int[] {1, 1}, new int[] {1, 2}, new Color[] {Color.Red, Color.Blue},
//         walls, boxes, new Color[] {}, goals);
//     System.err.println(s);
//     int expected_h_val = 4;
//     Heuristic heur = new HeuristicPlaceholder(s);
//     int actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);

//     boxes = new char[][] {{' ', ' ', ' ', ' '}, {' ', 'A', ' ', ' '}, {' ', ' ', 'A', ' '},
//         {' ', ' ', ' ', ' '}};
//     s = new State(new int[] {1, 1}, new int[] {1, 2}, new Color[] {Color.Red}, walls, boxes,
//         new Color[] {}, goals);
//     System.err.println(s);
//     expected_h_val = 3;
//     actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);


//     boxes = new char[][] {{' ', ' ', ' ', ' '}, {' ', 'A', 'A', ' '}, {' ', ' ', ' ', ' '},
//         {' ', ' ', ' ', ' '}};
//     s = new State(new int[] {2, 2}, new int[] {1, 2}, new Color[] {Color.Red}, walls, boxes,
//         new Color[] {}, goals);
//     System.err.println(s);
//     expected_h_val = 0;
//     actual_h_val = heur.goalCountHeuristic(s);
//     assertEquals(expected_h_val, actual_h_val);
//   }
// }
