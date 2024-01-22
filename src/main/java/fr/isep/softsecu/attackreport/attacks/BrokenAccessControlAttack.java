package fr.isep.softsecu.attackreport.attacks;

import fr.isep.softsecu.attackreport.Attack;
import fr.isep.softsecu.attackreport.Report;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BrokenAccessControlAttack implements Attack {

    private static final int TIMEOUT = 2000;
    private static final String TARGET_URL = "http://127.0.0.1:3000/#/administration";

    @Override
    public void run(Report report, String ip, int port) {
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            WebDriver driver = new ChromeDriver(options);

            driver.get(TARGET_URL);

            checkURL(report, driver);

            driver.quit();
        } catch (Exception e) {
            System.out.println("BrokenAccessControlAttack failed on " + TARGET_URL);
            e.printStackTrace();
        }
    }
    @Override
    public String getAttackName() {
        return "BrokenAccessControl";
    }

    private void checkURL(Report report, WebDriver driver) {
        try {
            String result = verifyURL(TARGET_URL);
            System.out.println("Result for URL " + TARGET_URL + ": " + result);

            if ("There is a vulnerability.".equals(result)) {
                report.append("Vulnerability detected on URL: " + TARGET_URL);
            }
        } catch (IOException e) {
            System.err.println("An error occurred while checking URL " + TARGET_URL + " : " + e.getMessage());
        }
    }

    private static String verifyURL(String urlToCheck) throws IOException {
        URL url = new URL(urlToCheck);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            return "There is a vulnerability.";
        } else {
            return "No vulnerability detected. Response code: " + responseCode;
        }
    }
}