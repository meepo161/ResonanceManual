package ru.avem.resonance.communication.devices.deltaC2000;

import com.ucicke.k2mod.modbus.ModbusException;
import com.ucicke.k2mod.modbus.procimg.InputRegister;
import com.ucicke.k2mod.modbus.procimg.SimpleRegister;
import ru.avem.resonance.communication.modbus.ModbusConnection;
import ru.avem.resonance.communication.devices.DeviceController;

import java.nio.ByteBuffer;
import java.util.Observer;

public class DeltaCP2000Controller implements DeviceController {
    public static final short ERRORS_REGISTER = 0x2100;
    public static final short STATUS_REGISTER = 0x2101;
    public static final short ENDS_STATUS_REGISTER = 0x041A;
    public static final short END_UP_CONTROL_REGISTER = 0x0405;
    public static final short END_DOWN_CONTROL_REGISTER = 0x0406;
    public static final short CURRENT_FREQUENCY_INPUT_REGISTER = 0x2103;
    public static final short CONTROL_REGISTER = 0x2000;
    public static final short CURRENT_FREQUENCY_OUTPUT_REGISTER = 0x2001;
    public static final short MAX_FREQUENCY_REGISTER = 0x0100;
    public static final short NOM_FREQUENCY_REGISTER = 0x0101;
    public static final short MAX_VOLTAGE_REGISTER = 0x0102;
    public static final short POINT_1_FREQUENCY_REGISTER = 0x0103;
    public static final short POINT_1_VOLTAGE_REGISTER = 0x0104;
    public static final short POINT_2_FREQUENCY_REGISTER = 0x0105;
    public static final short POINT_2_VOLTAGE_REGISTER = 0x0106;

    private DeltaCP2000Model model;
    private int modbusAddress;
    private ModbusConnection modbusConnection;
    public byte readAttempt = NUMBER_OF_READ_ATTEMPTS;
    public byte readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    public byte writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    public byte writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    private boolean isNeedToRead;

    public DeltaCP2000Controller(int modbusAddress, Observer observer, ModbusConnection controller, int deviceID) {
        this.modbusAddress = (byte) modbusAddress;
        model = new DeltaCP2000Model(observer, deviceID);
        this.modbusConnection = controller;
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
                    InputRegister[] readInputRegisters = modbusConnection.readInputRegisters(modbusAddress, ERRORS_REGISTER, 1, 4);

                    if (readInputRegisters.length == 4) {
                        model.setReadResponding(true);
                        model.setErrors((byte) readInputRegisters[0].toInt());
                        model.setStatusVfd((byte) readInputRegisters[1].toInt());
                        model.setCurrentFrequency((byte) readInputRegisters[3].toInt());
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
                    InputRegister[] readInputRegisters = modbusConnection.readInputRegisters(modbusAddress, ENDS_STATUS_REGISTER, 1, 4);

                    if (readInputRegisters.length == 1) {
                        model.setReadResponding(true);
                        model.setEndsStatus((byte) readInputRegisters[0].toInt());
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
        short register = (short) args[0];

        byte[] value = new byte[4]; // reduant
        if (args[1] instanceof Integer) {
            value = intToByteArray((int) args[1]);
        } else if (args[1] instanceof Float) {
            value = floatToByteArray((float) args[1]);
        }

        if (thereAreWriteAttempts()) {
            writeAttempt--;

            try {
                modbusConnection.writeSingleRegister(modbusAddress, register, new SimpleRegister(value[0], value[1], value[2], value[3]), 4);
                model.setWriteResponding(true);
                resetWriteAttempts();
            } catch (ModbusException e) {
                write(args);
            }
        } else {
            model.setWriteResponding(false);
        }
    }

    private byte[] intToByteArray(int i) {
        ByteBuffer convertBuffer = ByteBuffer.allocate(4);
        convertBuffer.clear();
        return convertBuffer.putInt(i).array();
    }

    private byte[] floatToByteArray(float f) {
        ByteBuffer convertBuffer = ByteBuffer.allocate(4);
        convertBuffer.clear();
        return convertBuffer.putFloat(f).array();
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