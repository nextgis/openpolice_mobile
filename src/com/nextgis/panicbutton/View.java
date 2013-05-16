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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.nextgis.panicbutton.PolicemanListAdapter.PolicemanItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

public class View extends Activity {
	
	protected double dfLat, dfLon;
    private ListView mListPolicemanInfo;
    private ArrayList <PolicemanItem> mPolicemanList = new ArrayList<PolicemanItem>();
    protected PolicemanListAdapter mListAdapter;
    private Handler mFillDataHandler; 
    private boolean mbFilled;

    @SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mbFilled = false;
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
	    
        mFillDataHandler = new Handler() {
            public void handleMessage(Message msg) {
            	Bundle resultData = msg.getData();
            	
            	String sName = resultData.getString("name");
            	String sPhone = resultData.getString("phone");
            	String sPic = resultData.getString("pic");
            	if(sPhone == null){
            		mPolicemanList.add(new PolicemanItem((String) getResources().getText(R.string.strPolice), "112", "police.png"));
            	}
            	else {
            		mPolicemanList.add(new PolicemanItem(sName, sPhone, sPic));
            	}            	

            	mListAdapter.notifyDataSetChanged();
            };
        };	    
	    
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

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();		
		
		if(!mbFilled)
		{
			DataQuerer oQ = new DataQuerer(com.nextgis.panicbutton.View.this, getResources().getString(R.string.stSearching), mFillDataHandler);
			oQ.execute();
		}
	}
	
	class DataQuerer extends AsyncTask<String, Void, Void> {
	    private Context mContext;
	    private ProgressDialog mDownloadDialog;
	    private String mDownloadDialogMsg;
	    private Handler mEventReceiver;

	    public DataQuerer(Context c, String sMsg, Handler eventReceiver) {        
	        super();
	        mContext = c;
	       	mDownloadDialog = null;
	        mDownloadDialogMsg = sMsg; 
	        mEventReceiver = eventReceiver;
	    }
	    
	    @Override
	    protected void onPreExecute() {
	    	super.onPreExecute();
    		mDownloadDialog = new ProgressDialog(mContext);
    		mDownloadDialog.setMessage(mDownloadDialogMsg);
    		mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		mDownloadDialog.setIndeterminate(true);
    		mDownloadDialog.show();
	    } 

	    @Override
	    protected Void doInBackground(String... urls) {
            List<String> list = new ArrayList<String>();
	        
		    //Queue nearby policemen
	    	File spatialDbFile = new File(getExternalFilesDir(null), "oppo3.sqlite");
	    	SQLiteDatabase policeDB = SQLiteDatabase.openDatabase(spatialDbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
	    	if(policeDB != null)
	    	{
	            double dfCoeff = Math.cos(dfLat * Math.PI / 180);
	            double dfDist = 1000;
	            double dfR = 6378137;
	            //
	            double dfDeltaLat = dfDist * 180 / (dfR * Math.PI);
	            double dfDeltaLon = dfDeltaLat * dfCoeff;
	            Log.d("PanicButton", "delta lat:" + dfDeltaLat + "delta lat:" + dfDeltaLon);
	            double dfXmin = dfLon - dfDeltaLon;
	            double dfXmax = dfLon + dfDeltaLon; 
	            double dfYmin = dfLat - dfDeltaLat;
	            double dfYmax = dfLat + dfDeltaLat;
	            Log.d("PanicButton", "lat:" + dfLat + " lon:" + dfLon);
	            String sSQL = "SELECT qqq_NAME, qqq_RANK, qqq_PHONE from all2 WHERE lon <= " + dfXmax + " AND lon >= " + dfXmin + " AND lat <= " + dfYmax + " AND lat >= " + dfYmin;
	    		Log.d("PanicButton", sSQL);
	    		Cursor cursor = policeDB.rawQuery(sSQL, null);
	    		Log.d("PanicButton", "db open success " + cursor.getCount());
	    		if(cursor.getCount() > 0)
	    		{
		    		cursor.moveToFirst();
		    		do
		    		{
		                String sName = cursor.getString(0);
		                String sRank = cursor.getString(1);
		                String sPhone = cursor.getString(2);
		                
		                //if(dfCurrentDist > dfDist)
		                //	continue;
		                	
		                if(!list.contains(sPhone)){
		                	list.add(sPhone);
		                	
		                	Bundle bundle = new Bundle();
		                    bundle.putString("name", sRank + " " + sName);
		                    bundle.putString("phone", sPhone);
		                    bundle.putString("pic", "police.png");
		                    
		                    Message msg = new Message();
		                    msg.setData(bundle);
		                    
		                    if(mEventReceiver != null){
		                    	mEventReceiver.sendMessage(msg);
		                    }
		                }
		    			
		    		}while(cursor.moveToNext());
		    	}
	    	}

			mbFilled = true;

			Message msg = new Message();
            if(mEventReceiver != null){
            	mEventReceiver.sendMessage(msg);
            }
			
	        return null;
	    }

	    @Override
	    protected void onPostExecute(Void unused) {
	    	super.onPostExecute(unused);
	    	DismissDowloadDialog();
	    }

	    public void DismissDowloadDialog(){
			if(mDownloadDialog != null){
				mDownloadDialog.dismiss();
			}	
		}
		
		public void Abort(){
			DismissDowloadDialog();
			this.cancel(true);
		}
	}
}
