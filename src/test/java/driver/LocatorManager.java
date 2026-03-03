package driver;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.InputStream;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LocatorManager {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration COOKIE_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration PAGE_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration POLLING = Duration.ofMillis(300);

    private static List<Map<String, String>> elementList;
    private static Map<String, String> dataMap;
    private static final Map<String, String> runtimeStore = new LinkedHashMap<>();

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream elementStream = LocatorManager.class
                    .getClassLoader()
                    .getResourceAsStream("elements/element.json");

            InputStream dataStream = LocatorManager.class
                    .getClassLoader()
                    .getResourceAsStream("data/data.json");

            if (elementStream == null) {
                throw new RuntimeException("element.json bulunamadı");
            }

            if (dataStream == null) {
                throw new RuntimeException("data.json bulunamadı");
            }

            elementList = mapper.readValue(
                    elementStream,
                    new TypeReference<List<Map<String, String>>>() {}
            );

            dataMap = mapper.readValue(
                    dataStream,
                    new TypeReference<Map<String, String>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("JSON dosyaları yüklenemedi", e);
        }
    }

    public static By getBy(String key) {
        for (Map<String, String> element : elementList) {
            if (key.equals(element.get("key"))) {
                String type = element.get("type");
                String value = element.get("value");

                switch (type) {
                    case "id":
                        return By.id(value);
                    case "css":
                        return By.cssSelector(value);
                    case "xpath":
                        return By.xpath(value);
                    case "name":
                        return By.name(value);
                    case "className":
                        return By.className(value);
                    default:
                        throw new RuntimeException("Desteklenmeyen locator type: " + type);
                }
            }
        }

        throw new RuntimeException("Element bulunamadı: " + key);
    }

    public static String getData(String dataKey) {
        String value = dataMap.get(dataKey);
        if (value == null) {
            throw new RuntimeException("Data bulunamadı: " + dataKey);
        }
        return value;
    }

    public static WebElement waitUntilVisible(String key) {
        return waitUntilVisible(key, DEFAULT_TIMEOUT);
    }

    public static WebElement waitUntilVisible(String key, Duration timeout) {
        return createWait(timeout).until(ExpectedConditions.visibilityOfElementLocated(getBy(key)));
    }

    public static WebElement waitUntilClickable(String key) {
        return waitUntilClickable(key, DEFAULT_TIMEOUT);
    }

    public static WebElement waitUntilClickable(String key, Duration timeout) {
        return createWait(timeout).until(ExpectedConditions.elementToBeClickable(getBy(key)));
    }

    public static void clickWhenReady(String key) {
        WebElement element = waitUntilClickable(key);
        scrollIntoView(element);
        clickWithFallback(element);
    }

    public static void clickSameTabAndWaitForPageLoad(String key) {
        String oldUrl = Driver.webDriver.getCurrentUrl();
        WebElement element = waitUntilClickable(key);

        removeTargetIfExists(element);
        scrollIntoView(element);
        clickWithFallback(element);

        createWait(PAGE_TIMEOUT)
                .ignoring(StaleElementReferenceException.class)
                .until(driver -> !driver.getCurrentUrl().equals(oldUrl));

        waitForPageLoadComplete();
    }

    public static void hoverElement(String key) {
        WebElement element = waitUntilVisible(key);
        scrollIntoView(element);

        new Actions(Driver.webDriver)
                .moveToElement(element)
                .perform();
    }

    public static void hoverWaitAndClick(String key, Duration hoverDuration) {
        waitForPageLoadComplete();

        WebElement element = waitUntilClickable(key, PAGE_TIMEOUT);
        scrollIntoView(element);

        new Actions(Driver.webDriver)
                .moveToElement(element)
                .pause(hoverDuration)
                .click()
                .perform();
    }

    public static void sendKeys(String key, String dataKey) {
        WebElement element = waitUntilClickable(key);
        scrollIntoView(element);
        clickWithFallback(element);

        element = Driver.webDriver.findElement(getBy(key));
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        element.sendKeys(getData(dataKey));
    }

    public static void sendKeysAndEnter(String key, String dataKey) {
        WebElement element = waitUntilVisible(key);
        scrollIntoView(element);

        try {
            ((JavascriptExecutor) Driver.webDriver).executeScript("arguments[0].focus();", element);
        } catch (Exception ignored) {
        }

        try {
            element.click();
        } catch (Exception ignored) {
        }

        element = Driver.webDriver.findElement(getBy(key));

        try {
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        } catch (Exception e) {
            try {
                element.clear();
            } catch (Exception ignored) {
            }
        }

        element.sendKeys(getData(dataKey));
        element.sendKeys(Keys.ENTER);
    }

    public static void verifyTextEquals(String key, String dataKey) {
        String actualText = waitUntilVisible(key).getText().trim();
        String expectedText = getData(dataKey).trim();

        if (!actualText.equals(expectedText)) {
            throw new AssertionError(
                    "Doğrulama başarısız!\nActual: " + actualText + "\nExpected: " + expectedText
            );
        }

        System.out.println("Doğrulama başarılı -> " + actualText);
    }

    public static void verifyElementVisible(String key) {
        waitUntilVisible(key);
        System.out.println("Element görünür durumda -> " + key);
    }

    public static void saveElementText(String key, String storeKey) {
        String actualText = waitUntilVisible(key).getText().trim();

        if (actualText.isEmpty()) {
            throw new AssertionError("Saklanacak text boş geldi -> " + key);
        }

        runtimeStore.put(storeKey, actualText);
        System.out.println("Text saklandı -> " + storeKey + " = " + actualText);
    }

    public static void verifyStoredTextEquals(String key, String memoryKey) {
        String expectedText = runtimeStore.get(memoryKey);

        if (expectedText == null) {
            throw new RuntimeException("Saklanan değer bulunamadı: " + memoryKey);
        }

        WebElement element = waitUntilVisible(key, VERIFY_TIMEOUT);
        String actualText = element.getText().trim();

        if (!normalizeText(actualText).equals(normalizeText(expectedText))) {
            throw new AssertionError(
                    "Doğrulama başarısız!\nActual: " + actualText + "\nExpected: " + expectedText
            );
        }

        System.out.println("Saklanan değer ile eşleşme başarılı -> " + actualText);
    }

    public static void verifyStoredTextContains(String key, String memoryKey) {
        System.out.println(">>> verifyStoredTextContains aktif | key=" + key + " memoryKey=" + memoryKey);

        String expectedText = runtimeStore.get(memoryKey);

        if (expectedText == null) {
            throw new RuntimeException("Saklanan değer bulunamadı: " + memoryKey);
        }

        waitForPageLoadComplete();

        List<WebElement> elements = createWait(VERIFY_TIMEOUT).until(driver -> {
            List<WebElement> found = driver.findElements(getBy(key));
            return found.isEmpty() ? null : found;
        });

        for (WebElement element : elements) {
            String actualText = element.getText().trim();
            System.out.println(">>> bulunan text = " + actualText);

            if (!actualText.isBlank() && actualText.contains(expectedText)) {
                System.out.println("Saklanan değer içerik doğrulaması başarılı (locator) -> " + actualText);
                return;
            }
        }

        throw new AssertionError(
                "İçerik doğrulama başarısız!\n" +
                        "Beklenen: " + expectedText + "\n" +
                        "Locator key: " + key
        );
    }

    public static void handleCookieIfPresent(String cookieButtonKey) {
        try {
            WebElement cookieButton = waitUntilClickable(cookieButtonKey, COOKIE_TIMEOUT);
            scrollIntoView(cookieButton);
            clickWithFallback(cookieButton);
            System.out.println("Cookie popup kapatıldı.");
        } catch (TimeoutException e) {
            System.out.println("Cookie popup görünmedi, test devam ediyor.");
        }
    }

    public static void waitForPageLoadComplete() {
        createWait(PAGE_TIMEOUT).until(driver -> {
            Object state = ((JavascriptExecutor) driver).executeScript("return document.readyState");
            return "complete".equals(String.valueOf(state));
        });
    }


    private static String normalizeText(String text) {
        return lower(text).replaceAll("[^\\p{L}\\p{Nd}]", "");
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }


    private static WebDriverWait createWait(Duration timeout) {
        WebDriverWait wait = new WebDriverWait(Driver.webDriver, timeout);
        wait.pollingEvery(POLLING);
        wait.ignoring(NoSuchElementException.class);
        wait.ignoring(StaleElementReferenceException.class);
        return wait;
    }

    private static void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) Driver.webDriver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'center'});",
                element
        );
    }

    private static void removeTargetIfExists(WebElement element) {
        try {
            ((JavascriptExecutor) Driver.webDriver).executeScript(
                    "arguments[0].removeAttribute('target');",
                    element
            );
        } catch (Exception ignored) {
        }
    }

    private static void clickWithFallback(WebElement element) {
        try {
            element.click();
            return;
        } catch (Exception ignored) {
        }

        try {
            new Actions(Driver.webDriver)
                    .moveToElement(element)
                    .click()
                    .perform();
            return;
        } catch (Exception ignored) {
        }

        ((JavascriptExecutor) Driver.webDriver).executeScript("arguments[0].click();", element);
    }
}
