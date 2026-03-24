package com.guiji.apiautomationfinal.functional.regex.impl;

import com.guiji.apiautomationfinal.context.TestContextHolder;
import com.guiji.apiautomationfinal.entity.ReplaceContext;
import com.guiji.apiautomationfinal.functional.plugins.FunctionManager;
import com.guiji.apiautomationfinal.functional.regex.RegexReplaceStrategy;
import com.guiji.apiautomationfinal.utils.RegexReplaceUtils;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DollarBraceReplaceStrategy implements RegexReplaceStrategy {
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9]+)\\((.*?)\\)}", Pattern.DOTALL);
    private static final Pattern MATCH_MT_PATTERN = Pattern.compile("\\$\\$\\{([a-zA-Z0-9]+)\\((.*?)\\)}", Pattern.DOTALL);

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    public String replace(ReplaceContext context) {
        if (context.getText() == null) return null;
        StringBuilder sb = new StringBuilder();
        Matcher matcher = (context.getCaseInfo().getMtFormat() != null)
                ? MATCH_MT_PATTERN.matcher(context.getText())
                : PATTERN.matcher(context.getText());
        while (matcher.find()) {
            String functionName = matcher.group(1);
            String arguments = matcher.group(3);
            if (context.getCaseInfo().getMtFormat() != null) {
                Object result = FunctionManager.invoke(functionName, arguments);
                TestContextHolder.getGlobalEnvMap().put("message-" + context.getCaseInfo().getCaseId(), result);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(result.toString()));
            } else {
                Object[] args = new Object[0];
                if (arguments != null && !arguments.isBlank()) {
                    String resolvedParams = RegexReplaceUtils.regexReplace(
                            new ReplaceContext(arguments, context.getGlobalEnvMap(),
                                    context.getCaseInfo()));
                    args = Arrays.stream(resolvedParams.split(";"))
                            .map(String::trim)
                            .toArray();
                }
                Object result = FunctionManager.invoke(functionName, args);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(result.toString()));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
