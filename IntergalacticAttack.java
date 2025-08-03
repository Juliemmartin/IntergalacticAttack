import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonBar;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class IntergalacticAttack extends Application {

    private static final double PLAYER_WIDTH = 60;
    private static final double PLAYER_HEIGHT = 60;

    private static final double ENEMY_WIDTH = 50;
    private static final double ENEMY_HEIGHT = 50;

    private static final double PROJECTILE_WIDTH = 10;
    private static final double PROJECTILE_HEIGHT = 20;

    private boolean started = false;
    private boolean gameOver = false;
    private double invulnTimer = 1.0;

    private Image startImg; //start screen 
    private Image playerImg;
    private double playerX, playerY;
    private final double playerSpeed = 300;

    private boolean shootingCooldown = false;
    private long lastShot = 0;
    private double player2X, player2Y;
    private boolean shootingCooldown2 = false;
    private long lastShot2 = 0;
    private int gameMode = 1; 

    private Image enemyImg1;
    private Image enemyImg2;
    private int spawnCount = 0;

    private final List<Enemy> enemies = new ArrayList<>();

    private double enemySpawnTimer = -1.0;
    private final double enemySpawnInterval = 0.5;

    private final List<Projectile> projectiles = new ArrayList<>();

    private final List<PowerUp> powerUps = new ArrayList<>();
    private boolean rapidFire = false;
    private double rapidFireTimer = 0;

    private final Set<KeyCode> pressed = new HashSet<>();
    private final Random rand = new Random();

    private MediaPlayer bgm;
    private int score = 0;
    private long lastNanoTime = 0;

    @Override
    public void start(Stage stage) {
        //load start image
        try {
            startImg = new Image(new File("start.png").toURI().toString());
            System.out.println("Loaded start.png: " + startImg.getWidth() + "x" + startImg.getHeight());
        } catch (Exception e) {
            startImg = null;
            System.out.println("Warning: could not load start.png: " + e.getMessage());
        }

        //load other assets
        try {

            playerImg = new Image(new File("playerRocket.png").toURI().toString());
            enemyImg1 = new Image(new File("enemyRocket.png").toURI().toString());
            enemyImg2 = new Image(new File("enemyRocket2.png").toURI().toString());
            Media music = new Media(new File("background.mp3").toURI().toString());

            bgm = new MediaPlayer(music);
            bgm.setCycleCount(MediaPlayer.INDEFINITE);
            bgm.setVolume(0.3);
            bgm.setOnReady(() -> bgm.play());

        } catch (Exception e) {
            System.out.println("Asset load warning: " + e.getMessage());
        }

        Canvas canvas = new Canvas(800, 600);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        playerX = 400 - PLAYER_WIDTH / 2;
        playerY = 600 - 80 ;
        player2X = 400 + PLAYER_WIDTH;
        player2Y = 600 - 80;

        shootingCooldown2 = false;
        lastShot2 = 0;

        Group root = new Group(canvas);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            pressed.add(e.getCode());

            if (e.getCode() == KeyCode.R && gameOver) {
                restart();
                started = false;
            }

            if (!started) {
                selectGameMode(stage);
            }
        });
        scene.setOnKeyReleased(e -> pressed.remove(e.getCode()));

        stage.setTitle("Intergalactic Attack");
        stage.setScene(scene);
        stage.show();

        lastNanoTime = System.nanoTime();

        new AnimationTimer() {

            @Override
            public void handle(long now) {
                double delta = (now - lastNanoTime) / 1_000_000_000.0;
                lastNanoTime = now;
                update(delta);
                render(gc);
            }

        }.start();
    
    }

    private void selectGameMode(Stage stage) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select Players");

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: limegreen;");

        TextArea instructions = new TextArea(

            "Controls:\n\n" +
            "1 Player:\n" +
            "  - Move: W A S D\n" +
            "  - Shoot: SPACE\n\n" +
            "2 Players:\n" +
            "  - Player 1:\n" +
            "      Move: W A S D\n" +
            "      Shoot: SPACE\n" +
            "  - Player 2:\n" +
            "      Move: Arrow Keys\n" +
            "      Shoot: ENTER"
        );
        instructions.setEditable(false);
        instructions.setWrapText(true);
        instructions.setPrefHeight(280);

        ButtonType onePlayer = new ButtonType("1 Player", ButtonBar.ButtonData.OK_DONE);
        ButtonType twoPlayers = new ButtonType("2 Players", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(onePlayer, twoPlayers);

        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Choose how you want to play:"), instructions);
        pane.setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == onePlayer) {

            gameMode = 1;
            stage.setTitle("Intergalactic Attack - 1 Player");

        } else {
            gameMode = 2;
            stage.setTitle("Intergalactic Attack - 2 Player");
        }

        started = true;
        invulnTimer = 1.0;
    }
    //Restart
    private void restart() {

        score = 0;
        enemies.clear();
        projectiles.clear();
        powerUps.clear();
        gameOver = false;
        started = false;
        invulnTimer = 1.0;
        enemySpawnTimer = -1.0;
        playerX = 400 - PLAYER_WIDTH / 2;
        playerY = 600 - 80;
        player2X = 400 + PLAYER_WIDTH;
        player2Y = 600 - 80;
        shootingCooldown = false;
        shootingCooldown2 = false;
        lastShot = 0;
        lastShot2 = 0;
        spawnCount = 0;
        rapidFire = false;
        rapidFireTimer = 0;

    }

    private void update(double dt) {
        if (!started || gameOver) return;

        if (invulnTimer > 0) invulnTimer -= dt;

        //Player 1 movement (A,D,W,S)
        if (pressed.contains(KeyCode.A)) playerX -= playerSpeed * dt;
        if (pressed.contains(KeyCode.D)) playerX += playerSpeed * dt;
        if (pressed.contains(KeyCode.W)) playerY -= playerSpeed * dt;
        if (pressed.contains(KeyCode.S)) playerY += playerSpeed * dt;

        playerX = clamp(playerX, 0, 800 - PLAYER_WIDTH);
        playerY = clamp(playerY, 0, 600 - PLAYER_HEIGHT);

        //Player 1 shoot
        if (pressed.contains(KeyCode.SPACE)) {

            long interval = rapidFire ? 100 : 300;
            if (!shootingCooldown || (System.currentTimeMillis() - lastShot) > interval) {
                double shotX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
                projectiles.add(new Projectile(shotX, playerY - 10, -400));
                shootingCooldown = true;
                lastShot = System.currentTimeMillis();
            }
        } else {
            shootingCooldown = false;
        }

        //Player 2 logic in 2-player mode
        if (gameMode == 2) {
            if (pressed.contains(KeyCode.LEFT)) player2X -= playerSpeed * dt;
            if (pressed.contains(KeyCode.RIGHT)) player2X += playerSpeed * dt;
            if (pressed.contains(KeyCode.UP)) player2Y -= playerSpeed * dt;
            if (pressed.contains(KeyCode.DOWN)) player2Y += playerSpeed * dt;

            player2X = clamp(player2X, 0, 800 - PLAYER_WIDTH);
            player2Y = clamp(player2Y, 0, 600 - PLAYER_HEIGHT);

            if (pressed.contains(KeyCode.ENTER)) {
                if (!shootingCooldown2 || (System.currentTimeMillis() - lastShot2) > 300) {
                    double shotX = player2X + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
                    projectiles.add(new Projectile(shotX, player2Y - 10, -400));
                    shootingCooldown2 = true;
                    lastShot2 = System.currentTimeMillis();
                }
            } else {
                shootingCooldown2 = false;
            }
        }

        //Enemy spawning with alternating images of ships
        enemySpawnTimer += dt;
        if (enemySpawnTimer >= enemySpawnInterval) {
            enemySpawnTimer = 0;
            double ex = rand.nextDouble() * (800 - ENEMY_WIDTH);
            boolean useSecond = (spawnCount % 2) == 1;
            Image chosen = useSecond ? enemyImg2 : enemyImg1;
            enemies.add(new Enemy(ex, -50, 60, chosen, useSecond));
            if (rand.nextDouble() < 0.4) {
                double px = rand.nextDouble() * (800 - 20);
                powerUps.add(new PowerUp(px, -10));
                //Console 
                System.out.println("Power-up spawned at x=" + px);
            }
            spawnCount++;
        }

        //Enemy movement
        Iterator<Enemy> eit = enemies.iterator();
        while (eit.hasNext()) {
            Enemy e = eit.next();
            e.y += e.speed * dt;
            if (e.y > 650) eit.remove();
        }

        //PowerUp 
        Iterator<PowerUp> puMove = powerUps.iterator();
        while (puMove.hasNext()) {
            PowerUp p = puMove.next();
            p.y += p.speed * dt;
            if (p.y > 650) puMove.remove();
        }

        if (rapidFire) {
            rapidFireTimer -= dt;
            if (rapidFireTimer <= 0) {
                rapidFire = false;
            }
        }

        //Projectiles
        Iterator<Projectile> pit = projectiles.iterator();
        while (pit.hasNext()) {
            Projectile p = pit.next();
            p.y += p.dy * dt;
            if (p.y < -20) pit.remove();
        }

        //Projectile/enemy collision
        for (Iterator<Projectile> pIt = projectiles.iterator(); pIt.hasNext(); ) {
            Projectile p = pIt.next();
            Rectangle2D pb = p.getBounds();
            boolean hit = false;
            for (Iterator<Enemy> eIt = enemies.iterator(); eIt.hasNext(); ) {
                Enemy e = eIt.next();
                if (pb.intersects(e.getBounds())) {
                    eIt.remove();
                    pIt.remove();
                    score += 10;
                    hit = true;
                    break;
                }
            }
            if (hit) break;
        }

        //Player collision
        Rectangle2D playerBounds = new Rectangle2D(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        Rectangle2D player2Bounds = new Rectangle2D(player2X, player2Y, PLAYER_WIDTH, PLAYER_HEIGHT);

        if (invulnTimer <= 0) {
            for (Enemy e : enemies) {
                if (playerBounds.intersects(e.getBounds()) ||
                    (gameMode == 2 && player2Bounds.intersects(e.getBounds()))) {
                    gameOver = true;
                    break;
                }
            }
        }

        //PowerUp pickup
        Iterator<PowerUp> puHit = powerUps.iterator();
        while (puHit.hasNext()) {
            PowerUp p = puHit.next();
            if (p.getBounds().intersects(playerBounds)) {
                rapidFire = true;
                rapidFireTimer = 5.0;
                puHit.remove();
                break;
            }
        }
    }

    private void render(GraphicsContext gc) {
        //clear
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);

        if (!started) {
            if (startImg != null && startImg.getWidth() > 0) {
                double maxW = 600;
                double scale = Math.min(maxW / startImg.getWidth(), 1.0);
                double drawW = startImg.getWidth() * scale;
                double drawH = startImg.getHeight() * scale;
                double x = (800 - drawW) / 2;
                double y = (600 - drawH) / 2;
                gc.drawImage(startImg, x, y, drawW, drawH);
            } else {
                gc.setFill(Color.DARKGRAY);
                gc.fillRect(0, 0, 800, 600);
            }
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(30));
            gc.fillText("By", 350, 500);
            gc.fillText("Julie Martin", 295, 560);
            return;
        }

        //invulnerability 
        if (invulnTimer > 0) {
            gc.setFill(Color.color(1, 1, 1, 0.15));
            gc.fillOval(playerX - 5, playerY - 5, PLAYER_WIDTH + 10, PLAYER_HEIGHT + 10);
        }

        //draw players
        if (playerImg != null) gc.drawImage(playerImg, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        else {
            gc.setFill(Color.CYAN);
            gc.fillOval(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        }
        if (gameMode == 2) {
            if (playerImg != null) gc.drawImage(playerImg, player2X, player2Y, PLAYER_WIDTH, PLAYER_HEIGHT);
            else {
                gc.setFill(Color.ORANGE);
                gc.fillOval(player2X, player2Y, PLAYER_WIDTH, PLAYER_HEIGHT);
            }
        }

        //draw enemies
        for (Enemy e : enemies) {
            e.draw(gc);
        }

        //draw powerups
        for (PowerUp p : powerUps) {
            p.draw(gc);
        }

        //draw projectiles
        gc.setFill(Color.YELLOW);
        for (Projectile p : projectiles) {
            gc.fillRect(p.x, p.y, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        //score
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(20));
        gc.fillText("Score: " + score, 10, 25);

        if (gameOver) {
            gc.setFill(Color.rgb(255, 255, 255, 0.9));
            gc.fillRect(0, 200, 800, 200);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(40));
            gc.fillText("GAME OVER", 260, 270);
            gc.setFont(Font.font(20));
            gc.fillText("Final Score: " + score, 340, 310);
            gc.fillText("Press R to restart", 325, 345);
        }
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private class Enemy {
        double x, y;
        double speed;
        Image img;
        boolean isSecond;

        Enemy(double x, double y, double speed, Image img, boolean isSecond) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.img = img;
            this.isSecond = isSecond;
        }

        Rectangle2D getBounds() {
            return new Rectangle2D(x, y, ENEMY_WIDTH, ENEMY_HEIGHT);
        }

        void draw(GraphicsContext gc) {
            if (img != null) {
                gc.drawImage(img, x, y, ENEMY_WIDTH, ENEMY_HEIGHT);
            } else {
                gc.setFill(isSecond ? Color.GREEN : Color.RED);
                gc.fillRect(x, y, ENEMY_WIDTH, ENEMY_HEIGHT);
            }
        }
    }

    private class Projectile {
        double x, y;
        double dy;

        Projectile(double x, double y, double dy) {
            this.x = x;
            this.y = y;
            this.dy = dy;
        }

        Rectangle2D getBounds() {
            return new Rectangle2D(x, y, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }
    }

    private class PowerUp {
        double x, y;
        double speed = 150;

        PowerUp(double x, double y) {
            this.x = x;
            this.y = y;
        }

        Rectangle2D getBounds() {
            return new Rectangle2D(x, y, 20, 20);
        }

        void draw(GraphicsContext gc) {
            gc.setFill(Color.PURPLE);
            gc.fillOval(x, y, 20, 20);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 14));
            gc.fillText("P", x + 6, y + 15);
        }
    }
//Run Main
    public static void main(String[] args) {
        launch(args);
    }
}
