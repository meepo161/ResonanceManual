package ru.avem.resonance.communication.devices.pm130;

import com.ucicke.k2mod.modbus.ModbusException;
import com.ucicke.k2mod.modbus.procimg.InputRegister;
import ru.avem.resonance.communication.modbus.ModbusConnection;
import ru.avem.resonance.communication.devices.DeviceController;

import java.nio.ByteBuffer;
import java.util.Observer;


public class PM130Controller implements DeviceController {
//    private static final short I1_REGISTER = 13318; // мгновенные
//    private static final short VL1_REGISTER = 13372;
//    private static final short P_REGISTER = 13696;

    private static final short I1_REGISTER = 13958; // за 1 секунду
    private static final short VL1_REGISTER = 14012;
    private static final short P_REGISTER = 14336;
    private static final short F_REGISTER = 14468;

    private static final int CONVERT_BUFFER_SIZE = 4;
    private static final int U_MULTIPLIER = 1;
    private static final float U_DIVIDER = 10.f;
    private static final float I_DIVIDER = 100.f;
    private static final int I_MULTIPLIER = 1000;
    private static final int NUM_OF_WORDS_IN_REGISTER = 2;
    private static final short NUM_OF_REGISTERS = 3 * NUM_OF_WORDS_IN_REGISTER;

    private PM130Model model;
    private ModbusConnection modbusConnection;
    public byte readAttempt = NUMBER_OF_READ_ATTEMPTS;
    public byte readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    public byte writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    public byte writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    private boolean isNeedToRead;
    private byte address;

    public PM130Controller(int address, Observer observer, ModbusConnection controller, int deviceID) {
        this.address = (byte) address;
        model = new PM130Model(observer, deviceID);
        modbusConnection = controller;
    }

    @Override
    public void resetAllAttempts() {
        resetReadAttempts();
        resetReadAttemptsOfAttempts();
        resetWriteAttempts();
        resetWriteAttemptsOfAttempts();
    }

    public void resetReadAttempts() {
        readAttempt = NUMBER_OF_READ_ATTEMPTS;
    }

    private void resetReadAttemptsOfAttempts() {
        readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    }

    public void resetWriteAttempts() {
        writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    }

    private void resetWriteAttemptsOfAttempts() {
        writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    }

    @Override
    public void read(Object... args) {
        int type = (int) args[0];
        if (type == 0) {
            if (thereAreReadAttempts()) {
                readAttempt--;

                try {
                    InputRegister[] readInputRegisters = modbusConnection.readInputRegisters(address, I1_REGISTER, 1, 4);

                    if (readInputRegisters.length == 3) {
                        model.setReadResponding(true);
                        model.setI1(convertUINTtoINT(readInputRegisters[0].toInt()) * I_MULTIPLIER / I_DIVIDER / 1000);
                        model.setI2(convertUINTtoINT(readInputRegisters[1].toInt()) * I_MULTIPLIER / I_DIVIDER / 1000);
                        model.setI3(convertUINTtoINT(readInputRegisters[2].toInt()) * I_MULTIPLIER / I_DIVIDER / 1000);
                        resetReadAttempts();
                        resetReadAttemptsOfAttempts();
                    } else {
                        read(args);
                    }
                } catch (ModbusException e) {
                    read(args);
                }
            } else {
                readAttemptOfAttempt--;
                if (readAttemptOfAttempt <= 0) {
                    model.setReadResponding(false);
                } else {
                    resetReadAttempts();
                }
            }
        } else if (type == 1) {
            if (thereAreReadAttempts()) {
                readAttempt--;

                try {
                    InputRegister[] readInputRegisters = modbusConnection.readInputRegisters(address, VL1_REGISTER, 1, 4);

                    if (readInputRegisters.length == 3) {
                        model.setReadResponding(true);
                        model.setV1(convertUINTtoINT(readInputRegisters[0].toInt()) * U_MULTIPLIER / U_DIVIDER);
                        model.setV2(convertUINTtoINT(readInputRegisters[1].toInt()) * U_MULTIPLIER / U_DIVIDER);
                        model.setV3(convertUINTtoINT(readInputRegisters[2].toInt()) * U_MULTIPLIER / U_DIVIDER);
                        resetReadAttempts();
                        resetReadAttemptsOfAttempts();
                    } else {
                        read(args);
                    }
                } catch (ModbusException e) {
                    read(args);
                }
            } else {
                readAttemptOfAttempt--;
                if (readAttemptOfAttempt <= 0) {
                    model.setReadResponding(false);
                } else {
                    resetReadAttempts();
                }
            }
        }
    }

    @Override
    public void write(Object... args) {
    }

    private long convertUINTtoINT(int i) {
        ByteBuffer convertBuffer = ByteBuffer.allocate(CONVERT_BUFFER_SIZE);
        convertBuffer.clear();
        convertBuffer.putInt(i);
        convertBuffer.flip();
        short rightSide = convertBuffer.getShort();
        short leftSide = convertBuffer.getShort();
        convertBuffer.clear();
        convertBuffer.putShort(leftSide);
        convertBuffer.putShort(rightSide);
        convertBuffer.flip();
        int preparedInt = convertBuffer.getInt();
        return (long) preparedInt & 0xFFFFFFFFL;
    }

    private int convertMidEndianINTtoINT(int i) {
        ByteBuffer convertBuffer = ByteBuffer.allocate(CONVERT_BUFFER_SIZE);
        convertBuffer.clear();
        convertBuffer.putInt(i);
        convertBuffer.flip();
        short rightSide = convertBuffer.getShort();
        short leftSide = convertBuffer.getShort();
        convertBuffer.clear();
        convertBuffer.putShort(leftSide);
        convertBuffer.putShort(rightSide);
        convertBuffer.flip();
        return convertBuffer.getInt();
    }

    @Override
    public boolean thereAreReadAttempts() {
        return readAttempt > 0;
    }

    @Override
    public boolean thereAreWriteAttempts() {
        return writeAttempt > 0;
    }

    @Override
    public boolean isNeedToRead() {
        return isNeedToRead;
    }

    @Override
    public void setNeedToRead(boolean isNeedToRead) {
        if (isNeedToRead) {
            model.resetResponding();
        }
        this.isNeedToRead = isNeedToRead;
    }

    @Override
    public void resetAllDeviceStateOnAttempts() {
        readAttempt = 1;
        readAttemptOfAttempt = 0;
        writeAttempt = 1;
        writeAttemptOfAttempt = 0;
    }
}