package ecdlp.algorithm;

import ecdlp.asset.EllipticCurve;
import ecdlp.asset.Point;
import ecdlp.asset.StepResult;

import java.math.BigInteger;

public class RoAlgorithmSettings {

    private final Point P;

    private final Point Q;

    private final EllipticCurve curve;

    private final StepResult startX;

    private final StepResult startY;

    private final boolean shouldPrint;

    private final BigInteger oneCycleIterationCount;

    private BigInteger retryCount;

    private RoAlgorithmSettings(RoAlgorithmSettingsBuilder builder) {
        P = builder.P;
        Q = builder.Q;
        curve = builder.curve;
        startX = builder.startX;
        startY = builder.startY;
        shouldPrint = builder.shouldPrint;
        oneCycleIterationCount = builder.oneCycleIterationCount;
        retryCount = builder.retryCount;
    }

    public Point getP() {
        return P;
    }

    public Point getQ() {
        return Q;
    }

    public EllipticCurve getCurve() {
        return curve;
    }

    public StepResult getStartX() {
        return startX;
    }

    public StepResult getStartY() {
        return startY;
    }

    public BigInteger getOneCycleIterationCount() {
        return oneCycleIterationCount;
    }

    public boolean shouldPrint() {
        return shouldPrint;
    }

    public boolean shouldRetry() {
        if (retryCount == null) {
            return true;
        }

        retryCount = retryCount.subtract(BigInteger.ONE);
        return retryCount.compareTo(BigInteger.ZERO) >= 0;
    }

    public static class RoAlgorithmSettingsBuilder {
        private final Point P;

        private final Point Q;

        private final EllipticCurve curve;

        private StepResult startX = null;

        private StepResult startY = null;

        private boolean shouldPrint = false;

        private BigInteger oneCycleIterationCount = null;

        private BigInteger retryCount = null;

        public RoAlgorithmSettingsBuilder(Point P, Point Q, EllipticCurve curve) {
            this.P = P;
            this.Q = Q;
            this.curve = curve;
        }

        public RoAlgorithmSettingsBuilder startPoint(StepResult startX, StepResult startY) {
            this.startX = startX;
            this.startY = startY;
            return this;
        }

        public RoAlgorithmSettingsBuilder shouldPrint() {
            this.shouldPrint = true;
            return this;
        }

        public RoAlgorithmSettingsBuilder oneCycleIterationCount(BigInteger iterationCount) {
            if (iterationCount.compareTo(BigInteger.ZERO) <= 0) {
                throw new IllegalArgumentException("Количество итераций должно быть >= 0");
            }

            this.oneCycleIterationCount = iterationCount;
            return this;
        }

        public RoAlgorithmSettingsBuilder retryCount(BigInteger retryCount) {
            if (retryCount.compareTo(BigInteger.ZERO) <= 0) {
                throw new IllegalArgumentException("Количество попыток должно быть >= 0");
            }

            this.retryCount = retryCount;
            return this;
        }

        public RoAlgorithmSettings build() {
            return new RoAlgorithmSettings(this);
        }
    }

}
