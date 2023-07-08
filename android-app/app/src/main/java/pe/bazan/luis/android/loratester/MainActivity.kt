package pe.bazan.luis.android.loratester

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.EditText
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
import java.text.DecimalFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.math.log

class MainActivity : ComponentActivity() {
    lateinit var serialCommunicationProvider: SerialCommunicationProvider
    private lateinit var gpsProvider: GpsProvider
    lateinit var gpsActual: Location
    private var loRaManager: LoRaManager? = null

    var serialStatus: Boolean = false

    private lateinit var logsComponent: TextView
    private lateinit var gpsComponent: TextView
    private var logsFile = "logs-${Date()}.log"
    private var logs: String = logsFile
    private var mode: Int = 0
    private var presetIndex: Int = 0

    private val presets: Array<Array<Int>> = arrayOf(
        arrayOf(7,125,0,8,5),
        arrayOf(10,125,2,8,22),
        arrayOf(10,125,3,8,22),
    )

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
                    Toast.makeText(this@MainActivity, "GPS UPDATED", Toast.LENGTH_SHORT).show()
                    runOnUiThread {
                        gpsComponent.text =  "Lat: ${it.latitude} | Long: ${it.longitude}"
                    }
                }
            }
        }

        gpsUpdater.scheduleAtFixedRate(gpsTask, 0, 10000L)
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
                loRaManager = LoRaManager(this, mode)
                loadValues(view)
                loRaManager!!.loadMode()
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
            loRaManager?.stop()
            loRaManager = null
        }
    }

    fun modeAlt(view: View) {
        if (mode >= 4) mode = 0
        else mode++
        runOnUiThread {
            findViewById<TextView>(R.id.mode_btn).text = "MODE: " + resources.getStringArray(R.array.modes)[mode]
        }

        loRaManager?.mode = mode
        loRaManager?.loadMode()
    }


    fun changePreset(view: View) {
        if (presetIndex >= presets.size - 1) presetIndex = 0
        else presetIndex++

        runOnUiThread {
            findViewById<TextView>(R.id.presets).text = "PRESET: " + (presetIndex + 1)
        }

        val preset = presets[presetIndex]
        runOnUiThread {
            findViewById<EditText>(R.id.values_sf).setText(preset[0].toString())
            findViewById<EditText>(R.id.values_BW).setText(preset[1].toString())
            findViewById<EditText>(R.id.values_cr).setText(preset[2].toString())
            findViewById<EditText>(R.id.values_pl).setText(preset[3].toString())
            findViewById<EditText>(R.id.values_txp).setText(preset[4].toString())
        }

        loadValues(view)
    }

    fun manualSend(view: View) {
        loRaManager?.manualSend()
    }

    fun loadValues(view: View) {
        val spreadingFactor: Int = findViewById<EditText>(R.id.values_sf).text.toString().toInt()
        val bandWidth: Int = findViewById<EditText>(R.id.values_BW).text.toString().toInt()
        val codeRate: Int = findViewById<EditText>(R.id.values_cr).text.toString().toInt()
        val preambleLength: Int = findViewById<EditText>(R.id.values_pl).text.toString().toInt()
        val txPower: Int = findViewById<EditText>(R.id.values_txp).text.toString().toInt()

        loRaManager?.spreadingFactor = spreadingFactor
        loRaManager?.bandWidth = bandWidth
        loRaManager?.codeRate = codeRate
        loRaManager?.preambleLength = preambleLength
        loRaManager?.txPower = txPower

        loRaManager?.config()

        if (loRaManager == null) {
            sendLog("LoRa", "Error - Not exists LoRaManager")
        }
    }

    fun clearLogs(view: View) {
        logsFile = "logs-${Date().time}.log"
        logs = logsFile
        sendLog("CLEAR LOGS", "Clear logs :)")
    }

    fun sendLog(module: String, line: String) {
        val date = Date()
        val formater = DecimalFormat("00")
        if (!logs.endsWith("\n")) logs += "\n"
        logs += "[${formater.format(date.hours)}:${formater.format(date.minutes)}:${formater.format(date.seconds)} | ${module}] ${line}"
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