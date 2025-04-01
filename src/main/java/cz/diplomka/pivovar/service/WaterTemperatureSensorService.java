package cz.diplomka.pivovar.service;

public class WaterTemperatureSensorService {

    public double getTemperatureFor(String sensorAddress) {
        W1Master w1Master = new W1Master();

        // Iterujeme cez všetky teplotné senzory
        for (TemperatureSensor device : w1Master.getDevices(TemperatureSensor.class)) {
            // Ak meno obsahuje zadanú adresu, vrátime teplotu
            if (device.getName().contains(sensorAddress)) {
                return device.getTemperature(TemperatureScale.CELSIUS);
            }
        }
        throw new RuntimeException("Senzor s adresou " + sensorAddress + " nebol nájdený.");
    }
}
