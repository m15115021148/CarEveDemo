package com.ntk.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class AlbumActivity extends Activity {
	
    private final static String TAG = "AlbumActivity";

    String imagePath;
    String name;
    int position;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        
        RelativeLayout layout_remote = (RelativeLayout) findViewById(R.id.layout_remote);
        layout_remote.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View arg0) { 
    			Intent intent = new Intent(AlbumActivity.this, ListActivity.class);
    			startActivity(intent);
        	}
        }); 
        
        RelativeLayout layout_local = (RelativeLayout) findViewById(R.id.layout_local);
        layout_local.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View arg0) { 
    			Intent intent = new Intent(AlbumActivity.this, LocalFileActivity.class);
    			startActivity(intent);
        	}
        }); 
    } 
}


