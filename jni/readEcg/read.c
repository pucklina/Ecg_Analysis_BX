#include "wfdb.h"
#include "ecgcodes.h"
#include "ecgmap.h"
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include "inputs.h"

int NextSample(int *vout,int nosig,int ifreq,int ofreq,int init) ;
int gcd(int x, int y);
int ADCZero, ADCUnit, InputFileSampleFrequency ;
int  ecg[2]={0,0};
WFDB_Siginfo s[2] ;
long lTemp, DetectionTime ;
int SAMPLE_RATE=200;
JNIEXPORT jint JNICALL Java_com_ecg_jni_JniWfdbServer_JNIinit(JNIEnv* env,jobject thiz,jstring filename)
{
	char *record;
	record=(*env)->GetStringUTFChars(env,filename,0);
	WFDB_Siginfo s[2] ;
	setwfdb(ECG_DB_PATH) ;
	if(isigopen(record,s,2))
	{
		ADCZero = s[0].adczero ;
		ADCUnit = s[0].gain ;
		InputFileSampleFrequency = sampfreq(record) ;
		NextSample(ecg,2,InputFileSampleFrequency,SAMPLE_RATE,1);
		return 1;
	}
	return 0;
}
void Java_com_ecg_jni_JniWfdbServer_JNIquit(JNIEnv* env,jobject thiz)
{
	wfdbquit();
}
JNIEXPORT jint JNICALL Java_com_ecg_jni_JniWfdbServer_JNIgetEcg(JNIEnv *env, jobject thiz)
{
	jint carr=-1024;
	if(NextSample(ecg,2,InputFileSampleFrequency,SAMPLE_RATE,0)>=0)
	{
		lTemp = ecg[0]-ADCZero ;
		lTemp *= 200 ;			lTemp /= ADCUnit ;			ecg[0] = lTemp ;
		carr=ecg[0];
	}
	return carr;
}
int  NextSample(int *vout,int nosig,int ifreq,int ofreq,int init)
{
	int i ;
	static int m, n, mn, ot, it, vv[WFDB_MAXSIG], v[WFDB_MAXSIG], rval ;
	if(init)
	{
		i = gcd(ifreq, ofreq);
		m = ifreq/i;
		n = ofreq/i;
		mn = m*n;
		ot = it = 0 ;
		getvec(vv) ;
		rval = getvec(v) ;
	}
	else
	{
		while(ot > it)
		{
	    	for(i = 0; i < nosig; ++i)
	    		vv[i] = v[i] ;
			rval = getvec(v) ;
		    if (it > mn) { it -= mn; ot -= mn; }
		    it += n;
		   }
	    for(i = 0; i < nosig; ++i)
	    	vout[i] = vv[i] + (ot%n)*(v[i]-vv[i])/n;
		ot += m;
	}
	return(rval) ;
}

int gcd(int x, int y)
{
	while (x != y)
	{
		if (x > y) x-=y;
		else y -= x;
	}
	return (x);
}


