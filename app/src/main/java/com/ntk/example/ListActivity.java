package com.ntk.example;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.ntk.album.AlbumListAdapter;
import com.ntk.album.ListItem;
import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.ParseResult;
import com.ntk.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ListActivity extends Activity {
	
	private final static String TAG = "ListActivity";
	
	AlbumListAdapter mCustomListAdapter;
	
	private boolean isPhoto = false;
	
	private Button button_photo;
	private Button button_movie;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        
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
        initAllList(isPhoto);
    }
    
	private void initAllList(final boolean isPhoto) {
		setLoading(true);
		new Thread(new Runnable(){
		    @Override
		    public void run() {
		    	// ??????playback mode
		    	final String ack = NVTKitModel.changeMode(NVTKitModel.MODE_PLAYBACK);
		    	
		    	// filelist
		    	ParseResult result = NVTKitModel.getFileList();	    	
		        ArrayList<ListItem> listMockData = new ArrayList<ListItem>();
				if (result.getFileItemList() != null) {
					for (int i = 0; i < result.getFileItemList().size(); i++) {
						if (isPhoto == true) {
							ListItem newsData = new ListItem();
							if (Util.isContainExactWord(result.getFileItemList().get(i).NAME, "JPG")) {
								
								String url = result.getFileItemList().get(i).FPATH;
								String url1 = url.replace("A:", "http://" + Util.getDeciceIP() + "");
								String url2 = url1.replace("\\", "/");
								//Log.e("JPG", url2);
								newsData.setUrl(url2);
							} else {
								continue;
							}
							newsData.setName(result.getFileItemList().get(i).NAME);
							newsData.setFpath(result.getFileItemList().get(i).FPATH);
							newsData.setTime(result.getFileItemList().get(i).TIME);
							listMockData.add(newsData);
						} else {
							ListItem newsData = new ListItem();
							if (Util.isContainExactWord(result.getFileItemList().get(i).NAME, "JPG")) {
								continue;
							} else {
								//Log.e(TAG, result.getFileItemList().get(i).FPATH );
								String url = result.getFileItemList().get(i).FPATH;
								String url1 = url.replace("A:", "http://" + Util.getDeciceIP() + "");
								String url2 = url1.replace("\\", "/");
								//Log.e("MOV", url2);
								newsData.setUrl(url2);
							}
							newsData.setName(result.getFileItemList().get(i).NAME);
							newsData.setFpath(result.getFileItemList().get(i).FPATH);
							newsData.setTime(result.getFileItemList().get(i).TIME);
							listMockData.add(newsData);
							
							
						}
					}
				}
		        
		        final ArrayList<ListItem> listData = listMockData;
		    			    	
		    	runOnUiThread(new Runnable() {
		            @Override
		            public void run() {
		            	
		            	setLoading(false);
		            	
		            	if(isPhoto == true) {
			        		button_photo.getBackground().setAlpha(255);
			        		button_movie.getBackground().setAlpha(100);
		            	} else {
			        		button_photo.getBackground().setAlpha(100);
			        		button_movie.getBackground().setAlpha(255);
		            	}
		                
		                final ListView listView = (ListView) findViewById(R.id.custom_list);
		                mCustomListAdapter = new AlbumListAdapter(ListActivity.this, listData);
		                listView.setAdapter(mCustomListAdapter);
		                listView.setOnItemClickListener(new OnItemClickListener() {
		                    @Override
		                    public void onItemClick(AdapterView<?> a, View v, final int position, long id) {
		                        final ListItem newsData = (ListItem) listView.getItemAtPosition(position);
		                        //Toast.makeText(ListActivity.this, "Selected :" + " " + newsData.getFpath(), Toast.LENGTH_LONG).show();		                        
		                        //Log.e(TAG, "seName : " + newsData.getName());
		                        //Log.e(TAG, "setUrl : " + newsData.getUrl());
		                        //Log.e(TAG, "setFpath : " + newsData.getFpath());
		                        //Log.e(TAG, "setTime : " + newsData.getTime());
                   
				            	if(isPhoto == true) {
				            		CharSequence items[] = new CharSequence[] {"DOWNLOAD", "DELETE"};
			                        final ArrayAdapter adapter_option = new ArrayAdapter(ListActivity.this,android.R.layout.simple_spinner_item, items );
			    					new AlertDialog.Builder(ListActivity.this).setTitle("Options").setAdapter(adapter_option, new DialogInterface.OnClickListener() {
			    						@Override
			    						public void onClick(DialogInterface dialog, final int which) {
			    							switch(which) {
			    							case 0: // DOWNLOAD
			    								if (Util.isContainExactWord(newsData.getName(), "JPG")) {
			    									new DownloadFileFromURL().execute(newsData.getUrl(), newsData.getName());
			    								} else {
			    									new DownloadFileFromURL().execute(newsData.getUrl(), newsData.getName());
			    								}
			    								
			    								break;
			    							case 1: // DELETE
			    								new Thread(new Runnable(){
			    				        		    @Override
			    				        		    public void run() {
			    				        		    	String encodedurl = null;
														try {
															encodedurl = java.net.URLEncoder.encode(newsData.getFpath().toString(), "ISO-8859-1");
														} catch (UnsupportedEncodingException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
			    				        		    	final String ack = NVTKitModel.delFileFromUrl(encodedurl );
			    				        		    	//Log.e(TAG, ack);
			    				        		    	runOnUiThread(new Runnable() {
			    				        		            @Override
			    				        		            public void run() {
			    				        		            	mCustomListAdapter.removeItem(position);
			    				        		            	mCustomListAdapter.notifyDataSetChanged();
			    				        		            }
			    				        		    	});
			    				        		    	
			    				        		    }            
			    				        		}).start();
			    								break;
			    							}
			    							dialog.dismiss();
			    						}
			    					}).create().show();
				            	}
				            	else {
				            	CharSequence items[] = new CharSequence[] {"DOWNLOAD", "PLAY", "DELETE"};
		                        final ArrayAdapter adapter_option = new ArrayAdapter(ListActivity.this,android.R.layout.simple_spinner_item, items );
		    					new AlertDialog.Builder(ListActivity.this).setTitle("Options").setAdapter(adapter_option, new DialogInterface.OnClickListener() {
		    						@Override
		    						public void onClick(DialogInterface dialog, final int which) {
		    							switch(which) {
		    							case 0: // DOWNLOAD
		    								if (Util.isContainExactWord(newsData.getName(), "JPG")) {
		    									new DownloadFileFromURL().execute(newsData.getUrl(), newsData.getName());
		    								} else {
		    									new DownloadFileFromURL().execute(newsData.getUrl(), newsData.getName());
		    								}
		    								
		    								break;
		    							case 1: // PLAY		    				                
		    								if(Util.isContainExactWord(newsData.getName(), "JPG")) {
		    									Intent intent = new Intent(ListActivity.this, GalleryActivity.class);
		    									Bundle bundle = new Bundle();
		    									bundle.putString("name", newsData.getName());
		    									bundle.putString("url", newsData.getUrl());
		    									bundle.putInt("position", position);
		    									intent.putExtras(bundle);
		    									startActivity(intent);
		    								} 
		    								else if(Util.isContainExactWord(newsData.getName(), "MP4")) {
			    				                Intent intent = new Intent(ListActivity.this, PlaybackActivity.class);		    				                
			    				                Bundle bundle = new Bundle();
			    				                bundle.putString("url", newsData.getUrl());
			    				                intent.putExtras(bundle);
			    				                startActivity(intent);
		    								}
		    				                		    				                		    				                
		    								break;

		    							case 2: // DELETE
		    								new Thread(new Runnable(){
		    				        		    @Override
		    				        		    public void run() {
		    				        		    	String encodedurl = null;
													try {
														encodedurl = java.net.URLEncoder.encode(newsData.getFpath().toString(), "ISO-8859-1");
													} catch (UnsupportedEncodingException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													final String ack = NVTKitModel.delFileFromUrl(encodedurl); 	
		    				        		    	//Log.e(TAG, ack);
		    				        		    	runOnUiThread(new Runnable() {
		    				        		            @Override
		    				        		            public void run() {
		    				        		            	mCustomListAdapter.removeItem(position);
		    				        		            	mCustomListAdapter.notifyDataSetChanged();
		    				        		            }
		    				        		    	});
		    				        		    	
		    				        		    }            
		    				        		}).start();
		    								break;
		    							}
		    							dialog.dismiss();
		    						}
		    					}).create().show();
				            	}
		                    }
		                });
		            }
		    	});
		    	
		    }            
		}).start();
	}
    
  
    
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0; 
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case progress_bar_type: // we set this to 0
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Downloading file. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(false);
            pDialog.show();
            return pDialog;
        default:
            return null;
        }
    }
 
    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {
    	
        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }
 
        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... params) {
        	String file_name;
        	
            int count;
            try {
            	file_name = params[1];
                URL url = new URL(params[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();
 
                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
 
                // Output stream
                OutputStream output;
				if (Util.isContainExactWord(file_name, "JPG")) {
					output = new FileOutputStream(Util.local_photo_path + "/" + file_name);
				} else {
					output = new FileOutputStream(Util.local_movie_path + "/" + file_name);
				}
                
 
                byte data[] = new byte[1024];
 
                long total = 0;
 
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
 
                    // writing data to file
                    output.write(data, 0, count);
                }
 
                // flushing output
                output.flush();
 
                // closing streams
                output.close();
                input.close();
 
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
 
            return null;
        }
 
        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
       }
 
        /**
         * After completing background task
         * Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);
 
        }
 
    }
    
	private void setLoading(boolean isLoading) {
		if (isLoading == true) {
			findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		}
	}
}
