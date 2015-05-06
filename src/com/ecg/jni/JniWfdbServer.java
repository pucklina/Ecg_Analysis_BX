package com.ecg.jni;

public class JniWfdbServer  {

	public static boolean setWFDB(String filename)
	{
		if(JNIinit(filename)==0)
			return false;
		return true;
	}
	public static void exitWFDB()
	{
		JNIquit();
	}
	public static int ReadSampleFromJNI() {

		return JNIgetEcg();

	}
	private native static void JNIquit();
	private native static int JNIinit(String filename);
	private native static int JNIgetEcg();
	
	 static {
	        System.loadLibrary("wfdb");
	 }


}
