package com.guiji.apiautomationfinal.listeners;


import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import io.cucumber.java.Before;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IHookListener {
    @Before
    public void beforeCucumber(io.cucumber.java.Scenario scenario) {
        ExtentTestNGListener.cucumberScenarioThreadLocal.set(scenario);
        log.info("Current Scenario: {}", scenario.getName());
        String uuid = Allure.getLifecycle().getCurrentTestCase().get();
        System.out.println("uuid = " + uuid);
        String testName = ExtentTestNGListener.cucumberScenarioThreadLocal.get().getName();
        ExtentTest extentTest = ExtentTestNGListener.getExtentReports().createTest(testName);
        ExtentTestNGListener.extentTestThreadLocal.set(extentTest);
        extentTest.log(Status.INFO, MarkupHelper.createLabel("Scenario Start Execute==>" + testName, ExtentColor.BLUE));
    }
}