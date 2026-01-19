# Masyu
Masyu is a logic puzzle made by [Nikoli](https://www.nikoli.co.jp/en/puzzles/masyu/). The game consists of a rectangular grid of cells that can be connected with horizontal or vertical edges. 
Some of these cells are empty, while others contain circles (pearls) that are either black or white. The rules of the puzzle are simple:
1. Draw edges to form a single loop that passes through all the circles.
2. The loop should not branch or cross itself.
3. The loop should pass straight through white circles and make a turn at least on one side after exiting the circle.
4. The loop should make a turn inside black circles and continue straight on both sides at least once after exiting the circle.

![Masyu Rules](/Masyu.png)

This project contains an object-oriented design of the puzzle written in Java, along with two solvers using two different methods to solve the puzzle.

## 1. Depth-First-Search (DFS)
In the first method, the line starts from a random circle and continues growing and exploring paths in a similar way to DFS until it closes on itself, forming a loop that satisfies all the rules.  

With each iteration, a new edge is drawn in one of the four directions (up, down, left, right). If the new edge breaks any rules, the solver backtracks and deletes the edge, then it tries exploring another direction.  

Although simple, this method comes at the cost of bad performance and scaling. As the size of the puzzle grows, the search space and time grow exponentially, making it fit only for small instances (smaller than 8x8). The distribution of circles and the order of directions in which the loop explores paths are also factors that affect the performance of this method. Therefore, a more efficient and reliable approach has been implemented to solve instances of this puzzle in a reasonable time.

## 2. Constraint Programming (CP)
In Constraint Programming, the problem is declared as variables with domains and constraints. After that, a SAT solver is used to assign values to these variables, ensuring all constraints are met. For this method, the CP-SAT solver from [Google OR-Tools](https://developers.google.com/optimization) is used to solve the problem. There are two types of variables used to solve this puzzle:
- **hVars[r][c]** and **vVars[r][c]** that represent horizontal and vertical edges with r and c as indices for the row and column. These are the main variables determined by the SAT solver, and they are boolean, which means their domain is either 0 (false) or 1 (true). If hVars[r][c] is true, that means there is a horizontal edge between the cells at (r,c) and (r,c+1). Similarly, if vVars[r][c] is true, there is a vertical edge between the cells at (r,c) and (r+1,c).
- **deg[r][c]** is a helper variable that represents the degree of a cell at (r,c) and helps to formulate and express some of the constraints. The degree of a cell is the number of edges going out of it. This variable is an integer variable, with its domain ranging from 0 to 4, considering that 0 to 4 edges can be drawn from a cell.

Alongside these variables, the rules of the puzzle should also be declared in CP-SAT as constraints to help the solver find the right assignments to variables that solve the problem.

- **White Circle Constraints**: Assuming there is a white circle at (r,c), we can ensure the line goes straight through by declaring a logical equivalence between the vertical edges above and below the circle (vVars[r-1][c] == vVars[r][c]) and horizontal edges left and right of the circle (hVars[r][c-1] == hVars[r][c]). If the line goes horizontally through the circle, we add the constraint (not(hVars[r][c-2]) OR not(hVars[r][c+1])), which ensures that the line does not go straight on both sides and makes a turn at least on one side. Analogously, the same constraint is applied vertically.
- **Black Circle Constraints**: By applying (hVars[r][c-1] != hVars[r][c]) and (vVars[r-1][c] != vVars[r][c]), the line is forbidden to go straight through the black circle and forced to make a turn. Using implications (e.g. vVars[r][c] => vVars[r+1][c]) for all directions, we can make sure that the line goes straight at least once after exiting the circle.
- **Line should go through all Circles**: using the helper variable deg[r][c] we declared before, the constraint (deg[r][c] == 2) is added for cells that contain circles. Thus, all circles are forced to be part of the loop.
- **No Branching**: Branching and intersecting occur when 3 or 4 edges are drawn from a cell. By setting the upper bound of all deg variables to 2 instead of 4, no branching or intersecting can happen.
- **Loop Constraint**: By reducing the domain of deg variables even further to include only 0 and 2, and exclude 1, the edges are forced to connect to each other and form a loop. No lines with dead ends of degree 1 can be formed. The final solution should only have cells of degree 2 that are part of the loop and cells of degree 0 that are not. This constraint alone is enough in the DFS method to form a single loop, since the line continuously grows from one point. Unlike the DFS solver, this solver uses CP-SAT, which assigns values to random variables that are not necessarily connected or close to each other. Therefore, even with this constraint, more than one loop can be formed in the final solution.
- **Single Loop**: encoding the single loop constraint for loop puzzles, like Masyu, is a difficult task to accomplish, but with some smart methods and workarounds, it is achievable. The method used in this solver is Lazy Constraints, which are constraints added during the solving process, after the solver has already generated solutions. Using a function that detects and counts how many loops the generated solution has, if it is only one, then that is the final solution. However, if the solution has more than one loop, we add a constraint to these loops that prohibits the solver from using the exact same combination of edges that formed this loop by forcing at least one of the edges to be false. This way, the solver is obligated to generate other solutions with different loops until it finds the solution with one loop.

This method has proven to be many times faster and more efficient than the DFS approach, and can solve even very large instances (~1000 cells) in a matter of seconds.

## Usage Guide
This project has been built with Maven and JDK 21. Simply clone the repository and open the project in an IDE (e.g. IntelliJ).  

The [`/instances`](/instances) directory contains a variety of Masyu instances of different sizes from the [Janko](https://www.janko.at/Raetsel/Masyu/index.htm) website as .txt files. You can create your own instances as well. The format of the files looks like:

```sh
6 6
- - w - - -
w - - - - b
- - - - - -
- - - - - -
b - - - - w
- - - w - -
```

where the first two digits represent the height and width, respectively. The rest of the lines represent the grid with *'w'* representing white circles, *'b'* representing black circles, and *'-'* as empty cells.

In the main file, change the path to the instance you want to solve and run it. In the terminal, you will be prompted to choose the DFS solver or CP solver. It will then print the solution of the instance.

