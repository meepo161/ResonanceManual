package ru.avem.resonance.communication.modbus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

class LogAnalyzer {
    private static int write;
    private static int success;

    static void addWrite() {
        write++;
    }

    static void addSuccess() {
        success++;
        DateFormat df = new SimpleDateFormat("mm:ss");
        System.out.printf(
                "%s Записано: %d, Удач: %d, Разница: %d, Процент неудач: %.4f\n",
                df.format(System.currentTimeMillis()), write, success, write - success, (write - success) / (float) write);
    }
}
