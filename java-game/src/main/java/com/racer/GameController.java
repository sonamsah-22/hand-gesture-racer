package com.racer;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    private static final int W = 620, H = 700;

    private final Stage stage;
    private Canvas canvas;
    private GraphicsContext gc;
    private SocketServer server;
    private AnimationTimer timer;

    private Road road;
    private PlayerCar player;
    private List<EnemyCar> enemies = new ArrayList<>();

    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private double baseSpeed = 4;
    private double currentRoadSpeed = 4;
    private double targetRoadSpeed = 4;
    private long lastSpawn = 0;
    private long invincibleUntil = 0;
    private int combo = 0;

    public GameController(Stage stage) { this.stage = stage; }

    public void start() {
        canvas = new Canvas(W, H);
        gc = canvas.getGraphicsContext2D();
        road = new Road(W, H);
        player = new PlayerCar(W);
        server = new SocketServer(9999);
        server.startAsync();

        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: #0a0a1a;");
        stage.setTitle("Hand Gesture Racer");
        stage.setScene(new Scene(root, W, H));
        stage.setResizable(false);
        stage.show();

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameOver) { update(now); render(now); }
                else renderGameOver();
            }
        };
        timer.start();

        canvas.setOnMouseClicked(e -> { if (gameOver) restart(); });
    }

    private void update(long now) {
        String cmd = server.getCommand();

        switch (cmd) {
            case "LEFT":       player.moveLeft();  targetRoadSpeed = baseSpeed; break;
            case "RIGHT":      player.moveRight(); targetRoadSpeed = baseSpeed; break;
            case "ACCELERATE": targetRoadSpeed = baseSpeed * 2.2; break;
            case "BRAKE":      targetRoadSpeed = baseSpeed * 0.15; break;
            default:           targetRoadSpeed = baseSpeed; break;
        }

        currentRoadSpeed += (targetRoadSpeed - currentRoadSpeed) * 0.12;
        road.setSpeed(currentRoadSpeed);

        for (EnemyCar e : enemies) e.setSpeedMultiplier(currentRoadSpeed / baseSpeed);

        player.update();
        road.update();
        score++;
        baseSpeed = 4 + (score / 500.0);

        long spawnInterval = Math.max(900_000_000L,
            2_200_000_000L - (long)(score * 6_000_000L));

        if (now - lastSpawn > spawnInterval) {
            enemies.add(new EnemyCar(W, baseSpeed));
            lastSpawn = now;
        }

        for (EnemyCar e : enemies) e.update();

        enemies.removeIf(e -> {
            if (e.isOffScreen(H)) { combo++; return true; }
            return false;
        });

        if (now > invincibleUntil) {
            for (EnemyCar e : enemies) {
                if (player.collidesWith(e)) {
                    lives--;
                    combo = 0;
                    enemies.remove(e);
                    invincibleUntil = now + 2_000_000_000L;
                    if (lives <= 0) { gameOver = true; timer.stop(); }
                    break;
                }
            }
        }
    }

    private void render(long now) {
        road.draw(gc);
        for (EnemyCar e : enemies) e.draw(gc);
        boolean showPlayer = now > invincibleUntil || ((now / 150_000_000L) % 2 == 0);
        if (showPlayer) player.draw(gc);
        drawHUD(now);
    }

    private void drawHUD(long now) {
        gc.setFill(Color.color(0, 0, 0, 0.55));
        gc.fillRect(0, 0, W, 64);

        // Score
        gc.setFill(Color.web("#FFD740"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("SCORE", 16, 22);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.fillText(String.valueOf(score / 60), 16, 52);

        // Speed with brake/boost label
        String speedLabel = "SPEED";
        Color speedColor = Color.web("#64FFDA");
        if (currentRoadSpeed > baseSpeed * 1.4) {
            speedLabel = "BOOST";
            speedColor = Color.web("#FFD740");
        } else if (currentRoadSpeed < baseSpeed * 0.5) {
            speedLabel = "BRAKE";
            speedColor = Color.web("#FF5252");
        }
        gc.setFill(speedColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(speedLabel, W / 2.0, 22);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.fillText(String.format("%.0f", currentRoadSpeed * 30) + " km/h", W / 2.0, 52);

        // Lives
        gc.setFill(Color.web("#FF5252"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("LIVES", W - 16, 22);
        gc.setFont(Font.font("Arial", 24));
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < 3; i++) hearts.append(i < lives ? "♥ " : "♡ ");
        gc.fillText(hearts.toString().trim(), W - 16, 52);

        // Combo
        if (combo >= 3) {
            gc.setFill(Color.web("#FFD740", 0.9));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("COMBO x" + combo + "!", W / 2.0, 85);
        }

        // AI status
        gc.setFill(server.isConnected() ? Color.web("#69F0AE") : Color.web("#FF5252"));
        gc.fillOval(W - 14, H - 20, 10, 10);
        gc.setFill(Color.web("#ffffff", 0.6));
        gc.setFont(Font.font("Arial", 12));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(server.isConnected() ? "AI Connected" : "Waiting for AI", W - 20, H - 11);
        gc.setTextAlign(TextAlignment.LEFT);

        // Collision warning
        if (now < invincibleUntil) {
            gc.setFill(Color.web("#FF5252", 0.7));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("! COLLISION !", W / 2.0, H - 20);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private void renderGameOver() {
        road.draw(gc);
        gc.setFill(Color.color(0, 0, 0, 0.75));
        gc.fillRect(0, 0, W, H);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#FF5252"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 56));
        gc.fillText("GAME OVER", W / 2.0, H / 2.0 - 70);

        gc.setStroke(Color.web("#FFD740", 0.5));
        gc.setLineWidth(1);
        gc.strokeLine(W * 0.2, H / 2.0 - 50, W * 0.8, H / 2.0 - 50);

        gc.setFill(Color.web("#FFD740"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        gc.fillText("SCORE: " + (score / 60), W / 2.0, H / 2.0);

        gc.setFill(Color.web("#64FFDA"));
        gc.setFont(Font.font("Arial", 20));
        gc.fillText("TOP SPEED: " + String.format("%.0f", baseSpeed * 30) + " km/h", W / 2.0, H / 2.0 + 40);

        gc.setFill(Color.web("#ffffff", 0.5));
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Best Combo: x" + combo, W / 2.0, H / 2.0 + 75);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("[ Click to Restart ]", W / 2.0, H / 2.0 + 120);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void restart() {
        score = 0;
        lives = 3;
        baseSpeed = 4;
        currentRoadSpeed = 4;
        targetRoadSpeed = 4;
        combo = 0;
        enemies.clear();
        player = new PlayerCar(W);
        gameOver = false;
        timer.start();
    }
}