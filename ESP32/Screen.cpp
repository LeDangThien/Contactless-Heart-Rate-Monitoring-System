#include "Screen.h"
// Oled DEF
#define i2c_Address 0x3c 
#define SCREEN_WIDTH 128 
#define SCREEN_HEIGHT 64 
#define OLED_RESET -1   
#define NUMFLAKES 10
#define XPOS 0
#define YPOS 1
#define DELTAY 2
#define LOGO16_GLCD_HEIGHT 16
#define LOGO16_GLCD_WIDTH  16

Adafruit_SH1106G display = Adafruit_SH1106G(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);
void lcd_Hr(uint16_t a){
  display.setTextSize(1);
  display.setTextColor(SH110X_BLACK);
  display.setCursor(10, 5);
  display.fillRect(0,0,64,32,SH110X_WHITE);
  display.println("Nhip Tim");
  display.setCursor(20, 20);
  display.print(a);
  display.println("bpm");
  }
void lcd_Res(uint16_t b){
  display.setTextSize(1);
  display.setTextColor(SH110X_WHITE);
  display.setCursor(75, 5);
  display.fillRect(64,0,64,32,SH110X_BLACK);
  display.println("Nhip Tho");
  display.setCursor(85, 20);
  display.print(b);
  display.println("bpm");
  }
void lcd_BodyVAl(uint16_t c){
  display.setTextSize(1);
  display.setTextColor(SH110X_WHITE);
  display.setCursor(10, 35);
  display.fillRect(0,32,64,32,SH110X_BLACK);
  display.println("Body Val");
  display.setCursor(20, 50);
  display.print(c);
  display.println("val");
 }
void lcd_Dist(uint16_t d){
  display.setTextSize(1);
  display.setTextColor(SH110X_BLACK);
  display.setCursor(65, 35);
  display.fillRect(64,32,64,32,SH110X_WHITE);
  display.println("KhoangCach");
  display.setCursor(85, 50);
  display.print(d);
  display.println("cm");
  }
void lcd_State(String status)
{
  display.setTextSize(2);
  display.setTextColor(SH110X_WHITE);
  display.setCursor(50, 30);
  display.fillRect(0,0,128,64,SH110X_BLACK);
  display.print(status);
}
void lcd_Init(void)
{
  display.begin(i2c_Address, true);
  display.clearDisplay();
}
void lcd_Set(void)
{
  display.display();
}
void lcd_Clear(void)
{
  display.clearDisplay();
}