package com.example.tracking_ui;

public class Point {
    private double x;
    private double y;
    private double yaw;

    public Point(double x, double y, double yaw) {
        this.x = x;
        this.y = y;
        this.yaw = yaw;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getYaw() {
        return yaw;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }
}
