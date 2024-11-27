package fitpeoAutomation;

import java.io.File;
import java.io.IOException;


import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class test {
    WebDriver driver;
    JavascriptExecutor js;
    Actions ac;
    TakesScreenshot ts;

    // Setup method to initialize WebDriver, maximize the browser, and set up objects
    @BeforeTest
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        js = (JavascriptExecutor) driver; 
        ac = new Actions(driver); 
        ts = (TakesScreenshot) driver;
    }

    // Test to verify navigation to Fitpeo's homepage and check its title
    @Test(priority = 1)
    public void navigateToFitpeo() {
        driver.get("https://fitpeo.com/");
        Assert.assertEquals(driver.getTitle(), "Remote Patient Monitoring (RPM) - fitpeo.com", "Homepage title verification failed.");
    }

    // Test to navigate to the Revenue Calculator page and verify the URL
    @Test(priority = 2)
    public void navigateToRevenueCalculator() {
        driver.navigate().to("https://fitpeo.com/revenue-calculator");
        Assert.assertTrue(driver.getCurrentUrl().contains("revenue-calculator"), "Navigation to Revenue Calculator page failed.");
    }

    // Test to scroll to the slider and adjust its value to a target (820)
    @Test(priority = 3)
    public void scrollToSliderAdjustSliderValue() throws InterruptedException {
        String textFieldValue = ""; 
        String targetValue = "820"; // Target value for the slider
      
        WebElement sliderInputFieldValue = driver.findElement(By.cssSelector(".MuiInputBase-input")); // Input field showing slider value
        WebElement slider = driver.findElement(By.cssSelector(".MuiSlider-thumb input"));
        ac.clickAndHold(slider).build().perform();
        // Scroll to the slider section
        WebElement sliderSection = driver.findElement(By.xpath("//h4[text()='Medicare Eligible Patients']"));
        js.executeScript("arguments[0].scrollIntoView(true)", sliderSection);
        Assert.assertTrue(sliderSection.isDisplayed(), "Failed to scroll to 'Medicare Eligible Patients' section.");

        // Move the slider until the target value is reached
        while (!textFieldValue.equals(targetValue)) {
            ac.sendKeys(Keys.ARROW_RIGHT).build().perform();
            textFieldValue = sliderInputFieldValue.getAttribute("value");
        }
        ac.release().build().perform();


        // Verify the slider's Text field value matches the target
        Assert.assertEquals(sliderInputFieldValue.getAttribute("value"), "820");
    }

    // Test to update the slider's value through the input field
    @Test(priority = 4)
    public void updateTextField() throws InterruptedException {
        String updateValue = "560"; // Value to set in the text field
        double expectedSliderValuePercentageCalculate = (Integer.parseInt(updateValue) * 0.05); // Expected percentage position of the slider

        WebElement slider = driver.findElement(By.cssSelector(".MuiSlider-thumb"));
        WebElement sliderInputFieldValue = driver.findElement(By.cssSelector("input.MuiInputBase-input")); // Input field for slider value

        // Clear existing text and enter new value
        sliderInputFieldValue.sendKeys(Keys.CONTROL + "a");
        sliderInputFieldValue.sendKeys(Keys.DELETE);
        sliderInputFieldValue.sendKeys(updateValue);

        // Verify the updated value in the text field
        Assert.assertEquals(updateValue, sliderInputFieldValue.getAttribute("value"));

        // Verify that the slider's position percentage matches the expected value
        String[] actualSliderPercentage = slider.getAttribute("style").replaceAll(" ", "").replaceAll("%", "").replaceAll(";", "").split(":");
        Assert.assertEquals(expectedSliderValuePercentageCalculate, Double.parseDouble(actualSliderPercentage[1]));
    }

    // Test to select multiple CPT codes by dynamically building their XPath
    @Test(priority = 5)
    public void selectCPTCodes() throws InterruptedException {
        String[] cptCodes = { "CPT-99091", "CPT-99453", "CPT-99454", "CPT-99474" }; // CPT codes to select

        for (String cptCode : cptCodes) {
            // Build the XPath dynamically for each CPT code
            String cptXPath = String.format("//p[text()='%s']//parent::div//following-sibling::label//input", cptCode);
            WebElement checkBox = driver.findElement(By.xpath(cptXPath));

            // Scroll to the checkbox and click it using JavaScript
            js.executeScript("arguments[0].scrollIntoView(true);", checkBox);
            js.executeScript("arguments[0].click();", checkBox);

            // Verify the checkbox is selected
            Assert.assertTrue(checkBox.isSelected(), "CPT Code selection failed for: " + cptCode);
        }
    }

    // Test to validate the total reimbursement and take a screenshot for verification
    @Test(priority = 6)
    public void validateTotalRecurringReimbursement() throws IOException {
        // Update slider value to 820 for validation
        WebElement sliderInputFieldValue = driver.findElement(By.cssSelector("input.MuiInputBase-input"));
        sliderInputFieldValue.sendKeys(Keys.CONTROL + "a");
        sliderInputFieldValue.sendKeys(Keys.DELETE);
        sliderInputFieldValue.sendKeys("820");

        // Validate the final reimbursement value
        WebElement actualReimbursement = driver.findElement(By.xpath("//p[text()='Total Recurring Reimbursement for all Patients Per Month:']//p"));
        String expectedReimbursementValue = "$110700";
        String actualReimbursementValue = actualReimbursement.getText();
        Assert.assertEquals(expectedReimbursementValue, actualReimbursementValue, "Reimbursement Value not as expected");

        // Take a screenshot if validation passes 
        File source = ts.getScreenshotAs(OutputType.FILE);

        // Save the screenshot in the project directory
        File destination = new File(System.getProperty("user.dir") + "/fitpeoresult.png");
        FileHandler.copy(source, destination);
    }

    // Method to close the browser after tests
    @AfterTest
    public void tearDown() {           
             driver.quit();      
    }
}
