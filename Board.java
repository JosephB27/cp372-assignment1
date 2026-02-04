import java.util.*;

public class Board {
    private final int boardWidth;
    private final int boardHeight;
    private final int noteWidth;
    private final int noteHeight;
    private final List<String> validColours;
    private final List<Note> notes;
    private final Set<Pin> pins;

    public Board(int boardWidth, int boardHeight, int noteWidth, int noteHeight, List<String> validColours) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.validColours = validColours;
        this.notes = new ArrayList<>();
        this.pins = new HashSet<>();
    }

    public boolean isColourValid(String colour) {
        return validColours.contains(colour);
    }

    public boolean isNoteWithinBounds(int x, int y) {
        return x >= 0 && y >= 0 && x + noteWidth <= boardWidth && y + noteHeight <= boardHeight;
    }

    private boolean hasCompleteOverlap(int x, int y) {
        for (Note note : notes) {
            if (note.x == x && note.y == y) {
                return true;
            }
        }
        return false;
    }

    private boolean isPointInsideAnyNote(int px, int py) {
        for (Note note : notes) {
            if (note.containsPoint(px, py, noteWidth, noteHeight)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotePinned(Note note) {
        for (Pin pin : pins) {
            if (note.containsPoint(pin.x, pin.y, noteWidth, noteHeight)) {
                return true;
            }
        }
        return false;
    }

    public synchronized String postNote(int x, int y, String colour, String message) {
        if (!isColourValid(colour)) {
            return "ERROR COLOUR_NOT_SUPPORTED " + colour + " is not a valid colour";
        }
        if (!isNoteWithinBounds(x, y)) {
            return "ERROR OUT_OF_BOUNDS Note exceeds board boundaries";
        }
        if (hasCompleteOverlap(x, y)) {
            return "ERROR COMPLETE_OVERLAP Note with identical position already exists";
        }
        notes.add(new Note(x, y, colour, message));
        return "OK NOTE_POSTED";
    }

    public synchronized String getAllNotes() {
        StringBuilder sb = new StringBuilder();
        sb.append("OK ").append(notes.size());
        for (Note note : notes) {
            sb.append("\n");
            sb.append("NOTE ");
            sb.append(note.x).append(" ");
            sb.append(note.y).append(" ");
            sb.append(note.colour).append(" ");
            sb.append(note.message).append(" ");
            sb.append("PINNED=").append(isNotePinned(note));
        }
        return sb.toString();
    }

    public synchronized String getFilteredNotes(String colour, Integer containsX, Integer containsY, String refersTo) {
        List<Note> matches = new ArrayList<>();
        for (Note note : notes) {
            boolean match = true;
            if (colour != null && !note.colour.equals(colour)) {
                match = false;
            }
            if (containsX != null && containsY != null) {
                if (!note.containsPoint(containsX, containsY, noteWidth, noteHeight)) {
                    match = false;
                }
            }
            if (refersTo != null && !note.message.contains(refersTo)) {
                match = false;
            }
            if (match) {
                matches.add(note);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("OK ").append(matches.size());
        for (Note note : matches) {
            sb.append("\n");
            sb.append("NOTE ");
            sb.append(note.x).append(" ");
            sb.append(note.y).append(" ");
            sb.append(note.colour).append(" ");
            sb.append(note.message).append(" ");
            sb.append("PINNED=").append(isNotePinned(note));
        }
        return sb.toString();
    }

    public synchronized String getAllPins() {
        StringBuilder sb = new StringBuilder();
        sb.append("OK ").append(pins.size());
        for (Pin pin : pins) {
            sb.append("\n");
            sb.append("PIN ").append(pin.x).append(" ").append(pin.y);
        }
        return sb.toString();
    }

    public synchronized String addPin(int x, int y) {
        if (!isPointInsideAnyNote(x, y)) {
            return "ERROR NO_NOTE_AT_COORDINATE No note contains the specified point";
        }
        Pin newPin = new Pin(x, y);
        if (pins.contains(newPin)) {
            return "ERROR PIN_ALREADY_EXISTS A pin already exists at this coordinate";
        }
        pins.add(newPin);
        return "OK PIN_ADDED";
    }

    public synchronized String removePin(int x, int y) {
        Pin target = new Pin(x, y);
        if (!pins.contains(target)) {
            return "ERROR PIN_NOT_FOUND No pin exists at the specified coordinate";
        }
        pins.remove(target);
        return "OK PIN_REMOVED";
    }

    public synchronized String shake() {
        List<Note> unpinned = new ArrayList<>();
        for (Note note : notes) {
            if (!isNotePinned(note)) {
                unpinned.add(note);
            }
        }
        notes.removeAll(unpinned);
        return "OK SHAKE_COMPLETE";
    }

    public synchronized String clear() {
        notes.clear();
        pins.clear();
        return "OK CLEAR_COMPLETE";
    }
}
