package com.guiji.apiautomationfinal.listeners;


import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class RetryAnalyzerListener implements IInvokedMethodListener {
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            method.getTestMethod().setRetryAnalyzerClass(RetryAnalyzer.class);
        }
    }
}