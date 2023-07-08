package pe.bazan.luis.android.loratester.objects

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.util.UUID

class LocalLog(
    val latitude: Int,
    val longitude: Int,
    val altitude: Int,
    private val rssi: Int?,
    private val snr: Int?,
    val payload: String,
    val spreadingFactor: Int,
    val bandWidth: Int,
    val codeRate: Int,
    val preambleLength: Int,
    val txPower: Int,
    val author: String
) {
    private val uuid: UUID = UUID.randomUUID()

    fun send(api: String, callback: () -> Unit) {
        val json = JSONObject().apply {
            put("latitude", latitude)
            put("longitude", longitude)
            put("altitude", altitude)
            put("rssi", rssi)
            put("snr", snr)
            put("payload", payload)
            put("spreading_factor", spreadingFactor)
            put("bandwidth", bandWidth)
            put("code_rate", codeRate)
            put("preamble_length", preambleLength)
            put("tx_power", txPower)
            put("author", author)
            put("uuid", uuid)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://tu-servidor.com/api/endpoint")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // En caso de error de conexión o petición fallida, intentar nuevamente
                send(api, callback)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val responseUuid = jsonResponse.getString("uuid")
                    if (responseUuid.equals(uuid.toString())) {
                        callback.invoke()
                    } else {
                        // En caso el uuid no coincida
                        send(api, callback)
                    }
                } else {
                    // En caso de respuesta no exitosa, intentar nuevamente
                    send(api, callback)
                }
            }
        })
    }

    fun getUuid(): UUID {
        return uuid
    }

    fun getRssi(): Int? {
        return rssi
    }

    fun getSnr(): Int? {
        return snr
    }
}