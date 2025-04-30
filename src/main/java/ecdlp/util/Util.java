package ecdlp.util;

import ecdlp.asset.NumberModule;

import java.math.BigInteger;
import java.util.Optional;

public class Util {

    public static BigInteger gcd(BigInteger a, BigInteger b) {
        while (!b.equals(BigInteger.ZERO)) {
            BigInteger tmp = b;
            b = a.mod(b);
            a = tmp;
        }

        return a;
    }

    public static Optional<NumberModule> solveLinearCongruence(BigInteger number, BigInteger xMultiplier, BigInteger module) {
        while (xMultiplier.compareTo(BigInteger.ZERO) < 0) {
            xMultiplier = xMultiplier.add(module);
        }

        BigInteger gcd = gcd(xMultiplier, module);

        if (!number.mod(gcd).equals(BigInteger.ZERO)) {
            return Optional.empty();
        }

        number = number.divide(gcd);
        xMultiplier = xMultiplier.divide(gcd);
        module = module.divide(gcd);

        BigInteger inverse = modInverse(xMultiplier, module);

        BigInteger x = number.multiply(inverse).mod(module);

        while (x.compareTo(BigInteger.ZERO) < 0) {
            x = x.add(module);
        }

        return Optional.of(new NumberModule(x, module));
    }

    private static BigInteger modInverse(BigInteger number, BigInteger module) {
        if (module.equals(BigInteger.ONE)) {
            return BigInteger.ZERO;
        }

        BigInteger y = BigInteger.ZERO, x = BigInteger.ONE;
        BigInteger originalModule = module;

        while (number.compareTo(BigInteger.ONE) > 0) {
            BigInteger q = number.divide(module);
            BigInteger t = module;

            module = number.mod(module);
            number = t;
            t = y;

            y = x.subtract(q.multiply(y));
            x = t;
        }

        if (x.compareTo(BigInteger.ZERO) < 0) {
            x = x.add(originalModule);
        }

        if (!gcd(number, module).equals(BigInteger.ONE)) {
            throw new ArithmeticException("Модульное обратное не существует");
        }

        return x;
    }

}
