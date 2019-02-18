package com.ntk.util;

public class FileItem  {
	
	public String NAME;
	public String FPATH;
	public String SIZE;
	public String TIMECODE;
	public String TIME;
	
	public FileItem(String name, String fpath, String size, String timecode, String time) {
		NAME = name;
		FPATH = fpath;
		SIZE = size;
		TIMECODE = timecode;
		TIME = time;
	}
}