package com.ecg.db;


import android.database.SQLException;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class TimeAnnoAdapter {

    static final int MAX_CONTAIN = 60;
    static final int ClassifyArrayCount = 30;
    static final double ClassifyArrayMin = 0.60;
    static final double ClassifyArraStep = 0.025;

    int newValueCount;
    private ArrayDeque<TimeAnno> timeAnnos;

    int numOfRRcount[];


    public TimeAnnoAdapter(){

        newValueCount=0;

        numOfRRcount = new int[ClassifyArrayCount];
        for(int i=0;i<ClassifyArrayCount;i++) numOfRRcount[i]=0;
        timeAnnos = new ArrayDeque<>();
    }


    public ArrayDeque<TimeAnno> getTimeAnnos() {
        return timeAnnos;
    }
    public int getNewValueCount() {
        return newValueCount;
    }
    public int[] getNumOfRRcount() {
        return numOfRRcount;
    }

    class TimeAnno{
        private long dectTime;
        private String beatType;

        private double qrsWidth;
        private  double rr;
        public TimeAnno(long dectTime, String beatType, double qrsWidth, double rr) {
            this.setBeatType(beatType);
            this.setDectTime(dectTime);
            this.setQrsWidth(qrsWidth);
            this.setRr(rr);
        }

        public long getDectTime() {
            return dectTime;
        }

        public String getBeatType() {
            return beatType;
        }

        public double getQrsWidth() {
            return qrsWidth;
        }

        public double getRr() {
            return rr;
        }

        public void setDectTime(long dectTime) {
            this.dectTime = dectTime;
        }

        public void setBeatType(String beatType) {
            this.beatType = beatType;
        }

        public void setQrsWidth(double qrsWidth) {
            this.qrsWidth = qrsWidth;
        }

        public void setRr(double rr) {
            this.rr = rr;
        }
    }


    public void insertAnno(Anno anno) {
        TimeAnno popAnno = null;
        TimeAnno TempTimeAnnos = new TimeAnno(anno.dectTime,anno.beatType,anno.qrsWidth,anno.RR);
        if(timeAnnos.size()>=MAX_CONTAIN)
        {
            popAnno = timeAnnos.pop();
            if(popAnno.getRr()<(ClassifyArrayMin-ClassifyArraStep));
            else {
                for (int i = 0; i < ClassifyArrayCount; i++) {
                    if (popAnno.getRr() < (ClassifyArrayMin + i * ClassifyArraStep)) {
                            numOfRRcount[i]--;
                        break;
                    }
                }
            }
        }
        timeAnnos.push(TempTimeAnnos);
        newValueCount++;


        if (anno.RR < (ClassifyArrayMin - ClassifyArraStep)) ;
        else {
                for (int i = 0; i < ClassifyArrayCount; i++) {
                    if (anno.RR  < (ClassifyArrayMin + i * ClassifyArraStep)) {
                        numOfRRcount[i]++;
                        break;
                    }
                }
            }

    }

    // ---检索一个指定标题---

    public int getTypeCount(String beatType){
        int i = 0;

        for(TimeAnno timeAnno:timeAnnos)
        {
            if(timeAnno.getBeatType().equals(beatType)) i++;
        }
        return i;
    }
    public int getRRCount(double RR){
        int i = 0;

        for(TimeAnno timeAnno:timeAnnos)
        {
            if(timeAnno.getRr()<RR) i++;
        }

        return i;
    }




}