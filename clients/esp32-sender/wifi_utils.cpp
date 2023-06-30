#include "wifi_utils.h"

WiFiUtils::WiFiUtils() {
  ssid = "";        // Nombre de la red Wi-Fi a la que se conectará la ESP32
  password = "";    // Contraseña de la red Wi-Fi
  apSSID = "ESP32_AP";
  apPassword = "12345678";
  server = AsyncWebServer(80);
}

void WiFiUtils::setup() {
  Serial.begin(115200);

  if (!SPIFFS.begin()) {
    Serial.println("Error al montar el sistema de archivos SPIFFS");
    return;
  }

  loadWiFiCredentials();

  if (strlen(ssid) == 0 || strlen(password) == 0) {
    setupWiFiAP();
  } else {
    setupWiFiClient();
  }

  server.on("/", HTTP_GET, [this](AsyncWebServerRequest *request) {
    this->handleRoot(request);
  });

  server.on("/config", HTTP_POST, [this](AsyncWebServerRequest *request) {
    this->handleConfig(request);
  });

  server.begin();
}

void WiFiUtils::setupWiFiClient() {
  Serial.println("Conectando a la red Wi-Fi guardada...");

  WiFi.begin(ssid, password);

  int intents = 0;

  while (WiFi.status() != WL_CONNECTED) {
    if(intents > 30) {
      Serial.println();
      Serial.println("Creando AP...");
      setupWiFiAP();
    }
    
    delay(1000);
    Serial.print(".");
    intents++;
  }

  Serial.println("");
  Serial.println("Conexión Wi-Fi establecida");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());
}

void WiFiUtils::setupWiFiAP() {
  Serial.println("Iniciando punto de acceso (AP)...");

  WiFi.softAP(apSSID, apPassword);

  IPAddress apIP = WiFi.softAPIP();
  Serial.println("");
  Serial.println("Punto de acceso (AP) iniciado");
  Serial.print("Dirección IP del AP: ");
  Serial.println(apIP);

  Serial.println("Conéctate al AP para configurar la red Wi-Fi");
}

void WiFiUtils::handleRoot(AsyncWebServerRequest *request) {
  String html = "<html><body><h1>Selecciona una red Wi-Fi:</h1>";

  int n = WiFi.scanNetworks(false, false, true, 5, 0);
  if (n == 0) {
    html += "<p>No se encontraron redes Wi-Fi disponibles</p>";
  } else {
    html += "<form method='POST' action='/config'>";
    for (int i = 0; i < n; ++i) {
      html += "<input type='radio' name='ssid' value='" + WiFi.SSID(i) + "'>" + WiFi.SSID(i) + "<br>";
    }
    html += "<br>Password: <input type='password' name='password'><br><br>";
    html += "<input type='submit' value='Conectar'>";
    html += "</form>";
  }

  html += "</body></html>";

  request->send(200, "text/html", html);
}

void WiFiUtils::handleConfig(AsyncWebServerRequest *request) {
  if (request->hasParam("ssid", true) && request->hasParam("password", true)) {
    String ssid = request->getParam("ssid", true)->value();
    String password = request->getParam("password", true)->value();

    saveWiFiCredentials(ssid, password);

    ESP.restart();
  } else {
    request->send(400);
  }
}

void WiFiUtils::saveWiFiCredentials(String ssid, String password) {
  Serial.println("Guardando credenciales de la red Wi-Fi...");

  File file = SPIFFS.open("/config.txt", FILE_WRITE);
  if (!file) {
    Serial.println("Error al abrir el archivo");
    return;
  }

  file.println(ssid);
  file.println(password);

  file.close();

  Serial.println("Credenciales guardadas");
  Serial.println("Reiniciando...");
}

void WiFiUtils::loadWiFiCredentials() {
  Serial.println("Cargando credenciales de la red Wi-Fi...");

  File file = SPIFFS.open("/config.txt", FILE_READ);
  if (!file) {
    Serial.println("No se encontraron credenciales guardadas");
    return;
  }

  String savedSSID = file.readStringUntil('\n');
  String savedPassword = file.readStringUntil('\n');

  file.close();

  if (savedSSID.length() > 0 && savedPassword.length() > 0) {
    savedSSID.trim();
    savedPassword.trim();
    ssid = new char[savedSSID.length() + 1];
    password = new char[savedPassword.length() + 1];
    strcpy(ssid, savedSSID.c_str());
    strcpy(password, savedPassword.c_str());

    Serial.println("Credenciales cargadas");
  } else {
    Serial.println("No se encontraron credenciales guardadas");
  }
}
