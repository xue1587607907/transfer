package com.guiji.apiautomationfinal.base;

import com.alibaba.fastjson.JSON;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.guiji.apiautomationfinal.Application;
import com.guiji.apiautomationfinal.constant.Constant;
import com.guiji.apiautomationfinal.context.TestContextHolder;
import com.guiji.apiautomationfinal.entity.CaseInfo;
import com.guiji.apiautomationfinal.entity.ReplaceContext;
import com.guiji.apiautomationfinal.entity.RunEnvEntity;
import com.guiji.apiautomationfinal.functional.plugins.FunctionManager;
import com.guiji.apiautomationfinal.functional.strategy.DependencySyncStrategy;
import com.guiji.apiautomationfinal.functional.strategy.factory.DependencySyncFactory;
import com.guiji.apiautomationfinal.listeners.ExtentTestNGListener;
import com.guiji.apiautomationfinal.utils.RegexReplaceUtils;
import com.guiji.apiautomationfinal.utils.TestReportUtils;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@SpringBootTest(classes = Application.class)
public class BaseTest extends AbstractTestNGSpringContextTests {

    @Autowired
    public RunEnvEntity runEnv;

    @Autowired
    private DependencySyncFactory dependencySyncFactory;

    protected DependencySyncStrategy dependencySyncStrategy;

    public static String expectedOperationName = "";

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        Object[] parameters = testResult.getParameters();
        CaseInfo caseInfo = null;
        if (parameters.length > 0 && parameters[0] instanceof CaseInfo) {
            caseInfo = (CaseInfo) parameters[0];
        }

        String scenarioName = caseInfo == null ? testResult.getMethod().getMethodName() :
                "[" + caseInfo.getCaseId() + "]" + "==>" + caseInfo.getDescription();
        log.info("Current Scenario: {}", scenarioName);

        if (!"runScenario".equalsIgnoreCase(scenarioName)) {
            ExtentTest extentTest = ExtentTestNGListener.getExtentReports().createTest(scenarioName);
            ExtentTestNGListener.getExtentTestThreadLocal().set(extentTest);
            extentTest.log(Status.INFO, MarkupHelper.createLabel("Execute Scenario====>" + scenarioName, ExtentColor.BLUE));
        }

        callBack.runTestMethod(testResult);
        if (testResult.getThrowable() != null) {
            log.error("{} :========>TEST FAILED==========", scenarioName);
        } else {
            log.info("{} :========>TEST PASSED==========", scenarioName);
        }
    }

    public <T> T executeStep(String stepName, Supplier<T> stepLogic) {
        try {
            T result = stepLogic.get();
            TestReportUtils.createReportNode(stepName, null);
            return result;
        } catch (Exception | AssertionError e) {
            TestReportUtils.createReportNode(stepName, e);
            throw e;
        }
    }

    public void assertResult(CaseInfo caseInfo, Response res) {
        Map<String, Object> expectedResults = JSON.parseObject(caseInfo.getExpectedResult());
        if (Objects.nonNull(expectedResults)) {
            expectedResults.forEach((String key, Object expectedValue) -> {
                try {
                    assertSingleField(key, expectedValue, res, caseInfo);
                } catch (AssertionError e) {
                    log.error("Assert Failed for key [{}]: {}", key, e.getMessage());
                    throw e;
                }
            });
        }
    }

    @SneakyThrows
    public void assertSingleField(String key, Object expectedValue, Response res, CaseInfo caseInfo) {
        Object actualValue = null;
        try {
            actualValue = key.equalsIgnoreCase("statusCode") ?
                    String.valueOf(res.getStatusCode()) : res.jsonPath().get(key);

            if (RegexReplaceUtils.isNeedConvertAndCalc(String.valueOf(expectedValue))) {
                Object actVal = parseAssertExpression(String.valueOf(expectedValue), res, caseInfo);
                if (actVal instanceof Boolean) {
                    Assert.assertTrue((Boolean) actVal, String.format("Expected True, Assertion is [%s]", key));
                    formatPrintLog(key, expectedOperationName, actualValue);
                    TestReportUtils.createReportNode(String.format("Assert [%s]: Expected condition met", key), null);
                    return;
                } else {
                    expectedValue = String.valueOf(actVal);
                }
            }

            formatPrintLog(key, expectedValue, actualValue);
            Assert.assertEquals(String.valueOf(actualValue), expectedValue,
                    String.format("Assertion failed for key [%s]: Expected %s = Actual %s", key, expectedValue, actualValue));
            TestReportUtils.createReportNode(String.format("Assert [%s]: Expected %s == Actual %s", key, expectedValue, actualValue), null);
        } catch (AssertionError e) {
            String stepName = String.format("Assert [%s]: Expected %s = Actual %s", key, expectedValue, actualValue);
            TestReportUtils.createReportNode(stepName, e);
            throw e;
        }
    }

    private void formatPrintLog(String key, Object expectedValue, Object actualValue) {
        log.info("{} | {} | {}",
                String.format("%-40s", "Assert Field [" + key + "]"),
                String.format("%-30s", "Expected: " + expectedValue),
                String.format("%-10s", "Actual: " + actualValue));
    }

    protected void prepareCaseDependency(String caseId, String dependsOn) {
        dependencySyncStrategy.prepare(caseId, dependsOn);
    }

    protected void waitForDependency(String caseId) {
        dependencySyncStrategy.waitFor(caseId);
    }

    protected void markCaseDone(String caseId) {
        dependencySyncStrategy.markDone(caseId);
    }

    protected void reset() {
        dependencySyncStrategy.reset();
    }

    @BeforeClass
    public void before() {
        dependencySyncStrategy = dependencySyncFactory.getDefaultStrategy();
        dependencySyncStrategy.setParallel(runEnv.isParallel());
    }

    public String regexReplace(String orgStr, CaseInfo caseInfo) {
        ReplaceContext replaceContext = new ReplaceContext(orgStr, TestContextHolder.getGlobalEnvMap(), caseInfo);
        return RegexReplaceUtils.regexReplace(replaceContext);
    }

    public CaseInfo paramsReplace(CaseInfo caseInfo) {
        // 实现动态参数替换逻辑
        return caseInfo;
    }

    @SneakyThrows
    public void extractResValueToGlobalMap(Response res, CaseInfo caseInfo) {
        try {
            String extractExpression = caseInfo.getExtractExpress();
            if (extractExpression == null) return;
            Map<String, Object> map = JSON.parseObject(extractExpression);
            for (String key : map.keySet()) {
                if (extractExpression.contains("$")) {
                    Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
                    Matcher matcher = pattern.matcher(extractExpression);
                    while (matcher.find()) {
                        String innerStr = matcher.group(1);
                        TestContextHolder.put(caseInfo.getCaseId(), key,
                                RegexReplaceUtils.extractXmlTagValue(res.getBody().asString(), innerStr));
                    }
                } else {
                    Object value = map.get(key);
                    Object actualValue = res.jsonPath().get(value.toString());
                    TestContextHolder.put(caseInfo.getCaseId(), key, actualValue);
                }
            }
        } catch (Exception e) {
            log.error("extract result failed...");
            throw e;
        }
    }

    public Object parseAssertExpression(String expr, Response response, CaseInfo caseInfo) {
        expr = RegexReplaceUtils.replaceAssertDynamicValue(expr, response, TestContextHolder.getGlobalEnvMap(), caseInfo);
        Pattern pattern = Pattern.compile("([a-zA-Z0-9]+)\\((.*?)\\)");
        Matcher matcher = pattern.matcher(expr);
        if (!matcher.find()) return expr;

        String functionName = matcher.group(1);
        expectedOperationName = expr;
        String params = matcher.group(2);

        Object[] paramsArray = params.contains("[") || params.contains("{") || params.contains("(")
                ? new Object[]{params}
                : new Object[]{params.split(",").length > 0 ? params.split(",")[0].trim() : params};
        return FunctionManager.invoke(functionName, paramsArray);
    }

    @SneakyThrows
    public static void executeCmdGenerateReport() {
        ProcessBuilder builder = new ProcessBuilder();
        String allureResult = Constant.BASE_PATH + "/target/allure-results";
        String allureReport = Constant.BASE_PATH + "/target/allure-report";
        String allureBinPath = Constant.BASE_PATH + "/src/main/resources/allure/allure-2.33.0/bin/allure";

        String os = System.getProperty("os.name").toLowerCase();
        String cmd;
        if (os.contains("win")) {
            cmd = allureBinPath + " generate " + allureResult + " -o " + allureReport;
            builder.command("cmd.exe", "/c", cmd);
        } else {
            log.info("Setting execute permission for allure binary at: {}", allureBinPath);
            Files.setPosixFilePermissions(Paths.get(allureBinPath), PosixFilePermissions.fromString("rwxr-xr-x"));
            log.info("permission set successfully. Executing command: {}", allureBinPath);
            cmd = allureBinPath + " generate " + allureResult + " -o " + allureReport;
            builder.command("sh", "-c", cmd);
        }

        builder.redirectErrorStream(true);
        Process process = builder.start();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            log.info("print cmd result: {}", line);
        }
        process.waitFor();
    }

    @AfterSuite
    public void generateReport() {
        executeCmdGenerateReport();
        TestContextHolder.reset();
        reset();
    }
}