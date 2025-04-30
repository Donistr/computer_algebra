package ecdlp.asset;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class EllipticCurve {

    private static final Random RANDOM = new Random();

    private final BigInteger a;

    private final BigInteger b;

    private final BigInteger p;

    public BigInteger getA() {
        return a;
    }

    public BigInteger getB() {
        return b;
    }

    public BigInteger getP() {
        return p;
    }

    public EllipticCurve(BigInteger a, BigInteger b, BigInteger p) {
        this.a = a;
        this.b = b;
        this.p = p;

        // 4a^3 + 27b^2 != 0 (mod p)
        BigInteger discriminant = a.pow(3).multiply(BigInteger.valueOf(4)).add(b.pow(2).multiply(BigInteger.valueOf(27))).mod(p);
        if (discriminant.equals(BigInteger.ZERO)) {
            throw new IllegalArgumentException("эллиптическая кривая - вырожденная");
        }
    }

    public boolean isPointOnCurve(Point point) {
        if (point.isInfinity()) {
            return true;
        }

        BigInteger x = point.getX();
        BigInteger y = point.getY();

        BigInteger left = y.pow(2).mod(p);
        BigInteger right = x.pow(3).add(a.multiply(x)).add(b).mod(p);

        return left.equals(right);
    }

    public Point addPoints(Point p1, Point p2) {
        if (p1.isInfinity()) {
            return p2;
        }
        if (p2.isInfinity()) {
            return p1;
        }

        BigInteger x1 = p1.getX(), y1 = p1.getY();
        BigInteger x2 = p2.getX(), y2 = p2.getY();

        if (x1.equals(x2) && y1.equals(y2.negate().mod(p))) {
            return Point.infinity();
        }

        BigInteger numerator;
        BigInteger denominator;
        if (p1.equals(p2)) { // lambda = (3x^2 + a) / (2y)
            numerator = x1.pow(2).multiply(BigInteger.valueOf(3)).add(a);
            denominator = y1.multiply(BigInteger.valueOf(2));
        } else { // lambda = (y2 - y1) / (x2 - x1)
            numerator = y2.subtract(y1);
            denominator = x2.subtract(x1);
        }
        BigInteger lambda = numerator.multiply(denominator.modInverse(p)).mod(p);

        BigInteger x3 = lambda.pow(2).subtract(x1).subtract(x2).mod(p); // x3 = lambda^2 - x1 - x2
        BigInteger y3 = lambda.multiply(x1.subtract(x3)).subtract(y1).mod(p); // y3 = lambda * (x1 - x3) - y1

        return new Point(x3, y3);
    }

    public Point multiplyPoint(Point point, BigInteger k) {
        if (point.isInfinity() || k.equals(BigInteger.ZERO)) {
            return Point.infinity();
        }

        Point result = Point.infinity();
        Point tmp = point;

        while (!k.equals(BigInteger.ZERO)) {
            if (k.testBit(0)) { // если текущий бит = 1, добавляем tmp к результату
                result = addPoints(result, tmp);
            }

            tmp = addPoints(tmp, tmp); // удваиваем tmp
            k = k.shiftRight(1); // отбрасываем пройденный бит
        }

        return result;
    }

    public Point getRandomPoint() {
        while (true) {
            BigInteger x = new BigInteger(p.bitLength(), RANDOM).mod(p);
            BigInteger ySquared = x.pow(3).add(a.multiply(x)).add(b).mod(p);

            BigInteger legendre = ySquared.modPow(p.subtract(BigInteger.ONE).divide(BigInteger.TWO), p);
            if (!legendre.equals(BigInteger.ONE)) {
                continue;
            }

            BigInteger y = tonelliShanks(ySquared, p);
            if (y == null) {
                continue;
            }

            Point point = new Point(x, y);
            if (!point.isInfinity() && isPointOnCurve(point)) {
                return point;
            }
        }
    }

    private static BigInteger tonelliShanks(BigInteger number, BigInteger p) {
        if (number.equals(BigInteger.ZERO)) {
            return BigInteger.ZERO;
        }

        BigInteger legendre = number.modPow(p.subtract(BigInteger.ONE).divide(BigInteger.TWO), p);
        if (!legendre.equals(BigInteger.ONE)) {
            return null;
        }

        // Разлагаем p-1 на Q * 2^S
        BigInteger Q = p.subtract(BigInteger.ONE);
        int S = 0;
        while (!Q.testBit(0)) {
            Q = Q.shiftRight(1);
            ++S;
        }

        if (S == 1) {
            return number.modPow(p.add(BigInteger.ONE).divide(BigInteger.valueOf(4)), p);
        }

        BigInteger z = BigInteger.TWO;
        while (z.modPow(p.subtract(BigInteger.ONE).divide(BigInteger.TWO), p).equals(BigInteger.ONE)) {
            z = z.add(BigInteger.ONE);
        }

        BigInteger c = z.modPow(Q, p);
        BigInteger R = number.modPow(Q.add(BigInteger.ONE).divide(BigInteger.TWO), p);
        BigInteger t = number.modPow(Q, p);
        int M = S;

        while (!t.equals(BigInteger.ONE)) {
            // Находим наименьшее i, такое что t^(2^i) ≡ 1 (mod p)
            BigInteger t2i = t;
            int i = 0;
            for (; i < M; ++i) {
                if (t2i.equals(BigInteger.ONE)) {
                    break;
                }
                t2i = t2i.multiply(t2i).mod(p);
            }

            BigInteger b = c.modPow(BigInteger.ONE.shiftLeft(M - i - 1), p); // b = c^(2^(M-i-1))
            R = R.multiply(b).mod(p);
            c = b.multiply(b).mod(p);
            t = t.multiply(c).mod(p);
            M = i;
        }

        return R;
    }



    public BigInteger findPointOrder(Point point) {
        /*try {
            return SageProvider.findPointOrder(this, point);
        } catch (Exception ignored) {

        }

        return null;*/

        if (point.isInfinity()) {
            return BigInteger.ONE;
        }

        if (!isPointOnCurve(point)) {
            throw new IllegalArgumentException("Точка не на кривой");
        }

        BigInteger upperBound = p.add(BigInteger.ONE).add(BigInteger.TWO.multiply(p.sqrt().add(BigInteger.ONE)));

        BigInteger m = upperBound.sqrt().add(BigInteger.ONE);

        Map<Point, BigInteger> babySteps = new HashMap<>();
        Point current = point;
        babySteps.put(current, BigInteger.ONE);

        for (BigInteger i = BigInteger.valueOf(2); i.compareTo(m) <= 0; i = i.add(BigInteger.ONE)) {
            current = addPoints(current, point);
            babySteps.put(current, i);
        }

        Point negPoint = new Point(point.getX(), point.getY().negate().mod(p));
        Point mTimesNegPoint = multiplyPoint(negPoint, m);
        current = Point.infinity();

        for (BigInteger i = BigInteger.ZERO; i.compareTo(m) <= 0; i = i.add(BigInteger.ONE)) {
            if (babySteps.containsKey(current)) {
                BigInteger j = babySteps.get(current);
                return i.multiply(m).add(j);
            }

            current = addPoints(current, mTimesNegPoint);
        }

        throw new RuntimeException("верхняя граница оказалась слишком мала");
    }

    public BigInteger findOrder() {
        BigInteger sqrtP = p.sqrt().add(BigInteger.ONE);
        BigInteger lowerBound = p.add(BigInteger.ONE).subtract(sqrtP.multiply(BigInteger.TWO));
        BigInteger upperBound = p.add(BigInteger.ONE).add(sqrtP.multiply(BigInteger.TWO));

        Set<BigInteger> possibleOrders = new HashSet<>();
        for (BigInteger n = lowerBound; n.compareTo(upperBound) <= 0; n = n.add(BigInteger.ONE)) {
            possibleOrders.add(n);
        }

        while (possibleOrders.size() > 1) {
            Point randomPoint = getRandomPoint();
            BigInteger pointOrder = findPointOrder(randomPoint);

            possibleOrders.removeIf(candidate -> !candidate.mod(pointOrder).equals(BigInteger.ZERO));
        }

        return possibleOrders.iterator().next();
    }

}
