//Simple 2D vector with chaining for add and scale
public class Vec2 {
    private final double x;
    private final double y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 add(Vec2 other) {
        return new Vec2(this.x + other.x, this.y + other.y);
    }

    public Vec2 scale(double s) {
        return new Vec2(this.x * s, this.y * s);
    }

    public double x() { return x; }
    public double y() { return y; }
}
