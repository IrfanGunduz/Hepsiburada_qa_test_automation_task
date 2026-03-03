package driver;

import com.thoughtworks.gauge.AfterSuite;
import com.thoughtworks.gauge.BeforeSuite;
import org.openqa.selenium.WebDriver;

public class Driver {

    public static WebDriver webDriver;

    @BeforeSuite
    public void initializeDriver() {
        System.out.println(">>> BeforeSuite başladı");
        webDriver = DriverFactory.getDriver();
        System.out.println(">>> Driver oluşturuldu");
        webDriver.manage().window().maximize();
        System.out.println(">>> Browser maximize edildi");
        webDriver.get("https://www.hepsiburada.com/");
        System.out.println(">>> Site açıldı");
    }

    @AfterSuite
    public void closeDriver() throws InterruptedException {
        System.out.println(">>> AfterSuite çalıştı");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (webDriver != null) {
            webDriver.quit();
        }
    }
}