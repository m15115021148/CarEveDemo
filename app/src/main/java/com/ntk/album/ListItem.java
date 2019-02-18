package com.ntk.album;

public class ListItem {

	private String name;
	private String Fpath;
	private String time;
	private String url;
	public boolean isDownload = false;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFpath() {
		return Fpath;
	}

	public void setFpath(String Fpath) {
		this.Fpath = Fpath;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "[ headline=" + name + ", Fpath=" + Fpath + " , time=" + time + "]";
	}
}
