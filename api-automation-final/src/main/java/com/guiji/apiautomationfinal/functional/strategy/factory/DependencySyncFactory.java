package com.guiji.apiautomationfinal.functional.strategy.factory;


import com.guiji.apiautomationfinal.functional.strategy.DependencySyncStrategy;
import com.guiji.apiautomationfinal.functional.strategy.annotation.DependencyStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DependencySyncFactory {
    private final Map<String, DependencySyncStrategy> strategyMap = new ConcurrentHashMap<>();

    @Autowired
    public DependencySyncFactory(Map<String, DependencySyncStrategy> strategies) {
        for (DependencySyncStrategy strategy : strategies.values()) {
            DependencyStrategy annotation = strategy.getClass().getAnnotation(DependencyStrategy.class);
            if (annotation != null) {
                strategyMap.put(annotation.value(), strategy);
            }
        }
    }

    public DependencySyncStrategy getStrategy(String strategyName) {
        DependencySyncStrategy strategy = strategyMap.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for name: " + strategyName);
        }
        return strategy;
    }

    public DependencySyncStrategy getDefaultStrategy() {
        // return getStrategy("LockSupport");
        return getStrategy("CountDownLatch");
    }
}