package com.ntk.example;

import java.io.File;
import java.util.ArrayList;

import com.ntk.album.AlbumListAdapter;
import com.ntk.album.ListItem;
import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.ClientScanResult;
import com.ntk.util.FinishScanListener;
import com.ntk.util.Util;
import com.ntk.util.WifiAPUtil;
import com.ntk.util.WifiAPUtil.WIFI_AP_STATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class LocalFileActivity extends Activity {

	private final static String TAG = "ListActivity";

	private AlbumListAdapter mCustomListAdapter;

	private boolean isPhoto = false;
	
	Button button_photo;
	Button button_movie;
	
	private WifiAPUtil mWifiAPUtil;
	private String device_mac;
	
	boolean isDevConnected = true;
	
	private ProgressDialog pausedialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		mWifiAPUtil = new WifiAPUtil(LocalFileActivity.this);
		
        button_photo = (Button) findViewById(R.id.button_photo);
        button_photo.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View arg0) { 
        		isPhoto = true;
        		initAllList(isPhoto);
        	}
        });  
        
        button_movie = (Button) findViewById(R.id.button_movie);
        button_movie.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View arg0) { 
        		isPhoto = false;
        		initAllList(isPhoto);
        	}
        });  
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		
		new Thread(new Runnable(){
		    @Override
		    public void run() {

		    	String result = NVTKitModel.devHeartBeat();
		    	if (result == null) {
		    		isDevConnected = false;
		    		Log.e("asdf", "false");
				}	    
		    }   
		         
		}).start();
		
		initAllList(isPhoto);
	}
	
	private void initAllList(boolean isPhoto) {
		
		String path;
		if(isPhoto) {
			path = Util.local_photo_path;
		} else {
			path = Util.local_movie_path;
		}
		
		File folder = new File(path);
		String[] list = folder.list();
		ArrayList<ListItem> listMockData = new ArrayList<ListItem>();

		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				ListItem newsData = new ListItem();
				newsData.setUrl("");
				newsData.setName(list[i]);
				newsData.setFpath("");
				newsData.setTime("");
				listMockData.add(newsData);
			}
		}
		final ArrayList<ListItem> listData = listMockData;

		final ListView listView = (ListView) findViewById(R.id.custom_list);
		mCustomListAdapter = new AlbumListAdapter(LocalFileActivity.this, listData);
		listView.setAdapter(mCustomListAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, final int position, long id) {
				final ListItem newsData = (ListItem) listView.getItemAtPosition(position);

				CharSequence items[] = new CharSequence[] { "PLAY", "DELETE", "SHARE" };
				final ArrayAdapter adapter_option = new ArrayAdapter(LocalFileActivity.this, android.R.layout.simple_spinner_item, items);
				new AlertDialog.Builder(LocalFileActivity.this).setTitle("Options").setAdapter(adapter_option, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, final int which) {
						switch (which) {
						case 0:
							if(Util.isContainExactWord(newsData.getName(), "JPG")) {
								Intent intent = new Intent(LocalFileActivity.this, GalleryActivity.class);
								Bundle bundle = new Bundle();
								bundle.putString("name", newsData.getName());
								bundle.putString("url", Util.local_photo_path + "/" + newsData.getName());
								bundle.putInt("position", position);
								intent.putExtras(bundle);
								startActivity(intent);
							} 
							else if(Util.isContainExactWord(newsData.getName(), "MP4")) {
								Intent intent = new Intent(LocalFileActivity.this, PlaybackActivity.class);
								Bundle bundle = new Bundle();
								bundle.putString("url", Util.local_movie_path + "/" + newsData.getName());
								intent.putExtras(bundle);
								startActivity(intent);
							}
							break;
						case 1:							
							if(Util.isContainExactWord(newsData.getName(), "JPG")) {
								File f= new File(Util.local_photo_path + "/" + newsData.getName());
								if(f.exists()){
									f.delete();
								}
							} 
							else {
								File f= new File(Util.local_movie_path + "/" + newsData.getName());
								if(f.exists()){
									f.delete();
								}
							}
	        		    	runOnUiThread(new Runnable() {
	        		            @Override
	        		            public void run() {
	        		            	mCustomListAdapter.removeItem(position);
	        		            	mCustomListAdapter.notifyDataSetChanged();
	        		            }
	        		    	});
							break;
						case 2:			
							
							AlertDialog.Builder editDialog = new AlertDialog.Builder(LocalFileActivity.this);
							
							if (mWifiAPUtil.getWifiApState().equals(WIFI_AP_STATE.WIFI_AP_STATE_DISABLED)) {
								editDialog.setTitle("?T?w?n??????AP????");
								editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface arg0, int arg1) {

										setLoading(true);
										new Thread(new Runnable() {
											@Override
											public void run() {												
												device_mac = mWifiAPUtil.getDeviceMac();
												String ack = NVTKitModel.send_hotspot_ssid_pwd(mWifiAPUtil.getWifiApSSID(), mWifiAPUtil.getWifiApPWD());
												NVTKitModel.set_station_mode(true);
												NVTKitModel.netReConnect();
												
												try {
													Thread.sleep(200);
												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
												mWifiAPUtil.setWifiApEnabled(null, true);	
												mWifiAPUtil.checkDeviceConnect(device_mac, false, new FinishScanListener() {

													@Override
													public void onFinishScan(ArrayList<ClientScanResult> clients) {

													}

													@Override
													public void onDeviceConnect(String device_ip) {
														// TODO Auto-generated method stub
														Util.setDeciceIP(device_ip);
														Log.e(TAG, device_ip);
														setLoading(false);
														
														if(Util.isContainExactWord(newsData.getName(), "JPG")) {
															Intent share = new Intent(android.content.Intent.ACTION_SEND); 
															share.setType("image/*");
															share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Util.local_photo_path + "/" + newsData.getName()));
															startActivity(Intent.createChooser(share, "Share image"));
														} 
														else if(Util.isContainExactWord(newsData.getName(), "MP4")) {
															Intent share = new Intent(android.content.Intent.ACTION_SEND); 
															share.setType("video/*");
															share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Util.local_movie_path + "/" + newsData.getName()));
															startActivity(Intent.createChooser(share, "Share video"));
														}													
													}
													
												});								
											}
										}).start();
									}

								});

								editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface arg0, int arg1) {

									}
								});
								editDialog.show();
								
							} else {
								if(Util.isContainExactWord(newsData.getName(), "JPG")) {
									Intent share = new Intent(android.content.Intent.ACTION_SEND); 
									share.setType("image/*");
									share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Util.local_photo_path + "/" + newsData.getName()));
									startActivity(Intent.createChooser(share, "Share image"));
								} 
								else if(Util.isContainExactWord(newsData.getName(), "MP4")) {
									Intent share = new Intent(android.content.Intent.ACTION_SEND); 
									share.setType("video/*");
									share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Util.local_movie_path + "/" + newsData.getName()));
									startActivity(Intent.createChooser(share, "Share video"));
								}
							}
							
							
							

							break;
						}
						dialog.dismiss();
					}
				}).create().show();
			}
		});
		
    	if(isPhoto == true) {
    		button_photo.getBackground().setAlpha(255);
    		button_movie.getBackground().setAlpha(100);
    	} else {
    		button_photo.getBackground().setAlpha(100);
    		button_movie.getBackground().setAlpha(255);
    	}
	}


	
	private void setLoading(boolean isLoading) {
		// TODO Auto-generated method stub
		if(isLoading == true) {
			pausedialog = ProgressDialog.show(LocalFileActivity.this,"Processing", "Please wait...",true);
		} else {
	    	runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	pausedialog.dismiss();
	            }
	    	});
		}
	}
}
