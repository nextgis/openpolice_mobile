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

import java.util.ArrayList;
import java.util.Calendar;

import com.nextgis.panicbutton.PolicemanListAdapter.PolicemanItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
//import com.nextgis.panicbutton.R;

public class View extends Activity {
	
	protected double dfLat, dfLon;
    private ListView mListPolicemanInfo;
    private ArrayList <PolicemanItem> mPolicemanList = new ArrayList<PolicemanItem>();
    protected PolicemanListAdapter mListAdapter;

    @SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);
        
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
        	getActionBar().setHomeButtonEnabled(true);
        }
	    getActionBar().setDisplayHomeAsUpEnabled(true);

	    Bundle extras = getIntent().getExtras(); 
	    if(extras != null) {
	    	dfLat = extras.getDouble("lat");
	    	dfLon = extras.getDouble("lon");
	    }
	    
	    // load list
	    mListPolicemanInfo = (ListView)findViewById(R.id.Mainlist);
        // create new adapter
	    mListAdapter = new PolicemanListAdapter(this, mPolicemanList);
        // set adapter to list view
	    mListPolicemanInfo.setAdapter(mListAdapter);	
	    
	    mPolicemanList.add(new PolicemanItem((String) getResources().getText(R.string.strPolice), "112", "police.png"));
		mListAdapter.notifyDataSetChanged();	    
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

}
