package ru.avem.resonance;


public final class Constants {

    public static final class Communication {
        public static final String RS485_DEVICE_NAME = "CP2103 USB to RS-485";
        public static final int BAUDRATE = 38400;
        public static final int DATABITS = 8;
        public static final int STOPBITS = 1;
        public static final int PARITY = 0;
        public static final int WRITE_TIMEOUT = 100;
        public static final int READ_TIMEOUT = 100;
    }

    public static final class Time {
        public static final double MILLS_IN_SEC = 1000.0;
    }

    public static final class Ends {
        public static final String LATR_STARTED = "01";
        public static final String LATR_WAITING = "02";
        public static final String LATR_CONFIG = "70";
        public static final String LATR_STOP_RESET = "03";
        public static final String LATR_UP_END = "81";
        public static final String LATR_DOWN_END = "82";
        public static final String LATR_BOTH_END = "83";
        public static final String LATR_TIME_ENDED = "84";
        public static final String LATR_ZASTRYAL = "85";

        public static final Short OMIK_DOWN_END = 59;
        public static final Short OMIK_UP_END = 55;
        public static final Short OMIK_BOTH_END = 63;
        public static final Short OMIK_NOONE_END = 51;
    }

    public static final class Vfd {
        public static final Short VFD_REVERSE = 1304;
        public static final Short VFD_FORWARD = 1280;
    }

    public enum Avem {
        VOLTAGE_AMP, VOLTAGE_AVERAGE, VOLTAGE_RMS, FREQUENCY
    }
}