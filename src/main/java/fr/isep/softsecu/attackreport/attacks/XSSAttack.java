package fr.isep.softsecu.attackreport.attacks;

import fr.isep.softsecu.attackreport.Attack;
import fr.isep.softsecu.attackreport.Report;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class XSSAttack implements Attack {

    private final String MALICIOUS_TEXT = "<div id='maliciousElement'></div>";
    private Map<String, Stack<String>> vulnerableInputsMap = new HashMap<>();

    @Override
    public void run(Report report, String ip, int port) {
        String urlString = "http://" + ip + ":" + port;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.get(urlString);

        Stack<String> linksToTest = getLinks(driver, port);
        Stack<String> testedLinks = new Stack<>();

        while(!linksToTest.isEmpty()) {
            String currentLink = linksToTest.pop();
            performXssAttack(driver, currentLink);
            testedLinks.add(currentLink);
        }

        // Write report
        if(vulnerableInputsMap.size() > 0) {
            for(String key: vulnerableInputsMap.keySet()) {
                report.append("Successful XSS attack on input of id: " + key);
                report.append("This input was present in : " + vulnerableInputsMap.get(key));
            }
        } else {
            report.append("No XSS attack were successful for this website.");
        }

        driver.quit();
    }

    @Override
    public String getAttackName() {
        return "XSS attack";
    }

    public void performXssAttack(WebDriver driver, String link) {
        driver.navigate().to(link);
        System.out.println("Performing XSS attack on page: " + link);

        List<WebElement> inputs = driver.findElements(By.tagName("input"));
        System.out.println("Found " + inputs.size() + " input(s)");

        // In case one of the input is a search bar
        List<WebElement> searchElements = driver.findElements(By.tagName("mat-search-bar"));
        searchElements.get(0).click();

        for(WebElement input : inputs) {
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(2000));
            String inputId = input.getAttribute("id");
            if(!vulnerableInputsMap.containsKey(inputId)) {
                if(input.isDisplayed() && input.isEnabled()) {
                    input.sendKeys(MALICIOUS_TEXT, Keys.ENTER);
                    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(2000));

                    if(hasMaliciousElement(driver)) {
                        System.out.println("Successful XSS attack on input: " + inputId);
                        Stack<String> vulnerableLinks = new Stack<>();
                        vulnerableLinks.add(link);
                        vulnerableInputsMap.put(inputId, vulnerableLinks);
                    }
                }
            } else {
                vulnerableInputsMap.get(inputId).add(link);
                System.out.println("Successful XSS attack on input: " + inputId);
            }
        }
    }

    private boolean hasMaliciousElement(WebDriver driver) {
        List<WebElement> maliciousElements = driver.findElements(By.id("maliciousElement"));
        if(maliciousElements.size()>0) {
            return true;
        } else {
            return false;
        }
    }

    public Stack<String> getLinks(WebDriver driver, int port) {
        Stack<String> links = new Stack<>();
        List<WebElement> webElementLinks = driver.findElements(By.cssSelector("a[href]"));

        for(WebElement webElementLink: webElementLinks) {
            String link = webElementLink.getAttribute("href");
            if(link.contains(String.valueOf(port)) && !link.contains("github")) {
                links.add(link);
            }
        }

        return links;
    }
}
