package com.racer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Road {
    private final double width, height;
    private double offset = 0;
    private double speed = 5;
    private double bgOffset = 0;

    public static final double KERB_W = 60;
    public static final int LANES = 5;

    public Road(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void setSpeed(double s) { this.speed = s; }
    public void update() {
        offset = (offset + speed) % 100;
        bgOffset = (bgOffset + speed * 0.3) % height;
    }

    public void draw(GraphicsContext gc) {
        // Sky gradient background
        gc.setFill(Color.web("#0a0a1a"));
        gc.fillRect(0, 0, width, height);

        // Stars
        gc.setFill(Color.web("#ffffff", 0.6));
        long seed = 42;
        for (int i = 0; i < 80; i++) {
            seed = (seed * 1103515245 + 12345) & 0x7fffffff;
            double sx = (seed % (int)width);
            seed = (seed * 1103515245 + 12345) & 0x7fffffff;
            double sy = (seed % (int)(height * 0.35));
            gc.fillOval(sx, sy, 1.5, 1.5);
        }

        // City skyline silhouette
        gc.setFill(Color.web("#1a1a2e"));
        double[] bx = {0,60,60,120,120,160,160,200,200,250,250,300,300,340,340,380,380,420,420,460,460,500,500,540,540,580,580,620,620,width,width,0};
        double[] by = {height*0.45,height*0.45,height*0.32,height*0.32,height*0.38,height*0.38,height*0.28,height*0.28,height*0.35,height*0.35,height*0.25,height*0.25,height*0.33,height*0.33,height*0.27,height*0.27,height*0.36,height*0.36,height*0.30,height*0.30,height*0.40,height*0.40,height*0.29,height*0.29,height*0.37,height*0.37,height*0.26,height*0.26,height*0.42,height*0.42,height,height};
        gc.fillPolygon(bx, by, bx.length);

        // Road surface
        gc.setFill(Color.web("#1c1c1c"));
        gc.fillRect(KERB_W, 0, width - KERB_W * 2, height);

        // Road texture overlay
        gc.setFill(Color.web("#222222"));
        for (int i = 0; i < height; i += 6) {
            if (i % 12 == 0) gc.fillRect(KERB_W, i, width - KERB_W * 2, 3);
        }

        // Kerb — red/white stripes
        for (int i = 0; i < (int)(height / 30) + 2; i++) {
            double yy = i * 30 - (offset % 30);
            gc.setFill(i % 2 == 0 ? Color.web("#cc2200") : Color.web("#eeeeee"));
            gc.fillRect(0, yy, KERB_W, 30);
            gc.fillRect(width - KERB_W, yy, KERB_W, 30);
        }

        // Lane dividers — glowing dashes
        double laneW = (width - KERB_W * 2) / (double) LANES;
        for (int lane = 1; lane < LANES; lane++) {
            double lx = KERB_W + lane * laneW;
            boolean isCenter = lane == LANES / 2;
            gc.setStroke(isCenter ? Color.web("#ffff00", 0.9) : Color.web("#ffffff", 0.5));
            gc.setLineWidth(isCenter ? 3 : 1.5);
            gc.setLineDashes(60, 40);
            gc.setLineDashOffset(-offset);
            gc.strokeLine(lx, 0, lx, height);
        }
        gc.setLineDashes(0);

        // Road edge glow
        gc.setStroke(Color.web("#ff6600", 0.4));
        gc.setLineWidth(2);
        gc.strokeLine(KERB_W, 0, KERB_W, height);
        gc.strokeLine(width - KERB_W, 0, width - KERB_W, height);

        // Speed lines for motion feel
        gc.setStroke(Color.web("#ffffff", 0.04));
        gc.setLineWidth(1);
        gc.setLineDashes(0);
        for (int i = 0; i < 12; i++) {
            double lx = KERB_W + 20 + i * ((width - KERB_W * 2 - 40) / 12.0);
            double lineOff = (bgOffset * 2 + i * 37) % height;
            gc.strokeLine(lx, lineOff, lx, lineOff + 60);
        }
    }

    public double getLaneWidth() {
        return (width - KERB_W * 2) / (double) LANES;
    }
}