package pe.bazan.luis.android.loratester

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.widget.NestedScrollView
import pe.bazan.luis.android.loratester.ui.theme.LoRaTesterTheme
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.math.log

class MainActivity : ComponentActivity() {
    private lateinit var serialCommunicationProvider: SerialCommunicationProvider
    private lateinit var gpsProvider: GpsProvider
    private lateinit var gpsActual: Location

    var serialStatus: Boolean = false

    private lateinit var logsComponent: TextView
    private lateinit var gpsComponent: TextView
    private var logsFile = "logs-${Date()}.log"
    private var logs: String = logsFile

    private lateinit var gpsUpdater: Timer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainlayout)

        serialCommunicationProvider = SerialCommunicationProvider(this)
        gpsProvider = GpsProvider(this)

        logsComponent = findViewById(R.id.logs)
        gpsComponent = findViewById(R.id.gps_monitor)
        logsComponent.movementMethod = ScrollingMovementMethod.getInstance()

        gpsUpdater = Timer()
        val gpsTask = object : TimerTask() {
            override fun run() {
                gpsProvider.getCurrentLocation {
                    gpsActual = it
                    sendLog("GPS", "Actualizacion de la posicion GPS")
                    runOnUiThread {
                        gpsComponent.text =  "Lat: ${it.latitude} | Long: ${it.longitude}"
                    }
                }
            }
        }

        gpsUpdater.scheduleAtFixedRate(gpsTask, 0, 5000L)
    }

    override fun onDestroy() {
        super.onDestroy()
        gpsUpdater.cancel()
        serialCommunicationProvider.unRegisterReceiver()
    }

    fun serialAlt(view: View) {
        if (!serialStatus) {
            if (serialCommunicationProvider.startUsbConnecting()) {
                runOnUiThread {
                    findViewById<TextView>(R.id.serial_alt_btn).text = "Serial: ON"
                    sendLog("Serial", "habilitado correctamente")
                }
                serialStatus = true
            } else {
                sendLog("Serial", "no se pudo habilitar")
            }
        } else {
            serialCommunicationProvider.close()
            runOnUiThread {
                findViewById<TextView>(R.id.serial_alt_btn).text = "Serial: OFF"
                sendLog("Serial", "deshabilitado correctamente")
            }
            serialStatus = false
        }
    }

    fun modeAlt(view: View) {

    }

    fun manualSend(view: View) {

    }

    fun clearLogs(view: View) {
        logsFile = "logs-${Date().time}.log"
        logs = logsFile
        sendLog("CLEAR LOGS", "Clear logs :)")
    }

    fun sendLog(module: String, line: String) {
        val date = Date()
        logs += "\n[${date.hours}:${date.minutes}:${date.seconds} | ${module}] ${line}"
        runOnUiThread {
            logsComponent.text = logs
            val layout = logsComponent.layout
            val scrollAmount = layout.getLineTop(logsComponent.lineCount) - logsComponent.height
            if (scrollAmount > 0) {
                logsComponent.scrollTo(0, scrollAmount)
            } else {
                logsComponent.scrollTo(0, 0)
            }
        }
        saveTextInFile(logs, logsFile)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        gpsProvider.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun saveTextInFile(texto: String, nombreArchivo: String) {
        try {
            val fileOutputStream: FileOutputStream = openFileOutput(nombreArchivo, Context.MODE_PRIVATE)
            fileOutputStream.write(texto.toByteArray())
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}