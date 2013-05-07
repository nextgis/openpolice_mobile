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


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

//import com.nextgis.panicbutton.R;

public class View extends Activity {
	
    protected LocationManager locationManager;
    protected CurrentLocationListener currentLocationListener;

    @SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);
        
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
        	getActionBar().setHomeButtonEnabled(true);
        }
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		currentLocationListener = new CurrentLocationListener();
		locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, currentLocationListener, null);
		locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, currentLocationListener, null);		
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	private final class CurrentLocationListener implements LocationListener {
		public CurrentLocationListener() {
			super();
		}

		public void onLocationChanged(Location location) {
			locationManager.removeUpdates(this);
			//add 
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
