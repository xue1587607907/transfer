package com.guiji.apiautomationfinal.testcase;


import com.alibaba.fastjson.JSON;
import com.guiji.apiautomationfinal.base.BaseTest;
import com.guiji.apiautomationfinal.constant.Constant;

import com.guiji.apiautomationfinal.entity.CaseInfo;
import com.guiji.apiautomationfinal.listeners.ExcelHandleListener;
import com.guiji.apiautomationfinal.listeners.ExtentTestNGListener;
import com.guiji.apiautomationfinal.listeners.RetryAnalyzerListener;
import com.guiji.apiautomationfinal.utils.ExcelUtils;
import com.guiji.apiautomationfinal.utils.RestAssuredUtils;
import com.guiji.apiautomationfinal.utils.TestReportUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.testng.AllureTestNg;
import io.qameta.allure.testng.Tag;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Epic("API Automation Framework")
@Feature("Quantum Business Api Test")
@Listeners(value = {AllureTestNg.class, RetryAnalyzerListener.class,
        ExtentTestNGListener.class})
public class APITest extends BaseTest {

    @Autowired
    private RestAssuredUtils restAssuredUtils;

    @Autowired
    private ExcelHandleListener excelHandleListener;

    @SneakyThrows
    private Map<String, Object> getRealHttpHeaders(CaseInfo caseInfo) {
        Map<String, Object> envHeaders = runEnv.getPapiHeaders().get(caseInfo.getCountry());
        envHeaders.putAll(runEnv.getPapiHeaders().get("commons"));
        Map<String, Object> headers = new HashMap<>(envHeaders);
        if (caseInfo.getAsyncApi() != null && caseInfo.getAsyncApi().equalsIgnoreCase("Y")) {
            TimeUnit.SECONDS.sleep(15);
        }
        if (caseInfo.getReqHeader() != null) {
            Map<String, Object> header = JSON.parseObject(caseInfo.getReqHeader());
            headers.putAll(header);
            headers.entrySet().removeIf(entry -> "remove".equalsIgnoreCase(entry.getValue().toString()));
        }
        return headers;
    }

    @DataProvider(parallel = true)
    public Object[] getTestData() {
        runEnv.getModules().forEach(moduleName -> ExcelUtils.getExcelDataBySheetNameAsync(
                Constant.DATA_PATH, moduleName, excelHandleListener));
        return excelHandleListener.getCaseInfoList().stream()
                .filter(caseInfo -> caseInfo.getRun().equalsIgnoreCase("Y"))
                .filter(caseInfo -> runEnv.getCountries().contains(caseInfo.getCountry()))
                .toArray();
    }

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = runEnv.getBaseUrl();
    }

    @Story("Quantum Module Api")
    @Tag("Regression")
    @Test(dataProvider = "getTestData")
    public void testApi(CaseInfo caseInfo) {
        try {
            CaseInfo finalCaseInfo = caseInfo;
            prepareCaseDependency(caseInfo.getCaseId(), caseInfo.getDependsOn());
            waitForDependency(caseInfo.getCaseId());

            caseInfo = executeStep("Dynamic Param Replace", () -> paramsReplace(finalCaseInfo));
            Response response = executeStep("Send Http Request", () -> restAssuredUtils.sendRequest(
                    finalCaseInfo.getPath(),
                    finalCaseInfo.getReqMethod(),
                    getRealHttpHeaders(finalCaseInfo),
                    finalCaseInfo.getReqParams(),
                    finalCaseInfo.getContentType()));

            TestReportUtils.addJsonAttachment("response body", response.getBody().asString());
            assertResult(finalCaseInfo, response);
            extractResValueToGlobalMap(response, finalCaseInfo);
            markCaseDone(finalCaseInfo.getCaseId());
        } catch (Exception e) {
            TestReportUtils.setFailureReason(caseInfo, e);
            e.printStackTrace();
            throw new RuntimeException("Test Failed", e);
        } finally {
            markCaseDone(caseInfo.getCaseId());
        }
    }

    public void addLogDetailPrint(CaseInfo caseInfo) {
        org.slf4j.MDC.put("CaseId", Thread.currentThread().getName());
    }
}