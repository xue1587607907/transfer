package com.guiji.apiautomationfinal.runner;


import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/main/resources/features",
        glue = {"com.hsbc.features", "com.hsbc.config", "com.hsbc.listeners"},
        monochrome = true,
        plugin = {
                "pretty",
                "com.hsbc.listeners.CucumberGlobalStepListener",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        }
)
public class TestRunner extends AbstractTestNGCucumberTests {
}