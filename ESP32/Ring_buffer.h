#ifndef _RING_BUFFER_H
#define _RING_BUFFER_H

#include <stdint.h>
#include "Arduino.h"

typedef struct 
{
    int buff[300];
    int maxContain;
    int head;
    int tail;
} ringBuffer;

void ringInit(ringBuffer* r, int max);
bool ringIsEmpty(ringBuffer* r);
bool ringIsFull(ringBuffer* r);
int ringLen(ringBuffer* r);
int ringAverage(ringBuffer* r, int newValue);




#endif
