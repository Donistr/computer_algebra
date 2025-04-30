package ecdlp.asset;

import java.math.BigInteger;
import java.util.Objects;

public class Point {

    private final BigInteger x;

    private final BigInteger y;

    private final boolean isInfinity;

    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
        this.isInfinity = false;
    }

    private Point() {
        this.isInfinity = true;
        this.x = null;
        this.y = null;
    }

    public static Point infinity() {
        return new Point();
    }

    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    public boolean isInfinity() {
        return isInfinity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Point point)) {
            return false;
        }

        if (isInfinity && point.isInfinity) {
            return true;
        }

        if (isInfinity || point.isInfinity) {
            return false;
        }

        return Objects.equals(x, point.x) && Objects.equals(y, point.y);
    }

    @Override
    public String toString() {
        if (isInfinity) {
            return "(INF)";
        }

        return "(" + x + ", " + y + ")";
    }

    @Override
    public int hashCode() {
        if (isInfinity) {
            return 0;
        }
        return Objects.hash(x, y);
    }

}
