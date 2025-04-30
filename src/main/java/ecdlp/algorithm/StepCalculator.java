package ecdlp.algorithm;

import ecdlp.asset.EllipticCurve;
import ecdlp.asset.Point;
import ecdlp.asset.StepResult;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Random;

public class StepCalculator {

    private static final Random RANDOM = new Random();

    private final BigDecimal firstRangeBorderLeft;
    private final BigDecimal firstRangeBorderRight;

    private final BigDecimal secondRangeBorderLeft;
    private final BigDecimal secondRangeBorderRight;

    private final BigDecimal thirdRangeBorderLeft;
    private final BigDecimal thirdRangeBorderRight;

    private final Point P;
    private final Point Q;
    private final BigInteger N;
    private final EllipticCurve curve;

    public StepCalculator(Point P, Point Q, BigInteger N, EllipticCurve curve) {
        this.P = P;
        this.Q = Q;
        this.N = N;
        this.curve = curve;

        BigDecimal pDecimal = new BigDecimal(curve.getP());
        BigDecimal three = BigDecimal.valueOf(3);

        firstRangeBorderLeft = BigDecimal.ZERO;
        firstRangeBorderRight = pDecimal.divide(three, 3, RoundingMode.HALF_UP);

        secondRangeBorderLeft = firstRangeBorderRight;
        secondRangeBorderRight = secondRangeBorderLeft.multiply(BigDecimal.valueOf(2));

        thirdRangeBorderLeft = secondRangeBorderRight;
        thirdRangeBorderRight = pDecimal;
    }

    public StepResult generateRandomZeroStep() {
        BigInteger a = BigInteger.valueOf(Math.abs(RANDOM.nextLong())).mod(N);
        BigInteger b = BigInteger.valueOf(Math.abs(RANDOM.nextLong())).mod(N);

        Point X = curve.addPoints(curve.multiplyPoint(P, a), curve.multiplyPoint(Q, b));

        return new StepResult(X, a, b);
    }

    public StepResult calculate(StepResult previousStep) {
        Point previousX = previousStep.X();

        return new StepResult(
                calculateX(previousX),
                calculateA(previousX, previousStep.a()),
                calculateB(previousX, previousStep.b())
        );
    }

    private enum Range {
        FIRST,
        SECOND,
        THIRD
    }

    private Range calculateRange(Point X) {
        if (X.isInfinity()) {
            return Range.FIRST;
        }

        BigDecimal xDecimal = new BigDecimal(X.getX());

        if (firstRangeBorderLeft.compareTo(xDecimal) <= 0 && xDecimal.compareTo(firstRangeBorderRight) < 0) {
            return Range.FIRST;
        }

        if (secondRangeBorderLeft.compareTo(xDecimal) <= 0 && xDecimal.compareTo(secondRangeBorderRight) < 0) {
            return Range.SECOND;
        }

        if (thirdRangeBorderLeft.compareTo(xDecimal) <= 0 && xDecimal.compareTo(thirdRangeBorderRight) <= 0) {
            return Range.THIRD;
        }

        throw new RuntimeException("X-координата точки не попадает ни в один интервал");
    }

    private Point calculateX(Point X) {
        return switch (calculateRange(X)) {
            case FIRST -> curve.addPoints(X, P);
            case SECOND -> curve.addPoints(X, X);
            case THIRD -> curve.addPoints(X, Q);
        };
    }

    private BigInteger calculateA(Point X, BigInteger a) {
        return switch (calculateRange(X)) {
            case FIRST -> a.add(BigInteger.ONE).mod(N);
            case SECOND -> a.multiply(BigInteger.TWO).mod(N);
            case THIRD -> a;
        };
    }

    private BigInteger calculateB(Point X, BigInteger b) {
        return switch (calculateRange(X)) {
            case FIRST -> b;
            case SECOND -> b.multiply(BigInteger.TWO).mod(N);
            case THIRD -> b.add(BigInteger.ONE).mod(N);
        };
    }

}
