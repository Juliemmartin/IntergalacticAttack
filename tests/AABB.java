// Axis-aligned bounding box with basic overlap and validity checks
public class AABB {
    private final double x, y, w, h;

    public AABB(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public boolean valid() {
        return !Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(w) && !Double.isNaN(h)
                && w >= 0 && h >= 0;
    }

    public boolean overlaps(AABB other) {
        if (!this.valid() || !other.valid()) return false;
        return this.x < other.x + other.w &&
               this.x + this.w > other.x &&
               this.y < other.y + other.h &&
               this.y + this.h > other.y;
    }
}
