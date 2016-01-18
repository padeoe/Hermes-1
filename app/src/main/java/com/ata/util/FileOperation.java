package com.ata.util;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Created by raven on 2015/8/29.
 */
public class FileOperation {
    public static byte[] fileToByte(File file){
        InputStream is = null;
        long length = file.length();
        byte[] bytes = new byte[(int)length];
        try {
            is = new FileInputStream(file);


            if (length > Integer.MAX_VALUE) {

                throw new IOException("File is to large "+file.getName());

            }




            int offset = 0;

            int numRead = 0;

            while (offset < bytes.length

                    && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {

                offset += numRead;

            }
            if (offset < bytes.length) {

                throw new IOException("Could not completely read file "+file.getName());

            }

            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return bytes;
        }

// 获取文件大小

    }
    public static void mergeFiles(String outFile, String[] files) {
        FileChannel outChannel = null;
        final int BUFSIZE = 1024 * 8;
        try {
            outChannel = new FileOutputStream(outFile).getChannel();
            for(String f : files){
                FileChannel fc = new FileInputStream(f).getChannel();
                ByteBuffer bb = ByteBuffer.allocate(BUFSIZE);
                while(fc.read(bb) != -1){
                    bb.flip();
                    outChannel.write(bb);
                    bb.clear();
                }
                fc.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {if (outChannel != null) {outChannel.close();}} catch (IOException ignore) {}
        }
    }
    public static void copyFile(File src,File dst){
        FileOutputStream fo=null;
        File parent =dst.getParentFile();
        if(!parent.exists()){
            parent.mkdirs();
        }
        if(!dst.exists()){
            try {
                dst.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fo=new FileOutputStream(dst);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] file1=fileToByte(src);

        try {
            fo.write(file1);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(fo!=null) {
                    fo.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public static Intent openFile(String filePath){

        File file = new File(filePath);
        if(!file.exists()) return null;
		/* 取得扩展名 */
        String end=file.getName().substring(file.getName().lastIndexOf(".") + 1,file.getName().length()).toLowerCase();
		/* 依扩展名的类型决定MimeType */
        if(end.equals("m4a")||end.equals("mp3")||end.equals("mid")||
                end.equals("xmf")||end.equals("ogg")||end.equals("wav")){
            return getAudioFileIntent(filePath);
        }else if(end.equals("3gp")||end.equals("mp4")){
            return getAudioFileIntent(filePath);
        }else if(end.equals("jpg")||end.equals("gif")||end.equals("png")||
                end.equals("jpeg")||end.equals("bmp")){
            return getImageFileIntent(filePath);
        }else if(end.equals("apk")){
            return getApkFileIntent(filePath);
        }else if(end.equals("ppt")){
            return getPptFileIntent(filePath);
        }else if(end.equals("xls")){
            return getExcelFileIntent(filePath);
        }else if(end.equals("doc")){
            return getWordFileIntent(filePath);
        }else if(end.equals("pdf")){
            return getPdfFileIntent(filePath);
        }else if(end.equals("chm")){
            return getChmFileIntent(filePath);
        }else if(end.equals("txt")){
            return getTextFileIntent(filePath,false);
        }else{
            return getAllIntent(filePath);
        }
    }
    //Android获取一个用于打开APK文件的intent
    public static Intent getAllIntent( String param ) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri,"*/*");
        return intent;
    }
    //Android获取一个用于打开APK文件的intent
    public static Intent getApkFileIntent( String param ) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        return intent;
    }

    //Android获取一个用于打开VIDEO文件的intent
    public static Intent getVideoFileIntent( String param ) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "video/*");
        return intent;
    }

    //Android获取一个用于打开AUDIO文件的intent
    public static Intent getAudioFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }

    //Android获取一个用于打开Html文件的intent
    public static Intent getHtmlFileIntent( String param ){

        Uri uri = Uri.parse(param ).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param ).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    //Android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent( String param ) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    //Android获取一个用于打开PPT文件的intent
    public static Intent getPptFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    //Android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    //Android获取一个用于打开Word文件的intent
    public static Intent getWordFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    //Android获取一个用于打开CHM文件的intent
    public static Intent getChmFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "application/x-chm");
        return intent;
    }

    //Android获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent( String param, boolean paramBoolean){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (paramBoolean){
            Uri uri1 = Uri.parse(param );
            intent.setDataAndType(uri1, "text/plain");
        }else{
            Uri uri2 = Uri.fromFile(new File(param ));
            intent.setDataAndType(uri2, "text/plain");
        }
        return intent;
    }
    //Android获取一个用于打开PDF文件的intent
    public static Intent getPdfFileIntent( String param ){

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }
    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s = encode;
                return s;
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return "";
    }
    public static void myMergeFiles(File res,File arg1,File arg2){
        FileOutputStream fo=null;
        try {
            fo=new FileOutputStream(res);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] file1=fileToByte(arg1);
        byte[] file2 =fileToByte(arg2);
        System.out.println("merging "+arg1.getAbsolutePath()+" length "
                +file1.length+arg2.getAbsolutePath()+"length:" +
                file2.length+" to " +res);

        try {
            fo.write(file1);
            fo.write(file2);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(fo!=null) {
                    fo.close();
                    fo=null;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
    private static String[] getFileByLines(File f,int s,int e){
        String[] res =new String[e-s+1];
        FileReader fr1 = null;
        int LineCount=0;
        BufferedReader br1=null;

        try {


            try {
                fr1 =new FileReader(f);
            } catch (FileNotFoundException e0) {
                e0.printStackTrace();
            }
             br1 =new BufferedReader(fr1);
            String Line1 = null;
            while ((Line1=br1.readLine())!=null){
                System.out.println(Line1);
                if(LineCount>=s&&LineCount<=e){
                    res[LineCount-s]=Line1;
                }
                LineCount++;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }finally {
            if(br1!=null)
                try {
                    br1.close();
                } catch (IOException e1) {

                }
            if(fr1!=null)
                try {
                    fr1.close();
                } catch (IOException e1) {

                }
            return res;
        }

    }
    private static void WriteToFileByLines(File f,String[] Line) throws IOException {
        FileWriter fo=null;
        BufferedWriter bw =null;
        try {
            File par =f.getParentFile();
            if(!par.exists())par.mkdirs();
            if(!f.exists())f.createNewFile();
            fo=new FileWriter(f);
            bw =new BufferedWriter(fo);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found!");
            throw new IOException("file not found");
        } catch (IOException e) {
            System.out.println("IO exception");

            throw new IOException();
        }


        try {
            for(String l:Line){
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(bw!=null)
                try {
                    bw.close();
                } catch (IOException e) {

                }
            try {
                if(fo!=null) {
                    fo.close();
                    fo=null;
                }

            } catch (IOException e) {

            }
        }

    }
    public static void ResetFile(File f ,int RelativeStart,int RelativeEnd){
        assert RelativeStart>=0&&RelativeEnd>=0;
        String[] Line =getFileByLines(f,RelativeStart,RelativeEnd);
        System.out.println(Arrays.asList(Line));
        assert  Line!=null;
        try {
            WriteToFileByLines(f,Line);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        TestMergeFile();
       // testResetFile();
    }
    private static void TestMergeFile() {
        String src1Path="E:/src1.txt";
        String src2Path ="E:/src2.txt";
        String dstPath ="E:/dst.txt";
        myMergeFiles(new File(dstPath),new File(src1Path),new File(src2Path));
    }
    private static void testResetFile(){
        String src1Path="E:/src1.txt";
        String src2Path ="E:/src2.txt";
        String dstPath ="E:/dst.txt";
        File f1=new File(src1Path);
        File f2 =new File(src2Path);
        File f3 =new File(dstPath);
        ResetFile(f1,10,20);
        ResetFile(f2,0,10);
        ResetFile(f3,15,30);
    }

}
