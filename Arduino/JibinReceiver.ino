#include <RFM69.h>
#include <SPI.h>
#include <SPIFlash.h>
#include <LowPower.h> 
#include <TimeLib.h>

#define NODEID      15
#define NETWORKID   100
#define FREQUENCY   RF69_433MHZ //Match this with the version of your Moteino! (others: RF69_433MHZ, RF69_868MHZ)
#define KEY         "sampleEncryptKey" //has to be same 16 characters/bytes on all nodes, not more not less!
#define LED         9
#define SERIAL_BAUD 115200
#define ACK_TIME    30  // # of ms to wait for an ack
#define IS_RFM69HW_HCW  //uncomment only for RFM69HW/HCW! Leave out if you have RFM69W/CW!

RFM69 radio;
SPIFlash flash(8, 0xEF30);      //EF40 for 16mbit windbond chip
bool promiscuousMode = false;     //set to 'true' to sniff all packets on the same network

typedef struct {
   byte pos_SINK_ID;
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
payLoad theData;

void setup() {
  Serial.begin(SERIAL_BAUD);
  delay(10);
  radio.initialize(FREQUENCY,NODEID,NETWORKID);
#ifdef IS_RFM69HW_HCW
  radio.setHighPower(); //must include this only for RFM69HW/HCW!
#endif
  radio.encrypt(KEY);
  radio.promiscuous(promiscuousMode);
  char buff[50];
  sprintf(buff, "\nListening at %d Mhz...", FREQUENCY==RF69_433MHZ ? 433 : FREQUENCY==RF69_868MHZ ? 868 : 915);
  Serial.println(buff);
  if (flash.initialize())
    Serial.println("SPI Flash Init OK!");
  else
    Serial.println("SPI Flash Init FAIL! (is chip present?)");
}

byte ackCount=0;
void loop() {
  //process any serial input
  if (Serial.available() > 0)
  {
    char input = Serial.read();
    if (input == 'r') //d=dump all register values
      radio.readAllRegs();
    if (input == 'E') //E=enable encryption
      radio.encrypt(KEY);
    if (input == 'e') //e=disable encryption
      radio.encrypt(null);
    if (input == 'p')
    {
      promiscuousMode = !promiscuousMode;
      radio.promiscuous(promiscuousMode);
      Serial.print("Promiscuous mode ");Serial.println(promiscuousMode ? "on" : "off");
    }
    
    if (input == 'd') //d=dump flash area
    {
      Serial.println("Flash content:");
      int counter = 0;

      while(counter<=256){
        Serial.print(flash.readByte(counter++), HEX);
        Serial.print('.');
      }
      while(flash.busy());
      Serial.println();
    }
    if (input == 'D')
    {
      Serial.print("Deleting Flash chip content... ");
      flash.chipErase();
      while(flash.busy());
      Serial.println("DONE");
    }
    if (input == 'i')
    {
      Serial.print("DeviceID: ");
      word jedecid = flash.readDeviceId();
      Serial.println(jedecid, HEX);
    }
  }

  if (radio.receiveDone())
  {
    Serial.print('[');Serial.print(radio.SENDERID, DEC);Serial.print("] ");
    Serial.print(" [RX_RSSI:");Serial.print(radio.readRSSI());Serial.print("]");
    if (promiscuousMode)
    {
      Serial.print("to [");Serial.print(radio.TARGETID, DEC);Serial.print("] ");
    }

    if (radio.DATALEN != sizeof(payLoad))
      Serial.print("Invalid payload received, not matching Payload struct!");
    else
    {
     theData = *(payLoad*)radio.DATA;     //assume radio.DATA actually contains our struct and not something else
      uint32_t time =          (uint32_t)theData.pos_EPOCH_0 << 24 |
                               (uint32_t)theData.pos_EPOCH_1 << 16 |
                               (uint32_t)theData.pos_EPOCH_2 << 8  |
                               (uint32_t)theData.pos_EPOCH_3; 

        
        uint16_t dielectric =  (uint16_t)theData.pos_DIELECTRIC_0 << 8 |
                               (uint16_t)theData.pos_DIELECTRIC_1;
                            
        uint16_t temperature = (uint16_t)theData.pos_TEMP_0 << 8 |
                               (uint16_t)theData.pos_TEMP_1;    
                      
        TimeElements tm;
        breakTime(time, tm);        
       // Serial.print(" {**Decagon5TMData ");
        Serial.println(theData.pos_PACKET_LENGTH );         
        Serial.println(theData.pos_SOURCE_ID );
       // Serial.println(time, tm.Day, tm.Month, tm.Year, tm.Hour, tm.Minute, tm.Second );
        Serial.println(temperature );
        Serial.println(dielectric);
        //Serial.print("Decagon5TMData**}");
    }
    
    if (radio.ACKRequested())
    {
      byte theNodeID = radio.SENDERID;
      radio.sendACK();
      Serial.print(" - ACK sent.");

      // When a node requests an ACK, respond to the ACK
      // and also send a packet requesting an ACK (every 3rd one only)
      // This way both TX/RX NODE functions are tested on 1 end at the GATEWAY
      if (ackCount++%3==0)
      {
        Serial.print(" Pinging node ");
        Serial.print(theNodeID);
        Serial.print(" - ACK...");
        delay(3); //need this when sending right after reception .. ?
        if (radio.sendWithRetry(theNodeID, "ACK TEST", 8, 0))  // 0 = only 1 attempt, no retries
          Serial.print("ok!");
        else Serial.print("nothing");
      }
    }
    Serial.println();
    Blink(LED,3);
  }
  //sleep MCU for 8seconds
 // LowPower.powerDown(SLEEP_8S, ADC_OFF, BOD_OFF);
}

void Blink(byte PIN, int DELAY_MS)
{
  pinMode(PIN, OUTPUT);
  digitalWrite(PIN,HIGH);
  delay(DELAY_MS);
  digitalWrite(PIN,LOW);
}
