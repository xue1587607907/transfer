package com.guiji.apiautomationfinal.listeners;


import io.qameta.allure.model.Status;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.TestResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllureTestResultModifier implements TestLifecycleListener {
    @Override
    public void afterTestStop(final TestResult result) {
        if (result.getStatus() == Status.BROKEN) {
            result.setStatus(Status.FAILED);
        }
    }
}