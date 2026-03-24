package com.guiji.apiautomationfinal.functional.plugins.impl;


import com.guiji.apiautomationfinal.functional.plugins.FunctionPlugin;

import java.util.Base64;

public class EncodingFunctions  {
    public static class Base64Decode implements FunctionPlugin {
        @Override
        public String getName() {
            return "base64Decode";
        }

        @Override
        public Object execute(Object... args) {
            return new String(Base64.getDecoder().decode(args[0].toString()));
        }
    }

    public static class Base64Encode implements FunctionPlugin {
        @Override
        public String getName() {
            return "base64Encode";
        }

        @Override
        public Object execute(Object... args) {
            return Base64.getEncoder().encodeToString(args[0].toString().getBytes());
        }
    }
}
