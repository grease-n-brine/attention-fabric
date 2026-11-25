package greasenbrine.fabric.visualization;

import greasenbrine.fabric.structures.Point;

/**
 * Orbiting perspective camera. The scene is rotated about the vertical axis by
 * {@code yaw}, tilted about the horizontal axis by {@code pitch}, pushed back by
 * {@code distance}, and projected with focal length {@code focal} onto an image
 * of the given size.
 */
public record Camera(double yaw, double pitch, double distance, double focal, int width, int height) {

    public Camera {
        if (distance <= 0.0 || focal <= 0.0) {
            throw new IllegalArgumentException("distance and focal must be positive");
        }
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("image size must be positive");
        }
    }

    /** Screen coordinates and view-space depth of a projected point. */
    public record Projected(double x, double y, double depth) {
    }

    public Projected project(Point p) {
        double cy = Math.cos(yaw);
        double sy = Math.sin(yaw);
        double x = p.getX() * cy + p.getZ() * sy;
        double z = -p.getX() * sy + p.getZ() * cy;
        double y = p.getY();

        double cp = Math.cos(pitch);
        double sp = Math.sin(pitch);
        double y2 = y * cp - z * sp;
        double z2 = y * sp + z * cp;

        double depth = z2 + distance;
        double scale = focal / depth;
        return new Projected(width / 2.0 + x * scale, height / 2.0 - y2 * scale, depth);
    }

    /** Apparent scale factor at the given view depth. */
    public double scaleAt(double depth) {
        return focal / depth;
    }

    public Camera withYaw(double value) {
        return new Camera(value, pitch, distance, focal, width, height);
    }
}
