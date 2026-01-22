package com.naviSafe.naviSafe.domain.MyRootPath.service;

public record Vector2D(double x, double y) {

    Vector2D normalize() {
        double len = Math.sqrt(x * x + y * y);
        return len == 0 ? this : new Vector2D(x / len, y / len);
    }

    Vector2D multiply(double k) {
        return new Vector2D(x * k, y * k);
    }

    double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }
}
