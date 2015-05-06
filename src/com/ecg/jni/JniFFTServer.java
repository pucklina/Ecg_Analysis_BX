package com.ecg.jni;

public class JniFFTServer {
	
	public static float[] fft(float td[],int n)
	{
		return FFT(td,n);
	}
	private native static float[] FFT(float td[],int n);
	 static {
	        System.loadLibrary("fft");
	 }

}
