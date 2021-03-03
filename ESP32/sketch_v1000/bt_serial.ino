// Author: Nana's Effector
// Date  : 2021/03/04


#include "BluetoothSerial.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "bt_serial.h"

static stBtCmd *btCmdProcList;
static int cmdProcListSize;
static funcCmdProc pfCmdErrFunc;

static char serialBTRxBuf[sizeof(stProtocolBT) + 1];
static char serialBTTxBuf[sizeof(stProtocolBT) + 1];
static int serialBTRxBufPointer = 0;
static unsigned long lastRcvTime = 0;

static void analyzeRcvData(){
  stProtocolBT *ptcl = (stProtocolBT*)serialBTRxBuf;
  funcCmdProc pfProc = pfCmdErrFunc;
  char cmd;
  int i;
  long value = -1; 
  char *e;

  if(ptcl->head == BTHEAD){
    // ヘッダ正常.
    cmd = ptcl->cmd;
    // コマンド判定.
    for(i=0; i<cmdProcListSize; i++){
      //Serial.println(cmd);
      //Serial.println(btCmdProcList[i].cmd);
      if(btCmdProcList[i].cmd == cmd){
        value = strtol(ptcl->data, &e, 10);
        if(*e == 0){
          // コマンド決定.
          pfProc = btCmdProcList[i].pfBtCmdProc;
        }
        else{
          Serial.println("ValueError.");
        }
        break;
      }
    }
  }
  else{
    Serial.println("HeaderError.");
  }

  // 処理実行.
  pfProc(value);
}

static bool isRcvComp(char c){
  return c == 0x0a;
}

static void clearRxBuffer(){
  //Serial.print("RxBufClear.");
  //Serial.println(serialBTRxBuf);
  
  memset(serialBTRxBuf, 0, sizeof(serialBTRxBuf));
  serialBTRxBufPointer = 0;
}

static bool rcvBtSerial(char c){
  if(isRcvComp(c)){
    analyzeRcvData();
    clearRxBuffer();
  }
  else{
    if(serialBTRxBufPointer < sizeof(serialBTRxBuf)){
      serialBTRxBuf[serialBTRxBufPointer] = c;
      serialBTRxBufPointer++;
    }
    else{
      // ovf.
      Serial.println("RxBufOVF.");
      clearRxBuffer();
    }
  }
}

// --------------------------------------------.
// ------------------ 公開関数 ------------------.
// --------------------------------------------.

void init_BtSerial(stBtCmd *pProcList, int listSize, funcCmdProc pfErrFunc){
  btCmdProcList = pProcList;
  cmdProcListSize = listSize;
  pfCmdErrFunc = pfErrFunc;
  
  clearRxBuffer();
  lastRcvTime = millis();
}

// BT受信 ポーリング処理.
void pollBtRxData(BluetoothSerial *serialBT){
  unsigned long time = millis();
  char c;
  
  if (serialBT->available()) {
    c = serialBT->read();
    //Serial.print(c);
    lastRcvTime = time;
    rcvBtSerial(c);
  }
  else{
    if(serialBTRxBufPointer > 0 && lastRcvTime + 100 < time){
      Serial.println("Rx timeout.");

      // バッファの定期クリア.
      clearRxBuffer();
    }
  }
}

void sendBtData(BluetoothSerial *serialBT, char cmd, int value){
  stProtocolBT ptcl = {BTHEAD, cmd, 0};
  if(value > 99999){
    value = 99999;
  }
  else if(value < -99999){
    value = -99999;
  }
  
  sprintf(ptcl.data, "%d", value);

  serialBT->println((char*)&ptcl);
}
