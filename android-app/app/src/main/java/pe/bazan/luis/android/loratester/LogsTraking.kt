package pe.bazan.luis.android.loratester

import android.location.Location
import pe.bazan.luis.android.loratester.objects.LocalLog
import java.util.LinkedList
import java.util.Queue

class LogsTraking {
    var LOGS_URL = "http://caf2.bazan.pe:8198/api/create"
    private val logsQueue: Queue<LocalLog> = LinkedList()
    private var isSendingLog = false

    fun logItem(location: Location, loRaManager: LoRaManager, payload: String, rssi: Int? = null, snr: Int? = null) {
        var localLog: LocalLog? = null
        val spreadingFactor = loRaManager.spreadingFactor
        val bandWidth = loRaManager.bandWidth
        val codeRate = loRaManager.codeRate
        val preambleLength = loRaManager.preambleLength
        val txPower = loRaManager.txPower
        val author = loRaManager.getAuthor()

        if (spreadingFactor != null &&
            bandWidth != null &&
            codeRate != null &&
            preambleLength != null &&
            txPower != null) {
            localLog = LocalLog(
                location.latitude,
                location.longitude,
                location.altitude,
                rssi,
                snr,
                payload,
                spreadingFactor,
                bandWidth,
                codeRate,
                preambleLength,
                txPower,
                author
            )
        }

        if (localLog == null) return

        logsQueue.offer(localLog)
    }

    fun sendNextLog(callback: () -> Unit = {}) {
        if (!isSendingLog && logsQueue.isNotEmpty()) {
            val actualLog = logsQueue.poll()
            isSendingLog = true

            actualLog.send(LOGS_URL) {
                isSendingLog  = false
                callback?.invoke()
                sendNextLog()
            }
        }
    }
}