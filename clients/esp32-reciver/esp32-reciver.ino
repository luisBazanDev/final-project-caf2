#include <SPIFFS.h>

void setup() {
  // Inicializar el puerto serie
  Serial.begin(115200);
  while (!Serial);

  // Formatear la memoria flash
  if (SPIFFS.format()) {
    Serial.println("Memoria flash formateada con éxito");
  } else {
    Serial.println("Error al formatear la memoria flash");
  }
}

void loop() {
  // Nada más por hacer en el bucle principal
}
