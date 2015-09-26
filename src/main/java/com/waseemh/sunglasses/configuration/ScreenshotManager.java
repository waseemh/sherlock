package com.waseemh.sunglasses.configuration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.waseemh.sunglasses.configuration.Configuration;
import com.waseemh.sunglasses.webdriver.WebDriverWaiter;

public class ScreenshotManager {

	WebDriver driver;
	Configuration configuration;

	public ScreenshotManager(WebDriver driver, Configuration configuration) {
		this.driver = driver;
		this.configuration = configuration;
	}

	private boolean takeScreenshot(WebElement element, String screenshotName) throws IOException {
		boolean doCompare;
		File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		BufferedImage  fullImg = ImageIO.read(screenshot);

		//Get the location of element on the page
		Point point = element.getLocation();

		//Get width and height of the element
		int eleWidth = element.getSize().getWidth();
		int eleHeight = element.getSize().getHeight();

		//Crop the entire page screenshot to get only element screenshot
		BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);
		ImageIO.write(eleScreenshot, "png", screenshot);

		//Set screenshot type (baseline, capture)
		File baselineImage = configuration.getResourceManager().getBaselineImage(screenshotName);

		//new capture file (baseline or capture)
		File newCapture;

		//check if baseline image doesn't exist (if first capture->no compare) or we are in baseline mode
		if(!baselineImage.exists() || configuration.isBaselineMode()) { 
			newCapture = baselineImage;
			doCompare=false;
		}
		else {
			newCapture = configuration.getResourceManager().getCaptureImage(screenshotName);
			doCompare=true;
		}

		//copy file to screenshots folder
		FileUtils.copyFile(screenshot, newCapture);

		return doCompare;
	}

	public boolean takeScreenshot(By by, String screenshotName) throws IOException {
		WebElement element;
		WebDriverWaiter waiter = configuration.getWaiter();
		if(waiter!=null) {
			element = waiter.waitForElement(by);
		}
		else {
			element = driver.findElement(by);
		}
		return takeScreenshot(element,screenshotName);
	}

}