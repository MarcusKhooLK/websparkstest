package com.websparks.assessment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootTest
class AssessmentApplicationTests extends BaseTest {

    private Map<String, Object> dataMap;

    @BeforeClass
    public void getData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            dataMap = mapper.readValue(Paths.get(System.getProperty("user.dir") + "\\src\\test\\resources\\data.json").toFile(), Map.class);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Test
    public void loginTest() throws InterruptedException {
        webDriver.get("https://todo-list-login.firebaseapp.com/");

        final String title = webDriver.findElement(By.className("login-head")).getText();
        Assert.assertEquals(title, "To continue you will need to sign in first,");

        webDriver.findElement(By.className("btn-github")).click();
        waitForWindowByElementAndSwitch("//input[@id='login_field']", defaultTimeout);
        webDriver.findElement(By.id("login_field")).sendKeys(dataMap.get("username").toString());
        webDriver.findElement(By.id("password")).sendKeys(dataMap.get("password").toString());
        webDriver.findElement(By.xpath("//input[@type='submit']")).click();

        try {
            waitForWindowByElementAndSwitch("//button[@id='js-oauth-authorize-btn']", 3);
            webDriver.findElement(By.id("js-oauth-authorize-btn")).click();
        } catch(TimeoutException ex) {
            System.out.println("Authorize button not found. It could be that the account has already authorized");
        } finally {
            waitForWindowByElementAndSwitch("//button[text()='Sign out']", defaultTimeout);
            clearList();
        }
    }

    @Test(dependsOnMethods = {"loginTest"})
    public void createTodoListTest() {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(defaultTimeout));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@ng-model='home.list']")));
        int listNameMin = (int)dataMap.get("listNameMin");
        int listNameMax = (int)dataMap.get("listNameMax");
        int noOfListToAdd = (int)dataMap.get("numOfListToAdd");
        for(int i = 0; i < noOfListToAdd; i++) {
            final String listName = RandomStringUtils.randomAlphanumeric(new Random().nextInt(listNameMin, listNameMax));
            WebElement webElem = webDriver.findElement(By.xpath("//input[@ng-model='home.list']"));
            webElem.sendKeys(listName);
            webDriver.findElement(By.xpath("//button[text()='Add List']")).click();
            webDriver.findElement(By.xpath("//ul[@class='list-group']//a[text()='%s List']".formatted(listName)));
        }

        WebElement list = webDriver.findElement(By.xpath("//ul[@class='list-group']"));
        List<WebElement> elems = list.findElements(By.tagName("li"));
        System.out.println(elems.size());
        Assert.assertEquals(elems.size(), 10);
    }

    @Test(dependsOnMethods = {"createTodoListTest"})
    public void signoutTest() {
        signout();
    }

    @Test(dependsOnMethods = {"signoutTest"})
    public void reloginTest() throws InterruptedException {
        webDriver.findElement(By.className("btn-github")).click();
        try {
            waitForWindowByElementAndSwitch("//button[@id='js-oauth-authorize-btn']", 3);
            webDriver.findElement(By.id("js-oauth-authorize-btn")).click();
        } catch(TimeoutException ex) {
            System.out.println("Authorize button not found. It could be that the account has already authorized");
        } finally {
            // should not ask for creds again, test for sign out button straight
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(defaultTimeout));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[text()='Sign out']")));
        }
    }

    @Test(dependsOnMethods = {"reloginTest"})
    public void removeList() {
        WebElement list = webDriver.findElement(By.xpath("//ul[@class='list-group']"));
        int start = (int)dataMap.get("removeListStart");
        int end = (int)dataMap.get("removeListEnd");
        int length = end - start;
        int indexToRemove = start;
        int count = 0;
        while(count < length) {
            list.findElements(By.tagName("li")).get(indexToRemove).findElement(By.tagName("button")).click();
            count++;
        }

        List<WebElement> elems = list.findElements(By.tagName("li"));
        System.out.println(elems.size());
        Assert.assertEquals(elems.size(), 5);
    }

    @Test(dependsOnMethods = {"removeList"})
    public void signoutTest2() {
        signout();
    }

    private void clearList() {
        WebElement list = webDriver.findElement(By.xpath("//ul[@class='list-group']"));
        List<WebElement> elems = list.findElements(By.tagName("li"));
        while(elems.size() > 0 ){
            list.findElements(By.tagName("li")).get(0).findElement(By.tagName("button")).click();
            elems = list.findElements(By.tagName("li"));
        }
        Assert.assertEquals(elems.size(), 0);
    }
}
