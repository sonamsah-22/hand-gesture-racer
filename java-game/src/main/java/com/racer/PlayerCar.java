package com.racer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PlayerCar {
    public double x, y;
    public final double width = 52, height = 96;
    private double vx = 0; // velocity for smooth movement
    private final double minX = Road.KERB_W + 4;
    private final double maxX;
    private double tilt = 0;

    public PlayerCar(double canvasWidth) {
        this.x = canvasWidth / 2 - width / 2;
        this.y = 520;
        this.maxX = canvasWidth - Road.KERB_W - width - 4;
    }

    public void moveLeft() {
        vx = Math.max(vx - 2.5, -18);
    }

    public void moveRight() {
        vx = Math.min(vx + 2.5, 18);
    }

    public void update() {
        x += vx;
        vx *= 0.72; // friction
        tilt = -vx * 0.6;
        x = Math.max(minX, Math.min(maxX, x));
        if (Math.abs(vx) < 0.1) vx = 0;
    }

    public void draw(GraphicsContext gc) {
        gc.save();
        gc.translate(x + width / 2, y + height / 2);
        gc.rotate(tilt);
        gc.translate(-(width / 2), -(height / 2));

        double cx = 0, cy = 0, w = width, h = height;

        // Shadow
        gc.setFill(Color.color(0, 0, 0, 0.35));
        gc.fillOval(cx + 6, cy + h - 10, w - 12, 18);

        // Headlight glow on road
        gc.setFill(Color.color(1, 1, 0.7, 0.12));
        gc.fillOval(cx - 10, cy + h - 5, w + 20, 40);

        // Body
        gc.setFill(Color.web("#1565C0"));
        gc.fillRoundRect(cx + 4, cy + 16, w - 8, h - 28, 12, 12);

        // Hood
        gc.setFill(Color.web("#1976D2"));
        gc.fillRoundRect(cx + 6, cy + h - 30, w - 12, 22, 8, 8);

        // Roof
        gc.setFill(Color.web("#0D47A1"));
        gc.fillRoundRect(cx + 10, cy + 20, w - 20, h * 0.38, 10, 10);

        // Windshield
        gc.setFill(Color.web("#B3E5FC", 0.85));
        gc.fillRoundRect(cx + 11, cy + 22, w - 22, 28, 6, 6);

        // Windshield reflection
        gc.setFill(Color.web("#ffffff", 0.3));
        gc.fillRoundRect(cx + 13, cy + 24, 8, 12, 3, 3);

        // Rear window
        gc.setFill(Color.web("#B3E5FC", 0.7));
        gc.fillRoundRect(cx + 12, cy + h - 46, w - 24, 18, 5, 5);

        // Side stripes
        gc.setFill(Color.web("#ffffff", 0.15));
        gc.fillRect(cx + 4, cy + 40, 3, h - 60);
        gc.fillRect(cx + w - 7, cy + 40, 3, h - 60);

        // Front headlights
        gc.setFill(Color.web("#FFFDE7"));
        gc.fillRoundRect(cx + 6, cy + h - 14, 12, 8, 3, 3);
        gc.fillRoundRect(cx + w - 18, cy + h - 14, 12, 8, 3, 3);

        // Headlight glow
        gc.setFill(Color.web("#FFFF99", 0.6));
        gc.fillOval(cx + 4, cy + h - 16, 16, 12);
        gc.fillOval(cx + w - 20, cy + h - 16, 16, 12);

        // Tail lights
        gc.setFill(Color.web("#FF1744"));
        gc.fillRoundRect(cx + 6, cy + 16, 10, 6, 2, 2);
        gc.fillRoundRect(cx + w - 16, cy + 16, 10, 6, 2, 2);

        // Wheels
        gc.setFill(Color.web("#111111"));
        gc.fillRoundRect(cx - 6, cy + 20, 10, 20, 4, 4);
        gc.fillRoundRect(cx + w - 4, cy + 20, 10, 20, 4, 4);
        gc.fillRoundRect(cx - 6, cy + h - 36, 10, 20, 4, 4);
        gc.fillRoundRect(cx + w - 4, cy + h - 36, 10, 20, 4, 4);

        // Wheel rims
        gc.setFill(Color.web("#888888"));
        gc.fillOval(cx - 4, cy + 23, 6, 14);
        gc.fillOval(cx + w - 2, cy + 23, 6, 14);
        gc.fillOval(cx - 4, cy + h - 33, 6, 14);
        gc.fillOval(cx + w - 2, cy + h - 33, 6, 14);

        gc.restore();
    }

    public boolean collidesWith(EnemyCar enemy) {
        double margin = 12; // generous hitbox
        return x + margin < enemy.x + enemy.width - margin
            && x + width - margin > enemy.x + margin
            && y + margin < enemy.y + enemy.height - margin
            && y + height - margin > enemy.y + margin;
    }
}