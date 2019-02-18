package com.ntk.example;

import org.videolan.libvlc.VideoInterface;

import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.Util;
import com.ntk.util.VideoEvent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PlaybackActivity extends Activity implements SurfaceHolder.Callback, VideoInterface {
    public final static String TAG = "PlaybackActivity";

    private static String videoPath;   
    private long videoLength;

    private SurfaceView mSurface;
    private static Context mContext;
    private static VideoInterface mVideoInterface;
    private static SurfaceHolder holder;
    
    private static SeekBar seekBar_videotime;
    private static TextView textView_time;
    private static TextView textView_length;
    private static Button button_play;

    
    private static Handler videoHandler = new Handler() {
        @Override  
        public void handleMessage(android.os.Message msg) {  
        	String info = msg.obj.toString();
        	if(info.equals(String.valueOf(VideoEvent.MediaPlayerPositionChanged))) {        		
				float progress = 100 * NVTKitModel.videoQryCurtime() / NVTKitModel.videoQryLenth();				
        		seekBar_videotime.setProgress((int) progress);
        		int sec = (int) (NVTKitModel.videoQryLenth()/1000);
        		textView_length.setText(String.format("%02d", sec/60) + ":" + String.format("%02d", sec%60));
        		
        		int sec2 = (int) (NVTKitModel.videoQryCurtime()/1000);
        		textView_time.setText(String.format("%02d", sec2/60) + ":" + String.format("%02d", sec2%60));
        		
        	} else if(info.equals(String.valueOf(VideoEvent.MediaPlayerEndReached))) {
        		seekBar_videotime.setProgress(0);
        		textView_time.setText("00:00");
        		button_play.setBackgroundResource(R.drawable.control_playing);
        	}
        };  
    };    

    private ProgressDialog psDialog;
    private Handler handler = new Handler() {  
        @Override  
        public void handleMessage(android.os.Message msg) {  
        	String info = msg.obj.toString();
        	if (info.equals("6")) {
        		//Toast.makeText(VideoActivity.this, "?????w????!!!", Toast.LENGTH_SHORT).show();        		       		
        		psDialog = new ProgressDialog(PlaybackActivity.this);
        		psDialog.setMessage("?????w????!!! ????????APP");
        		psDialog.setCancelable(false);
        		psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "????", new DialogInterface.OnClickListener() {
        		    @Override
        		    public void onClick(DialogInterface dialog, int which) {
        		        dialog.dismiss();
        		        finish();
        		    }
        		});
        		psDialog.show();
        	}
        	else if (info.equals("1")) {
        		Toast.makeText(PlaybackActivity.this, "Motion Detect!!!!!", Toast.LENGTH_SHORT).show();
        	}
        	else if (info.equals("-9")) {
        		Toast.makeText(PlaybackActivity.this, "Slow Card!!!!!", Toast.LENGTH_SHORT).show();
        	}
        };  
    }; 

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     
        setContentView(R.layout.activity_playback); 
        
        mContext = PlaybackActivity.this;
        mVideoInterface = PlaybackActivity.this;
       
        //NVTKitModel.setWifiEventListener(handler);
        
        Bundle bundle = this.getIntent().getExtras();
        videoPath = bundle.getString("url");
        button_play = (Button) findViewById(R.id.button_play);   
        button_play.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View arg0) {
        		
        		if(NVTKitModel.isVideoEngineNull() == true) {
    		        NVTKitModel.videoPlayForFile(videoPath, mContext, mVideoInterface, videoHandler, holder);	        
    		        button_play.setBackgroundResource(R.drawable.control_pause);
				} else {
					if (NVTKitModel.videoQryisPlaying() == true) {
						NVTKitModel.videoPause();
						button_play.setBackgroundResource(R.drawable.control_playing);
					} else {
						NVTKitModel.videoResumePlay();
						button_play.setBackgroundResource(R.drawable.control_pause);
					}
				}
        		
        	}
        });  
        
        seekBar_videotime = (SeekBar) findViewById(R.id.seekBar_videotime);
        seekBar_videotime.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {	
				float progress = seekBar.getProgress();				
				NVTKitModel.videoSetPosition( (long) ((progress/100) * NVTKitModel.videoQryLenth()) );
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				//Log.e(TAG, "videoLength " + videoLength + " , progress " + progress);
			}
		});	
        
        textView_time = (TextView) findViewById(R.id.textView_time);
        textView_length = (TextView) findViewById(R.id.textView_length);
    }
	   
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // init video surface
		mSurface = (SurfaceView) findViewById(R.id.surface);
		holder = mSurface.getHolder();
		holder.addCallback(this);
		Log.e("url", videoPath);
		NVTKitModel.videoPlayForFile(videoPath, mContext, mVideoInterface, videoHandler, holder);
		
		videoLength = (long) NVTKitModel.videoQryLenth();

		if (NVTKitModel.videoQryisPlaying() == true) {
			button_play.setBackgroundResource(R.drawable.control_playing);
		} else {
			button_play.setBackgroundResource(R.drawable.control_pause);
		}

	}
    
    @Override
    protected void onPause() {
        super.onPause();     
        NVTKitModel.videoStop();
    }

     // Surface callback
    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
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
}
