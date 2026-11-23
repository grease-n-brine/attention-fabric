package greasenbrine.fabric.structures;

import java.util.Arrays;

public class Point {
    private final double[] coordinates;

    public Point(double[] coordinates) {
        if (coordinates == null || coordinates.length == 0) {
            throw new IllegalArgumentException("Coordinates cannot be null or empty");
        }
        this.coordinates = Arrays.copyOf(coordinates, coordinates.length);
    }

    public double getX() {
        return coordinates[0];
    }
    
    public double getY() {
        return coordinates[1];
    }

    public double getZ() {
        return coordinates[2];
    }

    public int getDimension() {
        return coordinates.length;
    }

    public double get(int index) {
        return coordinates[index];
    }

    public double getDistanceTo(Point otherPoint) {
        if (this.getDimension() != otherPoint.getDimension()) {
            throw new IllegalArgumentException("Points must have the same dimension");
        }

        double sum = 0;

        for (int i = 0; i < this.getDimension(); i++) {
            double diff = this.get(i) - otherPoint.get(i);
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    public double[] getCoordinates() {
        return Arrays.copyOf(coordinates, coordinates.length);
    }

    public double magnitudeSquared() {
        return coordinates[0] * coordinates[0]
             + coordinates[1] * coordinates[1]
             + coordinates[2] * coordinates[2];
    }

    public double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }

    public Point normalize() {
        double mag = magnitude();

        if (mag == 0) {
            throw new ArithmeticException("Cannot normalize a zero vector");
        }
        return new Point(new double[] {
            coordinates[0] / mag,
            coordinates[1] / mag,
            coordinates[2] / mag
        });
    }

    @Override 
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (!(obj instanceof Point)) {
            return false;
        }

        Point other = (Point) obj;

        return Arrays.equals(coordinates, other.coordinates);
    }

    @Override 
    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }

    @Override 
    public String toString() {
        return Arrays.toString(coordinates);
    }
}
