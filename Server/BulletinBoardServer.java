import java.io.*;
import java.net.*;
import java.util.*;

public class BulletinBoardServer {
    private final int port;
    private final int boardWidth;
    private final int boardHeight;
    private final int noteWidth;
    private final int noteHeight;
    private final List<String> validColours;
    private final Board board;
    private volatile boolean running = true;

    public BulletinBoardServer(int port, int boardWidth, int boardHeight,
                                int noteWidth, int noteHeight, List<String> validColours) {
        this.port = port;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.validColours = Collections.unmodifiableList(new ArrayList<>(validColours));
        this.board = new Board(boardWidth, boardHeight, noteWidth, noteHeight, this.validColours);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Bulletin Board Server started on port " + port);
            System.out.println("Board: " + boardWidth + "x" + boardHeight);
            System.out.println("Notes: " + noteWidth + "x" + noteHeight);
            System.out.println("Colours: " + String.join(", ", validColours));

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket, board, this)).start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + port + ": " + e.getMessage());
            System.exit(1);
        }
    }

    public void stop() {
        running = false;
    }

    public int getBoardWidth() { return boardWidth; }
    public int getBoardHeight() { return boardHeight; }
    public int getNoteWidth() { return noteWidth; }
    public int getNoteHeight() { return noteHeight; }
    public List<String> getValidColours() { return validColours; }

    public static void main(String[] args) {
        if (args.length < 6) {
            printUsage();
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[0]);
            int boardWidth = Integer.parseInt(args[1]);
            int boardHeight = Integer.parseInt(args[2]);
            int noteWidth = Integer.parseInt(args[3]);
            int noteHeight = Integer.parseInt(args[4]);

            if (port < 1 || port > 65535) {
                System.err.println("Error: Port must be between 1 and 65535");
                System.exit(1);
            }
            if (boardWidth <= 0 || boardHeight <= 0) {
                System.err.println("Error: Board dimensions must be positive integers");
                System.exit(1);
            }
            if (noteWidth <= 0 || noteHeight <= 0) {
                System.err.println("Error: Note dimensions must be positive integers");
                System.exit(1);
            }
            if (noteWidth > boardWidth || noteHeight > boardHeight) {
                System.err.println("Error: Note dimensions cannot exceed board dimensions");
                System.exit(1);
            }

            List<String> colours = new ArrayList<>();
            for (int i = 5; i < args.length; i++) {
                colours.add(args[i]);
            }

            if (colours.isEmpty()) {
                System.err.println("Error: At least one colour must be specified");
                System.exit(1);
            }

            BulletinBoardServer server = new BulletinBoardServer(
                port, boardWidth, boardHeight, noteWidth, noteHeight, colours
            );
            server.start();

        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format in arguments");
            printUsage();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.err.println("Usage: java BulletinBoardServer <port> <board_width> <board_height> <note_width> <note_height> <colour1> [colour2 ...]");
        System.err.println("Example: java BulletinBoardServer 4554 200 100 20 10 red white green yellow");
    }
}
