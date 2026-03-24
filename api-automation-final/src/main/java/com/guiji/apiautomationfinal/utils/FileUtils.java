package com.guiji.apiautomationfinal.utils;


import java.io.File;
import java.nio.file.Files;

public class FileUtils {
    public static void deleteDirectory(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteDirectory(f);
                }
            }
        }
        file.delete();
    }
}