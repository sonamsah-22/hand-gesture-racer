package com.racer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PlayerCar {
    public double x, y;
    public final double width = 52, height = 96;

    private double vx = 0;          // horizontal velocity
    private double tilt = 0;        // visual tilt angle
    private double tiltVelocity = 0;

    // acceleration and friction tuning
    private final double ACCEL     = 1.8;   // how fast it picks up sideways speed
    private final double MAX_VX    = 14;    // max sideways speed
    private final double FRICTION  = 0.78;  // how quickly it slows (lower = slidier)
    private final double MAX_TILT  = 12;    // max visual tilt in degrees

    private final double minX;
    private final double maxX;

    // for smooth speed-linked steering feel
    private double externalSpeedRatio = 1.0; // set by GameController

    public PlayerCar(double canvasWidth) {
        this.x = canvasWidth / 2 - width / 2;
        this.y = 520;
        this.minX = Road.KERB_W + 4;
        this.maxX = canvasWidth - Road.KERB_W - width - 4;
    }

    // GameController calls this every frame to pass current speed ratio
    public void setSpeedRatio(double ratio) {
        this.externalSpeedRatio = ratio;
    }

    public void moveLeft() {
        // braking = tighter turn, accelerating = wider drift
        double accel = ACCEL * (0.6 + externalSpeedRatio * 0.8);
        vx = Math.max(vx - accel, -MAX_VX);
    }

    public void moveRight() {
        double accel = ACCEL * (0.6 + externalSpeedRatio * 0.8);
        vx = Math.min(vx + accel, MAX_VX);
    }

    public void update() {
        // apply velocity
        x += vx;

        // friction — car naturally slows sideways movement
        vx *= FRICTION;
        if (Math.abs(vx) < 0.05) vx = 0;

        // clamp to road
        if (x < minX) { x = minX; vx = vx * -0.3; } // slight bounce off kerb
        if (x > maxX) { x = maxX; vx = vx * -0.3; }

        // smooth tilt — follows velocity with slight lag
        double targetTilt = (vx / MAX_VX) * MAX_TILT;
        tiltVelocity += (targetTilt - tilt) * 0.25;
        tiltVelocity *= 0.6;
        tilt += tiltVelocity;
    }

    public void draw(GraphicsContext gc) {
        gc.save();
        gc.translate(x + width / 2, y + height / 2);
        gc.rotate(tilt);
        gc.translate(-(width / 2), -(height / 2));

        double cx = 0, cy = 0, w = width, h = height;

        // Shadow — shifts with tilt for depth
        gc.setFill(Color.color(0, 0, 0, 0.3));
        gc.fillOval(cx + 4 + tilt * 0.5, cy + h - 8, w - 8, 16);

        // Headlight glow on road
        gc.setFill(Color.color(1, 1, 0.7, 0.10));
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

        // Windshield reflection — shifts with tilt
        double reflectX = cx + 13 + (tilt > 0 ? 2 : 0);
        gc.setFill(Color.web("#ffffff", 0.3));
        gc.fillRoundRect(reflectX, cy + 24, 8, 12, 3, 3);

        // Rear window
        gc.setFill(Color.web("#B3E5FC", 0.7));
        gc.fillRoundRect(cx + 12, cy + h - 46, w - 24, 18, 5, 5);

        // Side stripes — brighter on the side we're turning toward
        double leftAlpha  = vx < 0 ? 0.35 : 0.12;
        double rightAlpha = vx > 0 ? 0.35 : 0.12;
        gc.setFill(Color.web("#ffffff", leftAlpha));
        gc.fillRect(cx + 4, cy + 40, 3, h - 60);
        gc.setFill(Color.web("#ffffff", rightAlpha));
        gc.fillRect(cx + w - 7, cy + 40, 3, h - 60);

        // Headlights
        gc.setFill(Color.web("#FFFDE7"));
        gc.fillRoundRect(cx + 6, cy + h - 14, 12, 8, 3, 3);
        gc.fillRoundRect(cx + w - 18, cy + h - 14, 12, 8, 3, 3);
        gc.setFill(Color.web("#FFFF99", 0.6));
        gc.fillOval(cx + 4, cy + h - 16, 16, 12);
        gc.fillOval(cx + w - 20, cy + h - 16, 16, 12);

        // Brake lights — glow brighter when braking
        double brakeAlpha = (externalSpeedRatio < 0.5) ? 1.0 : 0.5;
        gc.setFill(Color.web("#FF1744", brakeAlpha));
        gc.fillRoundRect(cx + 6, cy + 16, 10, 6, 2, 2);
        gc.fillRoundRect(cx + w - 16, cy + 16, 10, 6, 2, 2);
        if (brakeAlpha > 0.8) {
            gc.setFill(Color.web("#FF6666", 0.5));
            gc.fillOval(cx + 2, cy + 12, 18, 12);
            gc.fillOval(cx + w - 20, cy + 12, 18, 12);
        }

        // Wheels
        gc.setFill(Color.web("#111111"));
        gc.fillRoundRect(cx - 6, cy + 20, 10, 20, 4, 4);
        gc.fillRoundRect(cx + w - 4, cy + 20, 10, 20, 4, 4);
        gc.fillRoundRect(cx - 6, cy + h - 36, 10, 20, 4, 4);
        gc.fillRoundRect(cx + w - 4, cy + h - 36, 10, 20, 4, 4);

        // Wheel rims — front wheels turn with tilt
        gc.setFill(Color.web("#888888"));
        gc.fillOval(cx - 4, cy + 23, 6, 14);
        gc.fillOval(cx + w - 2, cy + 23, 6, 14);
        gc.fillOval(cx - 4, cy + h - 33, 6, 14);
        gc.fillOval(cx + w - 2, cy + h - 33, 6, 14);

        gc.restore();
    }

    public boolean collidesWith(EnemyCar enemy) {
        double margin = 12;
        return x + margin < enemy.x + enemy.width - margin
            && x + width - margin > enemy.x + margin
            && y + margin < enemy.y + enemy.height - margin
            && y + height - margin > enemy.y + margin;
    }
}