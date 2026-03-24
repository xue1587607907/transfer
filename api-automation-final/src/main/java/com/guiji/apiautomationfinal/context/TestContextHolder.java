package com.guiji.apiautomationfinal.context;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestContextHolder {
    private static final Map<String, Object> GLOBAL_ENV_MAP = new ConcurrentHashMap<>();

    static {
        GLOBAL_ENV_MAP.put("uid", "2");
    }

    public static void put(String caseId, String key, Object value) {
        String realKey = buildKey(caseId, key);
        GLOBAL_ENV_MAP.computeIfAbsent(realKey, k -> value);
    }

    public static Object get(String caseId, String key) {
        String realKey = buildKey(caseId, key);
        return GLOBAL_ENV_MAP.get(realKey);
    }

    public static void reset() {
        GLOBAL_ENV_MAP.clear();
    }

    public static Map<String, Object> getGlobalEnvMap() {
        return GLOBAL_ENV_MAP;
    }

    public static String buildKey(String caseId, String key) {
        return caseId + "." + key;
    }
}
