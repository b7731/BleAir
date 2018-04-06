#include <TimerOne.h>
#include "DHT.h"
#include <SPI.h>
#include <BLEPeripheral.h>

// define pins (varies per shield/board)
#define BLE_REQ   9
#define BLE_RDY   8
#define BLE_RST   10

#define DHTTYPE DHT11
#define DHTPIN 7

DHT dht(DHTPIN, DHTTYPE);

BLEPeripheral blePeripheral = BLEPeripheral(BLE_REQ, BLE_RDY, BLE_RST);
BLEBondStore bleBondStore;

BLEService tempService = BLEService("1800");
BLEFloatCharacteristic tempCharacteristic = BLEFloatCharacteristic("2A00", BLERead | BLENotify);
BLEDescriptor tempDescriptor = BLEDescriptor("2901", "Temperature");

BLEService humidityService = BLEService("1801");
BLEIntCharacteristic humidityCharacteristic = BLEIntCharacteristic("2A01", BLERead | BLENotify);
BLEDescriptor humidityDescriptor = BLEDescriptor("2901", "Humidity");

volatile bool readFromSensor = false;

float lastTempReading;
float lastHumidityReading;

void setup() {
  Serial.begin(115200);
  //bleBondStore.clearData();
  //blePeripheral.setBondStore(bleBondStore);
  
  blePeripheral.setLocalName("JNAir");
  blePeripheral.setDeviceName("Arduino Air Quality");

  blePeripheral.setAdvertisedServiceUuid(tempService.uuid());
  blePeripheral.addAttribute(tempService);
  blePeripheral.addAttribute(tempCharacteristic);
  blePeripheral.addAttribute(tempDescriptor);

  blePeripheral.setAdvertisedServiceUuid(humidityService.uuid());
  blePeripheral.addAttribute(humidityService);
  blePeripheral.addAttribute(humidityCharacteristic);
  blePeripheral.addAttribute(humidityDescriptor);

  blePeripheral.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  blePeripheral.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);
  blePeripheral.setEventHandler(BLEBonded, blePeripheralBondedHandler);
  
  delay(5000);
  blePeripheral.begin();

  Timer1.initialize(2 * 1000000); // in milliseconds
  Timer1.attachInterrupt(timerHandler);

  Serial.println(F("BLE Temperature Sensor Peripheral"));
}

void loop() {
  blePeripheral.poll();

  if (readFromSensor) {
    setTempCharacteristicValue();
    setHumidityCharacteristicValue();
    readFromSensor = false;
  }
}

void timerHandler() {
  readFromSensor = true;
}

void setTempCharacteristicValue() {
  float reading = dht.readTemperature();
//  float reading = random(100);

  if (!isnan(reading) && significantChange(lastTempReading, reading, 0.5)) {
    tempCharacteristic.setValue(reading);

    Serial.print(F("Temperature: ")); Serial.print(reading); Serial.println(F("C"));

    lastTempReading = reading;
  }
}

void setHumidityCharacteristicValue() {
  float reading = dht.readHumidity();
//  float reading = random(100);

  if (!isnan(reading) && significantChange(lastHumidityReading, reading, 1.0)) {
    humidityCharacteristic.setValue(reading);

    Serial.print(F("Humidity: ")); Serial.print(reading); Serial.println(F("%"));

    lastHumidityReading = reading;
  }
}

boolean significantChange(float val1, float val2, float threshold) {
  return (abs(val1 - val2) >= threshold);
}

void blePeripheralConnectHandler(BLECentral& central) {
  Serial.print(F("Connected event, central: "));
  Serial.println(central.address());
}

void blePeripheralDisconnectHandler(BLECentral& central) {
  Serial.print(F("Disconnected event, central: "));
  Serial.println(central.address());
}

void blePeripheralBondedHandler(BLECentral& central) {
  // central bonded event handler
  Serial.print(F("Remote bonded event, central: "));
  Serial.println(central.address());
}

