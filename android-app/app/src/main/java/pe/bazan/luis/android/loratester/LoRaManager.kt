package pe.bazan.luis.android.loratester

import android.os.Handler
import android.os.Looper
import com.google.android.gms.common.util.Hex
import java.util.Date
import java.util.LinkedList
import java.util.Queue
import java.util.UUID

class LoRaManager {
    private lateinit var mainActivity: MainActivity
    var mode: Int = 0
    var spreadingFactor: Int? = null
    var bandWidth: Int? = null
    var codeRate: Int? = null
    var preambleLength: Int? = null
    var txPower: Int? = null
    var logsTraking = LogsTraking()

    constructor(mainActivity: MainActivity, mode: Int) {
        this.mainActivity = mainActivity
        this.mode = mode
        sendCommand("AT", true)
        sendCommand("AT+PRECV=0", true)
        sendCommand("AT+NWM=0", true)
    }

    fun loadMode() {
        when (mode) {
            0 -> {
                // NONE
                sendCommand("AT+PRECV=0")
                mainActivity.serialCommunicationProvider.callbacks = HashMap()
            }
            1 -> {
                // SENDER
                sendCommand("AT+PRECV=0")
                mainActivity.serialCommunicationProvider.callbacks = HashMap()
            }
            2 -> {
                // RECEIVER - TX MODE NOT ALLOWED
                sendCommand("AT+PRECV=0")
                sendCommand("AT+PRECV=65532")
                mainActivity.serialCommunicationProvider.callbacks = HashMap()
                mainActivity.serialCommunicationProvider.registerReadData("+EVT:RXP2P", receiverP2PPackage)
                mainActivity.serialCommunicationProvider.registerReadData("raw-receiver", receiverLogicEnable)
            }
            3 -> {
                // RECEIVER-PING - TX MODE ALLOWED
                sendCommand("AT+PRECV=0")
                sendCommand("AT+PRECV=65533")
                mainActivity.serialCommunicationProvider.callbacks = HashMap()
                mainActivity.serialCommunicationProvider.registerReadData("+EVT:RXP2P", receiverP2PPackage)
                mainActivity.serialCommunicationProvider.registerReadData("raw-receiver", receiverLogicEnable)
            }
            4 -> {
                // SENDER-PING
                sendCommand("AT+PRECV=0")
                mainActivity.serialCommunicationProvider.callbacks = HashMap()
            }
            5 -> {
                // SENDER-PING
                sendCommand("AT+PRECV=0")
                mainActivity.serialCommunicationProvider.callbacks = HashMap()
            }
        }
    }

    val receiverLogicEnable = {data: String ->
        if (mode == 2) {
            if (
                data.equals("+EVT:RXP2P RECEIVE TIMEOUT")
                || data.startsWith("+EVT:RXP2P:")
            ) {
                sendCommand("AT+PRECV=65532")
            }
        }
        if (mode == 3) {
            if (
                data.equals("+EVT:RXP2P RECEIVE TIMEOUT")
                || data.startsWith("+EVT:RXP2P:")
            ) {
                sendCommand("AT+PRECV=65533")
            }
        }
    }

    val receiverP2PPackage = {data: String ->
        mainActivity.sendLog("receiverP2PPackage", data)
        var values = data.split(":")
        if (values.size >= 3) {
            val rssi = values.first().toInt()
            values.drop(1)
            val snr = values.first().toInt()
            values.drop(1)
            val payload = values.joinToString(":")
            if (mode == 3) {
                sendCommand("AT+PSEND="+payload)
            }
            logsTraking.logItem(mainActivity.gpsActual, this, payload, rssi, snr)
        }
    }

    enum class Frequency(val value: Int) {
        PERU(915000000),
    }

    fun stop() {
    }

    fun manualSend() {
        if (mode == 1 || mode == 2) {
            val uuid = UUID.randomUUID().toString()
            sendCommand("AT+PSEND="+convertToHex(uuid))
            logsTraking.logItem(mainActivity.gpsActual, this, uuid)
        }
    }

    private val sendCommandQueue: Queue<String> = LinkedList()
    private var isSendingCommand = false

    fun sendCommand(sendCommand: String, bypass: Boolean = false) {
        if (mode == 0 && !sendCommand.startsWith("AT+P2P=")) {
            if (bypass == null) return
        }

        sendCommandQueue.offer(sendCommand)
        processCommandQueue()
    }

    private fun processCommandQueue() {
        if (!isSendingCommand && sendCommandQueue.isNotEmpty()) {
            val cmd = sendCommandQueue.poll()
            isSendingCommand = true
            mainActivity.serialCommunicationProvider.sendData("$cmd\n")

            Handler(Looper.getMainLooper()).postDelayed({
                isSendingCommand = false
                processCommandQueue()
            }, 500L)
        }
    }

    fun config() {
        sendCommand("AT+P2P=${Frequency.PERU.value}:$spreadingFactor:$bandWidth:$codeRate:$preambleLength:$txPower")
    }

    fun getAuthor(): String {
        return mainActivity.resources.getStringArray(R.array.modes)[mode]
    }

    fun convertToHex(input: String): String {
        val bytes = input.toByteArray()
        val hexStringBuilder = StringBuilder()

        for (byte in bytes) {
            val hexString = String.format("%02X", byte)
            hexStringBuilder.append(hexString)
        }

        return hexStringBuilder.toString()
    }
}