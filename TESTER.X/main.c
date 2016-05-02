
// DOM-IGNORE-END


// *****************************************************************************
// *****************************************************************************
// Section: Included Files
// *****************************************************************************
// *****************************************************************************
//#include <libpic33f.h>
//#include <libpic30.h>
#include <string.h>

#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <stdbool.h>

#include <math.h>

#include "app.h"

#define __delay_us(x) _delay((unsigned long)((x)*(_XTAL_FREQ/4000000.0)))

#define ONE_VOLT 310
#define ONE_TENTH_VOLT 31
#define ONE_HUNDREDTH_VOLT 3

#define N1_default 2            // N1, N2, M values after clearing CLKDIV 
#define N2_default 2            // 
#define M_default  2            // 
//currently at 42105.263
#define Ffrc 7372800UL         // FRC oscillator frequency, Hz 
#define Fosc 16000000UL       // Desired system clock frequency, Hz 
//#define Fosc 16000000UL
#define FCY Fosc/2              // MCU instruction cycle frequency 
#define BAUDRATE 115200
#define BRG FCY/(16*BAUDRATE)-1

#define PI 3.14159265358979323846
#define TO_RAD (3.1415926536 / 180)
#define R 6371

//#define MAG_DECLINATION (0.07097672291+3.0*2*3.14159265358979323846/360.0)
#define MAG_DECLINATION 0
#define delay_us(x) for (j = 0; j < x;j++) {Nop();Nop();}
#define delay_ms delayMs
#define TICKS_PER_METER 55.39411
#define countMax  3000 //max count for ir delay
int RXct = 0;
char RXmsg[200];
int i, j;

//#define RS_PIN _LATE8
//#define E_PIN _LATE7

#define BITMASK(in, d) ((in&(1<<d))>>d) 
#define RS_PIN LATEbits.LATE7
//#define RS_PIN LATEbits.LATE4
#define E_PIN LATAbits.LATA12 
//LCD
#define LCDON 0x0F
#define LCDCLR 0x01
#define TWOLINE 0x38
#define CURMOV 0xFE
 
//pwm
/*#define PWM1 PORTEbits.RE0
#define PWM2 PORTEbits.RE6
#define PWM1_DIR PORTDbits.RD8
#define PWM2_DIR PORTDbits.RD11*/

#define PWM1 LATEbits.LATE0
#define PWM2 LATEbits.LATE6
#define PWM1_DIR LATDbits.LATD8
#define PWM2_DIR LATDbits.LATD11
#define ROVER_LEN 5 //rover buffer len
#define ROVER_LEN_D 5.0
//void delay_ms(unsigned int n);
void send_nibble(unsigned char nibble);
void send_command_byte(unsigned char byte);
void send_data_byte(unsigned char byte);
double ultraSonicPing(void);
void updateHeading(void);

double dist(double th1, double ph1, double th2, double ph2);
double convertGPSToDeg( double input);
double bearing(double lat1, double lon1, double lat2, double lon2);
// *****************************************************************************
// *****************************************************************************
// Section: File Scope Variables and Functions
// *****************************************************************************
// *****************************************************************************

void tim2_init(void);
void Update_LCD ( void ) ;
void SYS_Initialize ( void ) ;
extern void ConvertADCVoltage ( unsigned int  ) ;
extern void Hex2Dec ( unsigned char ) ;
double mod(double a, double n);

int i2c_write3(char a, char b, char c);
int i2c_write2(char a, char b);
int i2c_read6(char a, char *out);
void i2c_init(void);
double headingDiff(double desired, double actual);
void tim1_init(void);
void __attribute__((__interrupt__)) _U2RXInterrupt( void );
void UART_Initialize( void );
void TX_str( char * );
void TX_char( char );

void lcd_clear(void);
void lcd_init(void);

void send_command_byte(unsigned char byte);
void send_data_byte(unsigned char byte);
void print_lcd(char * buff);
void send_byte(unsigned char in);
void set_byte(unsigned char in);
void lcd_line2(void);
//T1 variables
int motorStopFlag = 1;
int motorDuty =0;
int motorPeriod = 20;
int motorCount = 0;
int tCount = 0;

int ultraSonicEn; //used for T2 interupt distance calculation
long long int ultraSonicCount;
long long int ultraSonicDelayCount; //used to delay in the drive mode
int ultraSonicDelayEnable;

char outputBuf[80];
char outputBuf2[80];
char cmdBuf[80];
double roverLat=0;
double roverLog = 0;
double roverlat[5];
double roverlog[5];
int roverlogIndex;
int roverlatIndex;


double gpsLonge[50];
double gpsLat[50];
int numGPSpoints=0;
int ipcommand=0;
int waypointsReady = 0;
long int gpsReq = 0;
int gpsLock;    //block until gps locked
double roverHeading;

char setPDP[] = "at+csocksetpn=2\r";
char openNet[] = "at+netopen\r";
char openConnection[] = "AT+CIPOPEN=0,\"TCP\",\"128.10.12.141\",3112\r";
char sendTCP1[] = "at+cipsend=0,3\r";
char sendTCP[] = "at+cipsend=0,";

char ipclose[] = "at+cipclose=0";
char netclose[] = "at+netclose";

char endGPS[] = "AT+CGPS=0\r";
char startGPS[] = "AT+CGPS=1\r";
char startGPShot[] = "AT+CGPSHOT\r";
char getGPS[]  = "AT+CGPSINFO\r";
APP_DATA appData = {
                    .messageLine1 = "Explorer 16 Demo" ,
                    .messageLine2 = "Press S3 to cont" ,
                    .messageTime = "Time 00: 00: 00 " ,
                    .messageADC = " Pot = 0.00 Vdc "
} ;

// *****************************************************************************
// *****************************************************************************
// Section: Main Entry Point
// *****************************************************************************
// *****************************************************************************

void delayMs(long int i){
    int j, k;
    for(j = 0; j <i;j++){
        for(k = 0;k<1000;k++);
    }
}

int main ( void )
{

    //TRISA = 0b0010000000000000;
    TRISA = 0b00001111;
    TRISAbits.TRISA12 = 0;
    
    TRISCbits.TRISC1 = 0;
    TRISCbits.TRISC2 = 0;
    TRISCbits.TRISC3 = 0;
    TRISCbits.TRISC4 = 0;
    
    //TRISD = 0;
    TRISD = 0b0000000000010000; // RD11: DIR2; RD8: PWM2
    
    //TRISE = 0b00001000;
    TRISE = 0b00001000; // RE6: DIR1; RE0: PWM1
    TRISEbits.TRISE7 = 0; 
   
    //LCD an
    _PCFG16 = 1; // AN16 is digital
    _PCFG17 = 1; // AN17 is digital
    _PCFG18 = 1; // AN18 is digital
    _PCFG19 = 1; // AN19 is digital
    _PCFG20 =1;
    _PCFG31=1;
    
    //pwm analog select
    _PCFG24=1;
    _PCFG30=1;
    
    //RE4 for lcd r/s
     _PCFG28=1;
     TRISEbits.TRISE4 = 0;
    
    AD1PCFGHbits.PCFG28 = 1;
    AD1PCFGHbits.PCFG27 = 1;
    AD1PCFGHbits.PCFG30 = 1;
    AD1PCFGHbits.PCFG24 = 1;
    AD1PCFGH = 0x0020;
    TRISAbits.TRISA13 = 1;
    //set pwm low
    PWM1=0;
    PWM2=0;
    
    SYS_Initialize ( ) ;
    
    CLKDIVbits.FRCDIV = 0;
    CLKDIVbits.PLLPOST = 0;  // N2 = 2
    CLKDIVbits.PLLPRE = 0;  // N1 = 2
    PLLFBD = (Fosc*N1_default*N2_default/Ffrc) - M_default; // M = 8 -> Fosc = 7.3728 MHz * 8 / 2 / 2 = 16 MHz
    while(!OSCCONbits.LOCK);	// Wait for PLL to lock
	RCONbits.SWDTEN = 0;      // Disable Watch Dog Timer
    
    //TRISF = 0;
    gpsLock = 0;
   
    lcd_init();
    print_lcd("Initializing");
    //delay_ms(500);
    //lcd_clear();
    
    /*lcd test
    char line1[] = " On Route ";
    char line2[] = " Arrived ";
    //send_command_byte(0xFF);
    //while(1){send_command_byte(0xFF);send_data_byte(0);}
   // delay_ms(2);
    //send_command_byte(0x02); // Go to start of line 1
    //send_command_byte(0b00001111); // Display: display on, cursor on, blink on
    //send_command_byte(0b00000110); // Set entry mode: ID=1, S=0
    //send_command_byte(0b00000001); // Clear display
    
    print_lcd(line1);
    delay_ms(5000);
    lcd_clear();
    print_lcd(line2);
    //send_command_byte(0xC0); // Go to start of line 1
    //while(1){send_command_byte(0b00000001);}
    while(1);*/
    
    /*int a;
    long long int ct;
    int i;
    int j=0;*/
    //i2c init

    
    //uart init
    UART_Initialize();
    delayMs(100);
    
    
    /* while(1){//test for delay ms configuration
        //PORTDbits.RD1 = 1;
        //delayUs(10);
        //for(i = 0;i <7;i++);
        delayMs(10);
        PORTDbits.RD12 = 1;
        //for (i = 0; i < 1000; i++);
        //PORTDbits.RD1 = 0;
        //for(i = 0;i <7;i++);
        delayMs(10);
        
        PORTDbits.RD12 = 0;
        //for (i  = 0; i < 1000; i++);
    }*/
    
    //ultrasonic test
    
   /* while(1){
        long double x;
        delayMs(500);
        PORTEbits.RE4 = 1;                  //TRIGGER HIGH
        PORTDbits.RD5 = 1; 
        delay_us(10);                     //10uS Delay 
        lcd_clear();
        ultraSonicEn = 1;
        ultraSonicCount = 0;

        PORTEbits.RE4 = 0;                  //TRIGGER LOW
        PORTDbits.RD5 = 0; 
        
        
        while (!PORTDbits.RD4);              //Waiting for Echo
       IEC0bits.T2IE = 1; //enable timer
        while(PORTDbits.RD4);// 
        IEC0bits.T2IE = 0; //disable timer
        x = ultraSonicCount/TICKS_PER_METER;
        sprintf(outputBuf,"%lf",x); 
        print_lcd(outputBuf);
    }*/

    //TX_str(endGPS);
    //delayMs(3000);
    //TX_str(startGPS);
    /*TX_str(startGPShot);
    delayMs(2000);
    while ( !gpsLock ) {
        TX_str(getGPS);
        delayMs(500);
    }*/
    
   //TCP code
    
    TX_str(openNet);
    delayMs(5000);
    TX_str(openConnection);
    delayMs(5000);
 
    // while(!BUTTON_IsPressed ( BUTTON_S3 ));
    //TX_str(sprintf("%s%d\r",sendTCP, strlen("10")));
    sprintf(outputBuf2,"2\n%lf,%lf\n\r",roverLog,roverLat );
    sprintf(cmdBuf,"%s%d\r", sendTCP,strlen(outputBuf2));
         
    TX_str(cmdBuf);
    delayMs(3000);
    TX_str(outputBuf2);
    while(!waypointsReady || !gpsLock);
    
    
    //while(1);
    delayMs(7000);
    lcd_clear();
    print_lcd("waypoints locked");
    
    i2c_init();
    //i2c_write3(0x3c, 0x00, 0x70);
    i2c_write3(0x3c, 0x00, 0b00011000);
    //i2c_write3(0x3c, 0x01, 0b11000000);
    i2c_write3(0x3c, 0x01, 0xA0);
    i2c_write3(0x3c, 0x02, 0x00);
    
    //timer init, do this after other initializations
    motorDuty=0;
    tim1_init();
    tim2_init();
   
    /*
    double angleTolerance = 8.0;
     motorDuty=2;
 
    while (1){ //adjust


        double angleDiff = headingDiff(0,roverHeading );


        if (angleDiff > 0 ||abs(angleDiff) > 175 ){ //turn left

            PWM1_DIR = 0; //left            NOTE: 0 is forward, 1 is reverse
            PWM2_DIR = 1; //right
        } else{ // turn right

            PWM1_DIR = 1;
            PWM2_DIR = 0;
        }
        if (abs(angleDiff) < angleTolerance){
            motorDuty = 0;
            //break;
        }else{
            //motorDuty =4;
            motorDuty =2;
        }   
        delayMs(13);
        updateHeading();
    }*/
    
    //hardcoded gps for tcpip test
    
    
     /*while(1){
//        PORTDbits.RD5 = 1;
//        for (a = 0; a < (100/33); a++) {}
//        PORTDbits.RD5 = 0;
//        for (a = 0; a < (100/33); a++) {}
        ct = 0;
        //TMR2 = 0;//Timer Starts
        delayMs(60);
        PORTEbits.RE4 = 1;                  //TRIGGER HIGH
        PORTDbits.RD5 = 1; 
        delay_us(15);
                           //10uS Delay 
        PORTEbits.RE4 = 0;                  //TRIGGER LOW
        PORTDbits.RD5 = 0; 
        while (!PORTDbits.RD4){              //Waiting for Echo
            ct++;
        }
            //T2CONbits.TON = 1;
        while(PORTDbits.RD4) {
            ct++;
        }// {
        //T2CONbits.TON = 0;
        sprintf(outputBuf,"%lld", ct); 
        //a = TMR2;
        //a = a / 58.82;
        
        //long int p;
        //for(p = 0; p <100000; p++);
        
    }*/
    //tim1_init();
    //while ( 1 );
    //UART_Initialize();
    
    /* Infinite Loop */
    /*
     long int i;
    while ( 1 )
    {//test for delay ms configuration
        //PORTDbits.RD1 = 1;
        delayMs(10);
        PORTDbits.RD12 = 1;
        //for (i = 0; i < 1000; i++);
        //PORTDbits.RD1 = 0;
        delayMs(10);
        PORTDbits.RD12 = 0;
        //for (i = 0; i < 1000; i++);
    }*/
    //IEC0bits.T1IE = 0;
    
    //IEC0bits.T1IE = 1;
    //motorDuty =2;
    motorStopFlag = 0; //ready to go
    char tempBuf1[50];
    int wapointsVisited;
    double desiredHeading = 0;
    double dToWaypoint = 99999.9;
    double angleTolerance = 6.0;
    for (wapointsVisited =0; wapointsVisited<numGPSpoints;wapointsVisited++ )
    {
        dToWaypoint = 99999.9;
        while (1)
        {

            int f;
            roverLog = 0;
            roverLat = 0;
            for(f = 0; f < ROVER_LEN; f++){
                roverLog+=roverlog[f]/ROVER_LEN_D;
                roverLat+=roverlat[f]/ROVER_LEN_D;
            }
            
            
            dToWaypoint = dist(convertGPSToDeg(roverLat),convertGPSToDeg(roverLog),convertGPSToDeg(gpsLat[wapointsVisited]),convertGPSToDeg(gpsLonge[wapointsVisited]));
            if(dToWaypoint <= 3.0){break;} //go to next waypoint

            desiredHeading = 330.0;
            desiredHeading = bearing(convertGPSToDeg(roverLat),convertGPSToDeg(roverLog),convertGPSToDeg(gpsLat[wapointsVisited]),convertGPSToDeg(gpsLonge[wapointsVisited]));
            
            updateHeading();
            
                
            if (!(abs(headingDiff(desiredHeading,roverHeading )) < angleTolerance))
            {
                motorDuty = 0;  //stop
                delayMs(300);
                while (1) //ADJUSTMENT LOOP
                {
                   
                    double angleDiff = headingDiff(desiredHeading,roverHeading );

                    //double dist = ultraSonicPing();
                    /*sprintf(tempBuf1,"%f",dist);
                    lcd_clear();
                    print_lcd(tempBuf1);*/
                     if (abs(angleDiff) < angleTolerance){
                        motorDuty = 0;
                        /*sprintf(tempBuf1,"%f",roverHeading);
                        IEC0bits.T1IE = 0;
                        print_lcd(tempBuf1);
                        IEC0bits.T1IE = 1;*/
                        break;
                    }
                    if (angleDiff > 0 || abs(angleDiff) > 175 ){ //turn right
                        /*PORTDbits.RD4 = 1; //right 0 is forwards, 1 i backwards
                        PORTDbits.RD2 = 1;
                        PORTDbits.RD8 = 0; //left*/
                        PWM1_DIR = 0; //left            NOTE: 0 is forward, 1 is reverse
                        PWM2_DIR = 1; //right
                    } else{ // turn left
                        /*PORTDbits.RD4 = 0; //right 0 is forwards, 1 i backwards
                        PORTDbits.RD2 = 0;
                        PORTDbits.RD8 = 1; //left*/
                        PWM1_DIR = 1;
                        PWM2_DIR = 0;
                    }
                   
                       
                    motorDuty =3;
                     
                    delayMs(15);
                    updateHeading();
        //        sprintf(tempBuf1,"%f",roverHeading);
        //        IEC0bits.T1IE = 0;
        //        lcd_clear();
        //        print_lcd(tempBuf1);
        //        IEC0bits.T1IE = 1;
                //delayMs(1000);
                //for(p = 0; p <100000; p++);
                }
            }
            PWM1_DIR = 0; //left            NOTE: 0 is forward, 1 is reverse
            PWM2_DIR = 0;

            motorDuty = 5;
            ultraSonicDelayEnable = 1;
            //frequency is around 20kHz
            while (ultraSonicDelayCount < 60000){ //for 4 seconds poll ultrasonic and check for obsticles
                long double x;
                //PORTEbits.RE4 = 1;                  //TRIGGER HIGH
                PORTDbits.RD5 = 1; 
                delay_us(10);                     //10uS Delay 
                lcd_clear();
                ultraSonicEn = 1;
                ultraSonicCount = 0;

                //PORTEbits.RE4 = 0;                  //TRIGGER LOW
                PORTDbits.RD5 = 0; 


                while (!PORTDbits.RD4);              //Waiting for Echo
               IEC0bits.T2IE = 1; //enable timer
                while(PORTDbits.RD4);// 
                IEC0bits.T2IE = 0; //disable timer
                ultraSonicEn = 0;
                x = ultraSonicCount/TICKS_PER_METER;
                if (x <= 1.4){
                    motorDuty = 0;
                }else{
                    motorDuty = 5;
                }
                delayMs(200);
            }
            ultraSonicDelayEnable = 0;
            ultraSonicDelayCount = 0;
        }
        
        motorDuty = 0;
        delayMs(1000);
        //IEC0bits.T1IE = 0;
        lcd_clear();
        char buf3[40];
        sprintf(buf3, "reached %d", wapointsVisited);
        print_lcd(buf3);
        delayMs(3000);
        //IEC0bits.T1IE = 1;
        
    }
    //IEC0bits.T1IE = 0;
    lcd_clear();
    print_lcd("ARRIVED!!!");
    //IEC0bits.T1IE = 1;
    while (1);
}

void updateHeading(void){ //note, we need to delay for 67ms before doing another reating
    char ret[15];
    i2c_read6(0x3d,ret);
    i2c_write2(0x3c, 0x03);
    //IEC0bits.T1IE = 1;
    delayMs(67);
    //lcd_clear();
    //sprintf(outputBuf,"%02x %02x, %02x %02x, %02x %02x", ret[0], ret[1], ret[2], ret[3], ret[4], ret[5]);
    int x = (ret[0]<<8)|ret[1];
    //int z = (ret[2]<<8)|ret[3];
    int y = (ret[4]<<8)|ret[5];

    float heading = atan2(y, x);
    heading = heading - MAG_DECLINATION;
    //magnetic declination for west lafayette = 4 degrees, 9"
    if(heading < 0){
         heading += 2*PI;
    }

    roverHeading = heading* 180.0 / PI;
}
double ultraSonicPing(void){
    long double x;
    PORTEbits.RE4 = 1;                  //TRIGGER HIGH
    PORTDbits.RD5 = 1; 
    delay_us(10);                     //10uS Delay 
    ultraSonicEn = 1;
    ultraSonicCount = 0;

    PORTEbits.RE4 = 0;                  //TRIGGER LOW
    PORTDbits.RD5 = 0; 


    while (!PORTDbits.RD4);              //Waiting for Echo
   IEC0bits.T2IE = 1; //enable timer
    while(PORTDbits.RD4);// 
    IEC0bits.T2IE = 0; //disable timer
    x = ultraSonicCount/TICKS_PER_METER;
    return x;
    
}
void print_lcd(char * buff)
{
    int i = 0;
    while(*(buff + i) != NULL)
    {
        send_data_byte(*(buff + i));
        i++;
    }
}

void send_data_byte(unsigned char byte)
{
    RS_PIN = 1;
    delay_us(100);
    send_byte(byte);
    
}

void lcd_clear(void){
    send_command_byte(LCDCLR);
    
}

void lcd_line2(void){
    send_command_byte(0xC0);
    
}
void lcd_line1(void){
    send_command_byte(0x02);
    
}
void lcd_init(void){
    RS_PIN = 0;
    E_PIN = 0;
    /*send_command_byte(0b00001111); // Display: display on, cursor on, blink on
    send_command_byte(0b00000001); // Clear display
    send_command_byte(0b00000110); // Set entry mode: ID=1, S=0)*/
    send_command_byte(LCDON);
    send_command_byte(TWOLINE);
    send_command_byte(LCDCLR);
}

void set_byte(unsigned char in){
    
    LATDbits.LATD1 = BITMASK(in, 0); //DB0 -- RD1
    LATDbits.LATD2 = BITMASK(in, 1); //DB1 -- RD2
    LATDbits.LATD12 = BITMASK(in, 2); //DB2 -- RD12
    LATDbits.LATD13 = BITMASK(in, 3); //DB3 -- RD13
    LATCbits.LATC1 = BITMASK(in, 4); //DB4 -- RC1
    LATCbits.LATC2 = BITMASK(in, 5); //DB5 -- RC2
    LATCbits.LATC3 = BITMASK(in, 6); //DB6 -- RC3 
    LATCbits.LATC4 = BITMASK(in, 7); //DB7 -- RC4
    
}
void send_byte(unsigned char in)
{
    // Note: data is latched on falling edge of pin E
    //LATC = nibble;
    set_byte(in);
    E_PIN = 1;
    E_PIN = 0;
    delay_us(40);
}

void send_command_byte(unsigned char byte)
{
    RS_PIN = 0;
   set_byte(byte);
   delay_ms(2);
    E_PIN = 1;
    delay_ms(3);
    E_PIN = 0;
    delay_ms(2); // Enough time even for slowest command
}

void Hex2Dec ( unsigned char count )
{
    /* reset values */
    appData.hunds = 0 ;
    appData.tens  = 0 ;
    appData.ones = 0 ;

    while ( count >= 10 )
    {

        if ( count >= 200 )
        {
            count -= 200 ;
            appData.hunds = 0x02 ;
        }

        if (count >= 100)
        {
            count -= 100 ;
            appData.hunds++ ;
        }

        if (count >= 10 )
        {
            count -= 10 ;
            appData.tens++ ;
        }
    }

    appData.ones = count ;
}

void tim2_init(void){
    
    IPC1bits.T2IP = 0x0004 ;

    T2CONbits.TON = 0; // Disable Timer
    
    TMR2 = 0;
    PR2 = 0x0030; //currently 9.417k
    //period = 48
    //9.417k*48 = 452.016k
    //2/340*9.417*1000 = 2659.0 ticks/M
    T2CONbits.TCKPS = 0b01; // Select 1:8 Prescaler
    
    
    T2CONbits.TCS = 0; // Select internal instruction cycle clock (Fosc/2)
    T2CONbits.TGATE = 0; // Disable Gated Timer mode
    T2CONbits.TCS = 0;
    IFS0bits.T2IF = 0 ;

    T2CONbits.TON = 1;
    
    IEC0bits.T2IE = 0;
}
void tim1_init(void){
    
    IPC0bits.T1IP = 0x0004 ;

    T1CONbits.TON = 0; // Disable Timer
    
    /*TMR1 = 0;
    PR1 = 0x0016;
    T1CONbits.TCKPS = 0b01; // Select 1:8 Prescaler*/
    TMR1 = 0;
    PR1 = 0x005;
    T1CONbits.TCKPS = 0b10; // Select 1:8 Prescaler
    
    T1CONbits.TCS = 0; // Select internal instruction cycle clock (Fosc/2)
    T1CONbits.TGATE = 0; // Disable Gated Timer mode
    
    T1CONbits.TON = 1;
    IFS0bits.T1IF = 0 ;

    IEC0bits.T1IE = 1 ;
}
void __attribute__ ( ( __interrupt__ , auto_psv ) ) _T2Interrupt ( void ){
    /*if (motorStopFlag == 0){
            motorStopFlag = 1;
    }else {
        motorStopFlag = 0;
    }
    PORTDbits.RD12 = motorStopFlag;*/

    if ( ultraSonicEn == 1){
        ultraSonicCount++;
    }
    IFS0bits.T2IF = 0 ; // reset Timer 1 interrupt flag
}
void __attribute__ ( ( __interrupt__ , auto_psv ) ) _T1Interrupt ( void )
{

    
   /*if (motorStopFlag == 0){
            motorStopFlag = 1;
    }else {
        motorStopFlag = 0;
    }
    PORTDbits.RD12 = motorStopFlag;*/
    
    //exactly 1 sec, %13636
    //gpsReq = (gpsReq+1)%10000;
    
    if (ultraSonicDelayEnable){
        ultraSonicDelayCount++;
    }
    if (motorStopFlag == 1){
        PWM1 = 0;
        PWM2 = 0;
    }else {
        if (motorCount < motorDuty){
            PWM1 = 1;
            PWM2 = 1;
        }else {
            PWM1 = 0;
            PWM2 = 0;
        }
        motorCount = (motorCount+1)%motorPeriod;
    }
    
    if (tCount < countMax){
        tCount++;
    }
    
    if(PORTAbits.RA13 == 0 && !(tCount < countMax)){
       
        if (motorStopFlag == 0){
            motorStopFlag = 1;
        }else {
            motorStopFlag = 0;
        }
        //PORTDbits.RD12 = motorStopFlag;
        
        //pwm1 = RE0, DIR1=RE6
        //pwm2 = rd8, dir2= rd11
        tCount = 0;
    }
    IFS0bits.T1IF = 0 ; // reset Timer 1 interrupt flag
}

void i2c_init(void)
{

// Configre SCA/SDA pin as open-drain
	ODCGbits.ODCG2=1;
	ODCGbits.ODCG3=1;

	I2C1CONbits.A10M=0;
	I2C1CONbits.SCLREL=1;
	I2C1BRG=17;

	I2C1ADD=0;
	I2C1MSK=0;

	I2C1CONbits.I2CEN=1;
	//IEC1bits.MI2C1IE = 1;
  	IFS1bits.MI2C1IF = 0;
}

int i2c_read6(char a, char *out){
    
    int i;
    I2C1CONbits.SEN = 1;
    while (I2C1CONbits.SEN);
    IFS1bits.MI2C1IF = 0; // Clear Interrupt
    I2C1TRN = a; // load the outgoing data byte
    while(I2C1STATbits.TRSTAT);
    if (I2C1STATbits.ACKSTAT == 1) {//error
        return 1;
    }
    //IEC0bits.T1IE = 0;
    for(i = 0;i < 6; i++){
        I2C1CONbits.RCEN=1;
        while (I2C1CONbits.RCEN);
        out[i] = I2C1RCV;
        //while(I2C1STATbits.TRSTAT);
        if (I2C1STATbits.ACKSTAT == 1){//error
            return 1;
        }
        if(i == 5) {
            I2C1CONbits.ACKDT=1;		// No ACK		
        } else {
            I2C1CONbits.ACKDT=0;		// ACK
        }
        I2C1CONbits.ACKEN=1;
         while(I2C1CONbits.ACKEN);
    }
    //IEC0bits.T1IE = 1;
    I2C1CONbits.PEN = 1;
    while (I2C1CONbits.PEN);
    I2C1CONbits.RCEN = 0;
    IFS1bits.MI2C1IF = 0; // Clear Interrupt
    I2C1STATbits.IWCOL = 0;
    I2C1STATbits.BCL = 0;
    return 0;
}

int i2c_write2(char a, char b){
    I2C1CONbits.SEN = 1;
    while (I2C1CONbits.SEN);
    IFS1bits.MI2C1IF = 0; // Clear Interrupt
    I2C1TRN = a; // load the outgoing data byte
    while(I2C1STATbits.TRSTAT);
    if (I2C1STATbits.ACKSTAT == 1) {//error
        return 1;
    }
    I2C1TRN = b; // load the outgoing data byte
    while(I2C1STATbits.TRSTAT);
    if (I2C1STATbits.ACKSTAT == 1) {//error
        return 1;
    }
    
    I2C1CONbits.PEN = 1;
    while (I2C1CONbits.PEN);
    I2C1CONbits.RCEN = 0;
    IFS1bits.MI2C1IF = 0; // Clear Interrupt
    I2C1STATbits.IWCOL = 0;
    I2C1STATbits.BCL = 0;
    return 0;
}
int i2c_write3(char a, char b, char c){
    I2C1CONbits.SEN = 1;
    while (I2C1CONbits.SEN);
    IFS1bits.MI2C1IF = 0; // Clear Interrupt
    I2C1TRN = a; // load the outgoing data byte
    while(I2C1STATbits.TRSTAT);
    if (I2C1STATbits.ACKSTAT == 1) {//error
        return 1;
    }
    I2C1TRN = b; // load the outgoing data byte
    while(I2C1STATbits.TRSTAT);
    if (I2C1STATbits.ACKSTAT == 1) {//error
        return 1;
    }
    I2C1TRN = c; // load the outgoing data byte
    while(I2C1STATbits.TRSTAT);
    if (I2C1STATbits.ACKSTAT == 1) {//error
        return 1;
    }
    I2C1CONbits.PEN = 1;
    while (I2C1CONbits.PEN);
    I2C1CONbits.RCEN = 0;
    IFS1bits.MI2C1IF = 0; // Clear Interrupt
    I2C1STATbits.IWCOL = 0;
    I2C1STATbits.BCL = 0;
    
    return 0;
}

double headingDiff(double desired, double actual){
    return mod((desired-actual)+180, 360)-180;
    
}

double bearing(double lat1, double lon1, double lat2, double lon2){
    
    return atan2(cos(lat1)*sin(lat2)-sin(lat1)*cos(lat2)*cos(lon2-lon1), sin(lon2-lon1)*cos(lat2));
    
}
//https://rosettacode.org/wiki/Haversine_formula#C
//double lat1, double lon1, double lat2, double lon2
//the * 1000 makes it in meters
double dist(double th1, double ph1, double th2, double ph2)
{
	double dx, dy, dz;
	ph1 -= ph2;
	ph1 *= TO_RAD, th1 *= TO_RAD, th2 *= TO_RAD;
 
	dz = sin(th1) - sin(th2);
	dx = cos(ph1) * cos(th1) - cos(th2);
	dy = sin(ph1) * cos(th1);
	return (asin(sqrt(dx * dx + dy * dy + dz * dz) / 2) * 2 * R)*1000;
}

double convertGPSToDeg( double input){
    //the gps coordinates given by the module need to be adjusted, they
    if (input>90){
        return floor(input/100.0)+floor(mod(input,100))/60.0+mod(input,1)*100.0/360.0;
    }
    return input;
}

double mod(double a, double n){
    return a-floor(a/n)*n;
}

void __attribute__((__interrupt__)) _U2RXInterrupt( void )
{    
    if (U2STAbits.OERR) {
        U2STAbits.OERR = 0;
    }
    RXmsg[RXct] = U2RXREG;
    
    //print_lcd("hello");
    if (!ipcommand && RXct> 4 && !strncmp(RXmsg,"+IPD", strlen("+IPD"))){
        ipcommand = 1;
    }
    
    if (RXct>0 && ((!ipcommand && (RXmsg[RXct] == '\n' && RXmsg[RXct-1] == '\r')) || (ipcommand &&(RXmsg[RXct] == '}')))) {//(RXmsg[RXct] = 'K') & (RXmsg[RXct-1] = 'O')) {
        RXmsg[RXct-1] = '\0';
        int m;
        ipcommand=0;
        char rxBuf[40];
        int rxBuffIndex=0;
        int gpsIndex=0;
        
        
        if (RXct> 4 && !strncmp(RXmsg,"+IPD", strlen("+IPD"))){
            
            //print_lcd("HERE");
            //lcd_clear();
            //print_lcd(RXmsg);
        
            for (m = 0; m < RXct; m++){
                if (RXmsg[m] == '@'){ //update rover location
                    //print_lcd("aaa");
                    m++;
                    while(RXmsg[m] !='{' && RXmsg[m] !='\0'){
                        //numGPSpoints = numGPSpoints*10+(RXmsg[m]-'0');
                        m++;
                    }
                    int k = 0;
                    for(k = 0;k<1;k++){
                        rxBuffIndex = 0;
                        m++;
                        for(;RXmsg[m] !='}';m++){
                            rxBuf[rxBuffIndex++]=RXmsg[m];
                        }
                        rxBuf[rxBuffIndex]='\0';
                        //
                        double roverTemp1, roverTemp2;
                     
                        sscanf(rxBuf,"%lf,%lf", &roverTemp1,&roverTemp2);
                        roverlat[roverlatIndex] = roverLat;
                        roverlog[roverlogIndex] = roverLog;
                        
                        roverlatIndex = (roverlatIndex+1)%ROVER_LEN;
                        roverlogIndex = (roverlogIndex+1)%ROVER_LEN;
                        
                        gpsIndex++;
                        m++;
                        m++;
                    }
                    /*sprintf(outputBuf, "%lf", roverLog);
                    lcd_clear();
                    print_lcd(outputBuf);
                    lcd_line2();
                    sprintf(outputBuf, "%lf", roverLat);
                    print_lcd(outputBuf);*/
                    gpsLock=1;
                    break;
                }
                if (RXmsg[m] == '$'){
                    //print_lcd("aaa");
                    m++;
                    while(RXmsg[m] !='{' && RXmsg[m] !='\0'){
                        numGPSpoints = numGPSpoints*10+(RXmsg[m]-'0');
                        m++;
                    }
                    int k = 0;
                    for(k = 0;k<numGPSpoints;k++){
                        rxBuffIndex = 0;
                        m++;
                        for(;RXmsg[m] !='}';m++){
                            rxBuf[rxBuffIndex++]=RXmsg[m];
                        }
                        rxBuf[rxBuffIndex]='\0';
                        //
                        sscanf(rxBuf,"%lf,%lf", &gpsLonge[gpsIndex],&gpsLat[gpsIndex]);
                        gpsIndex++;
                        m++;
                        m++;
                    }
                    sprintf(outputBuf, "%lf", gpsLat[numGPSpoints-1]);
                    print_lcd(outputBuf);
                    lcd_line2();
                    sprintf(outputBuf, "%lf", gpsLonge[numGPSpoints-1]);
                    print_lcd(outputBuf);
                    //waypointsReady = 1;
                    
                    waypointsReady = 1;
                    break;
                }
            }
        }
        
        if (RXct> 14 && !strncmp(RXmsg,"+CGPSINFO:", strlen("+CGPSINFO:"))){
            RXmsg[RXct-1] = '\0';
            
            int q;
            int commaCount = 0;
            int cgpsOffset = 10;    //offset to gps data in string
            for (cgpsOffset = 0; cgpsOffset <RXct;cgpsOffset++ ){
                if (RXmsg[cgpsOffset] == ':'){
                    break;
                }
            }
            cgpsOffset++;
            int latIndex = 0;
            int logIndex = 0;
            char latBuf[60];    //buffer for lat
            char logBuf[60];    //buffer for long
            //print_lcd("gps not locked");
            if (RXmsg[cgpsOffset] == ','){    //remove 0 && if not test
                
            }else{
                //strcpy(RXmsg,"+CGPSINFO:4567.87,N,8546.12,date,utc,alt");   //copy over test string
                for (q = cgpsOffset; RXmsg[q] !='\0' && q <RXct ; q++){
                    if (commaCount == 0 && RXmsg[q]!=','){
                        latBuf[latIndex++]=RXmsg[q];
                    }
                    if (commaCount == 0 && RXmsg[q]==','){
                        latBuf[latIndex++]='\0';
                        sscanf(latBuf, "%lf", &roverLat);
                    }
                    if (commaCount == 2 && RXmsg[q]!=','){
                        logBuf[logIndex++]=RXmsg[q];
                    }
                    if (commaCount == 2 && RXmsg[q]==','){
                        logBuf[logIndex++]='\0';
                        sscanf(logBuf, "%lf", &roverLog);
                        //break;
                    }
                    if (RXmsg[q]==','){
                        commaCount++;
                    }
                }
                //test 
                 /*sprintf(outputBuf, "%lf", roverLat);
                 lcd_clear();
                 print_lcd(outputBuf);
                 print_lcd(" ");
                 sprintf(outputBuf, "%lf",roverLog);
                 print_lcd(outputBuf);*/
                
                 gpsLock=1;
                 //while (1);
                }
           /* lcd_clear();
             print_lcd(RXmsg);*/
            }
        
        RXct = 0;
        //lcd_clear();
        //:wq
        //delay_ms(100);
        //print_lcd(RXmsg);
    } else {
        RXct++;
    }
    IFS1bits.U2RXIF = 0;
}

void UART_Initialize() 
{
    
    U2MODEbits.UARTEN = 0;	// Bit15 TX, RX DISABLED, ENABLE at the end
	U2MODEbits.USIDL = 0;	// Bit13 Continue in Idle
	U2MODEbits.IREN = 0;	// Bit12 No IR translation
	U2MODEbits.RTSMD = 0;	// Bit11 Simplex Mode
	//U1MODEbits.UEN = 0;		// Bits8,9 TX,RX enabled, CTS,RTS not
	U2MODEbits.WAKE = 0;	// Bit7 No Wake up (since we don't sleep here)
	U2MODEbits.LPBACK = 0;	// Bit6 No Loop Back
	U2MODEbits.ABAUD = 0;	// Bit5 No Autobaud (would require sending '55')
	U2MODEbits.URXINV = 0;	// Bit4 IdleState = 1  (for dsPIC)
	U2MODEbits.BRGH = 0;	// Bit3 16 clocks per bit period
	U2MODEbits.PDSEL = 0;	// Bits1,2 8bit, No Parity
	U2MODEbits.STSEL = 0;	// Bit0 One Stop Bit
    
    U2BRG = BRG;
    
	U2STAbits.UTXISEL1 = 0;	//Bit15 Int when Char is transferred (1/2 config!)
	U2STAbits.UTXINV = 0;	//Bit14 N/A, IRDA config
	U2STAbits.UTXISEL0 = 0;	//Bit13 Other half of Bit15
	U2STAbits.UTXBRK = 0;	//Bit11 Disabled
	U2STAbits.URXISEL = 0;	//Bits6,7 Int. on 4 char received (RXreg full)
	U2STAbits.ADDEN = 0;	//Bit5 Address Detect Disabled
    
    //IEC1bits.U2TXIE = 1; // Enable UART TX interrupt
    IEC1bits.U2RXIE = 1;
    
	U2MODEbits.UARTEN = 1;	// UART on
	U2STAbits.UTXEN = 1;
}

void TX_str( char * array ) {
    int i;
    for (i = 0; i < strlen(array); i++) {
        TX_char(array[i]);
    }
    delayMs(10);
    //__delay_ms(10);
}

void TX_char ( char x ) {
    U2TXREG = x;
    while (!U2STAbits.TRMT); // Transmission in progress
}