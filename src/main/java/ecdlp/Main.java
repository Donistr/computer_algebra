package ecdlp;

import ecdlp.algorithm.RoAlgorithmECDLPSolver;
import ecdlp.algorithm.RoAlgorithmSettings;
import ecdlp.asset.EllipticCurve;
import ecdlp.asset.NumberModule;
import ecdlp.asset.Point;
import ecdlp.asset.StepResult;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Scanner;

public class Main { // 31137731 18618646 4163347 15538160 25214033 25387036 27939652 14629137

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите p (модуль): ");
        BigInteger p = new BigInteger(scanner.nextLine());
        System.out.print("Введите a (коэффициент кривой): ");
        BigInteger a = new BigInteger(scanner.nextLine());
        System.out.print("Введите b (коэффициент кривой): ");
        BigInteger b = new BigInteger(scanner.nextLine());

        EllipticCurve curve = new EllipticCurve(a, b, p);

        System.out.print("\nВведите x_P (x-координата точки P): ");
        BigInteger xP = new BigInteger(scanner.nextLine());
        System.out.print("Введите y_P (y-координата точки P): ");
        BigInteger yP = new BigInteger(scanner.nextLine());
        Point P = new Point(xP, yP);
        if (!curve.isPointOnCurve(P)) {
            System.out.println("Ошибка: Точка P не лежит на кривой");
            return;
        }

        System.out.print("\nВведите x_Q (x-координата точки Q): ");
        BigInteger xQ = new BigInteger(scanner.nextLine());
        System.out.print("Введите y_Q (y-координата точки Q): ");
        BigInteger yQ = new BigInteger(scanner.nextLine());
        Point Q = new Point(xQ, yQ);
        if (!curve.isPointOnCurve(Q)) {
            System.out.println("Ошибка: Точка Q не лежит на кривой");
            return;
        }

        RoAlgorithmSettings.RoAlgorithmSettingsBuilder settingsBuilder = new RoAlgorithmSettings.RoAlgorithmSettingsBuilder(P, Q, curve);

        System.out.print("\nРаспечатывать подробную информацию в ходе решения? (да - y, нет - любой другой ввод): ");
        if (scanner.nextLine().equals("y")) {
            settingsBuilder.shouldPrint();
        }

        System.out.print("\nЗадать начальную точку? (да - y, нет - любой другой ввод): ");
        if (scanner.nextLine().equals("y")) {
            System.out.println("\nВвод X0:");
            System.out.println("Введите α: ");
            BigInteger a1 = new BigInteger(scanner.nextLine());
            System.out.println("Введите β: ");
            BigInteger b1 = new BigInteger(scanner.nextLine());

            System.out.println("\nВвод Y0:");
            System.out.println("Введите γ: ");
            BigInteger a2 = new BigInteger(scanner.nextLine());
            System.out.println("Введите δ: ");
            BigInteger b2 = new BigInteger(scanner.nextLine());

            Point X = curve.addPoints(curve.multiplyPoint(P, a1), curve.multiplyPoint(Q, b1));
            Point Y = curve.addPoints(curve.multiplyPoint(P, a2), curve.multiplyPoint(Q, b2));

            settingsBuilder.startPoint(
                    new StepResult(X, a1, b1),
                    new StepResult(Y, a2, b2)
            );
        } else {
            System.out.print("\nЗадать количество попыток решения ECDLP? (да - y, нет - любой другой ввод): ");
            if (scanner.nextLine().equals("y")) {
                System.out.println("Введите количество попыток решения ECDLP: ");
                settingsBuilder.retryCount(new BigInteger(scanner.nextLine()));
            }
        }

        System.out.print("\nЗадать количество итераций внутри одной попытки решения ECDLP? (да - y, нет - любой другой ввод): ");
        if (scanner.nextLine().equals("y")) {
            System.out.println("Введите количество итераций внутри одной попытки решения ECDLP: ");
            settingsBuilder.oneCycleIterationCount(new BigInteger(scanner.nextLine()));
            System.out.println("\n\n");
        } else {
            settingsBuilder.oneCycleIterationCount(p.sqrt().multiply(BigInteger.TWO));
        }

        RoAlgorithmECDLPSolver solver = new RoAlgorithmECDLPSolver(settingsBuilder.build());
        Optional<NumberModule> resultOptional = solver.solveECDLP();

        System.out.println("\n\n");
        if (resultOptional.isEmpty()) {
            System.out.println("Не удалось решить задачу ECDLP");
        } else {
            NumberModule result = resultOptional.get();
            System.out.println("Ответ: k = " + result.number() + " (mod " + result.module() + ")");
        }

        solver = new RoAlgorithmECDLPSolver(settingsBuilder.build());
        resultOptional = solver.solveECDLP();

        System.out.println("\n\n");
        if (resultOptional.isEmpty()) {
            System.out.println("Не удалось решить задачу ECDLP");
        } else {
            NumberModule result = resultOptional.get();
            System.out.println("Ответ: k = " + result.number() + " (mod " + result.module() + ")");
        }
    }

}
