package com.dumbster.smtp;

public class StringUtil {
    public static String longString(int size) {
        StringBuilder b = new StringBuilder();
        for(int i=0; i<size; i++)
            b.append("X");
        return b.toString();
    }
}
