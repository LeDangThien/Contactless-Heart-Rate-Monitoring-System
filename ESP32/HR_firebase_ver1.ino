#define ENABLE_USER_AUTH
#define ENABLE_DATABASE

#include <60ghzbreathheart.h>
#include "Ring_buffer.h"
#include <HardwareSerial.h>

#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include "Screen.h"
#include <string>

#include <FirebaseClient.h>
#include "ExampleFunctions.h"
#include "My_time.h"

#define WIFI_SSID "Wifi"
#define WIFI_PASSWORD "12345678"

#define API_KEY "AIzaSyDMjS9n_Pfg0_lVHaieAO2hHyihQFgj7PM"
#define USER_EMAIL "test@gmail.com"
#define USER_PASSWORD "12345678"
#define DATABASE_URL "https://heartrate-monitor-system-99d05-default-rtdb.asia-southeast1.firebasedatabase.app/" 

#define RX_Pin 18
#define TX_Pin 19


#define AVERAGE_ELEMENT_NUM 200



SSL_CLIENT ssl_client;

using AsyncClient = AsyncClientClass;
AsyncClient aClient(ssl_client);
UserAuth user_auth(API_KEY, USER_EMAIL, USER_PASSWORD, 3000 /* expire period in seconds (<3600) */);
FirebaseApp app;
RealtimeDatabase Database;
AsyncResult databaseResult;

String startTime ,fireDate, fireTime,UID;
JsonWriter writer;
String cachedStatus ;
SSL_CLIENT  stream_ssl_client;
using AsyncClient = AsyncClientClass;
AsyncClient streamClient(stream_ssl_client);

int dist = 0, lastResTime=0, curResTime=0;
int lastResetTime=0, curResetTime=0;
//set val
HardwareSerial mySerial = HardwareSerial(2);
BreathHeart_60GHz radar = BreathHeart_60GHz(&mySerial);


void add_hr_fb(void);
void add_resp_fb(void);
void add_dist_body_fb(void);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  while(!Serial);
  mySerial.begin(115200, SERIAL_8N1, RX_Pin, TX_Pin);
  radar.reset_func();
  delay(1000);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED)
  {
      Serial.print(".");
      delay(300);
  }

  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();


  Firebase.printf("Firebase Client v%s\n", FIREBASE_CLIENT_VERSION);
  set_ssl_client_insecure_and_buffer(ssl_client);
  set_ssl_client_insecure_and_buffer(stream_ssl_client);

  Serial.println("Initializing app...");
  initializeApp(aClient, app, getAuth(user_auth), auth_debug_print, "🔐 authTask");

  // Or intialize the app and wait.
  // initializeApp(aClient, app, getAuth(user_auth), 120 * 1000, auth_debug_print);

  app.getApp<RealtimeDatabase>(Database);

  Database.url(DATABASE_URL);
  streamClient.setSSEFilters("get,put,patch,keep-alive,cancel,auth_revoked");
  Database.get(streamClient, "communicate/signal", processData, true /* SSE mode (HTTP Streaming) */, "streamTask");
    //When the serial port is opened, the program starts to execute.

  Serial.println("SSD1306 Ready");  
  lcd_Init();
}

// varible
static int resetedFlag=0, newData=0;
uint32_t temp_tick=0;
TimeCounter timeStart;
String measure_path;
bool taskComplete = false;
unsigned long sendDataPrevMillis = 0;
unsigned long tick_gettime =0; 
unsigned long count = 0;
uint8_t flag_run =0;
uint8_t flag_idle =0;
uint16_t resultHR =0,resultResp =0,resultBody = 0,resultDist =0;
int R=400, errorData = 0;
float Q=0.01, resultHR_Kalman =80;
double K, P = 100 +Q;

void loop()
{
  app.loop();

  if(app.ready()){
    object_t status_obj;
    if(cachedStatus == "RUNNING")
    {
      if(flag_run==0)
      {
        startTime = Database.get<String>(aClient,"/communicate/start_time") ;
        UID = Database.get<String>(aClient,"/communicate/UID");
        fireTime=startTime.substring(11,startTime.length());
        fireDate= startTime.substring(0,10);
        int firstColon = fireTime.indexOf(':');
        int secondColon = fireTime.indexOf(':', firstColon + 1);
        timeStart.hours  = fireTime.substring(0, firstColon).toInt();                
        timeStart.minutes = fireTime.substring(firstColon + 1, secondColon).toInt(); 
        timeStart.seconds = fireTime.substring(secondColon + 1).toInt();
        lcd_Clear();

        writer.create(status_obj, "ESP32_State", cachedStatus);
        Database.update(aClient, "communicate/", status_obj);
        Serial.println(cachedStatus);
        measure_path="/user_infor/"+ UID +"/measurement/"+ startTime;
        flag_run=1;
        K=0;
        P=100+Q;
        resultHR_Kalman =80;
        radar.reset_func();
        while(mySerial.available()>0){
          mySerial.read();
        }
        flag_idle =0;
        tick_gettime=millis();
      }
      else{
        if(mySerial.available()){
          newData=1;
        }

        radar.Breath_Heart();
        add_hr_fb();
        add_resp_fb();
        add_dist_body_fb();
        lcd_Hr((int)resultHR_Kalman);
        lcd_Res(resultResp);
        lcd_Dist(resultDist);
        lcd_BodyVAl(resultBody);
        lcd_Set();
        lcd_Clear();
      }
    }
    else{
      if(flag_idle ==0)
      {
        writer.create(status_obj, "ESP32_State", cachedStatus);
        Database.update(aClient, "communicate/", status_obj); 
        flag_run=0;
        flag_idle=1;
        newData=0;
        lcd_Clear();
        lcd_State(cachedStatus);
        lcd_Set();
      }
    }
  }                 
}


void processData(AsyncResult &aResult)
{
    if (!aResult.isResult())
        return;

    if (aResult.isEvent())
    {
        Firebase.printf("Event task: %s, msg: %s, code: %d\n", aResult.uid().c_str(), aResult.eventLog().message().c_str(), aResult.eventLog().code());
    }

    if (aResult.isDebug())
    {
        Firebase.printf("Debug task: %s, msg: %s\n", aResult.uid().c_str(), aResult.debug().c_str());
    }

    if (aResult.isError())
    {
        Firebase.printf("Error task: %s, msg: %s, code: %d\n", aResult.uid().c_str(), aResult.error().message().c_str(), aResult.error().code());
    }

    if (aResult.available())
    {
        RealtimeDatabaseResult &RTDB = aResult.to<RealtimeDatabaseResult>();
        if (RTDB.isStream())
        {
          if(RTDB.to<String>()!="null"){
            cachedStatus = RTDB.to<String>();
          }
          
          Serial.printf("process: ");
          Serial.println(cachedStatus);
        }
        else
        {
            Serial.println("----------------------------");
            Firebase.printf("task: %s, payload: %s\n", aResult.uid().c_str(), aResult.c_str());
        }

#if defined(ESP32) || defined(ESP8266)
        Firebase.printf("Free Heap: %d\n", ESP.getFreeHeap());
#elif defined(ARDUINO_RASPBERRY_PI_PICO_W)
        Firebase.printf("Free Heap: %d\n", rp2040.getFreeHeap());
#endif
    }
}

void add_hr_fb(void){
  TimeCounter tempTime = timeStart;
  if(radar.sensor_report == HEARTRATEVAL)
  {
    resultHR=radar.heart_rate;
    object_t json;
    String path = measure_path+"/radar_heart_rate/";
    time_increase(tempTime, (millis()-tick_gettime)/1000);
    writer.create(json, format_time(tempTime), resultHR);
    Database.update(aClient, path.c_str(), json);
    if(resultHR!=0 && errorData == 0)
    {
      K= P/(P+R);
      resultHR_Kalman = resultHR_Kalman + K*(resultHR - resultHR_Kalman);
      P = (1-K)* P + Q;
    }
    else if(errorData!=0)
    {
      errorData --;
    }
    // Serial.println((millis()-tick_gettime)/1000);
    Serial.printf("Heart rate: ");
    Serial.print(resultHR, DEC);
    Serial.println("bpm");
    // Serial.println(heartRate.head);
    // Serial.println(heartRate.tail);
    newData=0;
  }

}

void add_resp_fb(void){
  TimeCounter tempTime = timeStart;
  if(radar.sensor_report == BREATHVAL)
  {
    resultResp=radar.breath_rate;
    object_t json;
    String path = measure_path+"/respiratory_rate/";
    time_increase(tempTime, (millis()-tick_gettime)/1000);
    writer.create(json, format_time(tempTime), resultResp);
    Database.update(aClient, path.c_str(), json);
    
    Serial.printf("Respiratory rate: ");
    Serial.print(resultResp, DEC);
    Serial.println("bpm");
    // Serial.println(heartRate.head);
    // Serial.println(heartRate.tail);
    newData=0;
  }

}

void add_dist_body_fb(void){
  object_t json;
  TimeCounter tempTime = timeStart;
  if(newData==1){
    if(radar.getMsg()[0]==HUMAN_PSE_RADAR)
    {
      if(radar.getMsg()[1]==DISTANCE){
        radar.sensor_report=DISVAL;
        resultDist=(int)(radar.getMsg()[4] << 8 | radar.getMsg()[5]);
        
        String path = measure_path+"/distance/";
        time_increase(tempTime, (millis()-tick_gettime)/1000);
        writer.create(json, format_time(tempTime), resultDist);
        Database.update(aClient, path.c_str(), json);
        
        Serial.printf("Distance: ");
        Serial.print(resultDist, DEC);
        Serial.println("cm");
        newData=0;
        // Serial.print(dist, DEC);
        // Serial.println("cm");
      }
      else if(radar.getMsg()[1]==BODY_SIG){
        radar.sensor_report=BODYVAL;
        radar.bodysign_val=radar.getMsg()[4];
        resultBody=radar.getMsg()[4];
        String path = measure_path+"/body_signal/";
        time_increase(tempTime, (millis()-tick_gettime)/1000);
        writer.create(json, format_time(tempTime), resultBody);
        Database.update(aClient, path.c_str(), json);
        if(radar.bodysign_val>90) errorData==3;
        Serial.printf("Body: ");
        Serial.println(resultBody, DEC);
        newData=0;
        
      }
    }
  }

}
