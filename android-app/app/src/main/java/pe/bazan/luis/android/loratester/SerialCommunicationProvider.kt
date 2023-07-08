package pe.bazan.luis.android.loratester

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import android.widget.Toast
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

class SerialCommunicationProvider {
    private val ACTION_USB_PERMISSION = "pe.bazan.pe.android.serialtest2.USB_PERMISSION"
    private val VENDOR_ID = 1027
    private val BAUD_RATE = 115200

    var callbacks: HashMap<String, (value: String) -> Unit> = HashMap()

    private lateinit var m_usbManager: UsbManager
    var m_device: UsbDevice? = null
    var m_serial: UsbSerialDevice? = null
    var m_connection: UsbDeviceConnection? = null

    lateinit var mainActivity: MainActivity

    constructor(mainActivity: MainActivity) {
        this.mainActivity = mainActivity

        m_usbManager = mainActivity.getSystemService(Context.USB_SERVICE) as UsbManager

        var filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED)
        mainActivity.registerReceiver(broadcastReceiver, filter)
    }

    fun close() {
        m_serial?.close()
    }

    fun unRegisterReceiver() {
        mainActivity.unregisterReceiver(broadcastReceiver)
    }

    fun sendData(data: String) {
        m_serial?.write((data).toByteArray())
        if (m_serial != null) {
            mainActivity.sendLog("Serial", "Sending data: $data")
            Log.i("serial", "Sending data: " + data.toByteArray())
        } else {
            mainActivity.sendLog("Serial", "Error on send data: $data")
            Log.i("serial", "Error on send data: " + data.toByteArray())
        }
    }

    var mCallBackLine = object : UsbSerialInterface.UsbReadCallback {
        var line: String = ""

        override fun onReceivedData(data: ByteArray) {
            var rawString = String(data, Charsets.UTF_8)
            var splitData = rawString.split("\n")
            for (splitDatum in splitData) {
                line += splitDatum
                if (splitDatum.isNotEmpty() && splitDatum.last() == '\r') {
                    completeLine()
                    line = ""
                }
            }
        }

        fun completeLine() {
            var data = line
            mainActivity.sendLog("Serial", "Received raw data: $data")

            for (callback in callbacks) {
                if (data.startsWith(callback.key + ":")) {
                    val value: String = data.slice(callback.key.length+1..data.length - 1)
                    mainActivity?.runOnUiThread {
                        callback.value.invoke(value)
                    }
                }
            }
        }
    }

    fun startUsbConnecting(): Boolean {
        val usbDevices: HashMap<String, UsbDevice>? = m_usbManager.deviceList
        if (!usbDevices?.isEmpty()!!) {
            var keep = true
            usbDevices.forEach { entry ->
                m_device = entry.value
                val deviceVendorId: Int? = m_device?.vendorId
                Toast.makeText(mainActivity, "vendorId: " + deviceVendorId, Toast.LENGTH_SHORT).show()
                Log.i("serial", "vendorId: " + deviceVendorId)
                if (deviceVendorId == VENDOR_ID) {
                    val intent: PendingIntent = PendingIntent.getBroadcast(mainActivity, 0, Intent(ACTION_USB_PERMISSION), 0)
                    m_usbManager.requestPermission(m_device, intent)
                    keep = false
                    Log.i("serial", "connection successful")
                    Toast.makeText(mainActivity, "connection successful", Toast.LENGTH_SHORT).show()
                    return true
                } else {
                    m_connection = null
                    m_device = null
                    Log.i("serial", "unable to connect")
                    Toast.makeText(mainActivity, "unable to connect", Toast.LENGTH_SHORT).show()
                }
                if (!keep) {
                    return false
                }
            }
        } else {
            Log.i("serial", "no usb device connected")
            Toast.makeText(mainActivity, "no usb device connected", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    fun registerReadData(event: String, callback: (data: String) -> Unit) {
        callbacks.put(event, callback)
    }

    private val broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action!! == ACTION_USB_PERMISSION) {
                val granted: Boolean = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if (granted) {
                    m_connection = m_usbManager.openDevice(m_device)
                    m_serial = UsbSerialDevice.createUsbSerialDevice(m_device, m_connection)

                    if (m_serial != null) {
                        if (m_serial!!.open()) {
                            m_serial!!.setBaudRate(BAUD_RATE)
                            m_serial!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
                            m_serial!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
                            m_serial!!.setParity(UsbSerialInterface.PARITY_NONE)
                            m_serial!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                            m_serial!!.read(mCallBackLine)
                            Toast.makeText(mainActivity, "adsdwads", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.i("serial", "port not open")
                        }
                    } else {
                        Log.i("serial", "port is null")
                    }
                } else {
                    Log.i("serial", "permission not granted")
                }
            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                startUsbConnecting()
            } else if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                close()
            }
        }
    }
}