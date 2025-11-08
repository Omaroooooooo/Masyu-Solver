import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("instances/janko/sample.txt"));
        String[] size = reader.readLine().trim().split("\\s+");
        int h = Integer.parseInt(size[0]);
        int w = Integer.parseInt(size[1]);
        Type[][] t = new Type[h][w];
        for (int i = 0; i < h; i++) {
            String line = reader.readLine().trim();
            String[] tok = line.split("\\s+");

            for (int j = 0; j < w; j++) {
                switch (tok[j]) {
                    case "w":
                        t[i][j] = Type.White;
                        break;
                    case "b":
                        t[i][j] = Type.Black;
                        break;
                    case "-":
                    default:
                        t[i][j] = Type.None;
                        break;
                }
            }
        }
        reader.close();
        Grid g = new Grid(h, w, t);
        g.solve();
        System.out.println(g.lines);
    }
}