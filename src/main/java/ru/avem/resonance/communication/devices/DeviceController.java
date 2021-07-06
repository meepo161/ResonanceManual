package ru.avem.resonance.communication.devices;

public interface DeviceController {
    int INPUT_BUFFER_SIZE = 256;
    byte NUMBER_OF_READ_ATTEMPTS = 5;
    byte NUMBER_OF_WRITE_ATTEMPTS = 5;

    byte NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS = 5;
    byte NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS = 5;

    int PM130_ID = 41;
    int AVEM_ID = 11;
    int PR200_ID = 4;
    int LATR_ID = 240;
    int DELTACP2000_ID = 91;
    int KILOAVEM_ID = 21;

    //PM130-41; ARN-250; Delta-91;PR-4;SI8-71; Avem-11; KILOAVEM_ID = 21




    void read(Object... args);

    boolean thereAreReadAttempts();

    void write(Object... args);

    boolean thereAreWriteAttempts();

    boolean isNeedToRead();

    void setNeedToRead(boolean isNeedToRead);

    void resetAllAttempts();

    void resetAllDeviceStateOnAttempts();
}