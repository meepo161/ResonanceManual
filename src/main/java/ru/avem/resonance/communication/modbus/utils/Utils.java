package ru.avem.resonance.communication.modbus.utils;

public class Utils {
    public static String toHexString(byte[] src) {
        return toHexString(src, src.length);
    }

    public static String toHexString(byte[] src, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String s = Integer.toHexString(src[i] & 0xFF);
            if (s.length() < 2) {
                builder.append(0);
            }
            builder.append(s).append(' ');
        }
        return builder.toString().toUpperCase().trim();
    }

    public static String toHexString(byte src) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1; i++) {
            String s = Integer.toHexString(src & 0xFF);
            if (s.length() < 2) {
                builder.append(0);
            }
            builder.append(s).append(' ');
        }
        return builder.toString().toUpperCase().trim();
    }
}
