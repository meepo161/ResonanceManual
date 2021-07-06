package ru.avem.resonance.communication.devices.owen_pr;

import com.ucicke.k2mod.modbus.ModbusException;
import com.ucicke.k2mod.modbus.procimg.InputRegister;
import com.ucicke.k2mod.modbus.procimg.SimpleRegister;
import ru.avem.resonance.communication.modbus.ModbusConnection;
import ru.avem.resonance.communication.devices.DeviceController;

import java.util.Observer;

public class OwenPRController implements DeviceController {
    public static final short STATES_PROTECTIONS_REGISTER = 513;
    public static final short MODE_REGISTER = 514;
    public static final short STATES_BUTTONS_REGISTER = 515;
    public static final short KMS1_REGISTER = 516;
    public static final short KMS2_REGISTER = 517;
    public static final short RESET_DOG = 512;
    public static final short RES = 518;

    private static final int NUM_OF_WORDS_IN_REGISTER = 1;
    private static final short NUM_OF_REGISTERS = 2 * NUM_OF_WORDS_IN_REGISTER;

    private OwenPRModel model;
    private int modbusAddress;
    private ModbusConnection modbusConnection;
    public byte readAttempt = NUMBER_OF_READ_ATTEMPTS;
    public byte readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    public byte writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    public byte writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    private boolean isNeedToRead;

    public OwenPRController(int modbusAddress, Observer observer, ModbusConnection modbusConnection, int id) {
        this.modbusAddress = modbusAddress;
        model = new OwenPRModel(observer, id);
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
                    InputRegister[] readInputRegisters = modbusConnection.readInputRegisters(modbusAddress, STATES_PROTECTIONS_REGISTER, NUM_OF_REGISTERS, 2);

                    if (readInputRegisters.length == 2) {
                        model.setReadResponding(true);
                        model.setStatesProtections(readInputRegisters[0].toShort());
                        model.setMode(readInputRegisters[1].toShort());
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
                    InputRegister[] readInputRegisters = modbusConnection.readInputRegisters(modbusAddress, STATES_BUTTONS_REGISTER, NUM_OF_REGISTERS, 2);

                    if (readInputRegisters.length == 2) {
                        model.setReadResponding(true);
                        model.setStatesButtons(readInputRegisters[0].toShort());
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
        short value = (short) args[1];

        if (thereAreWriteAttempts()) {
            writeAttempt--;

            try {
                modbusConnection.writeSingleRegister(modbusAddress, register, new SimpleRegister(value), 2);
                model.setWriteResponding(true);
                resetWriteAttempts();
            } catch (ModbusException e) {
                write(args);
            }
        } else {
            model.setWriteResponding(false);
        }
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
