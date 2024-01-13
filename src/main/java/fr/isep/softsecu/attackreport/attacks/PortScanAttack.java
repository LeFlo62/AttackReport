package fr.isep.softsecu.attackreport.attacks;

import fr.isep.softsecu.attackreport.Attack;
import fr.isep.softsecu.attackreport.Report;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PortScanAttack implements Attack {

    private final List<Integer> openedPorts = new ArrayList<>();
    @Override
    public void run(Report report, String ip, int port) {
        String addressStr = ip + ":" + port;
        System.out.println("Port scan attack on " + addressStr);

        try{
            ExecutorService es = Executors.newFixedThreadPool(20);
            int timeout = 200;

            List<Future<PortScanResult>> futures = new ArrayList<>();
            for (int scannedPort = 1; scannedPort <= 65535; scannedPort++) {
                futures.add(portIsOpen(es, ip, scannedPort, timeout));
            }

            es.shutdown();

            for (Future<PortScanResult> f : futures) {
                if (f.get().isOpen()) {
                    System.out.println("Port " + f.get().port() + " is open");
                    openedPorts.add(f.get().port());
                }
            }

            System.out.println("Port scan attack on " + addressStr + " finished");
            System.out.println("Opened ports: " + Arrays.toString(openedPorts.toArray()));
            report.append("Opened ports: " + Arrays.toString(openedPorts.toArray()));
        } catch (Exception e){
            System.out.println("Error while running port scan attack on " + addressStr);
            e.printStackTrace();
        }
    }

    @Override
    public String getAttackName() {
        return "Port scan";
    }

    private static Future<PortScanResult> portIsOpen(final ExecutorService es, final String ip, final int port, final int timeout) {
        return es.submit(() -> new PortScanResult(port, isPortOpen(ip, port, timeout)));
    }

    public static boolean isPortOpen(final String ip, final int port, final int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public List<Integer> getOpenedPorts() {
        return openedPorts;
    }

    public record PortScanResult(int port, boolean isOpen) {}
}
