package com.guiji.apiautomationfinal.functional.strategy.impl;


import com.guiji.apiautomationfinal.entity.CaseInfo;
import com.guiji.apiautomationfinal.functional.strategy.DependencySyncStrategy;
import com.guiji.apiautomationfinal.functional.strategy.annotation.DependencyStrategy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@DependencyStrategy("CountDownLatch")
public class CountDownLatchStrategy implements DependencySyncStrategy {
    private static final long DEFAULT_TIMEOUT = 10;
    private static boolean PARALLEL;
    private static final Map<String, CountDownLatch> LATCH_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> CASE_DEPENDS_ON = new ConcurrentHashMap<>();

    @Override
    public void setParallel(boolean parallel) {
        PARALLEL = parallel;
    }

    @Override
    public void prepare(String caseId, String dependsOn) {
        if (!PARALLEL) return;
        LATCH_MAP.computeIfAbsent(caseId, k -> new CountDownLatch(1));
        if (dependsOn != null && !dependsOn.isBlank()) {
            Set<String> dependCaseIds = Arrays.stream(dependsOn.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(CopyOnWriteArraySet::new));
            CASE_DEPENDS_ON.put(caseId, dependCaseIds);
        } else {
            CASE_DEPENDS_ON.put(caseId, new CopyOnWriteArraySet<>());
        }
        log.error("CASE_DEPENDS_ON: {}", CASE_DEPENDS_ON);
    }

    @SneakyThrows
    @Override
    public void waitFor(String caseId) {
        if (!PARALLEL) return;
        Set<String> dependCases = CASE_DEPENDS_ON.get(caseId);
        if (dependCases == null || dependCases.isEmpty()) return;
        log.info("Case {} is waiting for dependencies {}, marking as done", caseId, dependCases);

        for (String dependCaseId : dependCases) {
            CountDownLatch latch = LATCH_MAP.get(dependCaseId);
            if (latch == null) {
                log.warn("No latch found for case: {}, this should not happen. Check prepare method.", caseId);
                return;
            }
            try {
                boolean await = latch.await(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
                if (!await) throw new RuntimeException("Timeout while waiting for dependency: " + caseId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting for dependency: " + caseId, e);
            }
        }
    }

    @Override
    public void markDone(String caseId) {
        if (!PARALLEL) return;
        CountDownLatch latch = LATCH_MAP.get(caseId);
        if (latch != null) {
            latch.countDown();
            log.info("Case {} marked as done, latch count: {}", caseId, latch.getCount());
        }
    }

    @Override
    public void markFailed(String caseId) {
        if (!PARALLEL) return;
        markDone(caseId);
    }

    @Override
    public void reset() {
        log.error("LATCH_MAP: {}", LATCH_MAP);
        LATCH_MAP.clear();
        CASE_DEPENDS_ON.clear();
    }

    // 全局预注册 latch
    public void prePrepareAllLatches(List<CaseInfo> caseInfos) {
        if (!PARALLEL) return;
        for (CaseInfo caseInfo : caseInfos) {
            LATCH_MAP.computeIfAbsent(caseInfo.getCaseId(), k -> new CountDownLatch(1));
            log.info("Pre-preparing latch for case: {}", caseInfo.getCaseId());
        }
    }
}