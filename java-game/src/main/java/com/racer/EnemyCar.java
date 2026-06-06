package com.racer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

public class EnemyCar {
    public double x, y;
    public final double width = 52, height = 96;
    private double speed;
    private double speedMultiplier = 1.0;
    private Color bodyColor, accentColor;
    private int carType; // 0=sedan, 1=suv, 2=sports

    private static final Color[][] SCHEMES = {
        {Color.web("#B71C1C"), Color.web("#FF5252")},
        {Color.web("#1B5E20"), Color.web("#69F0AE")},
        {Color.web("#F57F17"), Color.web("#FFD740")},
        {Color.web("#4A148C"), Color.web("#EA80FC")},
        {Color.web("#006064"), Color.web("#84FFFF")},
        {Color.web("#37474F"), Color.web("#90A4AE")},
        {Color.web("#BF360C"), Color.web("#FF6E40")},
    };

     public void setSpeedMultiplier(double m) {   // ← ADD THIS METHOD
        this.speedMultiplier = m;
    }

    public EnemyCar(double canvasWidth, double baseSpeed) {
        Random rand = new Random();
        double laneW = (canvasWidth - Road.KERB_W * 2) / Road.LANES;
        int lane = rand.nextInt(Road.LANES);
        this.x = Road.KERB_W + lane * laneW + (laneW / 2) - width / 2;
        this.y = -height - rand.nextInt(80);
        this.speed = baseSpeed + rand.nextDouble() * 1.2;
        int scheme = rand.nextInt(SCHEMES.length);
        this.bodyColor = SCHEMES[scheme][0];
        this.accentColor = SCHEMES[scheme][1];
        this.carType = rand.nextInt(3);
    }

    public void update() { y += speed  * speedMultiplier ; }
    public boolean isOffScreen(double h) { return y > h + 20; }

    public void draw(GraphicsContext gc) {
        double cx = x, cy = y, w = width, h = height;

        // Shadow
        gc.setFill(Color.color(0, 0, 0, 0.3));
        gc.fillOval(cx + 6, cy + h - 8, w - 12, 16);

        if (carType == 1) { // SUV — taller body
            gc.setFill(bodyColor);
            gc.fillRoundRect(cx + 3, cy + 12, w - 6, h - 22, 10, 10);
            gc.setFill(bodyColor.brighter());
            gc.fillRoundRect(cx + 7, cy + 14, w - 14, h * 0.5, 8, 8);
        } else if (carType == 2) { // Sports — low profile
            gc.setFill(bodyColor);
            gc.fillRoundRect(cx + 5, cy + 20, w - 10, h - 30, 14, 14);
            gc.setFill(bodyColor.brighter());
            gc.fillRoundRect(cx + 12, cy + 22, w - 24, h * 0.35, 8, 8);
        } else { // Sedan
            gc.setFill(bodyColor);
            gc.fillRoundRect(cx + 4, cy + 16, w - 8, h - 26, 12, 12);
            gc.setFill(bodyColor.brighter());
            gc.fillRoundRect(cx + 9, cy + 18, w - 18, h * 0.42, 9, 9);
        }
    


        // Windshield
        gc.setFill(Color.web("#B2EBF2", 0.8));
        gc.fillRoundRect(cx + 11, cy + 20, w - 22, 24, 5, 5);
        gc.setFill(Color.web("#ffffff", 0.25));
        gc.fillRoundRect(cx + 13, cy + 22, 7, 10, 3, 3);

        // Rear window
        gc.setFill(Color.web("#B2EBF2", 0.6));
        gc.fillRoundRect(cx + 12, cy + h - 44, w - 24, 16, 4, 4);

        // Accent stripe
        gc.setFill(accentColor.deriveColor(0, 1, 1, 0.4));
        gc.fillRect(cx + 4, cy + h / 2 - 3, w - 8, 5);

        // Tail lights (top — facing player)
        gc.setFill(Color.web("#FF1744"));
        gc.fillRoundRect(cx + 5, cy + 14, 11, 5, 2, 2);
        gc.fillRoundRect(cx + w - 16, cy + 14, 11, 5, 2, 2);
        gc.setFill(Color.web("#FF6666", 0.5));
        gc.fillOval(cx + 3, cy + 12, 15, 8);
        gc.fillOval(cx + w - 18, cy + 12, 15, 8);

        // Wheels
        gc.setFill(Color.web("#111111"));
        gc.fillRoundRect(cx - 5, cy + 18, 9, 20, 3, 3);
        gc.fillRoundRect(cx + w - 4, cy + 18, 9, 20, 3, 3);
        gc.fillRoundRect(cx - 5, cy + h - 34, 9, 20, 3, 3);
        gc.fillRoundRect(cx + w - 4, cy + h - 34, 9, 20, 3, 3);
        gc.setFill(Color.web("#777777"));
        gc.fillOval(cx - 3, cy + 21, 5, 14);
        gc.fillOval(cx + w - 2, cy + 21, 5, 14);
        gc.fillOval(cx - 3, cy + h - 31, 5, 14);
        gc.fillOval(cx + w - 2, cy + h - 31, 5, 14);
    }
}