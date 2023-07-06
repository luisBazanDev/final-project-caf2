package pe.bazan.luis.android.loratester

import android.os.AsyncTask
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpPostTask(private val url: String, private val postData: String, private val callback: Callback) : AsyncTask<Void, Void, String>() {

    interface Callback {
        fun onResponse(response: String)
        fun onError(error: Exception)
    }

    override fun doInBackground(vararg params: Void?): String {
        val connection = URL(url).openConnection() as HttpURLConnection

        try {
            // Configurar la conexión HTTP
            connection.requestMethod = "POST"
            connection.readTimeout = 15000
            connection.connectTimeout = 15000
            connection.doOutput = true

            // Agregar los datos a enviar en la solicitud POST
            val postDataBytes = postData.toByteArray(Charsets.UTF_8)

            // Escribir los datos en el cuerpo de la solicitud POST
            val outputStream = BufferedOutputStream(connection.outputStream)
            outputStream.write(postDataBytes)
            outputStream.flush()
            outputStream.close()

            // Leer la respuesta del servidor
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                return response.toString()
            } else {
                throw IOException("Server returned non-OK status: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    override fun onPostExecute(result: String?) {
        if (result != null) {
            // Llamar al método onResponse del callback con la respuesta del servidor
            callback.onResponse(result)
        } else {
            // Llamar al método onError del callback en caso de error
            callback.onError(Exception("No se pudo obtener una respuesta del servidor"))
        }
    }
}