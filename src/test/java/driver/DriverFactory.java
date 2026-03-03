package driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverFactory {

    public static WebDriver getDriver() {
        System.out.println(">>> getDriver içine girdi");

        String browser = System.getenv("BROWSER");
        browser = (browser == null) ? "CHROME" : browser;

        switch (browser.toUpperCase()) {
            case "CHROME":
            default:
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--start-maximized"); // Sadece tam ekran açması yeterli
                options.addArguments("--disable-notifications");
                options.addArguments("--disable-blink-features=AutomationControlled"); //Botu Cozen Kod

                options.addArguments("--incognito");


                if ("Y".equalsIgnoreCase(System.getenv("HEADLESS"))) {
                    options.addArguments("--headless=new");
                    options.addArguments("--disable-gpu");
                }

                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");

                System.out.println(">>> ChromeDriver oluşturuluyor");
                WebDriver driver = new ChromeDriver(options);
                System.out.println(">>> ChromeDriver oluşturuldu");

                return driver;

        }
    }
}