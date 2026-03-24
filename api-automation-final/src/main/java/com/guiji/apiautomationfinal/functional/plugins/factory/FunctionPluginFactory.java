package com.guiji.apiautomationfinal.functional.plugins.factory;


import com.guiji.apiautomationfinal.functional.plugins.FunctionPlugin;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class FunctionPluginFactory {
    private static final Map<String, FunctionPlugin> FUNCTION_PLUGIN_MAP = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadPlugins() {
        ServiceLoader.load(FunctionPlugin.class)
                .forEach(plugin -> FUNCTION_PLUGIN_MAP.put(plugin.getName(), plugin));
        log.info("Load Function Plugins: {}", FUNCTION_PLUGIN_MAP.keySet());
    }

    public static Object execute(String functionName, Object... args) {
        FunctionPlugin plugin = FUNCTION_PLUGIN_MAP.get(functionName);
        if (plugin == null) {
            throw new IllegalArgumentException("No plugin found for function: " + functionName);
        }
        return plugin.execute(args);
    }

}