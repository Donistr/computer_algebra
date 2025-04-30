package ecdlp.util;

import ecdlp.algorithm.RoAlgorithmSettings;
import ecdlp.asset.EllipticCurve;
import ecdlp.asset.Point;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;

public class SageProvider {

    private static final BufferedReader READER;

    private static final BufferedWriter WRITER;

    static {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "wsl", "-d", "Ubuntu", "--", "bash"
            );
            pb.redirectErrorStream(true);
            Process process;
            process = pb.start();

            READER = new BufferedReader(new InputStreamReader(process.getInputStream()));
            WRITER = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            WRITER.write("source /mnt/d/miniforge3/etc/profile.d/conda.sh\n");
            WRITER.write("conda activate sage\n");
            WRITER.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BigInteger findEllipticCurveOrder(EllipticCurve curve) throws IOException {
        return new BigInteger(executeCommand("'p=" + curve.getP() + "; a=" + curve.getA() + "; b=" + curve.getB() + "; E=EllipticCurve(GF(p), [a, b]); print(E.order())'"));
    }

    public static BigInteger findPointOrder(EllipticCurve curve, Point p) throws IOException {
        return new BigInteger(executeCommand("'p=" + curve.getP() + "; a=" + curve.getA() + "; b=" + curve.getB() + "; E=EllipticCurve(GF(p), [a, b]); P = E" + p + "; print(P.order())'"));
    }

    public static BigInteger solveECDLP(RoAlgorithmSettings settings) throws IOException {
        EllipticCurve curve = settings.getCurve();

        return new BigInteger(executeCommand("'p=" + curve.getP() + "; a=" + curve.getA() + "; b=" + curve.getB() + "; E=EllipticCurve(GF(p), [a, b]); P = E" + settings.getP() + "; Q = E" + settings.getQ() + "; print(Q.log(P))'"));
    }

    private static String executeCommand(String sageCode) throws IOException {
        WRITER.write("sage -c " + sageCode + "\n");
        WRITER.flush();

        return READER.readLine();

        /*String result = READER.readLine();

        while (READER.ready()) {
            READER.read();
        }*/

        /*char[] buff = new char[2048];
        READER.read(buff);*/

        /*while (READER.ready()) {
            int ch = READER.read();

            if (ch == -1 || ch == '\n') {
                break;
            }
            stringBuilder.append((char) ch);
        }*/

        //return result;
    }

}
