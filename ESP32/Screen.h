#include <stdint.h>
#include <Arduino.h>
#include <Adafruit_SH110X.h>
#ifndef _SCREEN_H
#define _SCREEN_H

void lcd_Hr(uint16_t a);
void lcd_Res(uint16_t b);
void lcd_BodyVAl(uint16_t c);
void lcd_Dist(uint16_t d);
void lcd_Init(void);
void lcd_State(String status);
void lcd_Set(void);
void lcd_Clear(void);
#endif
