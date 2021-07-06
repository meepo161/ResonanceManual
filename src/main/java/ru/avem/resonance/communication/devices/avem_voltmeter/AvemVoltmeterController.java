package ru.avem.resonance.communication.devices.avem_voltmeter;

import com.ucicke.k2mod.modbus.ModbusException;
import com.ucicke.k2mod.modbus.procimg.InputRegister;
import com.ucicke.k2mod.modbus.procimg.SimpleRegister;
import ru.avem.resonance.communication.modbus.ModbusConnection;
import ru.avem.resonance.communication.devices.DeviceController;

import java.nio.ByteBuffer;
import java.util.Observer;

public class AvemVoltmeterController implements DeviceController {
    private static final short U_AMP_REGISTER = 0;
    private static final short U_RMS_REGISTER = 2;
    public static final short CHANGE_SHOW_VALUE = 108;

    private AvemVoltmeterModel model;
    private int modbusAddress;
    private ModbusConnection modbusConnection;
    public byte readAttempt = NUMBER_OF_READ_ATTEMPTS;
    public byte readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    public byte writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    public byte writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    private boolean isNeedToRead;

    public AvemVoltmeterController(int modbusAddress, Observer observer, ModbusConnection modbusConnection, int id) {
        this.modbusAddress = modbusAddress;
        model = new AvemVoltmeterModel(observer, id);
        this.modbusConnection = modbusConnection;
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
                    InputRegister[] readInputRegisters = modbusConnection.readInputRegisters(modbusAddress, U_AMP_REGISTER, 1, 4);

                    if (readInputRegisters.length == 1) {
                        model.setReadResponding(true);
                        model.setUAMP((byte) readInputRegisters[0].toInt());
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
                    InputRegister[] readInputRegisters = modbusConnection.readInputRegisters(modbusAddress, U_RMS_REGISTER, 1, 4);

                    if (readInputRegisters.length == 1) {
                        model.setReadResponding(true);
                        model.setURMS(readInputRegisters[0].toFloat());
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