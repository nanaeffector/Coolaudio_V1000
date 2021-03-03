// Author: Nana's Effector
// Date  : 2021/03/04


// esp32のhw_timer1を利用するので注意.
// ピンアサインが直打ちなので、メイン側から渡すように変更した方がスマート.
// タイマーはメインで宣言して、ポインタ渡す方がスマート.

#include "Word.h"

#define SERIAL_CLK  32
#define SERIAL_DATA 25

hw_timer_t *timer1 = NULL;    // For Wait
portMUX_TYPE timerMux = portMUX_INITIALIZER_UNLOCKED;

static const uint8_t AttnBit = 0x40;
static const uint8_t AttnBits = 3;
static const uint8_t SelBit = 0x00;
static const uint8_t SelBits = 1;
static const uint8_t ReadBit = 0x00;
static const uint8_t WriteBit = 0x80;
static const uint8_t RWBits = 1;
static const uint8_t DeselBit = 0x80;
static const uint8_t DeselBits = 1;

static volatile uint8_t txByteData = 0x00;
static volatile int8_t txBitLength = 0;
static volatile uint8_t txByteLength = 0;
static volatile uint8_t txAttnMode = 0;
static volatile uint8_t txEndFlg = 0;
static volatile uint8_t txNextClkEdge = 0;
static volatile uint8_t enableInterrupt = 0;
static volatile uint8_t isReadMode = 0;

static void IRAM_ATTR outputClock(){
  // クロックエッジを立てる.
  digitalWrite(SERIAL_CLK, txNextClkEdge);
  txNextClkEdge ^= 1;
}

static void IRAM_ATTR outputBit(){
  //Serial.println("tx");
  if(!isReadMode){
    digitalWrite(SERIAL_DATA, (txByteData & 0x80)>>7);
  }

  portENTER_CRITICAL_ISR(&timerMux);
  // データ更新.
  txByteData = txByteData<<1;
  txBitLength--;
  portEXIT_CRITICAL_ISR(&timerMux);
}

static void IRAM_ATTR onTimer1(){
  // Increment the counter and set the time of ISR  

  if(enableInterrupt){
    if(txBitLength > 0){
      // データ出力開始.
      outputBit();
    
      // データ確定のため、クロックエッジを立てる.
      if(!txAttnMode){
        // Attn送信でなければクロックエッジを立てる.
        outputClock();
      }
    }
    else{
      txEndFlg = 1;
      enableInterrupt = false;
    
      //Serial.println("txend.");
    }
  }
}

static void startTxData(uint8_t data, uint8_t bitLength){
  char c;
  txByteData = data;
  txBitLength = bitLength;
  txEndFlg = false;
  
  // 送信開始.
  timerAlarmWrite(timer1, 100, true);
  enableInterrupt = 1;
  
  //　完了待ち.
  while(!txEndFlg);
}

static void write_WordData(Word wordData){
  int i;
  
  // Data 出力.
  for(i=0; i<4; i++){
    startTxData(wordData.Byte[i], 8);
  }
}

static void attn_sel_v1000(bool isWrite){
  digitalWrite(SERIAL_DATA, LOW);
  delayMicroseconds(1);
  outputClock();
  delayMicroseconds(1);
  // Attn送信開始.
  txAttnMode = 1;
  startTxData(AttnBit, AttnBits);
  txAttnMode = 0;

  // Sel送信開始.
  startTxData(SelBit, SelBits);

  // Read/Write送信開始.
  if(isWrite){
    startTxData(WriteBit, RWBits);
  }
  else{
    startTxData(ReadBit, RWBits);
  }
}

static void attn_desel_v1000(){
  // Attn送信開始.
  txAttnMode = 1;
  startTxData(AttnBit, AttnBits);
  txAttnMode = 0;
  
  // Desel送信開始.
  startTxData(DeselBit, DeselBits);
}

// --------------------------------------------.
// ------------------ 公開関数 ------------------.
// --------------------------------------------.

void init_V1000Drv(){
  digitalWrite(SERIAL_CLK, HIGH);
  digitalWrite(SERIAL_DATA, HIGH);
  
  pinMode(SERIAL_CLK, OUTPUT);
  pinMode(SERIAL_DATA, OUTPUT);
  
  timer1 = timerBegin(0, 80, true);
  timerAttachInterrupt(timer1, &onTimer1, true);
  timerAlarmWrite(timer1, 20, true);
  timerAlarmEnable(timer1);
}

void write_v1000(Word *pWordData, int wordLength)
{
  uint8_t i,j;
  
  // Attn Sel 出力.
  attn_sel_v1000(true);

  // 先頭のAddressを出力.
  startTxData(pWordData->Addr, 8);
    
  // data出力.
  for(i=0; i<wordLength; i++){
    write_WordData(pWordData[i]);
  }
  
  // Attn Desel 出力.
  attn_desel_v1000();
}

//　※リードできない...
void read_v1000(Word *pWordData)
{
  uint8_t i;
  
  // Attn Sel 出力.
  attn_sel_v1000(false);
  
  // Address 出力.
  startTxData(pWordData->Addr, 8);

  // データピンを入力に設定.
  pinMode(SERIAL_DATA, INPUT);
  delayMicroseconds(20);
  isReadMode = 1;
  
  // Data 出力.
  write_WordData(*pWordData);
  
  // データピンを出力に設定.
  delayMicroseconds(20);
  pinMode(SERIAL_DATA, OUTPUT);
  isReadMode = 0;
}
