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

public class FindLoginPageAttack implements Attack {

    private static final int TIMEOUT = 2000;
    private String loginUrlStr;

    @Override
    public void run(Report report, String ip, int port) {
        if(!PortScanAttack.isPortOpen(ip, port, TIMEOUT)){
            System.out.println("Port " + port + " is not open on " + ip);
            return;
        }

        String urlStr = "http://" + ip + ":" + port;
        System.out.println("Connecting to " + urlStr);
        report.append("Connecting to " + urlStr);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.get(urlStr);
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(TIMEOUT));

        findLoginUrl(driver, ip, port);

        if(loginUrlStr == null){
            report.append("Login page not found");
            return;
        }
        report.append("Login page found at " + loginUrlStr);
        driver.quit();
    }

    private void findLoginUrl(WebDriver driver, String ip, int port){
        List<WebElement> links  = driver.findElements(By.cssSelector("a[href]"));
        System.out.println("Found " + links.size() + " links");
        for(int i = 0; i < links.size(); ++i){
            String link = links.get(i).getAttribute("href");
            if(link.contains("login")){
                if(link.startsWith("http")){
                    loginUrlStr = link;
                } else {
                    if (link.startsWith("/")) {
                        link = link.substring(1);
                    }
                    loginUrlStr = "http://" + ip + ":" + port + "/" + link;
                }
            }
        }

        if(isPageLoadable(driver, loginUrlStr)){
            System.out.println("Login page found at " + loginUrlStr);
            return;
        }

        driver.findElement(By.cssSelector("button[label*='account']")).click();

        List<WebElement> routerlinks  = driver.findElements(By.cssSelector("[routerlink]"));
        System.out.println("Found " + routerlinks.size() + " routerlinks");
        for(int i = 0; i < routerlinks.size(); ++i){
            String link = routerlinks.get(i).getAttribute("routerlink");
            if(link.contains("login")){
                if(link.startsWith("http")){
                    loginUrlStr = link;
                } else {
                    if (link.startsWith("/")) {
                        link = link.substring(1);
                    }
                    loginUrlStr = "http://" + ip + ":" + port + "/" + link;
                }
            }
        }

        if(isPageLoadable(driver, loginUrlStr)){
            System.out.println("Login page found at " + loginUrlStr);
        } else {
            System.out.println("Login page not found");
        }
    }

    @Override
    public String getAttackName() {
        return "Find login page";
    }

    private boolean isPageLoadable(WebDriver driver, String urlStr){
        try {
            driver.get(urlStr);
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(2000));
            driver.navigate().back();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getLoginUrlStr() {
        return loginUrlStr;
    }
}
