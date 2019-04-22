package com.sunrain.timetablev4.utils;

import androidx.annotation.Nullable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZipUtil {

    public static String zipString(String unzip) {
        Deflater deflater = new Deflater(9);
        deflater.setInput(unzip.getBytes());
        deflater.finish();

        final byte[] bytes = new byte[256];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);

        while (!deflater.finished()) {
            int length = deflater.deflate(bytes);
            outputStream.write(bytes, 0, length);
        }

        deflater.end();
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_PADDING);
    }

    @Nullable
    public static String unzipString(String zip) {
        byte[] decode;
        try {
            decode = Base64.decode(zip, Base64.NO_PADDING);
        } catch (IllegalArgumentException e) {
            return null;
        }

        Inflater inflater = new Inflater();
        inflater.setInput(decode);

        final byte[] bytes = new byte[256];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);

        try {
            while (!inflater.finished()) {
                int length = inflater.inflate(bytes);
                outputStream.write(bytes, 0, length);
            }
        } catch (DataFormatException e) {
            e.printStackTrace();
            return null;
        } finally {
            inflater.end();
        }

        return outputStream.toString();
    }
}
