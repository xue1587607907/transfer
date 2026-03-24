package com.guiji.apiautomationfinal.functional.plugins;


import com.guiji.apiautomationfinal.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.testng.Assert;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FunctionManager {
    private static final Map<String, Method> FUNCTION_MAP = new ConcurrentHashMap<>();
    private static final FunctionManager INSTANCE = new FunctionManager();

    @PostConstruct
    public void initFunction() {
        for (Method method : FunctionManager.class.getDeclaredMethods()) {
            FUNCTION_MAP.put(method.getName(), method);
        }
    }

    public static Object invoke(String functionName, Object... args) {
        Method method = FUNCTION_MAP.get(functionName);
        if (method == null) {
            throw new IllegalArgumentException("Function not found: " + functionName);
        }
        try {
            return method.invoke(INSTANCE, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke function: " + functionName, e);
        }
    }

    public String base64Decode(String arg) {
        return new String(Base64.getDecoder().decode(arg));
    }

    public String base64Encode(String arg) {
        return Base64.getEncoder().encodeToString(arg.getBytes());
    }

    public String multiply(Object a, Object b) {
        BigDecimal numA = new BigDecimal(a.toString());
        BigDecimal numB = new BigDecimal(b.toString());
        return numA.multiply(numB).setScale(2, RoundingMode.HALF_UP).toString();
    }

    public String divide(Object a, Object b) {
        BigDecimal numA = new BigDecimal(a.toString());
        BigDecimal numB = new BigDecimal(b.toString());
        return numA.divide(numB, 2, RoundingMode.HALF_UP).toString();
    }

    public String add(Object a, Object b) {
        BigDecimal numA = new BigDecimal(a.toString());
        BigDecimal numB = new BigDecimal(b.toString());
        return numA.add(numB).setScale(2, RoundingMode.HALF_UP).toString();
    }

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public String timestamp() {
        return DateUtils.getCurrentTimeStamp();
    }

    public String time() {
        return DateUtils.getTime();
    }

    public String offsetDate(Object offset) {
        return DateUtils.getOffsetDate(Integer.parseInt(offset.toString()));
    }

    public boolean startsWith(String prefix, String text) {
        return text.startsWith(prefix);
    }

    public boolean endsWith(String suffix, String text) {
        return text.endsWith(suffix);
    }

    public boolean notNull(Object args) {
        Assert.assertNotNull(args, "Value must not be null");
        return true;
    }
}