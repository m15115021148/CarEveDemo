package com.ntk.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;



import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

public class WifiAPUtil  {
	
	public enum WIFI_AP_STATE {
		WIFI_AP_STATE_DISABLING, WIFI_AP_STATE_DISABLED, WIFI_AP_STATE_ENABLING, WIFI_AP_STATE_ENABLED, WIFI_AP_STATE_FAILED
	}
    
    private final WifiManager mWifiManager;
    private final ConnectivityManager connManager;
	private Context context;
	
	public WifiAPUtil(Context context) {
		this.context = context;
		mWifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
		connManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	public String getDeviceMac() {
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi.isConnected()) {
			WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
			String deviceMac = wifiInfo.getBSSID();
			//Log.e("mac", deviceMac);
			
			return deviceMac;
		}
		
		return null;		
	}
	
	
	public WIFI_AP_STATE getWifiApState() {
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApState");

			int tmp = ((Integer)method.invoke(mWifiManager));

			// Fix for Android 4
			if (tmp >= 10) {
				tmp = tmp - 10;
			}

			return WIFI_AP_STATE.class.getEnumConstants()[tmp];
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
		}
	}
	
	public String getWifiApSSID() {
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
			WifiConfiguration mWifiConfiguration = (WifiConfiguration) method.invoke(mWifiManager);
			return mWifiConfiguration.SSID;
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return null;
		}
	}
	
	public String getWifiApPWD() {
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
			WifiConfiguration mWifiConfiguration = (WifiConfiguration) method.invoke(mWifiManager);
			return mWifiConfiguration.preSharedKey;
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return null;
		}
	}
	
	public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
		try {
			if (enabled) { // disable WiFi in any case
				mWifiManager.setWifiEnabled(false);
			}
			/*
			WifiConfiguration wifiConfiguration = new WifiConfiguration();
			wifiConfiguration.SSID = "SomeName";
			wifiConfiguration.preSharedKey = "SomeKey";
			wifiConfiguration.hiddenSSID = false;
			wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wifiConfiguration.allowedKeyManagement.set(4);
			wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			*/

			Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
			boolean isSuccess = (boolean) method.invoke(mWifiManager, wifiConfig, enabled);
			
			if (!enabled) { // disable WiFi in any case
				mWifiManager.setWifiEnabled(true);
			}
			
			return isSuccess;
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return false;
		}
	}
	
	public void getClientList(final boolean onlyReachables, final int reachableTimeout, final FinishScanListener finishListener) {
		Runnable runnable = new Runnable() {
			public void run() {

				BufferedReader br = null;
				final ArrayList<ClientScanResult> result = new ArrayList<ClientScanResult>();
				
				try {
					br = new BufferedReader(new FileReader("/proc/net/arp"));
					String line;
					while ((line = br.readLine()) != null) {
						String[] splitted = line.split(" +");

						if ((splitted != null) && (splitted.length >= 4)) {
							// Basic sanity check
							String mac = splitted[3];

							if (mac.matches("..:..:..:..:..:..")) {
								boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);

								if (!onlyReachables || isReachable) {
									result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
								}
							}
						}
					}
				} catch (Exception e) {
					Log.e(this.getClass().toString(), e.toString());
				} finally {
					try {
						br.close();
					} catch (IOException e) {
						Log.e(this.getClass().toString(), e.getMessage());
					}
				}

				// Get a handler that can be used to post to the main thread
				Handler mainHandler = new Handler(context.getMainLooper());
				Runnable myRunnable = new Runnable() {
					@Override
					public void run() {
						finishListener.onFinishScan(result);
					}
				};
				mainHandler.post(myRunnable);
			}
		};

		Thread mythread = new Thread(runnable);
		mythread.start();
	}
	
	public void checkDeviceConnect(final String device_mac, final boolean isHurry, final FinishScanListener finishListener) {

		Runnable runnable = new Runnable() {
			
			String device_ip;
			
			public void run() {

				BufferedReader br = null;
				final ArrayList<ClientScanResult> result = new ArrayList<ClientScanResult>();
				
				try {
					boolean isConnect = false;
					
					while (!isConnect) {
						//Log.e("try", "11");
						br = new BufferedReader(new FileReader("/proc/net/arp"));
						String line;
						while ((line = br.readLine()) != null) {
							Log.e("line", line);
							String[] splitted = line.split(" +");

							if ((splitted != null) && (splitted.length >= 4)) {
								// Basic sanity check
								String mac = splitted[3];

								if (mac.matches("..:..:..:..:..:..")) {
									boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(300);

									if (isReachable == true) {
										result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5],isReachable));
									}
								}
								
								if(mac.equals(device_mac)) {
									if(!splitted[0].equals("192.168.1.254")) {
										isConnect = true;
										device_ip = splitted[0];
										
										//Log.e("ipAddr", splitted[0]);
										//Log.e("hWAddr", splitted[3]);
										//Log.e("device", splitted[5]);
									}
								}
							}

						}
						Thread.sleep(500);
						if(isHurry == true) {
							break;
						}
					}
				} catch (Exception e) {
					Log.e(this.getClass().toString(), e.toString());
				} finally {
					try {
						br.close();
					} catch (IOException e) {
						Log.e(this.getClass().toString(), e.getMessage());
					}
				}

				// Get a handler that can be used to post to the main thread
				Handler mainHandler = new Handler(context.getMainLooper());
				Runnable myRunnable = new Runnable() {
					@Override
					public void run() {
						finishListener.onDeviceConnect(device_ip);
					}
				};
				mainHandler.post(myRunnable);
			}
		};

		Thread mythread = new Thread(runnable);
		mythread.start();
	}
}