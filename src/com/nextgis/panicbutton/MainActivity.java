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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	protected Button imageButton;
	
    protected LocationManager locationManager;
    protected CurrentLocationListener currentLocationListener;
    
    public static double dfLon;
	public static double dfLat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		addListenerOnButton();

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		currentLocationListener = new CurrentLocationListener();
		
		Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(loc != null)
		{
			dfLon = loc.getLongitude();
			dfLat = loc.getLatitude();
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
	
	
	public void addListenerOnButton() {
		 
		imageButton = (Button) findViewById(R.id.sosButton);
 
		imageButton.setOnClickListener(new OnClickListener() {
 
			public void onClick(View arg0) {
 
	            Intent intentView = new Intent(MainActivity.this, com.nextgis.panicbutton.View.class);
	            intentView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	            
				Bundle bundle = new Bundle();
				bundle.putDouble("lat", dfLat);
				bundle.putDouble("lon", dfLon);

				intentView.putExtras(bundle);
	            
	            MainActivity.this.startActivity(intentView);
			}
 
		});
		
    	final File spatialDbFile = new File(getExternalFilesDir(null), "oppo3.sqlite");
    	if(!spatialDbFile.exists())
    	{
    		imageButton.setEnabled(false);
    		
    		BroadcastReceiver onComplete = new BroadcastReceiver() {
    		    public void onReceive(Context ctxt, Intent intent) {
    		    	if(spatialDbFile.exists())
    		    		imageButton.setEnabled(true);
    		    }
    		};
    		registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    		
    		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    		    @Override
    		    public void onClick(DialogInterface dialog, int which) {
    		        switch (which){
    		        case DialogInterface.BUTTON_POSITIVE:
    		        	
    		        	String url = "http://gis-lab.info/data/zp-gis/data/oppo3.sqlite";
    		        	DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
   		        	
    		        	request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
    		        	request.setAllowedOverRoaming(false);
    		        	request.setTitle(getResources().getText(R.string.stDnldTitle));
    		        	request.setDescription(getResources().getText(R.string.stDnldDecription));
    		        	
    		        	// in order for this if to run, you must use the android 3.2 to compile your app
    		        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		        	    request.allowScanningByMediaScanner();
    		        	    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
    		        	}
    		        	request.setDestinationInExternalFilesDir(getApplicationContext(), null, "oppo3.sqlite");

    		        	// get download service and enqueue file
    		        	DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    		        	manager.enqueue(request);
    		        	
    		            break;

    		        case DialogInterface.BUTTON_NEGATIVE:
    		            //No button clicked
    		            break;
    		        }
    		    }
    		};

    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage(R.string.stDownloadConfirm).setPositiveButton(R.string.stYes, dialogClickListener)
    		    .setNegativeButton(R.string.stNo, dialogClickListener).show();    		
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
