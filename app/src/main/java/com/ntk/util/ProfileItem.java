package com.ntk.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import com.ntk.nvtkit.NVTKitModel;

import android.util.Log;

public class ProfileItem {

	public ProfileItem() {
		if (list_capturesize.isEmpty())
			get_pofile();
	}

	String TAG = "ProfileItem";
	TreeMap itemMap = null;

	public static ArrayList<String> list_capturesize = new ArrayList<String>();
	public static ArrayList<String> list_capturesize_index = new ArrayList<String>();

	public static ArrayList<String> list_movie_rec_size = new ArrayList<String>();
	public static ArrayList<String> list_movie_rec_size_index = new ArrayList<String>();

	public static ArrayList<String> list_cyclic_rec = new ArrayList<String>();
	public static ArrayList<String> list_cyclic_rec_index = new ArrayList<String>();

	public static ArrayList<String> list_movie_hdr = new ArrayList<String>();
	public static ArrayList<String> list_movie_hdr_index = new ArrayList<String>();

	public static ArrayList<String> list_movie_ev = new ArrayList<String>();
	public static ArrayList<String> list_movie_ev_index = new ArrayList<String>();

	public static ArrayList<String> list_motion_det = new ArrayList<String>();
	public static ArrayList<String> list_motion_det_index = new ArrayList<String>();

	public static ArrayList<String> list_movie_audio = new ArrayList<String>();
	public static ArrayList<String> list_movie_audio_index = new ArrayList<String>();

	public static ArrayList<String> list_dateimprint = new ArrayList<String>();
	public static ArrayList<String> list_dateimprint_index = new ArrayList<String>();

	public static ArrayList<String> list_movie_gsensor_sens = new ArrayList<String>();
	public static ArrayList<String> list_movie_gsensor_sens_index = new ArrayList<String>();

	public static ArrayList<String> list_set_auto_recording = new ArrayList<String>();
	public static ArrayList<String> list_set_auto_recording_index = new ArrayList<String>();

	public static ArrayList<String> list_auto_power_off = new ArrayList<String>();
	public static ArrayList<String> list_auto_power_off_index = new ArrayList<String>();

	public static ArrayList<String> list_language = new ArrayList<String>();
	public static ArrayList<String> list_language_index = new ArrayList<String>();

	public static ArrayList<String> list_tvformat = new ArrayList<String>();
	public static ArrayList<String> list_tvformat_index = new ArrayList<String>();

	public ArrayList<String> get_auto_power_off_list() {
		return null;
	}

	private void get_pofile() {
		itemMap = NVTKitModel.qryMenuItemList();

		if (itemMap != null) {

			Set keys = itemMap.keySet();
			for (Iterator i = keys.iterator(); i.hasNext();) {
				String key = (String) i.next();
				TreeMap menuListMap = (TreeMap) itemMap.get(key);

				Set menuListKey = menuListMap.keySet();
				for (Iterator j = menuListKey.iterator(); j.hasNext();) {
					String menuListKeys = (String) j.next();
					String menuListValue = (String) menuListMap.get(menuListKeys);
					Log.e(TAG, menuListKeys + "  " + menuListValue);

					switch (key) {
					case DefineTable.WIFIAPP_CMD_CAPTURESIZE:
						list_capturesize.add(menuListValue);
						list_capturesize_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_MOVIE_REC_SIZE:
						list_movie_rec_size.add(menuListValue);
						list_movie_rec_size_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_CYCLIC_REC:

						list_cyclic_rec.add(menuListValue);
						list_cyclic_rec_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_MOVIE_HDR:
						list_movie_hdr.add(menuListValue);
						list_movie_hdr_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_MOVIE_EV:
						list_movie_ev.add(menuListValue);
						list_movie_ev_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_MOTION_DET:
						list_motion_det.add(menuListValue);
						list_motion_det_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_MOVIE_AUDIO:
						list_movie_audio.add(menuListValue);
						list_movie_audio_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_DATEIMPRINT:
						list_dateimprint.add(menuListValue);
						list_dateimprint_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_MOVIE_GSENSOR_SENS:
						list_movie_gsensor_sens.add(menuListValue);
						list_movie_gsensor_sens_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_SET_AUTO_RECORDING:
						list_set_auto_recording.add(menuListValue);
						list_set_auto_recording_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_POWEROFF:
						list_auto_power_off.add(menuListValue);
						list_auto_power_off_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_LANGUAGE:
						list_language.add(menuListValue);
						list_language_index.add(menuListKeys);
						break;
					case DefineTable.WIFIAPP_CMD_TVFORMAT:
						list_tvformat.add(menuListValue);
						list_tvformat_index.add(menuListKeys);
						break;
					}
				}
			}
		}
	}
}
