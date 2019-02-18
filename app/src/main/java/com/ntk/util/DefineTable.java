package com.ntk.util;

public class DefineTable  {

		//Battery Status
		public static final String NVTKitBatterStatus_FULL = "0";
		public static final String NVTKitBatterStatus_MED = "1"; 
		public static final String NVTKitBatterStatus_LOW = "2"; 
		public static final String NVTKitBatterStatus_EMPTY = "3"; 
		public static final String NVTKitBatterStatus_Exhausted = "4"; 
		public static final String NVTKitBatterStatus_CHARGE = "5";
		
		// Card Status
		public static final String NVTKitCardStatus_Removed = "0";
		public static final String NVTKitCardStatus_Inserted = "1"; 
		public static final String NVTKitCardStatus_Locked = "2"; 
		public static final String NVTKitCardStatus_DiskError = "3024"; 
		public static final String NVTKitCardStatus_UnknownFormat = "3025"; 
		public static final String NVTKitCardStatus_Unformatted = "3026";
		public static final String NVTKitCardStatus_NotInit = "3027"; 
		public static final String NVTKitCardStatus_InitOK = "3028"; 
		public static final String NVTKitCardStatus_NumFull = "3029";
		
		// 
		public static final String WIFIAPP_CMD_CAPTURESIZE = "1002";
		public static final String WIFIAPP_CMD_MOVIE_REC_SIZE = "2002";
		public static final String WIFIAPP_CMD_CYCLIC_REC = "2003";
		public static final String WIFIAPP_CMD_MOVIE_HDR = "2004";
		public static final String WIFIAPP_CMD_MOVIE_EV = "2005";
		public static final String WIFIAPP_CMD_MOTION_DET = "2006";
		public static final String WIFIAPP_CMD_MOVIE_AUDIO = "2007";
		public static final String WIFIAPP_CMD_DATEIMPRINT = "2008";
		public static final String WIFIAPP_CMD_MOVIE_GSENSOR_SENS = "2011";
		public static final String WIFIAPP_CMD_SET_AUTO_RECORDING = "2012";
		public static final String WIFIAPP_CMD_MOVIE_RECORDING_TIME = "2016";
		public static final String WIFIAPP_CMD_POWEROFF = "3007";
		public static final String WIFIAPP_CMD_LANGUAGE = "3008";
		public static final String WIFIAPP_CMD_TVFORMAT = "3009";

		
		public static final String NVTKitFormat_DEVICE = "0";
		public static final String NVTKitFormat_CARD = "1";
}