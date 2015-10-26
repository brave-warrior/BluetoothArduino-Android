#include <SoftwareSerial.h>   //Software Serial Port
#define RxD 2
#define TxD 3

#define MASTER 0    //change this macro to define the Bluetooth as Master or not 

SoftwareSerial blueToothSerial(RxD,TxD);//the software serial port 

char recv_str[100];

void setup() 
{ 
  pinMode(9, OUTPUT);
  digitalWrite(9, HIGH);
  
    Serial.begin(9600);   //Serial port for debugging
    pinMode(RxD, INPUT);    //UART pin for Bluetooth
    pinMode(TxD, OUTPUT);   //UART pin for Bluetooth
    Serial.println("\r\nPower on!!");
    
   // sendBlueToothCommand("AT+BAUD4");//reset the module's baud rate
   // sendBlueToothCommand("AT+RESET");
    
    blueToothSerial.begin(9600);
    //blueToothSerial.listen();
    
    //setupBlueToothConnection();

    //sendBlueToothCommand("AT\r\n");
    //sendBlueToothCommand("AT+UART9600");
    
    //setupBlueToothConnection2();
 /**  if(setupBlueToothConnection() != 0) while(1);   //initialize Bluetooth
    //this block is waiting for connection was established.
    while(1)
    {
        if(recvMsg(1000) == 0)
        {
            if(strcmp((char *)recv_str, (char *)"OK+CONB") == 0)
            {
                Serial.println("connected\r\n");
                break;
            }
        }
        delay(200);
    }
    
    */
    
    
} 

void setupBlueToothConnection2()
{
    Serial.print("Setting up Bluetooth link");       //For debugging, Comment this line if not required    
    blueToothSerial.begin(9600); //Set BluetoothBee BaudRate to default baud rate 38400
    delay(1000);
    sendBlueToothCommand2("\r\n+STWMOD=0\r\n");
    sendBlueToothCommand2("\r\n+STNA=modem\r\n");
    sendBlueToothCommand2("\r\n+STAUTO=0\r\n");
    sendBlueToothCommand2("\r\n+STOAUT=1\r\n");
    sendBlueToothCommand2("\r\n+STPIN=0000\r\n");
    delay(2000); // This delay is required.
    blueToothSerial.print("\r\n+INQ=1\r\n");
    delay(2000); // This delay is required.
    Serial.print("Setup complete");
 
}

void sendBlueToothCommand2(char command[])
{
    char a;
    blueToothSerial.print(command);
    Serial.print(command);                          //For debugging, Comment this line if not required    
    delay(3000);
 
    while(blueToothSerial.available())              //For debugging, Comment this line if not required  
    {                                               //For debugging, Comment this line if not required   
       Serial.print(char(blueToothSerial.read()));  //For debugging, Comment this line if not required  
    }                                               //For debugging, Comment this line if not required   
}

void loop() 
{ 
  //sendBlueToothCommand("AT\r\n");
   /** #if MASTER  //central role
    //in master mode, the bluetooth send message periodically. 
    delay(400);
    Serial.println("send: hi");
    blueToothSerial.print("hi");
    delay(100);
    //get any message to print
    if(recvMsg(1000) == 0)
    {
        Serial.print("recv: ");
        Serial.print((char *)recv_str);
        Serial.println("");
    }
    #else   //peripheral role
    delay(200);
    //the slave role only send message when received one.
    if(recvMsg(1000) == 0)
    {
        Serial.print("recv: ");
        Serial.print((char *)recv_str);
        Serial.println("");
        Serial.println("send: hello");
        blueToothSerial.print("hello");//return back message
    }
    #endif
    
    **/
    
 /*  if( blueToothSerial.available() > 0 )
     {
       Serial.print("Recv: ");
       byte recv = (byte)blueToothSerial.read();
       Serial.print(recv);
       Serial.println("");
       blueToothSerial.flush();
       //recvMsg(1000);
       //Serial.print(recv_str);
     }
     */
 
 int i = 0;
 while(Serial.available() && (i < 100))
    {                                              
        recv_str[i] = (char)Serial.read();
        i++;
    }
    if(i > 0)
    {
      recv_str[i] = '\0';
      Serial.print(" Sent: ");
      Serial.print((char *)recv_str);
      blueToothSerial.print(recv_str);
    }
 
/** if( Serial.available() > 0 )
 {
   char data = (char)Serial.read();
   Serial.print(" Sent: ");
   Serial.print(data); 
   Serial.println("");
    blueToothSerial.print(data);//return back message
 }
 */
 
  if(recvMsg(1000) == 0)
    {
        Serial.print("recv (char*): ");
        Serial.print((char *)recv_str);
        blueToothSerial.flush();
        Serial.println("");
        delay(3000);
        Serial.println("send: Ack");
        blueToothSerial.print("Ack");//return back message
    }
    
    
}

//used for compare two string, return 0 if one equals to each other
int strcmp(char *a, char *b)
{
    unsigned int ptr = 0;
    while(a[ptr] != '\0')
    {
        if(a[ptr] != b[ptr]) return -1;
        ptr++;
    }
    return 0;
}

//configure the Bluetooth through AT commands
int setupBlueToothConnection()
{
   /** #if MASTER
    Serial.println("this is MASTER\r\n");
    #else
    Serial.println("this is SLAVE\r\n");
    #endif
    */

    Serial.print("Setting up Bluetooth link\r\n");
    delay(3500);//wait for module restart

    //send command to module in different baud rate
    while(1)
    {
        delay(500);
        blueToothSerial.begin(9600);
        delay(500);
        Serial.print("try 9600\r\n");
        if(sendBlueToothCommand("AT") == 0)
            break;
        delay(500);
       /**  blueToothSerial.begin(115200);
        delay(500);
        Serial.print("try 115200\r\n");
        if(sendBlueToothCommand("AT") == 0)
            break;
            **/
    }
    
    //we have to set the baud rate to 9600, since the soft serial is not stable at 115200
    sendBlueToothCommand("AT+RENEW");//restore factory configurations
    sendBlueToothCommand("AT+UART9600");
    // sendBlueToothCommand("AT+BAUD2");//reset the module's baud rate
    //sendBlueToothCommand("AT+AUTH1");//enable authentication
    sendBlueToothCommand("AT+RESET");//restart module to take effect
    blueToothSerial.begin(9600);//reset the Arduino's baud rate
    delay(3500);//wait for module restart
    //sendBlueToothCommand("AT+LADD?");//get EDR MAC
    //configure parameters of the module
   // sendBlueToothCommand("AT+VERS?");//get firmware version
   // sendBlueToothCommand("AT+ADDE?");//get EDR MAC
   // sendBlueToothCommand("AT+ADDB?");//get BLE MAC
   // sendBlueToothCommand("AT+NAMEHM-13-EDR");//set EDR name
   // sendBlueToothCommand("AT+NAMBHM-13-BLE");//set BLE name
   // sendBlueToothCommand("AT+PINE123451");//set EDR password
   // sendBlueToothCommand("AT+PINB123451");//set BLE password
    sendBlueToothCommand("AT+SCAN0");//set module visible
    sendBlueToothCommand("AT+NOTI1");//enable connect notifications
    //sendBlueToothCommand("AT+NOTP1");//enable address notifications
   // sendBlueToothCommand("AT+PIO01");//enable key function
   // #if MASTER
   // sendBlueToothCommand("AT+ROLB1");//set to master mode
  //  #else
    sendBlueToothCommand("AT+ROLB0");//set to slave mode
  //  #endif
//     sendBlueToothCommand("AT+RESET");//restart module to take effect

char flag = 1;
do{
    if(Serial.available())
    {
      if( Serial.read() == 'S')
      {
        sendBlueToothCommand("AT+RESET\r\n");
        Serial.print("resetting...\r\n");
        flag = 0;
      }
    }
  }while(flag);
  
    delay(3500);//wait for module restart
    // if(sendBlueToothCommand("AT") != 0) return -1;//detect if the module exists
    Serial.print("Setup complete\r\n\r\n");
    return 0;
}

//send command to Bluetooth and return if there is a response
int sendBlueToothCommand(char command[])
{
    Serial.print("send: ");
    Serial.print(command);
    Serial.println("");

    blueToothSerial.print(command);
    delay(200);

    if(recvMsg(200) != 0) return -1;
    Serial.print(recv_str);

/** while(1)
  {
    if(blueToothSerial.available())
    {
    char a = blueToothSerial.read();
    Serial.print("recv: ");
    Serial.print(a);
    break;
   }
  } */

   // Serial.print("recv: ");
   // Serial.print(recv_str);
    Serial.println("");
    return 0;
}

//Checks if the response "OK" is received
void CheckOK()
{
 char a,b;
 while(1)
 {
   if(blueToothSerial.available())
   {
     a = blueToothSerial.read();

     if('O' == a)
     {
       // Wait for next character K. available() is required in some cases, as K is not immediately available.
       while(blueToothSerial.available()) 
       {
         b = blueToothSerial.read();
         Serial.print(b);
         break;
       }
       if('K' == b)
       {
         break;
       }
     }
   }
 }

 while( (a = blueToothSerial.read()) != -1)
 {
   //Wait until all other response chars are received
 }
}

void sendBlueToothCommand3(char command[])
{
 blueToothSerial.print(command);
 Serial.print(command);
 CheckOK();   
}

//receive message from Bluetooth with time out
int recvMsg(unsigned int timeout)
{
    //wait for feedback
    unsigned int time = 0;
    unsigned char num;
    unsigned char i;
    
    //waiting for the first character with time out
    i = 0;
    while(1)
    {
        delay(50);
        if(blueToothSerial.available())
        {
            recv_str[i] = char(blueToothSerial.read());
            i++;
            break;
        }
        time++;
        if(time > (timeout / 50)) return -1;
    }

    //read other characters from uart buffer to string
    while(blueToothSerial.available() && (i < 100))
    {                                              
        recv_str[i] = char(blueToothSerial.read());
        i++;
    }
    recv_str[i] = '\0';

    return 0;
}


