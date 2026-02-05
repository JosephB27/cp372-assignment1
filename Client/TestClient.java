import java.io.*;
import java.net.*;

public class TestClient {
    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 4554;

        Socket socket = new Socket(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Connected. Handshake: " + in.readLine());

        test(in, out, "POST 10 20 red Hello World");
        test(in, out, "POST 50 50 blue Another note");
        test(in, out, "GET");
        test(in, out, "PIN 15 25");
        test(in, out, "GET PINS");
        test(in, out, "DISCONNECT");

        socket.close();
    }

    private static void test(BufferedReader in, PrintWriter out, String cmd) throws IOException {
        System.out.println("\n> " + cmd);
        out.println(cmd);
        System.out.println("< " + in.readLine());
    }
}
