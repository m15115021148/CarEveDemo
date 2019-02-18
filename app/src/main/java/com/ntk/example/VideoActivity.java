package com.ntk.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.videolan.libvlc.VideoInterface;

import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.ClientScanResult;
import com.ntk.util.DefineTable;
import com.ntk.util.ErrorCode;
import com.ntk.util.FinishScanListener;
import com.ntk.util.ParseResult;
import com.ntk.util.ProfileItem;
import com.ntk.util.Util;
import com.ntk.util.WifiAPUtil;
import com.ntk.util.WifiAPUtil.WIFI_AP_STATE;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class VideoActivity extends Activity implements VideoInterface {
	public final static String TAG = "VideoActivity";

	private Map deviceStatusMap;
	private ArrayList<String> movie_res_indexList;
	private ArrayList<String> movie_res_infoList;

	private boolean isHeartbeat = false;

	private int mode = NVTKitModel.MODE_MOVIE; // mjpg=0 , rtps=1

	private String max_rec_time;
	private String free_capture_num;

	private boolean isRecording = false;
	private boolean hidePanel = true;
	private boolean hideEV = true;

	private ImageView image_record;
	private Button button_record;
	private Button button_pic_on_record;
	private Button button_album;
	private Button button_changeMode;
	private Button button_MovieEV;
	private SeekBar seekBar_MovieEV;
	private Button button_menu;
	private Button button_capture;
	private TextView resTextView;
	private TextView recordTimeTextView;
	private ImageView imageView_battery;
	private RelativeLayout movie_leftPanel;
	private RelativeLayout movie_rightPanel;
	private RelativeLayout movie_topPanel;
	private RelativeLayout photo_rightPanel;
	private RelativeLayout layout_blank;

	private SurfaceView mSurface;
	private SurfaceHolder holder;

	private WifiAPUtil mWifiAPUtil;

	private ProgressDialog psDialog;
	private boolean isProcessing = false;
	
	private ProgressDialog pausedialog;
	private boolean isLoading = false;
	
	Timer blinkTimer;

	private Handler videoHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {

		};
	};

	@SuppressLint("HandlerLeak")
	private Handler eventHandler = new Handler() {
		@SuppressLint("NewApi")
		@Override
		public void handleMessage(android.os.Message msg) {
			String info = msg.obj.toString();
			if (Util.isContainExactWord(info, "&")) {
				if (Util.isContainExactWord(info, "2&")) {

				} else {

					switch (info) {
					case "1&0":
						button_changeMode.callOnClick();
						break;
					case "3&0":
						button_menu.callOnClick();
						break;
					case "5&0":
						button_record.callOnClick();
						break;
					case "6&0":
						button_pic_on_record.callOnClick();
						break;
					default:
						new Thread(new Runnable() {
							@Override
							public void run() {
								final String ack3 = NVTKitModel.autoTestDone();
							}
						}).start();
						break;
					}

				}
			} else {
				if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_POWER_OFF))) {
					Toast.makeText(VideoActivity.this, "Test!!!", Toast.LENGTH_SHORT).show();
					psDialog = new ProgressDialog(VideoActivity.this);
					psDialog.setMessage("Test!!! TestAPP");
					psDialog.setCancelable(false);

					psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Test--", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					});
					psDialog.show();
				} else if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_RECORD_STARTED))) {
					Toast.makeText(VideoActivity.this, "Motion Detect!!!!!", Toast.LENGTH_SHORT).show();
				} else if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_MOVIE_SLOW))) {
					Toast.makeText(VideoActivity.this, "Slow Card!!!!!", Toast.LENGTH_SHORT).show();
				} else if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_REMOVE_BY_USER))) {
					Toast.makeText(VideoActivity.this, "Remove by other!!!!!", Toast.LENGTH_SHORT).show();
				}
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		
		new NVTKitModel(this);
		mWifiAPUtil = new WifiAPUtil(this);

		psDialog = new ProgressDialog(VideoActivity.this);
		layout_blank = (RelativeLayout) findViewById(R.id.layout_blank);
		initMovieLeftPanel();
		initMovieRightPanel();
		initMovieTopPanel();
		initPhotoRightPanel();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();// �����
		switch (action) {
		case MotionEvent.ACTION_UP:

			if (hidePanel == true) {
				hidePanel = false;
				if (mode == NVTKitModel.MODE_MOVIE) {
					setMovieVisible(View.VISIBLE);
				} else if (mode == NVTKitModel.MODE_PHOTO) {
					setPhotoVisible(View.VISIBLE);
				}
			} else {
				hidePanel = true;
				setMovieVisible(View.GONE);
				setPhotoVisible(View.GONE);
			}
			hideEV = true;
			seekBar_MovieEV.setVisibility(View.GONE);
			button_MovieEV.setBackgroundResource(R.drawable.ev_off);
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void onResumeTask() {
		seekBar_MovieEV.setVisibility(View.GONE);
		if (mWifiAPUtil.getWifiApState().equals(WIFI_AP_STATE.WIFI_AP_STATE_DISABLED)) {
			
			NVTKitModel.setWifiEventListener(eventHandler);

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					new ProfileItem();
					
					// init video surface
					mSurface = (SurfaceView) findViewById(R.id.surface);
					holder = mSurface.getHolder();
					mode = 1;

					checkDeviceStatus(); 

					String result = NVTKitModel.changeMode(NVTKitModel.MODE_MOVIE);
					if (result == null) {
						Log.e(TAG, "mode_change fail");
					} else {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								NVTKitModel.videoPlayForLiveView(VideoActivity.this, VideoActivity.this, videoHandler,holder);
							}
						});
						blinkTimer = new Timer(true);
						blinkTimer.schedule(new MyTimerTask(), 1000, 1000);
					}
				}

			});
			t.start();		

		} else if (mWifiAPUtil.getWifiApState().equals(WIFI_AP_STATE.WIFI_AP_STATE_ENABLED)) {
			SharedPreferences settings2 = getSharedPreferences("device_info", 0);
			String mac = settings2.getString("device_mac", null);
			if (mac == null) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						psDialog = new ProgressDialog(VideoActivity.this);
						psDialog.setMessage("�L�k�s�u�A�нT�{�˸m���A!!! ��������APP");
						psDialog.setCancelable(false);
						psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "����",
								new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								finish();
							}
						});
						psDialog.show();
					}
				});
			} else {
				mWifiAPUtil.checkDeviceConnect(mac, true, new FinishScanListener() {
					@Override
					public void onFinishScan(ArrayList<ClientScanResult> clients) {

					}

					@Override
					public void onDeviceConnect(String device_ip) {
						// TODO Auto-generated method stub
						Util.setDeciceIP(device_ip);
						if (device_ip == null) {
							Log.e(TAG, "device_ip == null");
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									psDialog = new ProgressDialog(VideoActivity.this);
									psDialog.setMessage("�L�k�s�u�A�нT�{�˸m���A!!! ��������APP");
									psDialog.setCancelable(false);
									psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "����",
											new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
											finish();
										}
									});
									psDialog.show();
								}
							});
						} else {
							Util.setDeciceIP(device_ip);
							// Log.e(TAG, device_ip);

							
							NVTKitModel.setWifiEventListener(eventHandler);

							// init video surface
							mSurface = (SurfaceView) findViewById(R.id.surface);
							holder = mSurface.getHolder();

							mode = 1;
							new Thread(new Runnable() {
								@Override
								public void run() {
									new ProfileItem();
									checkDeviceStatus();
									String result = NVTKitModel.changeMode(NVTKitModel.MODE_MOVIE);
									if (result == null) {
										Log.e(TAG, "mode_change fail");
									} else {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												NVTKitModel.videoPlayForLiveView(VideoActivity.this, VideoActivity.this,
														videoHandler, holder);
											}
										});
									}
									//
								}
							}).start();

							blinkTimer = new Timer(true);
							blinkTimer.schedule(new MyTimerTask(), 1000, 1000);
						}
					}
				});
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		onResumeTask();
	}

	@Override
	protected void onPause() {
		super.onPause();
		NVTKitModel.stopWifiEventListener();
		NVTKitModel.videoStop();
		setMovieVisible(View.GONE);
		setPhotoVisible(View.GONE);
		// blinkTimer.cancel();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		NVTKitModel.releaseNVTKitModel();
	}

	// SetSizeListener callback
	@Override
	public void setSize(int width, int height) {
		LayoutParams lp = mSurface.getLayoutParams();
		lp.width = width;
		lp.height = height;
		mSurface.setLayoutParams(lp);
		mSurface.invalidate();
	}

	private void setMovieVisible(int visible) {
		movie_leftPanel.setVisibility(visible);
		if (isRecording == true) {
			movie_leftPanel.setVisibility(View.INVISIBLE);
		}
		movie_rightPanel.setVisibility(visible);
		movie_topPanel.setVisibility(visible);
	}

	private void setPhotoVisible(int visible) {
		movie_leftPanel.setVisibility(visible);
		photo_rightPanel.setVisibility(visible);
		movie_topPanel.setVisibility(visible);
	}

	private void checkDeviceStatus() {

		// heartbeat
		String ack_heartbeat = NVTKitModel.devHeartBeat();
		if (ack_heartbeat == null) {
			Log.e(TAG, "heartbeat no response");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setProcessing(true);
				}
			});
		} else if (NVTKitModel.getInitState() == 2) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					psDialog = new ProgressDialog(VideoActivity.this);
					psDialog.setMessage("Bad Command!!");
					psDialog.setCancelable(false);

					psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					});
					psDialog.show();
				}
			});
		} 
		
		else if (NVTKitModel.getInitState() == 3) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					psDialog = new ProgressDialog(VideoActivity.this);
					psDialog.setMessage("Unknown Device!!");
					psDialog.setCancelable(false);

					psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					});
					psDialog.show();
				}
			});
		} 
		
		else {
			// save device_mac for station mode <-> AP mode
			isHeartbeat = true;
			if (mWifiAPUtil.getWifiApState().equals(WIFI_AP_STATE.WIFI_AP_STATE_DISABLED)) {
				SharedPreferences settings = getSharedPreferences("device_info", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("device_mac", mWifiAPUtil.getDeviceMac());
				editor.commit();
			}
		}

		if (isHeartbeat == true) {
			
			//get stream url
			Map map = NVTKitModel.get_liveView_FMT();
			if (map != null && map.get("MovieLiveViewLink") != null) {
				Util.movie_url = map.get("MovieLiveViewLink").toString();
				Util.photo_url = map.get("PhotoLiveViewLink").toString();
			}

			// get battery status
			final String ack_battery = NVTKitModel.qryBatteryStatus();
			if (ack_battery == null) {
				Log.e(TAG, "battery no response");
			} else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						switch (ack_battery) {
						case DefineTable.NVTKitBatterStatus_FULL:
							imageView_battery.setBackgroundResource(R.drawable.battery_full);
							break;
						case DefineTable.NVTKitBatterStatus_MED:
							imageView_battery.setBackgroundResource(R.drawable.battery_75);
							break;
						case DefineTable.NVTKitBatterStatus_LOW:
							imageView_battery.setBackgroundResource(R.drawable.battery_half);
							break;
						case DefineTable.NVTKitBatterStatus_EMPTY:
							imageView_battery.setBackgroundResource(R.drawable.battery_zero);
							break;
						case DefineTable.NVTKitBatterStatus_Exhausted:
							imageView_battery.setBackgroundResource(R.drawable.battery_25);
							break;
						case DefineTable.NVTKitBatterStatus_CHARGE:
							imageView_battery.setBackgroundResource(R.drawable.battery_charging);
							break;
						}
					}
				});
			}

			// get resolution list
			final ParseResult result = NVTKitModel.qryDeviceRecSizeList();
			movie_res_indexList = result.getRecIndexList();
			movie_res_infoList = result.getRecInfoList();
			if (movie_res_indexList.isEmpty()) {
				Log.e(TAG, "query_movie_size fail");
			}

			// get device status
			deviceStatusMap = NVTKitModel.qryDeviceStatus();
			Iterator iter = deviceStatusMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				final String val = (String) entry.getValue();
				// Log.e(key, val);

				if ((key.equals(DefineTable.WIFIAPP_CMD_MOVIE_REC_SIZE)) && (mode == NVTKitModel.MODE_MOVIE)) {
					int i = 0;
					while (i < movie_res_indexList.size()) {
						if (val.equals(movie_res_indexList.get(i))) {
							final int index = i;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									resTextView.setText(movie_res_infoList.get(index));
								}
							});
							break;
						}
						i = i + 1;
					}
				}
				if (key.equals(DefineTable.WIFIAPP_CMD_MOVIE_EV)) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							seekBar_MovieEV.setProgress(Integer.valueOf(val));
						}
					});
				}
				if ((key.equals(DefineTable.WIFIAPP_CMD_CAPTURESIZE)) && (mode == NVTKitModel.MODE_PHOTO)) {

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							resTextView.setText(ProfileItem.list_capturesize.get(Integer.valueOf(val)));
						}
					});
				}
			}

			// movie max record time , free capture number
			max_rec_time = NVTKitModel.qryMaxRecSec();
			free_capture_num = NVTKitModel.qryMaxPhotoNum();

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					image_record = (ImageView) findViewById(R.id.image_onRecord);
					// check is recording
					if (deviceStatusMap.get(DefineTable.WIFIAPP_CMD_MOVIE_RECORDING_TIME).equals("1")) {
						isRecording = true;

					} else {
						isRecording = false;
					}
					setRecordUI();

					if (mode == 0) {
						recordTimeTextView.setText(free_capture_num);
					} else if (mode == 1) {
						int sec = Integer.valueOf(max_rec_time);
						recordTimeTextView.setText(String.format("%02d", sec / 3600) + ":"
								+ String.format("%02d", sec / 60 % 60) + ":" + String.format("%02d", sec % 60));
					}
				}
			});
		}
	}

	private void initMovieLeftPanel() {
		movie_leftPanel = (RelativeLayout) findViewById(R.id.layout_left);
		movie_leftPanel.setVisibility(View.GONE);

		button_changeMode = (Button) findViewById(R.id.button_mode_switch);
		button_changeMode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mode == NVTKitModel.MODE_MOVIE) {
					changeMode(NVTKitModel.MODE_PHOTO);
				} else {
					changeMode(NVTKitModel.MODE_MOVIE);
				}
			}
		});

		button_MovieEV = (Button) findViewById(R.id.button_MovieEV);
		button_MovieEV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if (isRecording == false) {
					if (hideEV == true) {
						hideEV = false;
						seekBar_MovieEV.setVisibility(View.VISIBLE);
						button_MovieEV.setBackgroundResource(R.drawable.ev_adjust);
					} else {
						hideEV = true;
						seekBar_MovieEV.setVisibility(View.GONE);
						button_MovieEV.setBackgroundResource(R.drawable.ev_off);
					}
				} else {
					Toast.makeText(VideoActivity.this, "���v��!! �L�k���EV!!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		seekBar_MovieEV = (SeekBar) findViewById(R.id.seekBar_MovieEV);
		seekBar_MovieEV.setVisibility(View.GONE);
		button_MovieEV.setBackgroundResource(R.drawable.ev_off);
		seekBar_MovieEV.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (isRecording == false) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							final String ack = NVTKitModel.setMovieEV(String.valueOf(seekBar_MovieEV.getProgress()));
							// Log.e(TAG, "seekBar_MovieEV"
							// +seekBar_MovieEV.getProgress());
						}
					}).start();
				} else {
					Toast.makeText(VideoActivity.this, "���v��!! �L�k���EV!!", Toast.LENGTH_SHORT).show();
				}

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
		});

		button_menu = (Button) findViewById(R.id.button_menu);
		button_menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isRecording == false) {
					Intent intent = new Intent();
					intent.setClass(VideoActivity.this, MenuActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(VideoActivity.this, "���v��!! �L�k�}��MENU!!", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void initMovieRightPanel() {
		movie_rightPanel = (RelativeLayout) findViewById(R.id.layout_right);
		movie_rightPanel.setVisibility(View.GONE);

		button_album = (Button) findViewById(R.id.button_album);
		button_album.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!isRecording) {
					Intent intent = new Intent(VideoActivity.this, AlbumActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(VideoActivity.this, "���v��!! �L�k�ϥάۥ�!!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		button_record = (Button) findViewById(R.id.button_record);
		button_record.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				seekBar_MovieEV.setVisibility(View.GONE);
				setLoading(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						if (isRecording == false) {
							final String result = NVTKitModel.recordStart();
							isRecording = true;
							setRecordUI();
						} else {
							final String result = NVTKitModel.recordStop();
							isRecording = false;
							checkDeviceStatus();
							setRecordUI();
						}
						setLoading(false);

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						NVTKitModel.videoPlayForLiveView(VideoActivity.this, VideoActivity.this, videoHandler,holder);
						final String ack3 = NVTKitModel.autoTestDone();
					}
				}).start();
			}
		});

		button_pic_on_record = (Button) findViewById(R.id.button_pic_on_record);
		button_pic_on_record.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if (isRecording == true) {
					setLoading(true);
					layout_blank.setVisibility(View.VISIBLE);
					new Thread(new Runnable() {
						@Override
						public void run() {
							final String result = NVTKitModel.takePictureOnRecord();
							if (!result.equals(null)) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										layout_blank.setVisibility(View.GONE);
									}
								});
							}
							setLoading(false);
							final String ack3 = NVTKitModel.autoTestDone();
						}
					}).start();
				} else {
					Toast.makeText(VideoActivity.this, "���b���v��!! �L�k�I��!!", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void initMovieTopPanel() {
		movie_topPanel = (RelativeLayout) findViewById(R.id.layout_top);
		movie_topPanel.setVisibility(View.GONE);

		resTextView = (TextView) findViewById(R.id.textView_top_resolution);
		recordTimeTextView = (TextView) findViewById(R.id.textView_top_max_record_time);

		imageView_battery = (ImageView) findViewById(R.id.imageView_battery);
	}

	private void initPhotoRightPanel() {
		photo_rightPanel = (RelativeLayout) findViewById(R.id.photo_layout_right);
		photo_rightPanel.setVisibility(View.GONE);

		Button button_photo_album = (Button) findViewById(R.id.button_photo_album);
		button_photo_album.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(VideoActivity.this, ListActivity.class);
				startActivity(intent);
			}
		});

		button_capture = (Button) findViewById(R.id.button_capture);
		button_capture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setLoading(true);
				layout_blank.setVisibility(View.VISIBLE);
				new Thread(new Runnable() {
					@Override
					public void run() {
						final Map result = NVTKitModel.takePhoto();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								layout_blank.setVisibility(View.GONE);
								free_capture_num = (String) result.get("FREEPICNUM");
								recordTimeTextView.setText(free_capture_num);
							}
						});
						setLoading(false);
					}
				}).start();
			}
		});
	}

	private void changeMode(final int mode) {
		this.mode = mode;
		setLoading(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String ack = NVTKitModel.changeMode(mode);
				if (ack != null) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(mode == NVTKitModel.MODE_PHOTO) {
						NVTKitModel.videoPlayForPhotoCapture(VideoActivity.this, VideoActivity.this, videoHandler,holder);
					} else if(mode == NVTKitModel.MODE_MOVIE){
						NVTKitModel.videoPlayForLiveView(VideoActivity.this, VideoActivity.this, videoHandler,holder);
					}
				} else {
					Toast.makeText(VideoActivity.this, "changeMode fail!!!", Toast.LENGTH_SHORT).show();
				}

				checkDeviceStatus(); // todo
				setLoading(false);

				final String ack3 = NVTKitModel.autoTestDone();
			}
		}).start();
			
		if (mode == NVTKitModel.MODE_MOVIE) {
			button_changeMode.setBackgroundResource(R.drawable.mode_changeto_still);
		} else {
			button_changeMode.setBackgroundResource(R.drawable.mode_changeto_video);
		}
		setMovieVisible(View.GONE);
		setPhotoVisible(View.GONE);
		seekBar_MovieEV.setVisibility(View.GONE);
	}

	private void setRecordUI() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (isRecording == false) {
					image_record.setVisibility(View.INVISIBLE);
					button_record.setBackgroundResource(R.drawable.shutter_rec_start);
					button_pic_on_record.setBackgroundResource(R.drawable.shutter_capture_instill_lock);
					button_album.setBackgroundResource(R.drawable.pbk);
					setMovieVisible(View.INVISIBLE);
					hidePanel = true;
				} else {
					image_record.setVisibility(View.VISIBLE);
					button_record.setBackgroundResource(R.drawable.shutter_rec_stop);
					button_pic_on_record.setBackgroundResource(R.drawable.shutter_capture_instill);
					button_album.setBackgroundResource(R.drawable.pbk_lock);
					setMovieVisible(View.INVISIBLE);
					recordTimeTextView.setText("(Recording...)");
					hidePanel = true;
				}
			}
		});
	}

	public class MyTimerTask extends TimerTask {
		boolean isOn = true;

		public void run() {
			if (isRecording) {
				if (isOn) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							image_record.setVisibility(View.GONE);
						}
					});
					isOn = false;
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							image_record.setVisibility(View.VISIBLE);
						}
					});
					isOn = true;
				}
			}
		}
	}
/*
	private void setLoading(final boolean isLoading) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (isLoading == true) {
					findViewById(R.id.loading).setVisibility(View.VISIBLE);
					setClickable(false);
				} else {
					findViewById(R.id.loading).setVisibility(View.INVISIBLE);
					setClickable(true);
				}
			}

		});
	}
	*/
	
	private void setLoading(final boolean isOpen) {
		// TODO Auto-generated method stub

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (isOpen == true) {
					if (isLoading == false) {
						pausedialog = ProgressDialog.show(VideoActivity.this, "Processing", "Please wait...", true);
						isLoading = true;
					}
				} else {
					isLoading = false;
					pausedialog.dismiss();
				}
			}
		});
	}

	private void setClickable(final boolean isClickable) {

		button_record.setClickable(isClickable);
		button_pic_on_record.setClickable(isClickable);
		button_album.setClickable(isClickable);
		button_changeMode.setClickable(isClickable);
		button_MovieEV.setClickable(isClickable);
		seekBar_MovieEV.setClickable(isClickable);
		button_menu.setClickable(isClickable);
		button_capture.setClickable(isClickable);
	}

	private void setProcessing(final boolean is) {
		// TODO Auto-generated method stub

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (is == true) {
					if (isProcessing == false) {

						isProcessing = true;

						psDialog.setMessage("Connection Fail!!!");
						psDialog.setCancelable(false);
						psDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Local File",
								new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								setProcessing(false);
								Intent intent = new Intent(VideoActivity.this, LocalFileActivity.class);
								startActivity(intent);
							}
						});
						psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Try again",
								new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// psDialog.dismiss();
								setProcessing(false);
								onResumeTask();
								// finish();
							}
						});
						psDialog.show();
					}
				} else {
					isProcessing = false;
					psDialog.dismiss();
				}
			}
		});
	}
}
