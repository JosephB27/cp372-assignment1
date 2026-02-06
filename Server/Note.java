public class Note {
    public final int x;
    public final int y;
    public final String colour;
    public final String message;

    public Note(int x, int y, String colour, String message) {
        this.x = x;
        this.y = y;
        this.colour = colour;
        this.message = message;
    }

    public boolean containsPoint(int px, int py, int noteWidth, int noteHeight) {
        return px >= x && py >= y && px < x + noteWidth && py < y + noteHeight;
    }

    public boolean overlapsCompletely(Note other) {
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note)) return false;
        Note note = (Note) o;
        return x == note.x && y == note.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
