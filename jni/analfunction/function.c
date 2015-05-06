#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include "qrsdet.h"

void ResetBDAC(void) ;
int BeatDetectAndClassify(int ecgSample, int *beatType, int *beatMatch,int *onset,int *offset);
int beatType,beatMatch,onset,offset,Rdelay,RR;
FILE *x;
JNIEXPORT void JNICALL Java_com_ecg_jni_JniBdacServer_JNIResetBDAC(JNIEnv* env,jobject thiz)
{
	beatType=beatMatch=onset=offset=RR=0;
	ResetBDAC();
	__android_log_print(ANDROID_LOG_INFO, "JNIMsg", "ResetBDAC() succeed1!");
	//x=fopen("mnt/sdcard/a.txt","w+");
}

JNIEXPORT jintArray JNICALL Java_com_ecg_jni_JniBdacServer_JNIBeatDetectAndClassify(JNIEnv* env,jobject thiz,jint ecg)
{
	beatType=beatMatch=onset=offset=0;
	RR++;
	int carr[]={0,0,0,0};
	int delay=BeatDetectAndClassify(ecg,&beatType,&beatMatch,&onset,&offset);
	if(delay!=0)
	{
		carr[0]=delay;
		carr[1]=beatType;
		carr[2]=RR;
		carr[3]=(offset-onset)*2;
		RR=0;
	}
	jintArray Results=(*env)->NewIntArray(env,4);
	(*env)->SetIntArrayRegion(env,Results, 0 , 4 ,carr);
	return Results;
}
