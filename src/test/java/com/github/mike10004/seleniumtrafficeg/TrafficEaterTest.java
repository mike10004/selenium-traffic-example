package com.github.mike10004.seleniumtrafficeg;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.client.ClientUtil;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TrafficEaterTest {

    public static final String SYSPROP_EXPECTED_ORIGIN = "TrafficEaterTest.expectedOrigin";

    @Test
    public void consume_chrome() throws Exception {
        ExampleVisitor generator = new ExampleVisitor(new URL("https://httpbin.org/get?foo=bar&foo=baz"), true);
        consume(new ChromeTrafficEater(), generator);
        String httpbinHtml = generator.getPageSource();
        String httpbinJson = Jsoup.parse(httpbinHtml).select("pre").text();
        System.out.format("%n%s%n%s%n%s%n%n", Strings.repeat("=", 80), httpbinJson, Strings.repeat("=", 80));
        JsonObject httpbinResponseData = new JsonParser().parse(httpbinJson).getAsJsonObject();
        List<String> origins = Splitter.onPattern(",\\s*").splitToList(httpbinResponseData.get("origin").getAsString());
        assertEquals("origins.size", 1, origins.size());
        System.out.format("origins: %s%n", origins);
        String expectedOrigin = System.getProperty(SYSPROP_EXPECTED_ORIGIN);
        if (expectedOrigin != null) {
            assertEquals("origin", expectedOrigin, origins.get(0));
        }
    }

    public static class ChromeTrafficEater extends TrafficEater {

        @Override
        protected WebDriver createWebDriver(BrowserMobProxy proxy) {
            ChromeDriverManager.getInstance().setup();
            org.openqa.selenium.Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
            WebDriver driver = new ChromeDriver(capabilities);
            return driver;
        }

    }

    private void consume(TrafficEater eater, TrafficEater.TrafficGenerator generator) throws Exception {
        System.out.println();
        net.lightbody.bmp.core.har.Har harInMemory = eater.consume(generator);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss").format(Calendar.getInstance().getTime());
        File buildDir = new File(System.getProperty("user.dir"), "target");
        File outputHarFile = new File(buildDir, "traffic-" + timestamp + ".har");
        harInMemory.writeTo(outputHarFile);
        System.out.format("traffic archived in  %s%n", outputHarFile);
        de.sstoehr.harreader.HarReader reader = new de.sstoehr.harreader.HarReader();
        de.sstoehr.harreader.model.Har harFromDisk = reader.readFromFile(outputHarFile);
        SstoehrHars.dumpInfo(harFromDisk, System.out);
        assertFalse("har entries empty", harFromDisk.getLog().getEntries().isEmpty());
    }

    private static class ExampleVisitor implements TrafficEater.TrafficGenerator {

        private final URL url;
        private final boolean retainPageSource;
        private String pageSource;

        public ExampleVisitor(URL url, boolean retainPageSource) {
            this.url = url;
            this.retainPageSource = retainPageSource;
        }

        public void generate(WebDriver driver) throws IOException {
            driver.get(url.toString());
            if (retainPageSource) {
                pageSource = driver.getPageSource();
            }
        }

        public String getPageSource() {
            return pageSource;
        }
    }



}