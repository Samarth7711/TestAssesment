package com.assesment.com;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;



import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.time.Duration;
import java.util.List;

public class DouglasTest {
    WebDriver driver;
    WebDriverWait wait;
    private static final Logger logger = LogManager.getLogger(DouglasTest.class);

    @BeforeClass
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get("https://www.douglas.de/de");
        handleCookieBanner();
    }
@Test(priority=1)
    public void handleCookieBanner() {
        try {
            WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@data-testid='uc-accept-all-button']")));
            acceptButton.click();
            logger.info("✅ Clicked on cookie accept button");
        } catch (TimeoutException e) {
            logger.warn("❌ No cookie banner detected.");
        }
    }

    @Test(priority = 2)
    public void testNavigateAndClickParfum() {
    	try {
            WebElement parfumLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Parfum')] | //a[contains(@href, 'parfum')]")));
            parfumLink.click();
            System.out.println("✅ Clicked on 'Parfum'.");

        } catch (TimeoutException e) {
            System.out.println("❌ Failed to find 'Parfum' link.");
            throw e;
        } catch (ElementClickInterceptedException e) {
            System.out.println("❌ Click intercepted. Retrying after removing banner.");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("document.getElementById('usercentrics-root').remove();");

            // Retry clicking
            WebElement parfumLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Parfum')] | //a[contains(@href, 'parfum')]")));
            parfumLink.click();
            System.out.println("✅ Successfully clicked 'Parfum' after retry.");
        }
    
    }

    @DataProvider(name = "filterData")
    public Object[][] filterData() {
        return new Object[][]{
                {"Marke", "CHANEL"},
                {"Preis", "50-100"},
                {"Duftnote", "Blumig"},
                {"Für Wen", "Damen"},
                {"Aktionen", "Sale"},
                {"Produktart", "Parfum"},
                {"Verantwortung", "Nachhaltig"}
        };
    }

    @Test(priority = 3, dataProvider = "filterData")
    public void testEachFilterSeparately(String filterType, String filterValue) {
        try {
            logger.info("🛒 Applying Filter: " + filterType + " -> " + filterValue);

            // Click on the filter category
            WebElement filterCategory = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(text(),'" + filterType + "')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", filterCategory);
            logger.info("✅ Clicked on filter category: " + filterType);

            // Search for the filter value (if search box exists)
            List<WebElement> searchBoxList = driver.findElements(
                    By.xpath("//input[contains(@placeholder, 'Suchen') or contains(@class, 'filter-search')]")
            );

            if (!searchBoxList.isEmpty()) {
                WebElement searchBox = searchBoxList.get(0);
                searchBox.click();
                searchBox.clear();
                searchBox.sendKeys(filterValue);
                logger.info("✅ Entered '" + filterValue + "' in the search bar.");
                Thread.sleep(2000);
            }

            // Select the checkbox for the filter value
            WebElement filterCheckbox = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//label[contains(text(),'" + filterValue + "')]/ancestor::div[contains(@class,'checkbox')]")
            ));

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", filterCheckbox);
            logger.info("✅ Selected filter: " + filterValue);

            // Wait for filtered products to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(@class,'product-title')]")));

            // Validate products are displayed
            List<WebElement> products = driver.findElements(By.xpath("//span[contains(@class,'product-title')]")
            );
            logger.info("🔍 Found " + products.size() + " products for filter: " + filterType + " -> " + filterValue);
            Assert.assertTrue(products.size() > 0, "❌ No products found for " + filterType + " -> " + filterValue);
        } catch (Exception e) {
            logger.error("❌ Error applying filter: " + filterType + " -> " + filterValue, e);
        }
    }
}
