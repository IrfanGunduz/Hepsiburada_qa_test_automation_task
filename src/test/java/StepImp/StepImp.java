package StepImp;

import com.thoughtworks.gauge.Step;
import driver.LocatorManager;

import java.time.Duration;

public class StepImp {

    @Step("Tarayıcı Açılır Çerez popup varsa kabul edilir")
    public void acceptCookieIfPresent() {
        LocatorManager.handleCookieIfPresent("btn_CerezKabul");
    }

    @Step("<key> li butonuna tıkla")
    public void clickElement(String key) {
        LocatorManager.clickWhenReady(key);
    }

    @Step("<key> li elemente tıklanır ve sayfanın değişmesi beklenir")
    public void clickElementAndWaitForPageChange(String key) {
        LocatorManager.clickSameTabAndWaitForPageLoad(key);
    }

    @Step("<key> li üzerine gel")
    public void hoverElement(String key) {
        LocatorManager.hoverElement(key);
    }

    @Step("<key> li üzerine gel 2 saniye bekle ve tıkla")
    public void hoverWaitAndClick(String key) {
        LocatorManager.hoverWaitAndClick(key, Duration.ofSeconds(2));
    }

    // Geriye dönük uyumluluk için bırakıldı.
    @Step("<key> li elementi force click yapılır")
    public void forceClickElement(String key) {
        LocatorManager.hoverWaitAndClick(key, Duration.ofSeconds(1));
    }

    @Step("<key> li elemente <dataKey> değerini yaz")
    public void sendKeysToElement(String key, String dataKey) {
        LocatorManager.sendKeys(key, dataKey);
    }

    @Step("<key> li elemente <dataKey> değerini yaz ve enterla")
    public void sendKeysToElementAndPressEnter(String key, String dataKey) {
        LocatorManager.sendKeysAndEnter(key, dataKey);
    }

    @Step("<key> li elementini kontrol <dataKey> et")
    public void verifyElementText(String key, String dataKey) {
        LocatorManager.verifyTextEquals(key, dataKey);
    }

    @Step("<key> li elementinin görünmesi beklenir")
    public void waitForElementToBeVisible(String key) {
        LocatorManager.verifyElementVisible(key);
    }

    @Step("<key> li elementinin görünür olduğu doğrulanır")
    public void verifyElementVisible(String key) {
        LocatorManager.verifyElementVisible(key);
    }

    @Step("<key> li elementinin texti <memoryKey> olarak saklanır")
    public void saveElementText(String key, String memoryKey) {
        LocatorManager.saveElementText(key, memoryKey);
    }

    @Step("<key> li elementinin texti saklanan <memoryKey> değerini içerir")
    public void verifyElementTextContainsStoredValue(String key, String memoryKey) {
        LocatorManager.verifyStoredTextContains(key, memoryKey);
    }

    @Step("<key> li elementinin texti saklanan <memoryKey> değeri ile eşleşir")
    public void verifyElementTextEqualsStoredValue(String key, String memoryKey) {
        LocatorManager.verifyStoredTextEquals(key, memoryKey);
    }
}
