#include <Wire.h>
#include <RFM69.h>
#include <SPI.h>

#define NODEID 10 //unique for each node on same network
#define FREQUENCY RF69_433MHZ
#define ENCRYPTKEY "sampleEncryptKey" //key for encryption
#define NETWORKID 98 //the same on all nodes in the network
#define GATEWAYID 65 //ID of Gateway
#define RADIOPOWERLEVEL 31
#define SERIAL_BAUD 115200


RFM69 radio;

byte sendSize = 23;
char payload[]= "3063021, Jibin";          // Put your matriculation numbers here
bool inttriggd;

struct Packet{
  byte pos_PACKET_TYPE ;
  byte pos_PACKET_LENGTH ;
  byte pos_SOURCE_ID;
  byte pos_EPOCH_0;
  byte pos_EPOCH_1;
  byte pos_EPOCH_2;
  byte pos_EPOCH_3;
  byte pos_DIELECTRIC_0;
  byte pos_DIELECTRIC_1;
  byte pos_TEMP_0;
  byte pos_TEMP_1;
  byte pos_PACKETS_SENT_0;
  byte pos_PACKETS_SENT_1;
  byte pos_SENDING_RETRIES;
  byte pos_PACKETS_LOST_0;
  byte pos_PACKETS_LOST_1;
  byte pos_RTT_0;
  byte pos_RTT_1;
  byte pos_RSSI_0;
  byte pos_RSSI_1;
  byte pos_EEPROM_CURRENT_PAGE_0;
  byte pos_EEPROM_CURRENT_PAGE_1;
  byte pos_EEPROM_CURRENT_PAGE_2;

} DataPacket;

void setup() {
  
  Serial.begin(115200);

/*
for(int i=0;i<=sendSize;i++){
  payload[i] = DataPacket[i];
}
*/
  
  // put your setup code here, to run once:
radio.initialize(FREQUENCY,NODEID,NETWORKID);
radio.setPowerLevel(RADIOPOWERLEVEL);
radio.encrypt(ENCRYPTKEY);


//  attachInterrupt(1, intHandler, HIGH);



}

void loop() {
  Serial.print("working \n");
  radio.sendWithRetry(GATEWAYID,payload,sendSize);
 // if(inttriggd){   
 // radio.sleep();
 // inttriggd = false;
 //    }
    delay(1000);
}

void intHandler()
{
  Serial.begin(115200);
inttriggd = true;

//
}
