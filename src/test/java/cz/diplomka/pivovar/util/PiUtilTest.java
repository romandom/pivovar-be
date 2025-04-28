package cz.diplomka.pivovar.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


class PiUtilTest {

    private final double kp = 3.4;
    private final double ki = 0.00095;
    private final double outputMin = 0;
    private final double outputMax = 100;
    private final PiUtil piUtil = new PiUtil(kp, ki, outputMin, outputMax);

    @Test
    void testComputePid() throws Exception {
        double setpoint = 70.0;
        long lastTime = 0;
        double integral = 0;
        int step = 0;

        File file = new File("src/test/resources/util/pivko-data.txt");
        List<Double> temperatures = loadTemperatures(file);

        File outputFile = new File("output_results.csv");
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("Cas [ms],Teplota [C],Vykon [%]");

            for (double input : temperatures) {
                Map<String, Number> result = piUtil.computePid(input, setpoint, lastTime, integral);

                double output = result.get("output").doubleValue();
                lastTime = result.get("lastTime").longValue();
                integral = result.get("integral").doubleValue();


                writer.printf("%d,%.2f,%.2f%n", step, input, output);
                step++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Results saved to output_results.csv");
    }

    private List<Double> loadTemperatures(File file) throws Exception {
        Scanner scanner = new Scanner(file);
        scanner.nextLine();
        List<Double> temperatures = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] columns = line.split(";");
            temperatures.add(Double.parseDouble(columns[2]));
        }
        scanner.close();
        return temperatures;
    }
}