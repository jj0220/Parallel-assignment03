import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Assignment03Problem02 {
    private static final int NUM_SENSORS = 8;
    private static final int NUM_READINGS_PER_HOUR = 60;
    private static final int MIN_TEMP = -100;
    private static final int MAX_TEMP = 70;
    private static final int NUM_SIMULATIONS = 5;

    private static final List<List<Double>> temperatureReadings = new ArrayList<>();
    private static final Lock lock = new ReentrantLock();

    public static void main(String[] args) {
        // Initialize temperature readings for each sensor
        for (int i = 0; i < NUM_SENSORS; i++) {
            temperatureReadings.add(new ArrayList<>());
        }

        // Create and start sensor threads
        for (int i = 0; i < NUM_SENSORS; i++) {
            Thread sensorThread = new Thread(new TemperatureSensor(i));
            sensorThread.start();
        }

        // Start hourly report thread
        Thread reportThread = new Thread(new HourlyReportGenerator());
        reportThread.start();
    }

    static class TemperatureSensor implements Runnable {
        private final int sensorId;

        public TemperatureSensor(int sensorId) {
            this.sensorId = sensorId;
        }

        @Override
        public void run() {
            Random random = new Random();
            for (int i = 0; i < NUM_SIMULATIONS; i++) {
                double temperature = MIN_TEMP + (MAX_TEMP - MIN_TEMP) * random.nextDouble();

                try {
                    Thread.sleep(6); // Read temperature every 6 milliseconds
                    lock.lock();
                    temperatureReadings.get(sensorId).add(temperature);
                } 
                catch (InterruptedException e) {
                    e.printStackTrace();
                } 
                finally {
                    lock.unlock();
                }
            }
        }
    }

    static class HourlyReportGenerator implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < NUM_SIMULATIONS; i++) {
                try {
                    Thread.sleep(600); // Wait for 600 miliseconds

                    lock.lock();

                    List<Double> hourlyReadings = new ArrayList<>();

                    for (List<Double> sensorReadings : temperatureReadings) {
                        hourlyReadings.addAll(sensorReadings.subList(Math.max(0, sensorReadings.size() - NUM_READINGS_PER_HOUR), sensorReadings.size()));
                    }

                    Collections.sort(hourlyReadings);
                    List<Double> highestTemps = hourlyReadings.subList(hourlyReadings.size() - 5, hourlyReadings.size());
                    List<Double> lowestTemps = hourlyReadings.subList(0, 5);

                    // Find interval with largest temperature difference
                    double maxDiff = Double.MIN_VALUE;
                    int maxDiffIndex = -1;

                    for (int j = 0; j < hourlyReadings.size() - 10; j++) {
                        double diff = hourlyReadings.get(j + 9) - hourlyReadings.get(j);
                        if (diff > maxDiff) {
                            maxDiff = diff;
                            maxDiffIndex = j;
                        }
                    }

                    List<Double> maxDiffInterval = hourlyReadings.subList(maxDiffIndex, maxDiffIndex + 10);

                    // Print hourly report
                    System.out.println("Hourly Report:");
                    System.out.println("Top 5 highest temperatures: " + highestTemps);
                    System.out.println("Top 5 lowest temperatures: " + lowestTemps);
                    System.out.println("10-minute interval with largest temperature difference: " + maxDiffInterval);
                    System.out.println("--------------------");

                } 
                catch (InterruptedException e) {
                    e.printStackTrace();
                } 
                finally {
                    lock.unlock();
                }
            }
        }
    }
}