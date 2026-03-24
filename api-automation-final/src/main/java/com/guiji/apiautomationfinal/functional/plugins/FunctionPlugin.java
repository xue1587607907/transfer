package com.guiji.apiautomationfinal.functional.plugins;


public interface FunctionPlugin {
    String getName();
    Object execute(Object... args);
}