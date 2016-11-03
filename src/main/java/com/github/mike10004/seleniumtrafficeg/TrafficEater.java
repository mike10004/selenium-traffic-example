package com.github.mike10004.seleniumtrafficeg;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public abstract class TrafficEater {

    public interface TrafficGenerator {
        void generate(WebDriver driver) throws IOException;
    }

    protected abstract WebDriver createWebDriver(BrowserMobProxy proxy);

    protected Set<CaptureType> getCaptureTypes() {
        return EnumSet.allOf(CaptureType.class);
    }

    protected String getHarName() {
        return "myExample";
    }

    public Har consume(TrafficGenerator generator) throws IOException, WebDriverException {
        BrowserMobProxy bmp = new BrowserMobProxyServer();
        Set<CaptureType> captureTypes = getCaptureTypes();
        bmp.enableHarCaptureTypes(captureTypes);
        bmp.newHar(getHarName());
        bmp.start();
        try {
            WebDriver driver = createWebDriver(bmp);
            try {
                generator.generate(driver);
            } finally {
                driver.quit();
            }
        } finally {
            bmp.stop();
        }
        net.lightbody.bmp.core.har.Har har = bmp.getHar();
        return har;

    }

}
