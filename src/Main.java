import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader("instances/janko/sample4.txt"));
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
        long start = System.currentTimeMillis();
        g.solve();
        long end = System.currentTimeMillis();

        if(g.solved) {
            System.out.println("Solved in " + (end - start) + " ms");
            g.print();
            // print edges
        } else {
            System.out.println("No solution.");
        }
    }
}
