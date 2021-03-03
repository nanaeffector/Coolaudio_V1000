;Chorus Effector.
;Author:Nana's Effector.
;Date 2021/03/04.

LFO0 TRI AMP=500 FREQ=100 ;f = FREQ * 0.022Hz for Fs=48kHz, 2.2Hz,

MEM chorusmeml 20ms ; define of chorus memory.

RZP ADCR 127 ; thorugh out.
WAP OUTR 0

RZP ADCL 0.5 ; Read left/2 into accumulator.
WZP chorusmeml ; Write acc to start left chorus mem
RZPB chorusmeml+400 ; Read delayed left to B reg
CHR0 RZP chorusmeml" COMPK LATCH ; Read middle of chorus memory
CHR0 RAP chorusmeml"+1 ; Read middle+1 chorus memory
WBP OUTL K=.999 ; Write dry (B) + chorus (acc) to OUTL

RZP 0x00 ;Add 16 extra reads for refresh
RZP 0x40
RZP 0x80
RZP 0xc0
RZP 0x100
RZP 0x140
RZP 0x180
RZP 0x1c0
RZP 0x200
RZP 0x240
RZP 0x280
RZP 0x2c0
RZP 0x300
RZP 0x340
RZP 0x380
RZP 0x3c0