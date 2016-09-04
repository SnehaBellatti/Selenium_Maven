package delta.main;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.relevantcodes.extentreports.LogStatus;

import generics.Excel;
import generics.Property;
import generics.Utility;

public class DeltaDriver extends BaseDriver{
	
	public String browser;
	public String hubURL;
	@BeforeMethod
	public void launchApp(XmlTest value) throws MalformedURLException
	{	
		browser =value.getParameter("browser");
		hubURL=value.getParameter("huburl");
		DesiredCapabilities dc = new DesiredCapabilities();
		dc.setBrowserName(browser);
		dc.setPlatform(Platform.ANY);
		driver = new RemoteWebDriver(new URL(hubURL),dc);
		
		
		String appURL=Property.getPropertyValue(configPptPath, "URL");
		String timeOut=Property.getPropertyValue(configPptPath, "TimeOut");
		
		/*if (browser.equalsIgnoreCase("firefox")){
			driver =new FirefoxDriver();
		}
		
		else if (browser.equalsIgnoreCase("chrome")){
			System.setProperty("webdriver.chrome.driver", "./exefiles/chromedriver.exe");
			driver = new ChromeDriver();
		}
		else {
			System.setProperty("webdriver.ie.driver", "./exefiles/IEDriverServer.exe");
			driver = new InternetExplorerDriver();
		}*/
		
		driver.get(appURL);
		driver.manage().timeouts().implicitlyWait(Long.parseLong(timeOut), TimeUnit.SECONDS);
		driver.manage().window().maximize();
	}
	
	@Test(dataProvider="getScenarios",dataProviderClass=BaseDriver.class)
	public void testScenerios(String scenerioSheet,String executionStatus) throws Exception
	{
		
		testReport=eReport.startTest(browser+ "_"+scenerioSheet) ;
		if (executionStatus.equalsIgnoreCase("YES"))
		{
			int stepCount=Excel.getRowCount(ScenariosPath, scenerioSheet);
			
			for (int i=1;i<=stepCount;i++)
			{
				String description=Excel.getCellValue(ScenariosPath, scenerioSheet, i, 0);
				String action=Excel.getCellValue(ScenariosPath, scenerioSheet, i, 1);
				String input1=Excel.getCellValue(ScenariosPath, scenerioSheet, i, 2);
				String input2=Excel.getCellValue(ScenariosPath, scenerioSheet, i, 3);
				
				testReport.log(LogStatus.INFO, "description:"+description+" action:"+action+" input1:"+input1+" input2:"+input2+"");
				KeyWord.executeKeyWord(driver, action, input1, input2);
			}
		}
		
		else{
			testReport.log(LogStatus.SKIP, "execution of scenario is skipped");
			throw new SkipException ("Skipping this Scenerio");
			
		}			
	}

	@AfterMethod
	public void quitApp(ITestResult test){
		if(test.getStatus()==ITestResult.FAILURE){
			String pImage =Utility.getPageScreenShot(driver, imageFolderPath);
			String p= testReport.addScreenCapture("."+pImage);
			testReport.log(LogStatus.FAIL, "Page Screen shot :" +p);
		}
		eReport.endTest(testReport);
		driver.close();
		
	}
	

}
