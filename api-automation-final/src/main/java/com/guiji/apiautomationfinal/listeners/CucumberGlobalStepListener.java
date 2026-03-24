package com.guiji.apiautomationfinal.listeners;


import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.PickleStepTestStep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CucumberGlobalStepListener implements ConcurrentEventListener {
    public static ThreadLocal<String> currentStepName = new ThreadLocal<>();

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestStepStarted.class, this::onTestStepStarted);
        eventPublisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
    }

    private void onTestStepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep pickleStepTestStep) {
            String stepName = pickleStepTestStep.getStep().getText();
            currentStepName.set(stepName);
            ExtentTest currentTest = ExtentTestNGListener.getCurrentExtentTest();
            if (currentTest != null) {
                currentTest.createNode(stepName).log(Status.INFO, MarkupHelper.createLabel("Step: "+stepName, ExtentColor.CYAN));
            }
        }
    }

    private void onTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep pickleStepTestStep) {
            String stepName = pickleStepTestStep.getStep().getText();
            currentStepName.remove();
            ExtentTest currentTest = ExtentTestNGListener.getCurrentExtentTest();
            if (currentTest != null) {
                ExtentTest stepNode = currentTest.createNode(stepName);
                if (event.getResult().getStatus().isOk()) {
                    stepNode.log(Status.PASS, MarkupHelper.createLabel("Step Success: "+stepName, ExtentColor.GREEN));
                } else if (event.getResult().getStatus().toString().contains("SKIP")) {
                    stepNode.log(Status.SKIP, MarkupHelper.createLabel("Step Skip: "+stepName, ExtentColor.AMBER));
                } else {
                    stepNode.log(Status.FAIL, MarkupHelper.createLabel("Failed Reason:"+ event.getResult().getError().getMessage(), ExtentColor.RED));
                }
            }
        }
    }
}