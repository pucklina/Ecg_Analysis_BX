package com.ecg.activity;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

/*
 定义一个文件工具类，重要包含的 方法有：
1、在SD卡的根目录下创建3HCare、medicalData、等文件夹
2、在一个指定文件夹下创建一个文件并返回文件名
3、
*/
@SuppressLint("SimpleDateFormat")
public class FileUtils {
    public static File topDir;
    public static File personalDir;
    public static File medicalDir;
    private String SDPATH;
    private int FILESIZE = 4 * 1024;
    public String getSDPATH(){
        return SDPATH;
    }
    //创建本程序所需要的文件夹,如果所有的文件夹创建成功就返回true
    public static String createDirs(){
        //如果储存并且可以被读写
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            //获取sd卡的目录
            File sdcardDir=Environment.getExternalStorageDirectory();
            try {
                //定义各个文件夹是否创建成功的标记
                boolean dirFlag=false;
                boolean personalDirFlag=false;
                boolean medicalDirFlag=false;
                //创建程序总目录
                topDir=new File(sdcardDir.getCanonicalPath()+"/"+"VlCare");
                //如果文件夹不存在，就创建
                if(!topDir.exists())
                    dirFlag=topDir.mkdir();
                //创建程序个人数据总目录
                personalDir=new File(topDir.getCanonicalPath()+"/"+"PersonalData");
                //如果文件夹不存在，就创建
                if(!personalDir.exists())
                    personalDirFlag=personalDir.mkdir();
                //创建医疗数据目录
                medicalDir=new File(personalDir.getCanonicalPath()+"/"+"MedicalData");
                //如果文件夹不存在，就创建
                if(!medicalDir.exists())
                    medicalDirFlag=medicalDir.mkdir();
                if(topDir.exists()&&personalDir.exists()&&medicalDir.exists()){
                    dirFlag=true;
                    personalDirFlag=true;
                    medicalDirFlag=true;
                }
                //需要新的文件夹就继续在这里添加
                if(dirFlag&&personalDirFlag&&medicalDirFlag){
                    return medicalDir.getAbsolutePath();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return null;
    }

    //返回一个8个数字组成的字符串
    public static String EightIntString(){
        Random random=new  Random(System.currentTimeMillis());
        //System.out.println(System.currentTimeMillis());
        String str="-";
        for(int i=0;i<8;i++)
        {
            str=str+random.nextInt(10);
        }
        return str;
    }
    //返回当前年月日时分组成的字符串
    public static String currentTimeString(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");
        String currentTime = simpleDateFormat.format(calendar.getTime());
        return currentTime;

    }
    /**
     * 在SD卡上创建文件  
     * @param fileName
     * @return
     * @throws java.io.IOException
     */
    public File createSDFile(String fileName) throws IOException{
        File file = new File(SDPATH + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 在SD卡上创建目录  
     * @param dirName
     * @return
     */
    public File createSDDir(String dirName){
        File dir = new File(SDPATH + dirName);
        dir.mkdir();
        return dir;
    }
    /**
     * 判断SD卡上的文件夹是否存在  
     * @param fileName
     * @return
     */
    public boolean isFileExist(String fileName){
        File file = new File(SDPATH + fileName);
        return file.exists();
    }
    /**
     * 将一个InputStream里面的数据写入到SD卡中  
     * @param path
     * @param fileName
     * @param input
     * @return
     */
    public File write2SDFromInput(String path,String fileName,InputStream input){
        File file = null;
        OutputStream output = null;
        try {
            createSDDir(path);
            file = createSDFile(path + fileName);
            output = new FileOutputStream(file);
            byte[] buffer = new byte[FILESIZE];
  
            /*真机测试，这段可能有问题，请采用下面网友提供的  
                            while((input.read(buffer)) != -1){  
                output.write(buffer);  
            }  
                            */  
  
                           /* 网友提供 begin */
            int length;
            while((length=(input.read(buffer))) >0){
                output.write(buffer,0,length);
            }
                           /* 网友提供 end */

            output.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
