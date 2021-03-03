#ifndef BT_SERIAL
#define BT_SERIAL

#define BTHEAD 'B'

typedef  void (*funcCmdProc)(int);

typedef struct{
  char head;
  char cmd;
  char data[7]; //-32768を想定.
}stProtocolBT;

typedef struct{
  char cmd;
  funcCmdProc pfBtCmdProc;
}stBtCmd;

#endif
