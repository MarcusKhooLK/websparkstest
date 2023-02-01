package com.websparks.assessment;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.time.Duration;
import java.util.Set;

public class BaseTest {

    protected WebDriver webDriver;

    protected long defaultTimeout = 60;

    @BeforeClass
    public void setup() {
        final String root = System.getProperty("user.dir");
        System.setProperty("webdriver.chrome.driver",
                root + "\\src\\test\\resources\\drivers\\chromedriver.exe");

        webDriver = new ChromeDriver();

        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        webDriver.manage().window().maximize();
    }

    @AfterClass
    public void teardown() {
        webDriver.quit();
    }

    public void waitForWindowByElementAndSwitch(String xpath, long timeout) {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeout));
        wait.until(webDriver->{
            Set<String> windowHandles = webDriver.getWindowHandles();
            for(String winHandle: windowHandles) {
                webDriver.switchTo().window(winHandle);
                if(webDriver.findElements(By.xpath(xpath)).size() > 0) {
                    return true;
                }
            }
            return false;
        });
    }

    public void signout() {
        webDriver.findElement(By.xpath("//button[text()='Sign out']")).click();
        final String title = webDriver.findElement(By.className("login-head")).getText();
        Assert.assertEquals(title, "To continue you will need to sign in first,");
    }
}
