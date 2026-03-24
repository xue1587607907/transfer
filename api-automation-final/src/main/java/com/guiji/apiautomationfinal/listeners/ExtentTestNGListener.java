package com.guiji.apiautomationfinal.listeners;


import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import com.guiji.apiautomationfinal.constant.Constant;
import com.guiji.apiautomationfinal.entity.CaseInfo;
import com.guiji.apiautomationfinal.utils.FileUtils;
import com.guiji.apiautomationfinal.utils.TestReportUtils;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Label;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;


@Slf4j
public class ExtentTestNGListener implements ITestListener {
    @Getter
    private static ExtentReports extentReports;
    public static ThreadLocal<io.cucumber.java.Scenario> cucumberScenarioThreadLocal = new ThreadLocal<>();
    private static final String REPORT_ROOT_DIR = "target/extent-report/";
    private static final String REPORT_FILE_NAME = "API-Auto_Test_Report_" +
            new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".html";
    private static final String FULL_REPORT_PATH = REPORT_ROOT_DIR + REPORT_FILE_NAME;
    @Getter
    public static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();
    public static final ThreadLocal<Integer> retryCount = ThreadLocal.withInitial(() -> 0);

    private synchronized void initExtentReport(ITestContext context) {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(FULL_REPORT_PATH);
        sparkReporter.config().setReportName("API Automation Test Report");
        sparkReporter.config().setDocumentTitle("Test Report");
        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
    }

    @SneakyThrows
    @Override
    public void onStart(ITestContext context) {
        initExtentReport(context);
        FileUtils.deleteDirectory(new File(Constant.ALLURE_RESULTS_PATH));
        FileUtils.deleteDirectory(new File(Constant.ALLURE_REPORT_PATH));
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info("************************************************onTestStart************************************************");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest extentTest = extentTestThreadLocal.get();
        if (extentTest != null) {
            extentTest.log(Status.PASS, MarkupHelper.createLabel("Scenario run PASSED", ExtentColor.GREEN));
        }
        addCaseInfoToAllure(result);
        clearThreadLocal();
        retryCount.remove();
        RetryAnalyzer retryAnalyzer = (RetryAnalyzer) result.getMethod().getRetryAnalyzer(result);
        retryAnalyzer.resetRetryAccount();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest extentTest = extentTestThreadLocal.get();
        if (extentTest != null) {
            extentTest.log(Status.FAIL, MarkupHelper.createLabel("Case RUN FAILED", ExtentColor.RED));
            Throwable failCause = result.getThrowable();
            if (failCause != null) {
                extentTest.log(Status.FAIL, MarkupHelper.createLabel("Failed reason: " + failCause.getMessage(), ExtentColor.RED));
                extentTest.log(Status.FAIL, MarkupHelper.createLabel("Exception Msg: " + failCause, ExtentColor.RED));
            }
        }
        addCaseInfoToAllure(result);
        clearThreadLocal();
        retryCount.remove();
        RetryAnalyzer retryAnalyzer = (RetryAnalyzer) result.getMethod().getRetryAnalyzer(result);
        retryAnalyzer.resetRetryAccount();
    }

    private void addCaseInfoToAllure(ITestResult iTestResult) {
        if (isTestingTestMethod(iTestResult)) {
            if (iTestResult.getThrowable() != null) {
                Allure.addAttachment("The " + retryCount.get() + " times retry failed ", iTestResult.getThrowable().toString());
            }
            Optional<String> currentTestCase = Allure.getLifecycle().getCurrentTestCase();
            if (currentTestCase.isPresent()) {
                String uuid = Allure.getLifecycle().getCurrentTestCase().get();
                Allure.getLifecycle().updateTestCase(uuid, testResult -> testResult.getLabels().add(new Label().setName("Retry Times").setValue(String.valueOf(retryCount.get()))));
            }
            Object[] parameters = iTestResult.getParameters();
            CaseInfo caseInfo;
            if (parameters != null && parameters.length > 0 && parameters[0] instanceof CaseInfo) {
                caseInfo = (CaseInfo) parameters[0];
                caseInfo.setRerunTimes("Failed Retry " + retryCount.get() + " times");
            } else {
                caseInfo = CaseInfo.builder()
                        .caseId("Test-1")
                        .description("Test_desc")
                        .rerunTimes("Retry Times: " + retryCount.get())
                        .build();
            }
            TestReportUtils.setCaseInfoToAllure(caseInfo);
        }
    }

    public boolean isTestingTestMethod(ITestResult result) {
        return !result.getMethod().getMethodName().contains("Scenario");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest extentTest = extentTestThreadLocal.get();
        if (extentTest != null) {
            extentTest.log(Status.SKIP, MarkupHelper.createLabel("Case Skipped", ExtentColor.YELLOW));
        }
        clearThreadLocal();
        int currentCount = retryCount.get();
        retryCount.set(++currentCount);
        addCaseInfoToAllure(result);
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extentReports != null) {
            extentReports.flush();
            log.info("===Report Generate Successfully! PATH====> {}", FULL_REPORT_PATH);
        }
    }

    public void clearThreadLocal() {
        extentTestThreadLocal.remove();
        cucumberScenarioThreadLocal.remove();
    }

    public static ExtentTest getCurrentExtentTest() {
        return extentTestThreadLocal.get();
    }
}