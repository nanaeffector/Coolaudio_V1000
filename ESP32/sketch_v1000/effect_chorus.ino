// Author: Nana's Effector
// Date  : 2021/03/04


// chorus 命令.
const Word isntruction_set_chorus[] = {
//0.22Hz(10) amp=500
//{0x00,{0x40,0x05,0x01,0xF4}},
//0.22Hz(10) amp=1500
//{0x00,{0x40,0x05,0x05,0xDC}},

//1.2Hz(41) amp=500
//{0x00,{0x00,{0x40,0x14,0x01,0xF4}},
//1.2Hz(41) amp=1500
{0x00,{0x40,0x14,0x05,0xDC}},

//2.4Hz(82) amp=500
//{0x00,{0x40,0x29,0x01,0xF4}},
//2.4Hz(82) amp=1500
//{0x00,{0x40,0x29,0x05,0xDC}},

//3Hz(100) amp=500
//{0x00,{0x40,0x29,0x01,0xF4}},
//3Hz(100) amp=1500
//{0x00,{0x40,0x29,0x05,0xDC}},


{0x01,{0x00,0x03,0x00,0x00}},
{0x02,{0x00,0x03,0x00,0x00}},
{0x03,{0x00,0x03,0x00,0x00}},
{0x04,{0x60,0x00,0x00,0x01}},
{0x05,{0x00,0x43,0x00,0x01}},
{0x06,{0x40,0x00,0x00,0x00}},
{0x07,{0x00,0x40,0x00,0x02}},
{0x08,{0x00,0x08,0x01,0x92}},
{0x09,{0x54,0x20,0x01,0xBA}},
{0x0A,{0x40,0x23,0x01,0xBB}},
{0x0B,{0x7F,0x41,0x00,0x00}},
{0x0C,{0x00,0x00,0x00,0x00}},
{0x0D,{0x00,0x00,0x00,0x40}},
{0x0E,{0x00,0x00,0x00,0x80}},
{0x0F,{0x00,0x00,0x00,0xC0}},
{0x10,{0x00,0x00,0x01,0x00}},
{0x11,{0x00,0x00,0x01,0x40}},
{0x12,{0x00,0x00,0x01,0x80}},
{0x13,{0x00,0x00,0x01,0xC0}},
{0x14,{0x00,0x00,0x02,0x00}},
{0x15,{0x00,0x00,0x02,0x40}},
{0x16,{0x00,0x00,0x02,0x80}},
{0x17,{0x00,0x00,0x02,0xC0}},
{0x18,{0x00,0x00,0x03,0x00}},
{0x19,{0x00,0x00,0x03,0x40}},
{0x1A,{0x00,0x00,0x03,0x80}},
};

const int isntruction_length_chorus = (sizeof(isntruction_set_chorus)/sizeof(Word));
