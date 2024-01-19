package fr.isep.softsecu.attackreport;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import fr.isep.softsecu.attackreport.attacks.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final int REACHABLE_TIMEOUT = 5000;

    private static final Attack[] ATTACKS = new Attack[] {
        new FindLoginPageAttack(),

        new SQLInjectionAttack(), //Require FindLoginPageAttack
        new XSSAttack(),
        new XXEAttack(),

        //Now attacks taking long time
        new PortScanAttack(),
        new DictionaryAttack(), //Require FindLoginPageAttack
        new DOSAttack(),
        new BruteForceAttack(), //Require FindLoginPageAttack
    };

    public static void main(String[] args) {
        printBanner();
        String addressStr = askAddress();

        Report report = new Report(addressStr);

        try {
            String ip = addressStr;
            int port = 80;
            if(addressStr.contains(":")){
                String[] split = addressStr.split(":");
                ip = InetAddress.getByName(split[0]).getHostAddress();
                port = Integer.parseInt(split[1]);
            }
            report.append("IP is " + ip);
            report.append("Port is " + port);

            System.out.println("Starting attacks on " + addressStr);
            for(int i = 0; i < ATTACKS.length; ++i){
                report.addSection(ATTACKS[i].getAttackName());
                System.out.println("Running attack " + (i + 1) + "/" + ATTACKS.length);

                List<Class<? extends Attack>> requiredPreviousAttacks = ATTACKS[i].getRequiredPreviousAttacks();
                if(requiredPreviousAttacks != null){
                    List<Attack> previousAttacks = new ArrayList<>();
                    for(int j = 0; j < i; ++j){
                        if(requiredPreviousAttacks.contains(ATTACKS[j].getClass())){
                            previousAttacks.add(ATTACKS[j]);
                        }
                    }

                    if(previousAttacks.size() != requiredPreviousAttacks.size()){
                        System.out.println("The attack " + ATTACKS[i].getClass().getSimpleName() + " requires the following previous attacks:");
                        for(Class<? extends Attack> requiredPreviousAttack : requiredPreviousAttacks){
                            System.out.println(" - " + requiredPreviousAttack.getSimpleName());
                        }
                        System.out.println("But only the following previous attacks were found:");
                        for(Attack previousAttack : previousAttacks){
                            System.out.println(" - " + previousAttack.getClass().getSimpleName());
                        }
                        System.out.println("Aborting.");
                        return;
                    }

                    ATTACKS[i].setPreviousAttacks(previousAttacks);
                }

                ATTACKS[i].run(report, ip, port);
            }
        } catch (Exception e) {
            System.out.println("An error occurred while running the attack.");
            e.printStackTrace();
        }
        report.print();
        System.out.println("All attacks finished.");
    }

    private static String askAddress() {
        String address;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.print("Enter the address of the target: ");
            address = scanner.nextLine();
        } while(!isAddressValidAndReachable(address.contains(":") ? address.split(":")[0] : address));
        return address;
    }

    private static boolean isAddressValidAndReachable(String address) {
        boolean valid = InetAddresses.isUriInetAddress(address) || InternetDomainName.isValid(address);
        if(!valid){
            System.out.println("The address you entered is not valid.");
            return false;
        }
        if(InternetDomainName.isValid(address)){
            address = InternetDomainName.from(address).toString();
        }
        try{
            boolean reachable = InetAddress.getByName(address).isReachable(REACHABLE_TIMEOUT);
            if(!reachable){
                System.out.println("The address you entered is not reachable.");
            }
            return reachable;
        } catch (Exception e){
            System.out.println("The address you entered is not reachable.");
            return false;
        }
    }

    private static void printBanner() {
        System.out.println("""
                     _   _   _             _      ____                       _  \s
                    / \\ | |_| |_ __ _  ___| | __ |  _ \\ ___ _ __   ___  _ __| |_\s
                   / _ \\| __| __/ _` |/ __| |/ / | |_) / _ \\ '_ \\ / _ \\| '__| __|
                  / ___ \\ |_| || (_| | (__|   <  |  _ <  __/ |_) | (_) | |  | |_\s
                 /_/   \\_\\__|\\__\\__,_|\\___|_|\\_\\ |_| \\_\\___| .__/ \\___/|_|   \\__|
                                                           |_|                  \s
                """);
    }

}
