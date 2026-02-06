import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Board board;
    private final BulletinBoardServer server;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, Board board, BulletinBoardServer server) {
        this.socket = socket;
        this.board = board;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            sendHandshake();

            String line;
            while ((line = in.readLine()) != null) {
                String response = processCommand(line.trim());
                if (response == null) {
                    break;
                }
                out.println(response);
            }
        } catch (IOException e) {
            // Client disconnected - this is expected and OK
        } finally {
            cleanup();
        }
    }

    private void sendHandshake() {
        StringBuilder sb = new StringBuilder();
        sb.append("WELCOME ");
        sb.append(server.getBoardWidth()).append(" ");
        sb.append(server.getBoardHeight()).append(" ");
        sb.append(server.getNoteWidth()).append(" ");
        sb.append(server.getNoteHeight());
        for (String colour : server.getValidColours()) {
            sb.append(" ").append(colour);
        }
        out.println(sb.toString());
        out.flush();
    }

    private String processCommand(String line) {
        if (line.isEmpty()) {
            return "ERROR INVALID_FORMAT Empty command";
        }

        String[] parts = line.split(" ", 2);
        String command = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        try {
            switch (command) {
                case "POST":
                    return handlePost(args);
                case "GET":
                    return handleGet(args);
                case "PIN":
                    return handlePin(args);
                case "UNPIN":
                    return handleUnpin(args);
                case "SHAKE":
                    return board.shake();
                case "CLEAR":
                    return board.clear();
                case "DISCONNECT":
                    return null;
                default:
                    return "ERROR INVALID_FORMAT Unknown command: " + command;
            }
        } catch (Exception e) {
            return "ERROR INVALID_FORMAT " + e.getMessage();
        }
    }

    private String handlePost(String args) {
        String[] parts = args.split(" ", 4);
        if (parts.length < 4) {
            return "ERROR INVALID_FORMAT POST requires: POST <x> <y> <colour> <message>";
        }

        int x, y;
        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be integers";
        }

        String colour = parts[2];
        String message = parts[3];

        return board.postNote(x, y, colour, message);
    }

    private String handleGet(String args) {
        if (args.isEmpty()) {
            return board.getAllNotes();
        }
        if (args.equals("PINS")) {
            return board.getAllPins();
        }

        String colour = null;
        Integer containsX = null;
        Integer containsY = null;
        String refersTo = null;

        String[] tokens = args.split(" ");
        int i = 0;
        while (i < tokens.length) {
            String token = tokens[i];
            if (token.startsWith("colour=")) {
                colour = token.substring(7);
            } else if (token.startsWith("contains=")) {
                try {
                    containsX = Integer.parseInt(token.substring(9));
                    if (i + 1 < tokens.length) {
                        i++;
                        containsY = Integer.parseInt(tokens[i]);
                    } else {
                        return "ERROR INVALID_FORMAT contains requires two coordinates";
                    }
                } catch (NumberFormatException e) {
                    return "ERROR INVALID_FORMAT contains coordinates must be integers";
                }
            } else if (token.startsWith("refersTo=")) {
                refersTo = token.substring(9);
            }
            i++;
        }

        return board.getFilteredNotes(colour, containsX, containsY, refersTo);
    }

    private String handlePin(String args) {
        String[] parts = args.split(" ");
        if (parts.length != 2) {
            return "ERROR INVALID_FORMAT PIN requires: PIN <x> <y>";
        }

        int x, y;
        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be integers";
        }

        return board.addPin(x, y);
    }

    private String handleUnpin(String args) {
        String[] parts = args.split(" ");
        if (parts.length != 2) {
            return "ERROR INVALID_FORMAT UNPIN requires: UNPIN <x> <y>";
        }

        int x, y;
        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be integers";
        }

        return board.removePin(x, y);
    }

    private void cleanup() {
        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}
        try {
            if (out != null) out.close();
        } catch (Exception ignored) {}
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}
