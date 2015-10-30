#include <SoftwareSerial.h> 
#define RxD 2
#define TxD 3


SoftwareSerial blueToothSerial(RxD,TxD);//the software serial port 

char recv_str[100];

void setup() 
{ 
    // user PIN 9 for AT mode
    pinMode(9, OUTPUT);
    digitalWrite(9, HIGH);
  
    Serial.begin(9600);   //Serial port for debugging
    pinMode(RxD, INPUT);    //UART pin for Bluetooth
    pinMode(TxD, OUTPUT);   //UART pin for Bluetooth
    Serial.println("\r\nPower on!!");
    
    blueToothSerial.begin(9600); 
} 

void loop() 
{
    int readBytes = recvMsg();
    if(readBytes > 0)
    {
        //check if there's any data sent from the remote bluetooth shield
        //recv_str[readBytes] = '\0';
        Serial.print("From BT: ");
        Serial.println((char *)recv_str);

        blueToothSerial.print("Return: ");
        blueToothSerial.println((char *)recv_str);
    }

}

// receives data from the bluetooth
int recvMsg()
{
    int i = 0;
    while(blueToothSerial.available())
    {                                              
        recv_str[i] = (char)blueToothSerial.read();
        i++;
        delay(20);
    }

    return i;
}

//configure the Bluetooth through AT commands
int setupBlueToothConnection()
{
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
        blueToothSerial.begin(115200);
        delay(500);
        Serial.print("try 115200\r\n");
        if(sendBlueToothCommand("AT") == 0)
            break;
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
    do
    {
        if(Serial.available())
        {
            if( Serial.read() == 'S')
            {
                sendBlueToothCommand("AT+RESET\r\n");
                Serial.print("resetting...\r\n");
                flag = 0;
            }
         }
    }
    while(flag);
  
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

    Serial.println("");
    return 0;
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

