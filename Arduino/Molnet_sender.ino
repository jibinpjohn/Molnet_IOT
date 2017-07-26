#include <Wire.h>
#include <RFM69.h>
#include <SPI.h>

#define NODEID 10 //unique for each node on same network
#define FREQUENCY RF69_433MHZ
#define ENCRYPTKEY "sampleEncryptKey" //key for encryption
#define NETWORKID 100 //the same on all nodes in the network
#define GATEWAYID 1 //ID of Gateway
#define RADIOPOWERLEVEL 31
#define SERIAL_BAUD 115200


RFM69 radio;

byte sendSize = 15;
bool inttriggd;

typedef struct {
  byte pos_PACKET_TYPE;
  byte pos_PACKET_LENGTH;
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

} payLoad;

payLoad DataPacket;


void setup() {
  
  Serial.begin(115200);

 DataPacket.pos_PACKET_TYPE = 1;
 DataPacket.pos_PACKET_LENGTH = 0x17;
 DataPacket.pos_SOURCE_ID = 1;
 DataPacket.pos_EPOCH_0 = 0x27; //shift bits
 DataPacket.pos_EPOCH_1 = 0xB6; // shift bits
 DataPacket.pos_EPOCH_2 = 0xFC;  // shift bits
 DataPacket.pos_EPOCH_3 = 0xDA;
 DataPacket.pos_DIELECTRIC_0 = 27; // shift bits
 DataPacket.pos_DIELECTRIC_1 = 8;
 DataPacket.pos_TEMP_0 = 0x1;
 DataPacket.pos_TEMP_1 = 0x16;
 DataPacket.pos_PACKETS_SENT_0 = 04;  // shift bits
 DataPacket.pos_PACKETS_SENT_1 = 00;
 DataPacket.pos_SENDING_RETRIES = 00;
 DataPacket.pos_PACKETS_LOST_0 = 00; // shift bits
 DataPacket.pos_PACKETS_LOST_1 = 00;
 DataPacket.pos_RTT_0 = 30; // shift bits
 DataPacket.pos_RTT_1 = 00; // shift bits
 DataPacket.pos_RSSI_0 = 5;// shift bits
 DataPacket.pos_RSSI_1 = 0;
 DataPacket.pos_EEPROM_CURRENT_PAGE_0 = 0;// shift bits
 DataPacket.pos_EEPROM_CURRENT_PAGE_1 = 2;// shift bits
 DataPacket.pos_EEPROM_CURRENT_PAGE_2 = 0;
  
  // put your setup code here, to run once:
radio.initialize(FREQUENCY,NODEID,NETWORKID);
radio.setPowerLevel(RADIOPOWERLEVEL);
radio.encrypt(ENCRYPTKEY);


//  attachInterrupt(1, intHandler, HIGH);



}

void loop() {
  Serial.print("working \n");
radio.sendWithRetry(GATEWAYID, (const void*)(&DataPacket),sizeof(DataPacket));
//  if(inttriggd){   
//radio.sleep();
//inttriggd = false;
 //   }
    delay(5000);
}

void intHandler()
{
  Serial.begin(115200);
inttriggd = true;

//
}

