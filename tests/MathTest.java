//Vector math and overlap tests
public class MathTest {
    public static void main(String[] args) {
        testVec2Operations();
        testAABBOverlap();
        System.out.println("Math tests are successful!");
    }

    static void testVec2Operations() {
        Vec2 v = new Vec2(5, 5).add(new Vec2(5, 5)).scale(2);
        if (v.x() != 20 || v.y() != 20) {
            throw new AssertionError("Vec2 failure! Expected (20,20) got (" + v.x() + "," + v.y() + ")");
        }
    }

    static void testAABBOverlap() {
        AABB a = new AABB(0, 0, 10, 10);
        AABB b = new AABB(5, 5, 10, 10);
        if (!a.overlaps(b) || !b.overlaps(a)) {
            throw new AssertionError("Overlap test failed!");
        }
    }
}
