// package searchclient;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Collections;
// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.Map;
// import java.util.Random;
// import java.util.stream.Collectors;
// import mapf.Constraint;

// public class State
// {
//     private static final Random RNG = new Random(1);

//     /*
//         The agent rows, columns, and colors are indexed by the agent number.
//         For example, this.agentRows[0] is the row location of agent '0'.
//     */
//     // public int[] agentRows;
//     // public int[] agentCols;
//     public Map<Integer, Integer> agentRows = new HashMap<>();
//     public Map<Integer, Integer> agentCols = new HashMap<>();
//     public Map<Integer, Integer> agentRowsInit = new HashMap<>();
//     public Map<Integer, Integer> agentColsInit = new HashMap<>();
//     public static Color[] agentColors;

//     /*
//         The walls, boxes, and goals arrays are indexed from the top-left of the level, row-major order (row, col).
//                Col 0  Col 1  Col 2  Col 3
//         Row 0: (0,0)  (0,1)  (0,2)  (0,3)  ...
//         Row 1: (1,0)  (1,1)  (1,2)  (1,3)  ...
//         Row 2: (2,0)  (2,1)  (2,2)  (2,3)  ...
//         ...

//         For example, this.walls[2] is an array of booleans for the third row.
//         this.walls[row][col] is true if there's a wall at (row, col).

//         this.boxes and this.char are two-dimensional arrays of chars. 
//         this.boxes[1][2]='A' means there is an A box at (1,2). 
//         If there is no box at (1,2), we have this.boxes[1][2]=0 (null character).
//         Simiarly for goals. 

//     */
//     public static boolean[][] walls;
//     public char[][] boxes;
//     public HashMap<Character, int[][]> boxPositions;
//     public static char[][] goals;
    

//     /*
//         The box colors are indexed alphabetically. So this.boxColors[0] is the color of A boxes, 
//         this.boxColor[1] is the color of B boxes, etc.
//     */
//     public static Color[] boxColors;
 
//     public final State parent;
//     public final Action[] jointAction;
//     private final int g;

//     private int hash = 0;

//     public static int numAgents;

//     public static HashSet<Constraint> constraints = null;


//     // Constructs an initial state.
//     // Arguments are not copied, and therefore should not be modified after being passed in.
//     public State(Map<Integer, Integer> agentRows, Map<Integer, Integer> agentCols, Color[] agentColors, boolean[][] walls,
//                  char[][] boxes, Color[] boxColors, char[][] goals, int numAgents
//     )
//     {
//         this.agentRows = agentRows;
//         this.agentCols = agentCols;
//         this.agentColors = agentColors;
//         this.walls = walls;
//         this.boxes = boxes;
//         this.boxColors = boxColors;
//         this.goals = goals;
//         this.parent = null;
//         this.jointAction = null;
//         this.g = 0;
//         this.numAgents = numAgents;
//         this.agentRowsInit = agentRows;
//         this.agentColsInit = agentCols;


//         // create new data structures for box positions
//         // this.boxPositions = new int['A' - 'Z' + 1][1];
//         this.boxPositions = new HashMap<Character, int[][]>();

//         int [] counter = new int[27];

//         // get all boxes
//         for(int row = 1; row < boxes.length - 1; ++row){
//             for(int column = 1; column < boxes[0].length - 1; ++column){
//                 char potentialBox = boxes[row][column];

//                 if (potentialBox >= 'A' && potentialBox <= 'Z'){
//                     counter[potentialBox - 'A'] += 1;
//                 }

//             }    
//         }

//         // init. hashmap
//         for (int i = 0; i < counter.length; ++i){
//             if (counter[i] != 0){
//                 this.boxPositions.put((char)('A' + i), new int[counter[i]][2]);
//                 counter[i] = 0;
//             }
//         }

//         // set the corresponding values
//         for(int row = 1; row < boxes.length - 1; ++row){
//             for(int column = 1; column < boxes[0].length - 1; ++column){
//                 char potentialBox = boxes[row][column];

//                 if (potentialBox >= 'A' && potentialBox <= 'Z'){
//                     this.boxPositions.get(potentialBox)[counter[potentialBox - 'A']][0] = row;
//                     this.boxPositions.get(potentialBox)[counter[potentialBox - 'A']++][1] = column;
//                 }

//             }    
//         }
        
//         // print out the values - uncomment to see
//         // for (char key: this.boxPositions.keySet()) {
//         //     // String value = example.get(name).toString();
//         //     System.err.println(key);

//         //     int [][] arr = this.boxPositions.get(key);
//         //     for(int crate = 0; crate < arr.length; ++crate)
//         //         for(int coord = 0; coord < arr[0].length; ++coord)
//         //         // System.err.println("");
//         //             System.err.println(arr[crate][coord]);
        
//         // }

//     }

//     // changes coordinates of the boxes
//     private void boxesChangeCoords(int oldX, int oldY, int newX,int newY, char boxType){
            
//         int [][] arr = this.boxPositions.get(boxType);
//         for(int crate = 0; crate < arr.length; ++crate)
//         {
//             if ((arr[crate][0] == oldX) && (arr[crate][1] == oldY)){
//                 arr[crate][0] = newX;
//                 arr[crate][1] = newY;
//                 return;
//             }

//         }
    
//     }

//     // Constructs the state resulting from applying jointAction in parent.
//     // Precondition: Joint action must be applicable and non-conflicting in parent state.
//     private State(State parent, Action[] jointAction, HashMap<Character, int[][]> boxPositions)
//     {
//         // Copy parent
//         this.agentRows.putAll(parent.agentRows);
//         this.agentCols.putAll(parent.agentCols);
//         this.boxes = new char[parent.boxes.length][];
        
//         // copy boxPositions
//         this.boxPositions = new HashMap<Character, int[][]>();
//         for (char key: boxPositions.keySet()) {

//             int [][] oldArr = boxPositions.get(key);
//             int [][] newArr = new int[oldArr.length][oldArr[0].length];
//             for(int crate = 0; crate < newArr.length; ++crate)
//                 for(int coord = 0; coord < newArr[0].length; ++coord){
//                     newArr[crate][coord] = oldArr[crate][coord];
//                     // System.err.println(newArr[crate][coord]);
//                 }
//             this.boxPositions.put(key, newArr);  

//         }

//         for (int i = 0; i < parent.boxes.length; i++)
//         {
//             this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
//         }

//         // Set own parameters
//         this.parent = parent;
//         this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
//         this.g = parent.g + 1;

//         // Apply each action
//         int numAgents = this.agentRows.size();
//         for (Integer agent : agentRows.keySet()) {
//             Action action = jointAction[agent];
//             char box;

//             switch (action.type)
//             {
//                 case NoOp:
//                     break;

//                 case Move:
//                     this.agentRows.put(agent, this.agentRows.get(agent) + action.agentRowDelta);
//                     this.agentCols.put(agent, this.agentCols.get(agent) + action.agentColDelta);
//                     break;

//                 // case Push:
//                 //     this.agentRows[agent] += action.agentRowDelta;
//                 //     this.agentCols[agent] += action.agentColDelta;
//                 //     box = this.boxes[this.agentRows[agent]][this.agentCols[agent]];
//                 //     this.boxes[this.agentRows[agent]+action.boxRowDelta][this.agentCols[agent]+action.boxColDelta] = box;
//                 //     this.boxes[this.agentRows[agent]][this.agentCols[agent]] = 0;

//                 //     // change coords of the boxes
//                 //     this.boxesChangeCoords(this.agentRows[agent], this.agentCols[agent], this.agentRows[agent]+action.boxRowDelta, this.agentCols[agent]+action.boxColDelta, box);
                    
//                 //     break;

//                 // case Pull:
//                 //     box = this.boxes[this.agentRows[agent]+action.boxRowDelta*(-1)][this.agentCols[agent]+action.boxColDelta*(-1)];
//                 //     this.boxes[this.agentRows[agent]][this.agentCols[agent]] = box;
//                 //     this.boxes[this.agentRows[agent]+action.boxRowDelta*(-1)][this.agentCols[agent]+action.boxColDelta*(-1)] = 0;

//                 //     // change coords of the boxes
//                 //     this.boxesChangeCoords(this.agentRows[agent]+action.boxRowDelta*(-1), this.agentCols[agent]+action.boxColDelta*(-1), this.agentRows[agent], this.agentCols[agent], box);

//                 //     this.agentRows[agent] += action.agentRowDelta;
//                 //     this.agentCols[agent] += action.agentColDelta;

//                 //     break;
//             }
//         }
//     }

//     // Note that duplicate agents are not allowed
//     public State[] partialStates() {
//         State[] partialStates = new State[numAgents];
//         for (Integer agent : this.agentRows.keySet()) {
//             Map<Integer, Integer> agentRows = new HashMap<>();
//             Map<Integer, Integer> agentCols = new HashMap<>();
//             agentRows.put(agent, this.agentRows.get(agent));
//             agentCols.put(agent, this.agentCols.get(agent));
//             // char[][] boxes = new char[this.boxes.length][this.boxes[0].length];
//             // for (int row = 1; row < this.boxes.length - 1; row++) {
//             //     for (int col = 1; col < this.boxes[row].length - 1; col++) {
//             //         char box = this.boxes[row][col];
//             //         if ('A' <= box && box <= 'Z' && agentColors[agent - '0'] == boxColors[box - 'A']) {
//             //             boxes[row][col] = box;
//             //         }
//             //     }
//             // }
//             State partialState = new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals, numAgents);
//             partialStates[agent] = partialState;
//         }
//         return partialStates;
//     }

//     public int g()
//     {
//         return this.g;
//     }

//     public boolean isGoalState()
//     {
//         for (int row = 1; row < this.goals.length - 1; row++)
//         {
//             for (int col = 1; col < this.goals[row].length - 1; col++)
//             {
//                 char goal = this.goals[row][col];
//                 // if (('A' <= goal && goal <= 'Z') || ('0' <= goal && goal <= '9')) {
//                 //     System.err.print(goal);
//                 // }
//                 // else {
//                 //     System.err.print("*");
//                 // }
//                 // If the value of 'goal' is part of alphabet from A to Z, and if there is no corresponding box with the same letter at goal's postion, then return false.
//                 // if(goal == 'B'){
//                 //     System.out.println(goal);
//                 //     System.out.println(this.boxes[row][col]);
//                 //     System.out.println(row);
//                 //     System.out.println(col);
//                 // }
//                 if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal)
//                 {
//                     return false;
//                 }
//                 // If the value of 'goal' is part of integers from 0 to 9, and if there is no corresponding agent with the same letter at goal's postion, then return false.
//                 else if ('0' <= goal && goal <= '9' && this.agentRows.containsKey(goal - '0') && this.agentCols.containsKey(goal - '0')) {
//                     if (!(this.agentRows.get(goal - '0') == row && this.agentCols.get(goal - '0') == col)) {
//                         return false;
//                     }
//                 }
//             }
//             // System.err.println("");
//         }
//         return true;
//     }

//     public ArrayList<State> getExpandedStates()
//     {
//         // Determine list of applicable actions for each individual agent.
//         HashMap<Integer, Action[]> applicableActions = new HashMap<>();

//         for (Integer agent : agentRows.keySet())
//         {
//             ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
//             for (Action action : Action.values())
//             {
//                 if (this.isApplicable(agent, action))
//                 {
//                     agentActions.add(action);
//                 }
//             }
//             applicableActions.put(agent, agentActions.toArray(new Action[0]));
//         }

//         // Iterate over joint actions, check conflict and generate child states.
//         Action[] jointAction = new Action[numAgents];
//         int[] actionsPermutation = new int[numAgents];
//         ArrayList<State> expandedStates = new ArrayList<>(16);
//         while (true)
//         {
//             for (Integer agent : agentRows.keySet())
//             {
//                 jointAction[agent] = applicableActions.get(agent)[actionsPermutation[agent]];
//             }

//             if (!this.isConflicting(jointAction))
//             {
//                 expandedStates.add(new State(this, jointAction, this.boxPositions));
//             }

//             // Advance permutation
//             boolean done = false; 
//             for (Integer agent : agentRows.keySet())
//             {
//                 if (actionsPermutation[agent] < applicableActions.get(agent).length - 1)
//                 {
//                     ++actionsPermutation[agent];
//                     break;
//                 }
//                 else
//                 {
//                     actionsPermutation[agent] = 0;
//                     if (agent == Collections.max(agentRows.keySet()))
//                     {
//                         done = true;
//                     }
//                 }
//             }
//             // Last permutation?
//             if (done)
//             {
//                 break;
//             }
//         }
//         Collections.shuffle(expandedStates, State.RNG);
//         return expandedStates;
//     }

//     private boolean isApplicable(int agent, Action action)
//     {
//         int agentRow = this.agentRows.get(agent);
//         int agentCol = this.agentCols.get(agent);
//         Color agentColor = this.agentColors[agent];
//         int boxRow;
//         int boxCol;
//         int destinationRow;
//         int destinationCol;
//         switch (action.type)
//         {
//             case NoOp:
//                 return true;

//             case Move:
//                 destinationRow = agentRow + action.agentRowDelta;
//                 destinationCol = agentCol + action.agentColDelta;
//                 if (isConstrained(agent, destinationRow, destinationCol)) {
//                     System.err.println("Constrained");
//                     return false;
//                 }
//                 if (this.cellIsFree(destinationRow, destinationCol)) {
//                     return true;
//                 }

//             // The color condition is not checked for now.
//             // case Push:
//             //     destinationRow = agentRow + action.agentRowDelta;
//             //     destinationCol = agentCol + action.agentColDelta;
//             //     boxRow = destinationRow + action.boxRowDelta; 
//             //     boxCol = destinationCol + action.boxColDelta;
//             //     if(this.cellContainsBox(destinationRow, destinationCol) && this.cellIsFree(boxRow,  boxCol))
//             //         return agentColor.equals(this.boxColors[this.boxes[destinationRow][destinationCol] - 'A']);
//             //     return false;

//             // The color condition is not checked for now.
//             // case Pull:
//             //     destinationRow = agentRow + action.agentRowDelta;
//             //     destinationCol = agentCol + action.agentColDelta;
//             //     boxRow = agentRow + action.boxRowDelta*(-1); 
//             //     boxCol = agentCol + action.boxColDelta*(-1);
//             //     if(this.cellIsFree(destinationRow, destinationCol) && this.cellContainsBox(boxRow,  boxCol))
//             //         return agentColor.equals(this.boxColors[this.boxes[boxRow][boxCol] - 'A']);
//             //     return false;

//                 // return this.cellIsFree(destinationRow, destinationCol) && this.cellContainsBox(boxRow, boxCol);          
//         }
//         // Unreachable:
//         return false;
//     }

//     private boolean isConflicting(Action[] jointAction)
//     {
//         int[] destinationRows = new int[numAgents]; // row of new cell to become occupied by action
//         int[] destinationCols = new int[numAgents]; // column of new cell to become occupied by action
//         int[] boxRows = new int[numAgents]; // current row of box moved by action
//         int[] boxCols = new int[numAgents]; // current column of box moved by action

//         // Collect cells to be occupied and boxes to be moved
//         for (Integer agent : agentRows.keySet())
//         {
//             Action action = jointAction[agent];
//             int agentRow = this.agentRows.get(agent);
//             int agentCol = this.agentCols.get(agent);
//             int boxRow;
//             int boxCol;

//             switch (action.type)
//             {
//                 case NoOp:
//                     break;

//                 case Move:
//                     destinationRows[agent] = agentRow + action.agentRowDelta;
//                     destinationCols[agent] = agentCol + action.agentColDelta;
//                     boxRows[agent] = agentRow; // Distinct dummy value
//                     boxCols[agent] = agentCol; // Distinct dummy value
//                     break;

//                 // Not tested
//                 // case Push:
//                 //     destinationRows[agent] = agentRow + action.agentRowDelta;
//                 //     destinationCols[agent] = agentCol + action.agentColDelta;
//                 //     boxRows[agent] = boxRow + action.boxRowDelta; 
//                 //     boxCols[agent] = boxCol + action.boxColDelta;
//                 //     break;
//            }
//         }

//         for (int a1 = 0; a1 < agentRows.keySet().size(); ++a1)
//         {
//             if (jointAction[a1] == Action.NoOp)
//             {
//                 continue;
//             }

//             for (int a2 = a1 + 1; a2 < agentRows.keySet().size(); ++a2)
//             {
//                 if (jointAction[a2] == Action.NoOp)
//                 {
//                     continue;
//                 }

//                 // Moving into same cell?
//                 if (destinationRows[a1] == destinationRows[a2] && destinationCols[a1] == destinationCols[a2])
//                 {
//                     System.err.println(a1 + " | " + destinationRows[a1]);
//                     return true;
//                 }
//             }
//         }

//         return false;
//     }

//     private boolean isConstrained(int agent, int row, int col) {
//         State state = this;
//         if (this.constraints == null) {
//             return false;
//         }
//         else {
//             for (Constraint constraint : this.constraints) {
//                 if (agent == constraint.agent && row == constraint.row && col == constraint.col && state.g == constraint.t) {
//                     return true;
//                 }
//             }
//         }
//         return false;
//     }

//     private boolean cellIsFree(int row, int col)
//     {
//         return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
//     }

//     private boolean cellContainsBox(int row, int col)
//     {
//         return this.boxes[row][col] != 0;
//     }

//     private char agentAt(int row, int col)
//     {
//         for (Integer agent : agentRows.keySet()) {
//             if (this.agentRows.get(agent) == row && this.agentCols.get(agent) == col)
//             {
//                 return (char) ('0' + agent);
//             }
//         }
//         return 0;
//     }

//     public Action[][] extractPlan()
//     {
//         Action[][] plan = new Action[this.g][];
//         State state = this;
//         while (state.jointAction != null)
//         {
//             plan[state.g - 1] = state.jointAction;
//             state = state.parent;
//         }
//         return plan;
//     }

//     @Override
//     public int hashCode()
//     {
//         if (this.hash == 0)
//         {
//             final int prime = 31;
//             int result = 1;
//             result = prime * result + Arrays.hashCode(this.agentColors);
//             result = prime * result + Arrays.hashCode(this.boxColors);
//             result = prime * result + Arrays.deepHashCode(this.walls);
//             result = prime * result + Arrays.deepHashCode(this.goals);
//             result = prime * result + this.agentRows.hashCode();
//             result = prime * result + this.agentCols.hashCode();
//             for (int row = 0; row < this.boxes.length; ++row)
//             {
//                 for (int col = 0; col < this.boxes[row].length; ++col)
//                 {
//                     char c = this.boxes[row][col];
//                     if (c != 0)
//                     {
//                         result = prime * result + (row * this.boxes[row].length + col) * c;
//                     }
//                 }
//             }
//             this.hash = result;
//         }
//         return this.hash;
//     }

//     @Override
//     public boolean equals(Object obj)
//     {
//         if (this == obj)
//         {
//             return true;
//         }
//         if (obj == null)
//         {
//             return false;
//         }
//         if (this.getClass() != obj.getClass())
//         {
//             return false;
//         }
//         State other = (State) obj;
//         return this.agentRows.equals(other.agentRows) &&
//                this.agentCols.equals(other.agentCols) &&
//                Arrays.equals(this.agentColors, other.agentColors) &&
//                Arrays.deepEquals(this.walls, other.walls) &&
//                Arrays.deepEquals(this.boxes, other.boxes) &&
//                Arrays.equals(this.boxColors, other.boxColors) &&
//                Arrays.deepEquals(this.goals, other.goals);
//     }

//     @Override
//     public String toString()
//     {
//         StringBuilder s = new StringBuilder();
//         for (int row = 0; row < this.walls.length; row++)
//         {
//             for (int col = 0; col < this.walls[row].length; col++)
//             {
//                 if (this.boxes[row][col] > 0)
//                 {
//                     s.append(this.boxes[row][col]);
//                 }
//                 else if (this.walls[row][col])
//                 {
//                     s.append("+");
//                 }
//                 else if (this.agentAt(row, col) != 0)
//                 {
//                     s.append(this.agentAt(row, col));
//                 }
//                 else
//                 {
//                     s.append(" ");
//                 }
//             }
//             s.append("\n");
//         }
//         return s.toString();
//     }
// }
