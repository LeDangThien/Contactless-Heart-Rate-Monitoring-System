#include <stdint.h>
#include <Arduino.h>
#ifndef _TIME_H
#define _TIME_H

struct TimeCounter{
  uint8_t hours=0;
  uint16_t minutes=0;
  uint16_t seconds=0;
};

void time_increase(TimeCounter &t, uint16_t second);
String format2Digits(int number);
String format_time (TimeCounter Time);

#endif
