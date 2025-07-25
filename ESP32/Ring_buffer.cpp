#include "Ring_buffer.h"

void ringInit(ringBuffer* r, int max){
  r->head=-1;
  r->tail=0;
  r->maxContain=max;
  memset(r->buff, 0, sizeof(r->buff));
}

bool ringIsEmpty(ringBuffer* r){return r->head<0;}
bool ringIsFull(ringBuffer* r){return r->head==r->tail;}

int ringLen(ringBuffer* r){
  if(ringIsEmpty(r)) return 0;
  if(r->tail>r->head) return r->tail-r->head;
  return r->maxContain-r->head+r->tail+1;
}

int ringAverage(ringBuffer* r, int newValue){
  int result=0;
  if(ringIsEmpty(r) || ringIsFull(r)) r->head=(r->head+1)%r->maxContain;
  r->buff[r->tail]=newValue;
  r->tail=(r->tail+1)%r->maxContain;
  for(int i=0;i<r->maxContain;i++){
    result+=r->buff[i];
  }
  return result/ringLen(r);
}
