package com.sunrain.timetablev4.utils;

import java.io.Closeable;
import java.io.IOException;

public class FileUtil {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }
}