package com.guiji.apiautomationfinal.functional.regex;


import com.guiji.apiautomationfinal.entity.ReplaceContext;

import java.util.regex.Pattern;

public interface RegexReplaceStrategy {
    Pattern getPattern();
    String replace(ReplaceContext context);

    default boolean supports(ReplaceContext context) {
        if (context == null || context.getText() == null) return false;
        return getPattern().matcher(context.getText()).find();
    }
}