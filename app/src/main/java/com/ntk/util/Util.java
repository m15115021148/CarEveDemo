package com.ntk.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;

public class Util  {

	public static String device_ip = "192.168.1.254";
	public static String movie_url = "rtsp://192.168.1.254/xxx.mov";
	public static String photo_url = "http://192.168.1.254:8192";
	
	public static String root_path = Environment.getExternalStorageDirectory().toString();
	public static String local_thumbnail_path = root_path + "/IPCAM/THUMBNAIL";
	public static String local_photo_path = root_path + "/IPCAM/PHOTO";
	public static String local_movie_path = root_path + "/IPCAM/MOVIE";
	public static String log_path = root_path + "/IPCAM/LOG";
	
	public static String getDeciceIP() {
		return device_ip;
	}
	
	public static void setDeciceIP(String ip) {
		device_ip = ip;
	}
	
	public static boolean checkLocalFolder(){
		
		File nvt_dir = new File(root_path + "/IPCAM");
		if (!nvt_dir.exists())
			nvt_dir.mkdir();
		
		File tn_dir = new File(root_path + "/IPCAM/THUMBNAIL");
		if (!tn_dir.exists())
			tn_dir.mkdir();
		
		File ph_dir = new File(root_path + "/IPCAM/PHOTO");
		if (!ph_dir.exists())
			ph_dir.mkdir();
		
		File mv_dir = new File(root_path + "/IPCAM/MOVIE");
		if (!mv_dir.exists())
			mv_dir.mkdir();
		
		File log_dir = new File(root_path + "/IPCAM/LOG");
		if (!log_dir.exists())
			log_dir.mkdir();
		
		return (tn_dir.exists() && ph_dir.exists() && mv_dir.exists() && log_dir.exists()) ; 
	}
	
    public static boolean isContainExactWord(String fullString, String partWord){
        String pattern = partWord;
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(fullString);
        return m.find();
    }
}