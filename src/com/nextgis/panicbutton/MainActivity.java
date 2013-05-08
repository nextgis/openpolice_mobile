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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
	        case R.id.action_settings:
	            //Intent intentView = new Intent(this, com.nextgis.panicbutton.View.class);
	            //intentView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	            //startActivity(intentView);
	        	
	            // app icon in action bar clicked; go home
	            //Intent intentSet = new Intent(this, SettingsMain.class);
	            //intentSet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	            //startActivity(intentSet);
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
 		
		try {
			File spatialDbFile = new File(getExternalFilesDir(null), "oppo.sqlite");
			jsqlite.Database db = new jsqlite.Database();
            db.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE);
        } catch (Exception e) {
            e.printStackTrace();
            //imageButton.setEnabled(false);
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
