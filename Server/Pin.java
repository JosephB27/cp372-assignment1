public class Pin {
    public final int x;
    public final int y;

    public Pin(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pin)) return false;
        Pin pin = (Pin) o;
        return x == pin.x && y == pin.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
