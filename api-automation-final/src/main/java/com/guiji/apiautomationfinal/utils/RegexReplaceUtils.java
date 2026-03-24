package com.guiji.apiautomationfinal.utils;


import com.guiji.apiautomationfinal.entity.CaseInfo;

import com.guiji.apiautomationfinal.entity.ReplaceContext;
import com.guiji.apiautomationfinal.functional.regex.RegexReplaceStrategy;
import com.guiji.apiautomationfinal.functional.regex.impl.DollarBraceReplaceStrategy;
import com.guiji.apiautomationfinal.functional.regex.impl.DoubleBraceReplaceStrategy;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexReplaceUtils {
    private static final List<RegexReplaceStrategy> STRATEGIES = Arrays.asList(
            new DollarBraceReplaceStrategy(), // 优先级解析 ${}
            new DoubleBraceReplaceStrategy()
    );

    private static final Pattern EXPECTED_RESULT_REGEX = Pattern.compile("\\$\\{[^}]+}|\\{[^}]+}|\\{\\{[^}]+}}");

    public static String regexReplace(ReplaceContext context) {
        if (context == null || context.getText() == null) return null;
        String result = context.getText();
        for (RegexReplaceStrategy strategy : STRATEGIES) {
            ReplaceContext newContext = new ReplaceContext(result, context.getGlobalEnvMap(), context.getCaseInfo());
            if (strategy.supports(newContext)) {
                result = strategy.replace(newContext);
            }
        }
        return result;
    }

    public static String replaceByStrategy(ReplaceContext context, Class<? extends RegexReplaceStrategy> strategyClass) {
        if (context == null || context.getText() == null) return null;
        for (RegexReplaceStrategy strategy : STRATEGIES) {
            if (strategy.getClass().equals(strategyClass)) {
                return strategy.replace(context);
            }
        }
        return context.getText();
    }

    public static String extractXmlTagValue(String text, String tagName) {
        String regex = "<" + tagName + ">(.*?)</" + tagName + ">";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static boolean isNeedConvertAndCalc(String input) {
        return EXPECTED_RESULT_REGEX.matcher(input).find();
    }

    public static String replaceAssertDynamicValue(String input, Response response, Map<String, Object> map, CaseInfo caseInfo) {
        input = replacePlaceholders(input, "#\\{([^}]+)\\}", key -> response.jsonPath().getString(key));
        return replacePlaceholders(input, "\\{\\{([^}]+)\\}}", key -> map.get(caseInfo.getDependsOn() + "." + key));
    }

    private static String replacePlaceholders(String input, String regex, Function<String, Object> valueProvider) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = String.valueOf(valueProvider.apply(key));
            matcher.appendReplacement(result, value != null ? value : "");
        }
        matcher.appendTail(result);
        return result.toString();
    }
}