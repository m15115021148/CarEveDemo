package com.ntk.album;

import java.io.File;
import java.util.ArrayList;

import com.ntk.example.R;
import com.ntk.util.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumListAdapter extends BaseAdapter {
    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;

    public AlbumListAdapter(Context context, ArrayList<ListItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
        
        Util.checkLocalFolder();
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    public void removeItem(int position){
        if(!listData.isEmpty()){
        	listData.remove(position);
            this.notifyDataSetChanged();
        }
    }
    

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            holder = new ViewHolder();
            holder.headlineView = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.thumbImage);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

		ListItem newsItem = listData.get(position);
		holder.headlineView.setText(newsItem.getName());

		if (holder.imageView != null) {	
			
			File f = new File(Util.local_thumbnail_path + "/" + newsItem.getName());

			if (f.exists() == false) {
				new ImageDownloaderTask(holder.imageView).execute(newsItem.getUrl(), newsItem.getName());		
			} else {
				Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
				holder.imageView.setImageBitmap(myBitmap);
			}
		}
		return convertView;
    }

    static class ViewHolder {
        TextView headlineView;
        ImageView imageView;
    }
}
