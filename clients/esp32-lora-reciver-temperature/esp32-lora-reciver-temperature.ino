#include <WiFi.h>
#include <WiFiClient.h>
#include <HTTPClient.h>

char ssid[] = "Luis";       // Nombre de la red Wi-Fi
char password[] = "12345678"; // Contraseña de la red Wi-Fi

char server[] = "caf2.bazan.pe:8198";  // Dirección del servidor API (sin http://)

String apiEndpoint = "/api/temp"; // Endpoint de la API para recepcionar la informacion
String jsonTemplate = "{\"data\":{\"temperature\":\"%temperature%\",\"humidity\":\"%humidity%\"}}"; // Template para poder reemplazar los datos unicamente

void setup() {
  // Configuracion de la comunicacion serial
  Serial.begin(115200);
  Serial2.begin(115200);

  // Configuracion para el Wifi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(1000);
  }
  Serial.println("Wifi connected!");

  // Configuracion de LoRa
  Serial.println("Setting config...");
  Serial2.println("AT+P2P=915000000:10:125:2:8:22");
  Serial2.println("AT+PRECV=65533");
  Serial.println("LoRa is ready! :)");
}

void loop() {
  // Existen datos bidireccionales
  if(Serial.available()) {
    int data = Serial.read();
    Serial2.write(data);
    Serial.write(data);
  }
  if(Serial2.available()) {
    int data = Serial2.read();
    Serial.write(data);
    readLoRaMessages(data);
  }
}

String lastLine = "";
void readLoRaMessages(int raw) {
  char letter = raw;
  if(letter == '\r') {
    // Finish line
    checkLoRaMessage(lastLine);

    lastLine = "";
  } else {
    // Continues line
    lastLine += letter;
  }
}

void checkLoRaMessage(String loraMessage) {
  if(loraMessage.indexOf("+EVT:RXP2P RECEIVE TIMEOUT") != -1) {
    Serial2.println("AT+PRECV=65533");
  } else if(loraMessage.indexOf("+EVT:RXP2P:") != -1) {
    String data = loraMessage.substring(loraMessage.lastIndexOf(":") + 1, loraMessage.length());
    Serial.println();
    processDataToServer(hexToString(data));
    Serial2.println("AT+PRECV=65533");
  }
}

void processDataToServer(String data) {
  String temperature = data.substring(data.indexOf(";") + 1, data.lastIndexOf(";") - 1);
  String humidity = data.substring(data.lastIndexOf(";") + 1, data.length());

  sendServerData(temperature, humidity);
}

// Funcion para enviar la informacion al servidor
void sendServerData(String temperature, String humidity) {
  HTTPClient http;

    // Construir la URL completa
  String url = "http://" + String(server) + apiEndpoint;

  // Configurar la cabecera de la solicitud HTTP
  http.begin(url);
  http.addHeader("Content-Type", "application/json");

  // Logica para empaquetar la informacion en un json
  String jsonData = String(jsonTemplate);
  jsonData.replace("%temperature%", temperature);
  jsonData.replace("%humidity%", humidity);

  // Enviar la solicitud POST con el cuerpo JSON
  int httpResponseCode = http.POST(jsonData);

  // Leer y mostrar la respuesta del servidor
  String response = http.getString();
  Serial.print("Código de estado HTTP: ");
  Serial.println(httpResponseCode);
  Serial.print("Respuesta del servidor: ");
  Serial.println(response);
  http.end();
}

// Funcion para reconstruir la informacion para poder ser enviada al servidor
String hexToString(String input) {
  String output = "";

  // Verificar que la longitud de la cadena sea par
  if (input.length() % 2 != 0) {
    Serial.println("Error: Longitud de cadena hexadecimal inválida.");
    return output;
  }

  // Procesar la cadena de entrada en pares de caracteres
  for (size_t i = 0; i < input.length(); i += 2) {
    // Leer dos caracteres hexadecimales y combinarlos en un byte
    char hexChar1 = input.charAt(i);
    char hexChar2 = input.charAt(i + 1);

    // Convertir los caracteres hexadecimales a sus valores numéricos
    int nib1 = (hexChar1 >= '0' && hexChar1 <= '9') ? (hexChar1 - '0') : (hexChar1 - 'A' + 10);
    int nib2 = (hexChar2 >= '0' && hexChar2 <= '9') ? (hexChar2 - '0') : (hexChar2 - 'A' + 10);

    // Combinar los nibbles para obtener el byte original
    char originalChar = (nib1 << 4) | nib2;

    // Agregar el carácter original a la cadena de salida
    output += originalChar;
  }

  return output;
}
