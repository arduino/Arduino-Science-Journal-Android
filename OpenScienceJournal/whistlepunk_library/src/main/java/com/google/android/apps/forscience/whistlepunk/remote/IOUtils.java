package com.google.android.apps.forscience.whistlepunk.remote;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

@SuppressWarnings({"WeakerAccess", "unused", "RedundantSuppression"})
public class IOUtils {

    public static void safeClose(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable ignored) {
            }
        }
    }

    public static void safeClose(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Throwable ignored) {
            }
        }
    }

    public static void safeClose(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Throwable ignored) {
            }
        }
    }

    public static void safeClose(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (Throwable ignored) {
            }
        }
    }

    public static void pour(File source, File target) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(target);
            pour(inputStream, outputStream);
        } finally {
            safeClose(outputStream);
            safeClose(inputStream);
        }
    }

    public static void pour(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int l;
        while ((l = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, l);
        }
    }

    public static void pourWithInterruptionChecking(InputStream inputStream, OutputStream outputStream) throws InterruptedException, IOException {
        byte[] buffer = new byte[1024];
        int l;
        while ((l = inputStream.read(buffer)) != -1) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            outputStream.write(buffer, 0, l);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
    }

    public static byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            pour(inputStream, outputStream);
        } finally {
            safeClose(outputStream);
        }
        return outputStream.toByteArray();
    }

    public static byte[] readAll(File file) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return readAll(inputStream);
        } finally {
            safeClose(inputStream);
        }
    }

    public static String readAllAsString(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        int l;
        char[] buffer = new char[1024];
        while ((l = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, l);
        }
        return builder.toString();
    }

    public static String readAllAsString(File file) throws IOException {
        Reader reader = null;
        try {
            reader = new FileReader(file);
            return readAllAsString(reader);
        } finally {
            safeClose(reader);
        }
    }

    public static void writeAll(byte[] bytes, File file) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
        } finally {
            safeClose(outputStream);
        }
    }

    public static void writeAll(String str, File file) throws IOException {
        Writer writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(str);
        } finally {
            safeClose(writer);
        }
    }

    public static void deltree(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File aux : files) {
                    deltree(aux);
                }
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

}
