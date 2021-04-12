package com.example.tracking_ui;

public class Point {
    private int x;
    private int y;
    private int yaw;

    public Point(int x, int y, int yaw) {
        this.x = x;
        this.y = y;
        this.yaw = yaw;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getYaw() {
        return yaw;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }
}
