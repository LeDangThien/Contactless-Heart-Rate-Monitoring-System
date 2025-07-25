#include <stdint.h>
#include "My_time.h"

void time_increase(TimeCounter &t, uint16_t second){
  uint8_t source_min, source_hour;
  source_min=(t.seconds+second)/60;
  t.seconds=(t.seconds+second)%60;
  source_hour=(t.minutes+source_min)/60;
  t.minutes=(t.minutes+source_min)%60;
  t.hours=(source_hour+t.hours)%24;
}
String format2Digits(int number) {
  if (number < 10)
    return "0" + String(number);
  else
    return String(number);
}
String format_time (TimeCounter Time){
  return format2Digits(Time.hours) + ":" + format2Digits(Time.minutes) + ":" + format2Digits(Time.seconds); 
}
