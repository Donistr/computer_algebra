package ecdlp;

import ecdlp.algorithm.RoAlgorithmECDLPSolver;
import ecdlp.algorithm.RoAlgorithmSettings;
import ecdlp.asset.EllipticCurve;
import ecdlp.asset.Point;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Test {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(Objects.requireNonNull(dlp.Test.class.getClassLoader().getResourceAsStream("ro_alg_ecdlp_data.txt")));

        int i = 1;
        while (scanner.hasNextLine()) {
            System.out.println("//" + i);
            ++i;

            BigInteger[] input = Arrays.stream(scanner.nextLine().split(" "))
                    .map(BigInteger::new)
                    .toArray(BigInteger[]::new);

            BigInteger p = input[0];
            BigInteger a = input[1];
            BigInteger b = input[2];

            Point P = new Point(input[3], input[4]);
            Point Q = new Point(input[5], input[6]);

            BigInteger expected = input[7];

            EllipticCurve curve = new EllipticCurve(a, b, p);
            RoAlgorithmSettings settings = new RoAlgorithmSettings.RoAlgorithmSettingsBuilder(P, Q, curve).oneCycleIterationCount(p.sqrt().multiply(BigInteger.TWO)).build();

            BigInteger provided = new RoAlgorithmECDLPSolver(settings).solveECDLP().get().number();

            if (provided.compareTo(expected) != 0) {
                System.out.println(provided);
                System.out.println("Неверно: " + Arrays.toString(input));
            }
        }
    }

}
