package com.ecg.activity;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;

public class FileDirUtils {

    /*
     在SD卡指定位置创建一个目录：1、获得要创建的目录的字符串 2、判断该目录是否存在 3、不存在就创建这个目录
    返回：1成功  0存在 -1 失败
    备注：创建a目录的子目录b 需要FileDirUtils.CreatDir("a");FileDirUtils.CreatDir("a/b")
    */
    public static int CreatDir(String dirName)
    {
        //如果有储存卡并且可以被读写
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            //获取sd卡的目录
            File sdcardDir=Environment.getExternalStorageDirectory();
            //创建一个目录
            try {
                File newFile=new File(sdcardDir.getCanonicalPath()+"/"+dirName);
                //如果文件夹不存在，就创建
                if(!newFile.exists())
                {
                    //如果创建成功；返回1
                    if(newFile.mkdir())
                        return 1;
                    else {
                        return -1;
                    }
                }
                else {
                    return 0;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return -1;
    }
    /*
     返回一个目录中所有的文件
     */
    public static File[] getAllFiles(String path){
        File file=new File(path);
        File[] files ;
        files= file.listFiles();
        if(files.length>0)
        {
            return files;
        }
        if(files.length==0)
        {
            return null;
        }
        return null;
    }


    //返回一个文件的大小的字符串的方法
    public static String getFileLength(File f)
    {
        double filelength=0.0;
        String str=null;
        File[] files;
        BigDecimal b ;
        //如果这是一个目录，就返回一个空格，表现ListView中，就是size的TextView显示一个空格，这时候什么也看不到
        if(f.isDirectory())
        {
            files=f.listFiles();
            if(files!=null)
            {
                for(int i=0;i<files.length;i++)
                {
                    filelength=files[i].length()+filelength;
                }
            }
            //如果这个文件夹下一个文件也没有
            if(files==null)
            {
                filelength=0.0;
            }

        }else {
            filelength=f.length();
        }
        //把字节单位的长度转换为KB和MB的方法
        if(filelength<=1024)
        {
            b= new BigDecimal(filelength);
            return b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()+"B";
        }
        if(1024<filelength&&filelength<=(1024*1024))
        {
            filelength=filelength/1024;
            b= new BigDecimal(filelength);
            return b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()+"KB";
        }
        if((1024*1024)<filelength)
        {
            filelength=filelength/1024/1024;
            b= new BigDecimal(filelength);
            return b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()+"MB";
        }
        return null;
    }


    //返回一个文件的大小的字符串的方法
    public static String getFileLengthFromByte(String size)
    {
        double filelength=0.0;
        BigDecimal b ;
        filelength=Double.parseDouble(size);

        //把字节单位的长度转换为KB和MB的方法
        if(filelength<=1024)
        {
            b= new BigDecimal(filelength);
            return b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()+"B";
        }
        if(1024<filelength&&filelength<=(1024*1024))
        {
            filelength=filelength/1024;
            b= new BigDecimal(filelength);
            return b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()+"KB";
        }
        if((1024*1024)<filelength)
        {
            filelength=filelength/1024/1024;
            b= new BigDecimal(filelength);
            return b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()+"MB";
        }
        return null;
    }


    /*
     返回一个目录包含的文件的个数
     */
    public  static int getFilesNumber(String path)
    {
        File file=new File(path);
        File[] files ;
        files= file.listFiles();
        if(files!=null)
        {
            return files.length;
        }
        else {
            return 0;
        }

    }
    /**
     * 排序文件列表
     * */
    public static void sortFiles(File[] files)
    {
        Arrays.sort(files, new Comparator<File>()
        {
            public int compare(File file1, File file2)
            {
                if(file1.isDirectory() && file2.isFile())
                    return -1;
                if(file1.isFile() && file2.isDirectory())
                    return 1;
                return (file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
            }
        });
    }

}
