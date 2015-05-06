package com.ecg.jni;
import android.util.Log;

import com.ecg.db.Anno;

public class JniBdacServer {

	private static long SampleCount=0;
	public static void resetBDAC()
	{
		JNIResetBDAC();
	}
	public static Anno BDAC(int sample)
	{
		SampleCount++;
		Anno anno=new Anno(0,"",0,0);
		int results[];
		results=JNIBeatDetectAndClassify(sample);
		if(results[0]!=0)
		{
			anno.dectTime=SampleCount-results[0];
			switch(results[1]){
			case 1:	anno.beatType="N";break;
			case 5:	anno.beatType="V";break;
			case 13:anno.beatType="Q";break;
			default:anno.beatType="|";
			}
			anno.qrsWidth=results[3]*0.005;
			anno.RR=results[2]*0.005;
			Log.w("Anno information", "QRS-width:"+anno.qrsWidth+";RR="+anno.RR+";beatType="+anno.beatType);
		}
		return anno;
	}
	public static long getSampleCount()
	{
		return SampleCount;
	}

	private native static int[] JNIBeatDetectAndClassify(int ecg);
	private native static void JNIResetBDAC();
	 static {
	        System.loadLibrary("bdac");
	 } 
}
