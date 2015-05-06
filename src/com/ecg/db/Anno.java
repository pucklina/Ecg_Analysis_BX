package com.ecg.db;
public class Anno {  
	
	public long dectTime;
	public String beatType;
	public double qrsWidth;
	public double RR;
    
	public Anno(){
		
	}
    public Anno(long dt,String bt,double qw,double rr) {
    	this.dectTime=dt;
    	this.beatType=bt;
    	this.qrsWidth=qw;
    	this.RR=rr;
    }  
    public boolean writeDB()
    {
    	return true;
    }
      
}  