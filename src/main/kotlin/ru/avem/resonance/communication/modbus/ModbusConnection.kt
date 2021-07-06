package ru.avem.resonance.communication.modbus

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import com.ucicke.k2mod.modbus.ModbusIOException
import com.ucicke.k2mod.modbus.facade.ModbusSerialMaster
import com.ucicke.k2mod.modbus.procimg.InputRegister
import com.ucicke.k2mod.modbus.procimg.Register
import com.ucicke.k2mod.modbus.util.SerialParameters
import org.slf4j.LoggerFactory
import ru.avem.resonance.MainModel
import java.lang.Thread.sleep
import kotlin.concurrent.thread

const val ENCODING = "rtu"

class ModbusConnection(val deviceName: String,
                       val baudrate: Int,
                       val databits: Int,
                       val stopbits: Int,
                       val parity: Int,
                       val writeTimeout: Int,
                       val readTimeout: Int) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val MUTEX = Any()

    private var master: ModbusSerialMaster? = null

    private var isModbusConnected = false

    init {
        connect()
        thread {
            while (MainModel.isAppRunning) {
                connect()
            }
        }
    }

    private fun connect() {
        if (!isSerialConnecting()) {
            isModbusConnected = try {
                synchronized(MUTEX) {
                    initModbusConnection()
                }
            } catch (e: Exception) {
                false
            }
        }
        sleep(100)
    }

    private fun isSerialConnecting(): Boolean {
        return isModbusConnected && master != null && master!!.connection != null && master!!.connection.isOpen && master!!.connection.bytesAvailable() >= 0
    }

    private fun initModbusConnection(): Boolean {
        val serialParams = SerialParameters()

        val cp2103 = try {
            detectInterfaceConverter()
        } catch (e: SerialPortInvalidPortException) {
            logger.error("Не подключен преобразователь $deviceName")
            return false
        }

        if (cp2103 != null) {
            serialParams.portName = cp2103.systemPortName
            serialParams.encoding = ENCODING
            serialParams.baudRate = baudrate
            serialParams.databits = databits
            serialParams.parity = parity
            serialParams.stopbits = stopbits
        }

        master = ModbusSerialMaster(serialParams, readTimeout)
        master!!.connect()
        master!!.setRetries(2)

        return master!!.connection.isOpen
    }

    private fun detectInterfaceConverter(): SerialPort? {
        val filter = SerialPort.getCommPorts().filter { it.portDescription == deviceName }

        if (filter.isNullOrEmpty()) {
            throw SerialPortInvalidPortException("Не удалось обнаружить преобразователь $deviceName")
        } else {
            return filter.first()
        }
    }

    @Throws(ModbusIOException::class)
    fun writeSingleRegister(unitID: Int, ref: Int, reg: Register, bytesInRegister: Int = 2) {
        synchronized(MUTEX) {
            try {
                master!!.writeSingleRegister(unitID, ref, reg, bytesInRegister)
            } catch (e: Exception) {
                throw ModbusIOException()
            }
        }
    }

    @Throws(ModbusIOException::class)
    fun readInputRegisters(unitID: Int, ref: Int, count: Int, bytesInRegister: Int = 2): Array<out InputRegister> {
        synchronized(MUTEX) {
            try {
                return master!!.readInputRegisters(unitID, ref, count, bytesInRegister)
            } catch (e: Exception) {
                throw ModbusIOException()
            }
        }
    }
}
