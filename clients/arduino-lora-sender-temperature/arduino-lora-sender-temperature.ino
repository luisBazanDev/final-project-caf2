#include <SoftwareSerial.h>
#include <DHT22.h>

#define RX_PIN 2  // Elige los pines que usarás para el SoftwareSerial
#define TX_PIN 3
#define DHTPIN 5      // Pin de datos conectado al pin 5 del Arduino

SoftwareSerial mySerial(RX_PIN, TX_PIN);
DHT22 dht22(DHTPIN); 
float temperature, humidity = 0;

void setup() {
  pinMode(13, OUTPUT);
  Serial.begin(9600);
  mySerial.begin(115200);
  Serial.println("Setting config...");
  mySerial.println("AT+P2P=915000000:10:125:2:8:22");
  Serial.println("LoRa is ready! :)");
}

long lastOff = 0;
long lastSend = 0;
long lastTemperatureHumidity = 0;
void loop() {
  // Intermediario entre ambas comunicaciones seriales
  if(Serial.available()) {
    int data = Serial.read();
    mySerial.write(data);
    Serial.write(data);
  } else if(mySerial.available()) {
    int data = mySerial.read();
    Serial.write(data);
  }

  // Check para apagar el led indicador cada 1 segundo
  if(lastOff != 0 && millis() - lastOff > 1000) {
    digitalWrite(13, LOW);
    lastOff = 0;
  }

  // Timer para enviar datos atravez de LoRa cada 10 segundos
  if(millis() - lastSend > 10000) {
    lastSend = millis();

    // Logica para empaquetar los datos
    String data = "T&H;" + String(temperature, 2) +";" + String(humidity, 2);
    sendMessage(data);
  }

  // Recoleccion de datos de temperatura y humedad cada 2 segundos
  if(millis() - lastTemperatureHumidity > 2000) {
    temperature = dht22.getTemperature();
    humidity = dht22.getHumidity();
    lastTemperatureHumidity = millis();
  }
}

// Funcion para enviar datos de mejor manera
void sendMessage(String data) {
  Serial.println("Sending to LoRa module: "+data);
  mySerial.println("AT+PSEND="+stringToHex(data));
  digitalWrite(13, HIGH);
  lastOff = millis();
}

// Funcion para convertir texto a hexadecimal
String stringToHex(String input) {
  String output = "";
  
  for (size_t i = 0; i < input.length(); i++) {
    char c = input.charAt(i);
    byte nib1 = (c >> 4) & 0x0F; // Primeros 4 bits (nibble alto)
    byte nib2 = c & 0x0F;        // Últimos 4 bits (nibble bajo)

    // Convertir cada nibble a su representación hexadecimal
    char hex1 = (nib1 < 10) ? ('0' + nib1) : ('A' + (nib1 - 10));
    char hex2 = (nib2 < 10) ? ('0' + nib2) : ('A' + (nib2 - 10));

    // Agregar los caracteres hexadecimales a la cadena de salida
    output += hex1;
    output += hex2;
  }

  return output;
}

