/*
 * (c) 2016 Novetta
 *
 * Created by mike
 */
package com.github.mike10004.seleniumtrafficeg;

import com.github.mike10004.xvfbselenium.WebDriverSupport;
import com.google.common.collect.ImmutableMap;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;

class ChromeTrafficEater extends TrafficEater {

    private final Map<String, String> environment;

    public ChromeTrafficEater() {
        this(ImmutableMap.<String, String>of());
    }

    public ChromeTrafficEater(Map<String, String> environment) {
        this.environment = ImmutableMap.copyOf(environment);
    }

    @Override
    protected ChromeDriver createWebDriver(BrowserMobProxy proxy) {
        ChromeDriverManager.getInstance().setup();
        org.openqa.selenium.Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        ChromeDriver driver = WebDriverSupport.chromeInEnvironment(environment).create(capabilities);
        return driver;
    }

}
