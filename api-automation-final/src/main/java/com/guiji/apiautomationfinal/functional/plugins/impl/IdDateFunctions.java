package com.guiji.apiautomationfinal.functional.plugins.impl;

import com.guiji.apiautomationfinal.functional.plugins.FunctionPlugin;
import com.guiji.apiautomationfinal.utils.DateUtils;

import java.util.UUID;

public class IdDateFunctions {
    public static class Uuid implements FunctionPlugin {
        @Override
        public String getName() {
            return "uuid";
        }

        @Override
        public Object execute(Object... args) {
            return UUID.randomUUID().toString();
        }
    }

    public static class CurrentTimeStamp implements FunctionPlugin {
        @Override
        public String getName() {
            return "timestamp";
        }

        @Override
        public Object execute(Object... args) {
            return DateUtils.getCurrentTimeStamp();
        }
    }

    public static class OffsetDate implements FunctionPlugin {
        @Override
        public String getName() {
            return "offsetDate";
        }

        @Override
        public Object execute(Object... args) {
            return DateUtils.getOffsetDate(
                    args[0].toString().isEmpty() ? 0 : Integer.parseInt(args[0].toString())
            );
        }
    }
}
