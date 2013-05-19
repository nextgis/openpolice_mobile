/*******************************************************************************
*
* PanicButton
* ---------------------------------------------------------
* Search nearest policeman
*
* Copyright (C) 2013 NextGIS (http://nextgis.ru)
*
* This source is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free
* Software Foundation; either version 2 of the License, or (at your option)
* any later version.
*
* This code is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
* details.
*
* A copy of the GNU General Public License is available on the World Wide Web
* at <http://www.gnu.org/copyleft/gpl.html>. You can also obtain it by writing
* to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
* MA 02111-1307, USA.
*
*******************************************************************************/
package com.nextgis.panicbutton;

import java.io.File;
import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PolicemanListAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList <PolicemanItem> mListPolicemanInfo;
	private ImageButton callButton;
	
	public PolicemanListAdapter(Context c, ArrayList <PolicemanItem> list) {
		mContext = c;
		mListPolicemanInfo = list;
	}

	public int getCount() {
		return mListPolicemanInfo.size();
	}

	public Object getItem(int arg0) {
		return mListPolicemanInfo.get(arg0);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// get the selected entry
		final PolicemanItem entry = mListPolicemanInfo.get(position);

		// reference to convertView
		View v = convertView;

		// inflate new layout if null
		if(v == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			v = inflater.inflate(R.layout.rowlayout, null);
		}

		// load controls from layout resources
		ImageView ivIcon = (ImageView)v.findViewById(R.id.ivIcon);
		TextView tvInspector = (TextView)v.findViewById(R.id.tvInspector);
		TextView tvPhone = (TextView)v.findViewById(R.id.tvPhone);

		// set data to display
		File path = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
	    File imgFile = new File(path, entry.GetImage());		
		Log.d("PanicButton", imgFile.getPath());
		if(imgFile.exists()){

		    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		    ivIcon.setImageBitmap(myBitmap);
		}	
		else
		{
			ivIcon.setImageDrawable(v.getResources().getDrawable(R.drawable.ic_police));
		}
		tvInspector.setText(entry.GetInspector());
		tvPhone.setText(entry.GetPhone());
		
		callButton = (ImageButton) v.findViewById(R.id.phone_call);		 
		callButton.setOnClickListener(new OnClickListener() {
 
			public void onClick(View arg0) {
			    try {
			        Intent callIntent = new Intent(Intent.ACTION_CALL);
			        callIntent.setData(Uri.parse("tel:" + entry.GetPhone()));
			        mContext.startActivity(callIntent);
			    } catch (ActivityNotFoundException e) {
			        Log.e("Panic Button", "Call failed", e);
			    }
			}
 
		});		

		return v;

	}
	
	public static class PolicemanItem implements Parcelable{
		private String sInspector;
		private String sPhone;
		private String sImage;
		public PolicemanItem(String sInspector, String sPhone, String sImage) {
			this.sInspector = sInspector;
			this.sPhone = sPhone;
			this.sImage = sImage;
		}		
		
		public String GetInspector(){
			return sInspector;
		}
		
		public String GetPhone(){
			return sPhone;
		}		
		
		public String GetImage(){        	
			return sImage;
		}

		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeString(sInspector);
			out.writeString(sPhone);
			out.writeString(sImage);
		}	
		
		public static final Parcelable.Creator<PolicemanItem> CREATOR
        = new Parcelable.Creator<PolicemanItem>() {
		    public PolicemanItem createFromParcel(Parcel in) {
		        return new PolicemanItem(in);
		    }
		
		    public PolicemanItem[] newArray(int size) {
		        return new PolicemanItem[size];
		    }
		};
		
		private PolicemanItem(Parcel in) {
			sInspector = in.readString();
			sPhone = in.readString();
			sImage = in.readString();
		}
	}
}
