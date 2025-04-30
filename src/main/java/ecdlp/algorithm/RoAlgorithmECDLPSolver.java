package ecdlp.algorithm;

import ecdlp.asset.CycleResult;
import ecdlp.asset.EllipticCurve;
import ecdlp.asset.NumberModule;
import ecdlp.asset.Point;
import ecdlp.asset.StepResult;

import java.math.BigInteger;
import java.util.Optional;

import static ecdlp.util.Util.solveLinearCongruence;

public class RoAlgorithmECDLPSolver {

    private final RoAlgorithmSettings settings;

    private final Point P;

    private final Point Q;

    private final BigInteger N;

    private final EllipticCurve curve;

    private final StepCalculator stepCalculator;

    public RoAlgorithmECDLPSolver(RoAlgorithmSettings settings) {
        this.settings = settings;
        this.P = settings.getP();
        this.Q = settings.getQ();
        this.curve = settings.getCurve();

        if (!curve.isPointOnCurve(P) && !curve.isPointOnCurve(Q)) {
            throw new IllegalArgumentException("точки P и Q не на кривой");
        }
        if (!curve.isPointOnCurve(P)) {
            throw new IllegalArgumentException("точка P не на кривой");
        }
        if (!curve.isPointOnCurve(Q)) {
            throw new IllegalArgumentException("точка P не на кривой");
        }

        N = curve.findPointOrder(P);
        this.stepCalculator = new StepCalculator(P, Q, N, curve);
    }

    public Optional<NumberModule> solveECDLP() {
        StepResult startX = settings.getStartX();
        StepResult startY = settings.getStartY();
        if (startX != null && startY != null) {
            return roAlgorithmTryToSolveECDLP(startX, startY);
        }

        while (settings.shouldRetry()) {
            Optional<NumberModule> result = roAlgorithmTryToSolveECDLP(
                    stepCalculator.generateRandomZeroStep(),
                    stepCalculator.generateRandomZeroStep()
            );
            if (result.isEmpty()) {
                continue;
            }

            return result;
        }

        return Optional.empty();
    }

    private Optional<NumberModule> roAlgorithmTryToSolveECDLP(StepResult stepX, StepResult stepY) {
        Optional<CycleResult> resultOptional = roAlgorithmCycle(stepX, stepY);
        if (resultOptional.isEmpty()) {
            if (settings.shouldPrint()) {
                System.out.println("\n");
            }

            return Optional.empty();
        }

        CycleResult result = resultOptional.get();
        stepX = result.stepX();
        stepY = result.stepY();

        Optional<NumberModule> congruenceResultOptional = solveLinearCongruence(
                stepX.a().subtract(stepY.a()),
                stepY.b().subtract(stepX.b()),
                N
        );
        if (congruenceResultOptional.isEmpty()) {
            return Optional.empty();
        }

        NumberModule congruenceResult = congruenceResultOptional.get();
        BigInteger answer = congruenceResult.number();
        BigInteger module = congruenceResult.module();

        if (settings.shouldPrint()) {
            System.out.println();
            System.out.println(P + " * k = " + Q);
            System.out.println("(" + stepX.a() + " - " + stepY.a() + ") * k = " + "(" + stepY.b() + " - " + stepX.b() + ")");
            System.out.println("k = " + answer + " (mod " + module + ")");
        }

        while (answer.compareTo(N) < 0) {
            Point computedQ = curve.multiplyPoint(P, answer);
            if (computedQ.equals(Q)) {
                if (settings.shouldPrint()) {
                    System.out.println("Ответ: k = " + answer + " (mod " + N + ")");
                }

                return Optional.of(new NumberModule(answer, N));
            }

            answer = answer.add(module);
        }

        throw new RuntimeException("ECDLP не имеет решения");
    }

    private Optional<CycleResult> roAlgorithmCycle(StepResult stepX, StepResult stepY) {
        printStep(BigInteger.ZERO, stepX, stepY);

        BigInteger iterationCount = settings.getOneCycleIterationCount();
        BigInteger i = BigInteger.ONE;
        while (true) {
            if (iterationCount != null && i.compareTo(iterationCount) > 0) {
                return Optional.empty();
            }

            stepX = stepCalculator.calculate(stepX);
            stepY = stepCalculator.calculate(stepCalculator.calculate(stepY));

            printStep(i, stepX, stepY);

            if (stepX.X().equals(stepY.X())) {
                if (stepX.a().equals(stepY.a()) || stepX.b().equals(stepY.b())) {
                    return Optional.empty();
                }
                return Optional.of(new CycleResult(stepX, stepY));
            }

            i = i.add(BigInteger.ONE);
        }
    }

    private void printStep(BigInteger i, StepResult stepX, StepResult stepY) {
        if (settings.shouldPrint()) {
            System.out.println("i = " + i + " | X = " + stepX.X() + " | Y = " + stepY.X() +
                    " | α = " + stepX.a() + " | β = " + stepX.b() +
                    " | γ = " + stepY.a() + " | δ = " + stepY.b());
        }
    }

}
