package com.guiji.apiautomationfinal.listeners;

import com.guiji.apiautomationfinal.context.SpringContextHolder;
import com.guiji.apiautomationfinal.entity.CaseInfo;
import com.guiji.apiautomationfinal.functional.strategy.factory.DependencySyncFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final int MAX_RETRY_COUNT = 2;
    private final ThreadLocal<AtomicInteger> retryCountHolder = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    @Override
    public boolean retry(ITestResult iTestResult) {
        AtomicInteger retryCount = retryCountHolder.get();
        if (iTestResult.getStatus() == ITestResult.FAILURE && retryCount.get() < MAX_RETRY_COUNT) {
            retryCount.incrementAndGet();
            String methodName = iTestResult.getMethod().getMethodName();
            log.error("====>Case {} Failed, Start the {} retry", methodName, retryCount.get());
            return true;
        } else if (iTestResult.getStatus() == ITestResult.FAILURE) {
            String caseId = extractCaseIdFromResult(iTestResult);
            DependencySyncFactory dependencySyncFactory = SpringContextHolder.getBean(DependencySyncFactory.class);
            dependencySyncFactory.getDefaultStrategy().markFailed(caseId);
        }
        return false;
    }

    private String extractCaseIdFromResult(ITestResult iTestResult) {
        Object[] parameters = iTestResult.getParameters();
        if (parameters != null && parameters.length > 0 && parameters[0] instanceof CaseInfo caseInfo) {
            log.error("====>Case {} Failed after retrying {} times", caseInfo.getCaseId(), retryCountHolder.get().get());
            return caseInfo.getCaseId();
        }
        return iTestResult.getMethod().getMethodName();
    }

    public void resetRetryAccount() {
        retryCountHolder.remove();
    }
}