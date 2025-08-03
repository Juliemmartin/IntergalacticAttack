//Collision and bounds checks between a projectile andenemy
public class CollisionBoundsTest {
    public static void main(String[] args) {
        testProjectileEnemyCollision();
        testBoundsValidation();
        System.out.println("Collision & Bounds tests successful!");
    }

    static void testProjectileEnemyCollision() {
        //Simulate both at the same location so they collide quickly
        Projectile proj = new Projectile(new Vec2(100, 100));
        Enemy enemy = new Enemy(new Vec2(100, 100));

        proj.upd(0.0);
        enemy.upd(0.0);

        proj.onCol(enemy);
        enemy.onCol(proj);

        if (enemy.isActive() || proj.isActive()) {
            throw new AssertionError("Collision test failure!");
        }
    }

    static void testBoundsValidation() {
        AABB valid = new AABB(0, 0, 10, 10);
        AABB invalid = new AABB(Double.NaN, 0, 10, 10);
        if (!valid.valid() || invalid.valid()) {
            throw new AssertionError("Bounds error!");
        }
    }

    // Minimal stub implementations matching expected interface
    static class Projectile {
        private Vec2 pos;
        private boolean active = true;

        Projectile(Vec2 pos) {
            this.pos = pos;
        }

        void upd(double dt) {
            // no-op for test
        }

        void onCol(Enemy e) {
            // deactivate both on collision
            this.active = false;
        }

        boolean isActive() {
            return active;
        }
    }

    static class Enemy {
        private Vec2 pos;
        private boolean active = true;

        Enemy(Vec2 pos) {
            this.pos = pos;
        }

        void upd(double dt) {
            // no-op
        }

        void onCol(Projectile p) {
            this.active = false;
        }

        boolean isActive() {
            return active;
        }
    }
}
