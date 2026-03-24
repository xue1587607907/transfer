package com.guiji.apiautomationfinal.entity;


import lombok.Getter;

import java.util.Map;

@Getter
public class ReplaceContext {
    private final String text;
    private final Map<String, Object> globalEnvMap;
    private final CaseInfo caseInfo;

    public ReplaceContext(String text, Map<String, Object> globalEnvMap, CaseInfo caseInfo) {
        this.text = text;
        this.globalEnvMap = globalEnvMap;
        this.caseInfo = caseInfo;
    }
}
