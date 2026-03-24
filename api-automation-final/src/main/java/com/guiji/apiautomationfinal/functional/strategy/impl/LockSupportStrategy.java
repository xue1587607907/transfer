package com.guiji.apiautomationfinal.functional.strategy.impl;


import com.guiji.apiautomationfinal.functional.strategy.DependencySyncStrategy;
import com.guiji.apiautomationfinal.functional.strategy.annotation.DependencyStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@Component
@DependencyStrategy("LockSupport")
public class LockSupportStrategy implements DependencySyncStrategy {
    private static final Set<String> DONE_CASES = new CopyOnWriteArraySet<>();
    private static final Set<String> FAILED_CASES = new CopyOnWriteArraySet<>();
    private static final Map<String, Set<String>> CASE_DEPENDS_ON = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> CASE_WAITED_BY = new ConcurrentHashMap<>();
    private static final Map<String, Thread> WAITING_THREADS = new ConcurrentHashMap<>();
    private static boolean PARALLEL;

    @Override
    public void setParallel(boolean parallel) {
        PARALLEL = parallel;
    }

    @Override
    public void prepare(String caseId, String dependsOn) {
        if (!PARALLEL) return;
        if (dependsOn == null || dependsOn.isBlank()) return;
        Set<String> dependsOnSet = new CopyOnWriteArraySet<>();
        String[] arr = dependsOn.split(";");
        for (String dep : arr) {
            String trim = dep.trim();
            if (!trim.isEmpty()) {
                dependsOnSet.add(trim);
            }
        }
        CASE_DEPENDS_ON.put(caseId, dependsOnSet);
        for (String dependCaseId : dependsOnSet) {
            CASE_WAITED_BY.computeIfAbsent(dependCaseId, k -> new CopyOnWriteArraySet<>()).add(caseId);
        }
    }

    @Override
    public void waitFor(String caseId) {
        if (!PARALLEL) return;
        Set<String> dependCases = CASE_DEPENDS_ON.get(caseId);
        if (dependCases == null || dependCases.isEmpty()) return;

        dependCases.removeAll(DONE_CASES);
        if (dependCases.isEmpty()) return;

        WAITING_THREADS.put(caseId, Thread.currentThread());
        try {
            while (!dependCases.isEmpty()) {
                LockSupport.park();
                dependCases.removeAll(DONE_CASES);
                Set<String> failedDepends = new CopyOnWriteArraySet<>(dependCases);
                failedDepends.retainAll(FAILED_CASES);
                if (!failedDepends.isEmpty()) {
                    throw new RuntimeException("Dependency cases failed: " + failedDepends + " for case: " + caseId);
                }
                if (Thread.currentThread().isInterrupted()) {
                    WAITING_THREADS.remove(caseId);
                    throw new RuntimeException("Thread interrupted while waiting for dependencies of case: " + caseId);
                }
            }
        } finally {
            WAITING_THREADS.remove(caseId);
        }
    }

    @Override
    public void markDone(String caseId) {
        if (!PARALLEL) return;
        DONE_CASES.add(caseId);
        Set<String> waitingCases = CASE_WAITED_BY.get(caseId);
        if (waitingCases != null) {
            for (String waitingCaseId : waitingCases) {
                Thread waitingThread = WAITING_THREADS.get(waitingCaseId);
                if (waitingThread != null) {
                    LockSupport.unpark(waitingThread);
                }
            }
        }
    }

    @Override
    public void markFailed(String caseId) {
        if (!PARALLEL) return;
        FAILED_CASES.add(caseId);
        markDone(caseId);
    }

    @Override
    public void reset() {
        DONE_CASES.clear();
        CASE_DEPENDS_ON.clear();
        CASE_WAITED_BY.clear();
        WAITING_THREADS.clear();
    }
}