#ifndef WIFI_UTILS_H
#define WIFI_UTILS_H

#include <WiFi.h>
#include <ESPAsyncWebSrv.h>
#include <SPIFFS.h>

class WiFiUtils {
public:
  WiFiUtils();
  void setup();
  void loop();

private:
  char* ssid;
  char* password;
  const char* apSSID;
  const char* apPassword;

  AsyncWebServer server{80};

  void setupWiFiClient();
  void setupWiFiAP();
  void handleRoot(AsyncWebServerRequest *request);
  void handleConfig(AsyncWebServerRequest *request);
  void saveWiFiCredentials(String ssid, String password);
  void loadWiFiCredentials();
};

#endif
