package com.guiji.apiautomationfinal.utils;


import com.alibaba.fastjson2.JSON;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.guiji.apiautomationfinal.entity.CaseInfo;
import com.guiji.apiautomationfinal.listeners.ExtentTestNGListener;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.TestResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class TestReportUtils {
    public static void setCaseInfoToAllure(CaseInfo caseInfo) {
        Optional<String> currentTestCase = Allure.getLifecycle().getCurrentTestCase();
        if (currentTestCase.isPresent()) {
            String uuid = Allure.getLifecycle().getCurrentTestCase().get();
            Allure.getLifecycle().updateTestCase(uuid, testResult -> {
                testResult.setName(String.format("[%s]", caseInfo.getCaseId()));
                // SeverityLevel severity = SeverityLevel.NORMAL;
                // if (caseInfo.getSeverity() != null) {
                //     severity = SeverityLevel.valueOf(caseInfo.getSeverity().toUpperCase());
                // }
                // testResult.getLabels().add(new Label().setName("severity").setValue(severity.value()));
                if (caseInfo.getDescription() != null) {
                    testResult.setDescription(String.format("Description: %s<br/>", caseInfo.getDescription()));
                }
                String[] tags = {caseInfo.getModule(), caseInfo.getRerunTimes()};
                Arrays.stream(tags).forEach(tag -> {
                    if (tag != null) {
                        testResult.getLabels().add(new Label().setName("tag").setValue(tag));
                    }
                });
                Map<String, Object> reqMap = getReqMap(caseInfo);
                addJsonAttachment("Request Params", JSON.toJSONString(reqMap));
                addTestCaseParam(testResult, "Test Api: ", caseInfo.getPath());
            });
        }
    }

    private static Map<String, Object> getReqMap(CaseInfo caseInfo) {
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("CaseID", caseInfo.getCaseId());
        reqMap.put("Request Path", caseInfo.getPath());
        reqMap.put("Request Method", caseInfo.getReqMethod());
        reqMap.put("Request headers", caseInfo.getReqHeader());
        reqMap.put("Country", caseInfo.getCountry());
        reqMap.put("Module", caseInfo.getModule());
        reqMap.put("Description", caseInfo.getDescription());
        return reqMap;
    }

    public static void addJsonAttachment(String name, String content) {
        Allure.addAttachment(name, "application/json", content, "json");
    }

    public static void addTestCaseParam(TestResult testResult, String name, String content) {
        testResult.getParameters().add(new io.qameta.allure.model.Parameter().setName(name).setValue(content));
    }

    public static void addTextAttachment(String name, String content) {
        Allure.addAttachment(name, "text/plain", content, "txt");
    }

    public static void setFailureReason(CaseInfo caseInfo, Exception e) {
        String uuid = Allure.getLifecycle().getCurrentTestCase().get();
        Allure.getLifecycle().updateTestCase(uuid, testResult -> {
            testResult.setStatus(io.qameta.allure.model.Status.FAILED);
            testResult.setStatusDetails(
                    new StatusDetails()
                            .setMessage(
                                    String.format("Case ID: %s<br/> Failed Reason: %s<br/> Request path: %s",
                                            caseInfo.getCaseId(), e.getMessage(), caseInfo.getPath()))
                            .setTrace(ExceptionUtils.getStackTrace(e))
            );
        });
        addTextAttachment("Failed Reason: ", ExceptionUtils.getStackTrace(e));
    }

    public static void createReportNode(String stepName, Throwable stepThrowable) {
        ExtentTest currentTest = ExtentTestNGListener.getCurrentExtentTest();
        if (currentTest != null) {
            ExtentTest stepNode = currentTest.createNode(stepName);
            if (stepThrowable != null) {
                stepNode.log(Status.FAIL, MarkupHelper.createLabel("Failed Reason: " + stepThrowable.getMessage(), ExtentColor.RED));
            } else {
                stepNode.log(Status.PASS, MarkupHelper.createLabel("Step Success: " + stepName, ExtentColor.GREEN));
            }
        }
    }
}