
#include <avr/sleep.h>

// Comment this line to disable performance measuring
//#define PERFORMANCE_MES

const unsigned char defaultBitResolution = 15;
const unsigned int averagingFactor = 4;
unsigned char numberOfExtraBits;
unsigned int numberofSamples;
unsigned int wakeupPinNo = 2;
static unsigned long currAinVal = 0u;

const unsigned char hwVer[3] = {1, 0, 0};
const unsigned char swVer[3] = {1, 0, 0};

const unsigned char avgQueueSize = 8;
unsigned long avgQueue[avgQueueSize] = {0};
unsigned char avgQueueNextIdx = 0; // index where to insert next item

bool capturingEnabled = true;

void avgQueueAddValue(unsigned long val) {
  avgQueue[avgQueueNextIdx] = val;
  avgQueueNextIdx++;
  if(avgQueueNextIdx == avgQueueSize)
    avgQueueNextIdx = 0;
}

unsigned long avgQueueGetAvg() {
  unsigned long val = 0;
  for(unsigned char pos = 0; pos < avgQueueSize; pos++)
    val += avgQueue[pos];

  return val / avgQueueSize;
}


void setup() {
  Serial.begin(9600);
  analogReference(INTERNAL);
  setResolution(defaultBitResolution);
}

void loop() {
#ifdef PERFORMANCE_MES
unsigned long perfStartMes = millis();
#endif
  if(capturingEnabled)
  {
    readAnalogIn();
    sendAinValue();
  }
  else
  {
    delay(500);
  }

  readCommand();
#ifdef PERFORMANCE_MES
unsigned long perfEndMes = millis();
float speed = 1000.0/(perfEndMes - perfStartMes);
Serial.print("Performance: ");
Serial.print(speed);
Serial.println(" SPS (=Shots per Second)");
#endif
}

void readCommand() {
  if (Serial.available() > 0) {
    // get incoming byte:
    int command = Serial.read();

    // send positive response
    //Serial.write(command+100);

    switch(command) {
      case 0: /* Disabled Adc capturing */
        capturingEnabled = false;
        break;
      case 1: /* Enable Adc capturing */
        capturingEnabled = true;
        break;
      case 's': /* Put device into deep sleep */
        Serial.println("Go to sleep");
        goToSleep();
        break;
      case 3: /* Get HW version */
        Serial.write(hwVer[0]);
        Serial.write(hwVer[1]);
        Serial.write(hwVer[2]);
        break;
      case 4: /* Get HW version */
        Serial.write(swVer[0]);
        Serial.write(swVer[1]);
        Serial.write(swVer[2]);
        break;
      case 20: /* Set to n bit */
      case 21: /* Set to n bit */
      case 22: /* Set to n bit */
      case 23: /* Set to n bit */
      case 24: /* Set to n bit */
      case 25: /* Set to n bit */
      case 26: /* Set to n bit */        
        setResolution(command - 10);
        break;
      default:
        break;
    }
  }
}

void goToSleep() {


  //Löschen des Global Interrupt Enable Bits (I) im Status Register (SREG)
  cli();


  //Setzen des PCIE2-Bit im Pin Change Interrupt Control Register (PCICR)
  PCICR |= (1 << PCIE2);


  //Setzen des Pin Change Enable Mask Bit 18 (PCINT18)  ==> Digital-Pin 2
  PCMSK2 |= (1 << PCINT16);


  //Setzen des Global Interrupt Enable Bits (I) im Status Register (SREG)
  sei();
  
  //attachInterrupt(digitalPinToInterrupt(wakeupPinNo), intWakeupRoutine, CHANGE);
  set_sleep_mode(SLEEP_MODE_PWR_DOWN); // choose power down mode

  delay(500);
  
  sleep_mode(); // sleep now!
}

void setResolution(unsigned char bits) {
  numberOfExtraBits = bits - 10;
  numberofSamples = pow(4, numberOfExtraBits);
}

void readAnalogIn() {
  // Decimation for Arduino ADC A0, averaged then decimated.
  currAinVal=0;
  for (byte avg=0;avg<averagingFactor;avg++)
     for (int j=0;j<numberofSamples;j++) currAinVal+= analogRead(A0); // analogRead(A0);
  currAinVal=(currAinVal/averagingFactor);
  currAinVal = currAinVal>>numberOfExtraBits;

  avgQueueAddValue(currAinVal);
}

void sendAinValue() {
  Serial.print("AIN");
  Serial.print(currAinVal);
  Serial.print("+");
  Serial.print(avgQueueGetAvg());
  Serial.print("-");
  Serial.print(numberOfExtraBits + 10);
  Serial.print("|");
}

ISR(PCINT2_vect)
{
    //Löschen des Global Interrupt Enable Bits (I) im Status Register (SREG)
  cli();


  //Setzen des PCIE2-Bit im Pin Change Interrupt Control Register (PCICR)
  PCICR |= (0 << PCIE2);


  //Setzen des Pin Change Enable Mask Bit 18 (PCINT18)  ==> Digital-Pin 2
  PCMSK2 |= (0 << PCINT16);


  //Setzen des Global Interrupt Enable Bits (I) im Status Register (SREG)
  sei();
}
