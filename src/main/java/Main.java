import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader("instances/janko/Janko760.txt"));

        String[] s = br.readLine().trim().split("\\s+");
        int h = Integer.parseInt(s[0]);
        int w = Integer.parseInt(s[1]);

        Type[][] t = new Type[h][w];
        for(int i=0;i<h;i++){
            String[] tok = br.readLine().trim().split("\\s+");
            for(int j=0;j<w;j++){
                t[i][j] = switch(tok[j]){
                    case "b" -> Type.BLACK;
                    case "w" -> Type.WHITE;
                    default  -> Type.NONE;
                };
            }
        }

        Grid g = new Grid(h,w,t);

        Scanner sc = new Scanner(System.in);
        System.out.println("Choose solver: 1) DFS  2) CP");
        int choice = sc.nextInt();

        if (choice == 1) {
            SolverDFS solver = new SolverDFS(g);
            long start = System.currentTimeMillis();
            solver.solve();
            long end = System.currentTimeMillis();
            if (g.solved) {
                g.print();
                System.out.println("Solved in " + (end - start) + " ms");
            } else {
                System.out.println("No solution.");
            }

        } else if (choice == 2) {
            SolverCP solver = new SolverCP(g);
            solver.solve();
        } else {
            System.out.println("Invalid choice.");
        }




    }
}
