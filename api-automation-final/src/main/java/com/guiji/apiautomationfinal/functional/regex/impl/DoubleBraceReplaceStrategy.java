package com.guiji.apiautomationfinal.functional.regex.impl;


import com.guiji.apiautomationfinal.entity.CaseInfo;
import com.guiji.apiautomationfinal.entity.ReplaceContext;
import com.guiji.apiautomationfinal.functional.regex.RegexReplaceStrategy;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoubleBraceReplaceStrategy implements RegexReplaceStrategy {
    private static final Pattern PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    public String replace(ReplaceContext context) {
        if (context.getText() == null) return null;
        Map<String, Object> globalEnvMap = context.getGlobalEnvMap();
        CaseInfo caseInfo = context.getCaseInfo();
        Matcher matcher = PATTERN.matcher(context.getText());
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String realKey = (caseInfo.getDependsOn() == null || caseInfo.getDependsOn().isBlank())
                    ? key
                    : caseInfo.getDependsOn() + "." + key;
            Object replacementObj = globalEnvMap.get(realKey);
            String replacement = (replacementObj != null) ? replacementObj.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
