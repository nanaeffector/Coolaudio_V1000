// Author: Nana's Effector
// Date  : 2021/03/04


#define LED_PIN 13

#include "BluetoothSerial.h"
#include "driver/adc.h" 
#include "esp_adc_cal.h"
#include "Word.h"
#include "bt_serial.h"
#include "effect_chorus.h"


static void procError(int val);
static void procGetDepth(int val);
static void procGetSpeed(int val);
static void procGetMix(int val);
static void procSetDepth(int val);
static void procSetSpeed(int val);
static void procSetMix(int val);

static int Depth = 0; // 0～100.
static int Speed = 0; // 0～100.
static int Mix = 64;  // 0～100.

BluetoothSerial SerialBT;

static stBtCmd btCmdList[] = {
  {'A', procGetMix},
  {'B', procGetDepth},
  {'C', procGetSpeed},
  {'D', procSetDepth},
  {'E', procSetSpeed},
  {'F', procSetMix},
};

static void procError(int val){
  Serial.print("procError.");
  Serial.println(val);
}

static void procGetDepth(int val){
  Serial.print("procGetDepth.");
  Serial.println(val);

  sendBtData(&SerialBT, 'B', Depth);
}

static void procGetSpeed(int val){
  Serial.print("procGetSpeed.");
  Serial.println(val);
  
  sendBtData(&SerialBT, 'C', Speed);
}

static void procGetMix(int val){
  Serial.print("procGetMix.");
  Serial.println(val);
  
  sendBtData(&SerialBT, 'A', Mixfv);
}

static void procSetDepth(int val){
  //Serial.print("procSetDepth.");
  //Serial.println(val);

  Depth = val;
  updateLFO0();
}

static void procSetSpeed(int val){
  //Serial.print("procSetSpeed.");
  //Serial.println(val);

  Speed = val;
  updateLFO0();
}

static void procSetMix(int val){
  //Serial.print("procSetMix.");
  //Serial.println(val);

  Mix = val;
  updateMix();
}

void setup() {
  Serial.begin(115200);
  SerialBT.begin("V1000_Efcector");
    
  pinMode(LED_PIN, OUTPUT);//5番ピンを出力に設定
  digitalWrite(LED_PIN, 1); //LED出力を設定.

  // bluetooth serial通信の準備.
  init_BtSerial(btCmdList, sizeof(btCmdList)/sizeof(stBtCmd), procError);
  
  // V1000の準備.
  init_V1000Drv();
  
  // 送信.
  write_v1000((Word*)isntruction_set_chorus, isntruction_length_chorus);
}

void loop() {
  pollBtRxData(&SerialBT);
}

void updateLFO0(){
  Word LFO0_Buf = {0x00, {0x40,0x00,0x01,0xF4}};
  
  updateLFOFreq(&LFO0_Buf);
  updateLFOAmp(&LFO0_Buf);
  
  write_v1000(&LFO0_Buf, 1);
}

void updateMix(){
  Word Buf = {0x06,{0x40,0x00,0x00,0x00}};

  Buf.Byte[0] = Mix;
  
  write_v1000(&Buf, 1);
}

void updateLFOFreq(Word *w){
  int spd;
  uint8_t LH;
  uint8_t LL;
  
  spd = 10 + Speed;  // 10～110.

  LH = (spd >> 8) & 0xFF;
  LL = spd & 0xFF;

  // mask.
  w->Byte[0] &= 0xF0;
  w->Byte[1] &= 0x00;
  w->Byte[2] &= 0x7F;

  // data set.
  w->Byte[0] |= (LH>>1)&0x0F;
  w->Byte[1] |= ((LH&0x01)<<7) | (LL>>1);
  w->Byte[2] |= ((LL&0x01)<<7);
}

void updateLFOAmp(Word *w){
  int dep;
  uint8_t LH;
  uint8_t LL;
  
  // ADC1_CH6の電圧値を取得

  dep = 500 + (Depth<<3);  // ×8.　500+0 ～500+800.

  LH = (dep >> 8) & 0x7F;
  LL = dep & 0xFF;

  // mask.
  w->Byte[2] &= 0x80;
  w->Byte[3] &= 0x00;

  // data set.
  w->Byte[2] |= LH;
  w->Byte[3] |= LL;
}
