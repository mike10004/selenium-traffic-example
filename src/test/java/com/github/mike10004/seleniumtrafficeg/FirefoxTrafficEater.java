/*
 * (c) 2016 Mike Chaberski
 */
package com.github.mike10004.seleniumtrafficeg;

import com.github.mike10004.xvfbselenium.WebDriverSupport;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import io.github.bonigarcia.wdm.MarionetteDriverManager;
import net.lightbody.bmp.BrowserMobProxy;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

class FirefoxTrafficEater extends TrafficEater {

    private final Map<String, String> environment;

    public FirefoxTrafficEater() {
        this(ImmutableMap.<String, String>of());
    }

    public FirefoxTrafficEater(Map<String, String> environment) {
        this.environment = ImmutableMap.copyOf(environment);
    }

    @Override
    protected FirefoxDriver createWebDriver(BrowserMobProxy proxy) {
        MarionetteDriverManager.getInstance().setup();
        FirefoxProfile profile = new CertificateSupplementingFirefoxProfile(Resources.asByteSource(getClass().getResource("/cert8.db")));
        // https://stackoverflow.com/questions/2887978/webdriver-and-proxy-server-for-firefox
        profile.setPreference("network.proxy.type", 1);
        profile.setPreference("network.proxy.http", "localhost");
        profile.setPreference("network.proxy.http_port", proxy.getPort());
        profile.setPreference("network.proxy.ssl", "localhost");
        profile.setPreference("network.proxy.ssl_port", proxy.getPort());
        FirefoxBinary binary = new FirefoxBinary();
        FirefoxDriver driver = WebDriverSupport.firefoxInEnvironment(environment).create(binary, profile);
        return driver;
    }

    private static class CertificateSupplementingFirefoxProfile extends SupplementingFirefoxProfile {

        public static final String CERTIFICATE_DB_FILENAME = "cert8.db";

        private final ByteSource certificateDbSource;

        private CertificateSupplementingFirefoxProfile(ByteSource certificateDbSource) {
            this.certificateDbSource = checkNotNull(certificateDbSource);
        }

        @Override
        protected void profileCreated(File profileDir) {
            File certificateDbFile = new File(profileDir, CERTIFICATE_DB_FILENAME);
            try {
                certificateDbSource.copyTo(Files.asByteSink(certificateDbFile));
            } catch (IOException e) {
                throw new IllegalStateException("failed to copy certificate database to profile dir", e);
            }
        }
    }

    private static class SupplementingFirefoxProfile extends FirefoxProfile {

        protected void profileCreated(File profileDir) {
            // no op
        }

        @Override
        public File layoutOnDisk() {
            File profileDir = super.layoutOnDisk();
            profileCreated(profileDir);
            return profileDir;
        }
    }

}

