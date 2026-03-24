package com.guiji.apiautomationfinal.functional.strategy;


/**
 * 初始化测试用例依赖关系
 */
public interface DependencySyncStrategy {
    void prepare(String caseId, String dependsOn);

    /**
     * 等待依赖用例执行完成后再执行当前用例
     */
    void waitFor(String caseId);

    /**
     * 标记测试用例为完成状态，并唤醒等待的线程
     */
    void markDone(String caseId);

    void markFailed(String caseId);

    /**
     * 重置所有状态
     */
    void reset();

    void setParallel(boolean isParallel);
}