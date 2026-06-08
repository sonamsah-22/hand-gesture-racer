package com.racer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Road {
    private final double width, height;
    private double offset = 0;
    private double speed = 5;
    private double bgOffset = 0;

    public static final double KERB_W = 72;
    public static final int LANES = 5;

    // Static scenery objects (trees, bushes, grass patches)
    private static class SceneryObject {
        double x, y;
        int type; // 0=tree, 1=bush, 2=tallTree, 3=grassPatch
        double scale;
        String side; // "left" or "right"

        SceneryObject(double x, double y, int type, double scale, String side) {
            this.x = x; this.y = y;
            this.type = type; this.scale = scale;
            this.side = side;
        }
    }

    private List<SceneryObject> scenery = new ArrayList<>();
    private Random rand = new Random(42);

    public Road(double width, double height) {
        this.width = width;
        this.height = height;
        generateScenery();
    }

    private void generateScenery() {
        // Pre-generate scenery objects spread across 3x the screen height
        // so they loop seamlessly
        double totalHeight = height * 3;
        for (double y = -totalHeight; y < totalHeight; y += 60) {
            // Left side
            if (rand.nextDouble() > 0.4) {
                double x = rand.nextDouble() * (KERB_W - 18) + 4;
                int type = rand.nextInt(4);
                double scale = 0.7 + rand.nextDouble() * 0.6;
                scenery.add(new SceneryObject(x, y, type, scale, "left"));
            }
            // Right side
            if (rand.nextDouble() > 0.4) {
                double x = width - KERB_W + rand.nextDouble() * (KERB_W - 18) + 4;
                int type = rand.nextInt(4);
                double scale = 0.7 + rand.nextDouble() * 0.6;
                scenery.add(new SceneryObject(x, y, type, scale, "right"));
            }
        }
    }

    public void setSpeed(double s) { this.speed = s; }

    public void update() {
        offset = (offset + speed) % 100;
        bgOffset = (bgOffset + speed) % (height * 3);

        // Move scenery objects down
        for (SceneryObject obj : scenery) {
            obj.y += speed;
        }
        // Wrap objects that go off screen back to top
        for (SceneryObject obj : scenery) {
            if (obj.y > height + 60) {
                obj.y -= height * 3 + 120;
            }
        }
    }

    public void draw(GraphicsContext gc) {
        // Night sky
        gc.setFill(Color.web("#0a0a1a"));
        gc.fillRect(0, 0, width, height);

        // Stars
        gc.setFill(Color.web("#ffffff", 0.5));
        long seed = 42;
        for (int i = 0; i < 80; i++) {
            seed = (seed * 1103515245 + 12345) & 0x7fffffff;
            double sx = seed % (int) width;
            seed = (seed * 1103515245 + 12345) & 0x7fffffff;
            double sy = seed % (int) (height * 0.38);
            double brightness = 0.3 + (seed % 10) * 0.07;
            gc.setFill(Color.web("#ffffff", brightness));
            gc.fillOval(sx, sy, 1.5, 1.5);
        }

        // City skyline
        gc.setFill(Color.web("#1a1a2e"));
        double[] bx = {0,60,60,120,120,160,160,200,200,250,250,300,300,340,340,380,380,420,420,460,460,500,500,540,540,580,580,620,620,width,width,0};
        double[] by = {height*0.45,height*0.45,height*0.32,height*0.32,height*0.38,height*0.38,height*0.28,height*0.28,height*0.35,height*0.35,height*0.25,height*0.25,height*0.33,height*0.33,height*0.27,height*0.27,height*0.36,height*0.36,height*0.30,height*0.30,height*0.40,height*0.40,height*0.29,height*0.29,height*0.37,height*0.37,height*0.26,height*0.26,height*0.42,height*0.42,height,height};
        gc.fillPolygon(bx, by, bx.length);

        // Grass base — left and right shoulders
        gc.setFill(Color.web("#1a3a1a"));
        gc.fillRect(0, 0, KERB_W, height);
        gc.fillRect(width - KERB_W, 0, KERB_W, height);

        // Grass texture stripes (darker patches)
        gc.setFill(Color.web("#163016", 0.5));
        for (int i = 0; i < height; i += 18) {
            double stripeOff = (bgOffset * 0.4 + i * 3.7) % height;
            gc.fillRect(0, stripeOff, KERB_W, 9);
            gc.fillRect(width - KERB_W, stripeOff, KERB_W, 9);
        }

        // Draw scenery objects (behind road edge glow)
        for (SceneryObject obj : scenery) {
            if (obj.y > -80 && obj.y < height + 80) {
                drawSceneryObject(gc, obj);
            }
        }

        // Road surface
        gc.setFill(Color.web("#1c1c1c"));
        gc.fillRect(KERB_W, 0, width - KERB_W * 2, height);

        // Road surface texture
        gc.setFill(Color.web("#1f1f1f"));
        for (int i = 0; i < height; i += 8) {
            gc.fillRect(KERB_W, i, width - KERB_W * 2, 4);
        }

        // Road edge — natural dirt/gravel strip
        gc.setFill(Color.web("#3a3020", 0.8));
        gc.fillRect(KERB_W - 6, 0, 6, height);
        gc.fillRect(width - KERB_W, 0, 6, height);

        // Lane dividers
        double laneW = (width - KERB_W * 2) / (double) LANES;
        for (int lane = 1; lane < LANES; lane++) {
            double lx = KERB_W + lane * laneW;
            boolean isCenter = lane == LANES / 2;
            gc.setStroke(isCenter ? Color.web("#ffff00", 0.85) : Color.web("#ffffff", 0.45));
            gc.setLineWidth(isCenter ? 3 : 1.5);
            gc.setLineDashes(60, 40);
            gc.setLineDashOffset(-offset);
            gc.strokeLine(lx, 0, lx, height);
        }
        gc.setLineDashes(0);

        // Road edge glow (subtle orange)
        gc.setStroke(Color.web("#ff8800", 0.25));
        gc.setLineWidth(2);
        gc.strokeLine(KERB_W, 0, KERB_W, height);
        gc.strokeLine(width - KERB_W, 0, width - KERB_W, height);

        // Speed motion lines
        gc.setStroke(Color.web("#ffffff", 0.03));
        gc.setLineWidth(1);
        gc.setLineDashes(0);
        for (int i = 0; i < 10; i++) {
            double lx = KERB_W + 20 + i * ((width - KERB_W * 2 - 40) / 10.0);
            double lineOff = (bgOffset * 1.5 + i * 53) % height;
            gc.strokeLine(lx, lineOff, lx, lineOff + 50);
        }
    }

    private void drawSceneryObject(GraphicsContext gc, SceneryObject obj) {
        double x = obj.x, y = obj.y, s = obj.scale;

        switch (obj.type) {
            case 0 -> drawTree(gc, x, y, s);
            case 1 -> drawBush(gc, x, y, s);
            case 2 -> drawTallTree(gc, x, y, s);
            case 3 -> drawGrassPatch(gc, x, y, s);
        }
    }

    private void drawTree(GraphicsContext gc, double x, double y, double s) {
        // Trunk
        gc.setFill(Color.web("#5D4037"));
        gc.fillRoundRect(x - 4 * s, y - 6 * s, 8 * s, 20 * s, 3, 3);

        // Foliage layers (pine style)
        gc.setFill(Color.web("#2E7D32"));
        gc.fillPolygon(
            new double[]{x, x - 16*s, x + 16*s},
            new double[]{y - 36*s, y + 2*s, y + 2*s}, 3);

        gc.setFill(Color.web("#388E3C"));
        gc.fillPolygon(
            new double[]{x, x - 13*s, x + 13*s},
            new double[]{y - 48*s, y - 16*s, y - 16*s}, 3);

        gc.setFill(Color.web("#43A047"));
        gc.fillPolygon(
            new double[]{x, x - 10*s, x + 10*s},
            new double[]{y - 58*s, y - 32*s, y - 32*s}, 3);

        // Highlight
        gc.setFill(Color.web("#66BB6A", 0.3));
        gc.fillPolygon(
            new double[]{x, x - 5*s, x + 2*s},
            new double[]{y - 58*s, y - 32*s, y - 32*s}, 3);
    }

    private void drawTallTree(GraphicsContext gc, double x, double y, double s) {
        // Trunk
        gc.setFill(Color.web("#4E342E"));
        gc.fillRoundRect(x - 3*s, y - 4*s, 6*s, 28*s, 3, 3);

        // Round canopy (deciduous)
        gc.setFill(Color.web("#1B5E20"));
        gc.fillOval(x - 18*s, y - 52*s, 36*s, 34*s);

        gc.setFill(Color.web("#2E7D32"));
        gc.fillOval(x - 15*s, y - 56*s, 30*s, 30*s);

        gc.setFill(Color.web("#388E3C", 0.7));
        gc.fillOval(x - 10*s, y - 58*s, 18*s, 18*s);

        // Highlight
        gc.setFill(Color.web("#A5D6A7", 0.2));
        gc.fillOval(x - 8*s, y - 56*s, 10*s, 8*s);
    }

    private void drawBush(GraphicsContext gc, double x, double y, double s) {
        gc.setFill(Color.web("#1B5E20"));
        gc.fillOval(x - 14*s, y - 14*s, 28*s, 16*s);

        gc.setFill(Color.web("#2E7D32"));
        gc.fillOval(x - 10*s, y - 18*s, 20*s, 16*s);

        gc.setFill(Color.web("#388E3C"));
        gc.fillOval(x - 6*s, y - 20*s, 12*s, 12*s);

        // Small flowers randomly
        gc.setFill(Color.web("#FFEE58", 0.6));
        gc.fillOval(x - 4*s, y - 12*s, 4*s, 4*s);
        gc.fillOval(x + 2*s, y - 16*s, 3*s, 3*s);
    }

    private void drawGrassPatch(GraphicsContext gc, double x, double y, double s) {
        gc.setFill(Color.web("#33691E", 0.7));
        // Several grass blades
        for (int i = -3; i <= 3; i++) {
            double bx = x + i * 4 * s;
            gc.fillPolygon(
                new double[]{bx - 2*s, bx + 2*s, bx + s*i*0.3},
                new double[]{y, y, y - 14*s - Math.abs(i)*2*s}, 3);
        }
        gc.setFill(Color.web("#558B2F", 0.5));
        for (int i = -2; i <= 2; i++) {
            double bx = x + i * 5 * s + 2;
            gc.fillPolygon(
                new double[]{bx - 1.5*s, bx + 1.5*s, bx},
                new double[]{y, y, y - 10*s}, 3);
        }
    }

    public double getLaneWidth() {
        return (width - KERB_W * 2) / (double) LANES;
    }
}