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

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String TAG = "PanicButton";
	
	protected Button searchButton;
	protected Button demoButton;
	
    protected LocationManager locationManager;
    protected CurrentLocationListener currentLocationListener;
    
    public static double dfLon;
	public static double dfLat;

	private Handler m_DBFillHandler; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_DBFillHandler = new Handler() {
            public void handleMessage(Message msg) {
            	super.handleMessage(msg);
            	
            	Bundle resultData = msg.getData();
            	boolean bHaveErr = resultData.getBoolean("error");
            	if(bHaveErr){
            		Toast.makeText(MainActivity.this, resultData.getString("err_msq"), Toast.LENGTH_LONG).show();
            	}
            	else{
            		if(CheckDBExist())
            		{
            			searchButton.setEnabled(true);
            			demoButton.setEnabled(true);
            		}
            	}
            }
        };		
		
		addListenerOnButton();
		addListenerOnDemoButton();
		addListenerOnDownloadDBButton();
		addListenerOnDownloadPhotoButton();

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		currentLocationListener = new CurrentLocationListener();
		
		Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(loc != null)
		{
			dfLon = loc.getLongitude();
			dfLat = loc.getLatitude();
		}
		else
		{
			dfLon = 0;
			dfLat = 0;
		}
		requestLocationUpdates();
	}
	
	private void requestLocationUpdates(){
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, currentLocationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, currentLocationListener);				 
	}
	
	private void removeUpdates(){
		locationManager.removeUpdates(currentLocationListener);
	}
	
	@Override
	protected void onResume() {
        super.onResume();
        
        requestLocationUpdates();
    }
 
	@Override
    protected void onPause() {
        super.onPause();
        
        removeUpdates();
    }
    
	@Override
    protected void onStop() {
        super.onStop();
        
        removeUpdates();
    }	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intentMain = new Intent(this, MainActivity.class);
	            intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intentMain);
	            return true;
	        case R.id.action_about:
	            // app icon in action bar clicked; go home
	            Intent intentAbout = new Intent(this, com.nextgis.panicbutton.About.class);
	            intentAbout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(intentAbout);
	            return true;	  
	    }
		
		return super.onOptionsItemSelected(item);
		
	}
	
	public void addListenerOnDownloadDBButton(){

		Button downloadDBButton =  (Button) findViewById(R.id.downloadDBBtn);
		
		downloadDBButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GetPoliceDB();
			} 
		});	
	}
	
	public void GetPoliceDB(){
		PolicemanDatabaseHelper dbHelper = new PolicemanDatabaseHelper(this);
		SQLiteDatabase PolicemanDB = dbHelper.getWritableDatabase(); 
		File dbFile = new File(PolicemanDB.getPath());
		if(dbFile.exists())
			dbFile.delete();
		
		String sDBPath = dbFile.getParent();
		DownloadAndUnzipAsync download = new DownloadAndUnzipAsync(MainActivity.this, sDBPath, getResources().getString(R.string.stDnldTitle), m_DBFillHandler);
		download.execute("http://gis-lab.info/data/zp-gis/data/policeman.zip");
	}
	
	public void addListenerOnDownloadPhotoButton(){
		Button downloadPhotoDBButton =  (Button) findViewById(R.id.downloadPhotoBtn);
		
		downloadPhotoDBButton.setOnClickListener(new OnClickListener() {
			 
			public void onClick(View arg0) {
				File photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
				photoDir.delete();
				photoDir.mkdirs();
				DownloadAndUnzipAsync download = new DownloadAndUnzipAsync(MainActivity.this, getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath(), getResources().getString(R.string.stDnldTitle), null);
				download.execute("http://gis-lab.info/data/zp-gis/data/photos.zip");
			} 
		});	
	}
	
	public boolean CheckDBExist(){
		PolicemanDatabaseHelper dbHelper = new PolicemanDatabaseHelper(this);
		SQLiteDatabase PolicemanDB = dbHelper.getWritableDatabase(); 
		boolean bExist = false;
		if(PolicemanDB != null){
			Cursor cursor = PolicemanDB.query(PolicemanDatabaseHelper.TABLE, null, null, null, null, null, null);
			if(cursor != null){
				if(cursor.getCount() > 0)
					bExist = true;
			}
		}
		return bExist;
	}
	
	public void addListenerOnButton() {
		 
		searchButton = (Button) findViewById(R.id.searchBtn);
 
		searchButton.setOnClickListener(new OnClickListener() {
 
			public void onClick(View arg0) {
				
				if(dfLon == 0 && dfLat == 0){
					Toast.makeText(MainActivity.this, getResources().getString(R.string.stLocFixError), Toast.LENGTH_LONG).show();
					return;
				}
 
	            Intent intentView = new Intent(MainActivity.this, com.nextgis.panicbutton.View.class);
	            intentView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	            
				Bundle bundle = new Bundle();
				bundle.putDouble("lat", dfLat);
				bundle.putDouble("lon", dfLon);

				intentView.putExtras(bundle);
	            
	            MainActivity.this.startActivity(intentView);
			}
 
		});
		
		boolean bActivated = CheckDBExist();
    	if(!bActivated)
    	{
    		searchButton.setEnabled(false);
    		
    		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    		    @Override
    		    public void onClick(DialogInterface dialog, int which) {
    		        switch (which){
    		        case DialogInterface.BUTTON_POSITIVE:
    		        	GetPoliceDB();    		        	
    		            break;

    		        case DialogInterface.BUTTON_NEGATIVE:
    		            break;
    		        }
    		    }
    		};

    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage(R.string.stDownloadConfirm).setPositiveButton(R.string.stYes, dialogClickListener)
    		    .setNegativeButton(R.string.stNo, dialogClickListener).show();    		
    	}
	}

	public void addListenerOnDemoButton() {
		
        demoButton = (Button) findViewById(R.id.demoBtn);
 
        demoButton.setOnClickListener(new OnClickListener() {
 
			public void onClick(View arg0) {
				
	            Intent intentView = new Intent(MainActivity.this, com.nextgis.panicbutton.View.class);
	            intentView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	            
				Bundle bundle = new Bundle();
				bundle.putDouble("lat", 55.753559);
				bundle.putDouble("lon", 37.609218);

				intentView.putExtras(bundle);
	            
	            MainActivity.this.startActivity(intentView);
			}
 
		});
		
		boolean bActivated = CheckDBExist();
    	if(!bActivated)
    	{
    		demoButton.setEnabled(false);
       	}
	}

	
	private final class CurrentLocationListener implements LocationListener {
		public CurrentLocationListener() {
			super();
		}

		public void onLocationChanged(Location location) {
			MainActivity.dfLat = location.getLatitude();
			MainActivity.dfLon = location.getLongitude();			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub	
		}
	}
}
