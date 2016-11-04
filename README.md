Capturing network traffic with Selenium and Firefox or Chrome
=============================================================

This project is an example of how to capture network traffic using Selenium 
WebDriver with Browsermob-proxy. Unit tests employing Firefox and Chrome 
webdrivers capturing both HTTP and HTTPS traffic are included.

The primary reason this project is shared is that many Selenium traffic 
capture examples and how-tos suggest the process is rather simple, but in 
practice, at least with the version of Firefox the author has, the following
problems were encountered:

* merely configuring the `DesiredCapabilities` object for the Firefox 
  webdriver does not actually configure the driver to use the proxy
* many examples of how to customize the Firefox profile for the webdriver
  fail to include instructions for specifying that the same proxy be used for 
  HTTPS requests
* empty HAR files are produced by browsermob-proxy if the webdriver proxy 
  is not configured correctly
* by default, the Firefox driver does not accept the certificate provided by 
  Browsermob-proxy to MITM SSL traffic, and configuring the Firefox profile
  for the webdriver to accept a custom self-signed certificate is nontrivial
* Browsermob-proxy does not respect the system properties that define a proxy
  for the JVM, so if you have an upstream proxy, the Browsermob proxy 
  must be configured explicitly from the system properties

This project is a working demonstration of network traffic capture that is 
small enough to comprehend but shows a fair number of ways you can customize 
it for your needs.
