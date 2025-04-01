package cz.diplomka.pivovar.util;

import java.util.HashMap;
import java.util.Map;

public class PiUtil {

    private final double kp;
    private final double ki;

    private final double outputMin;
    private final double outputMax;

    public PiUtil(double kp, double ki, double outputMin, double outputMax) {
        this.kp = kp;
        this.ki = ki;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
    }

    public Map<String, Number> computePid(double input, double setpoint, long lastTime, double integral) {
        final long now = System.currentTimeMillis();

        if (lastTime == 0) lastTime = now;

        final double dt = (now - lastTime) / 1000.0;

        final double error = setpoint - input;
        final double tempIntegral = integral + error * dt;
        final double tempOutput = kp * error + ki * tempIntegral;

        if (tempOutput >= outputMin && tempOutput <= outputMax) {
            integral = tempIntegral;
        }

        double output = kp * error + ki * integral;
        output = Math.clamp(output, outputMin, outputMax);

        final Map<String, Number> computedMap = new HashMap<>();
        computedMap.put("output", output);
        computedMap.put("lastTime", now);
        computedMap.put("integral", integral);

        return computedMap;
    }
}
