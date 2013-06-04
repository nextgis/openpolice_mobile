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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.nextgis.panicbutton.PolicemanListAdapter.PolicemanItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

public class View extends Activity {
	
	protected double dfLat, dfLon;
    private ListView mListPolicemanInfo;
    private TextView mTextClosestAddr;
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
	    else
	    {
	    	dfLat = 0;
	    	dfLon = 0;	    
	    }
	    
	    // load list
	    mListPolicemanInfo = (ListView)findViewById(R.id.Mainlist);
        // create new adapter
	    mListAdapter = new PolicemanListAdapter(this, mPolicemanList);
        // set adapter to list view
	    mListPolicemanInfo.setAdapter(mListAdapter);	
	    
	    mTextClosestAddr = (TextView)findViewById(R.id.tvAddresText);
	    
        mFillDataHandler = new Handler() {
            public void handleMessage(Message msg) {        	    

        	    Bundle resultData = msg.getData();
        	    int nType = resultData.getInt("type");
            	if(nType == 1){
	            	String sPhone = resultData.getString("phone");
	             	if(sPhone == null){
	            		mPolicemanList.add(new PolicemanItem((String) getResources().getText(R.string.strPolice), "112", "police.png", 99999, "г. Москва, ул. Петровка, 38"));
	            	}
	            	else {
	            		String sAddr = resultData.getString("addr");
		            	String sName = resultData.getString("name");
		            	String sPic = resultData.getString("pic");
		            	double dfDist = resultData.getDouble("dist");
	            		mPolicemanList.add(new PolicemanItem(sName, sPhone, sPic, dfDist, sAddr));
	            	}   
            	}
            	else if(nType == 2){
	            	String sPhone = resultData.getString("phone");
	            	String sName = resultData.getString("name");
	            	double dfDist = resultData.getDouble("dist");
	            	
	            	for(int i = 0; i < mPolicemanList.size(); i++){
	            		if(mPolicemanList.get(i).GetInspector().equals(sName) && 
	            			mPolicemanList.get(i).GetPhone().equals(sPhone)	&&
	            			mPolicemanList.get(i).GetDistance() > dfDist){
	            			mPolicemanList.get(i).SetDistance(dfDist);
	            		}
	            	}            		
            	}
            	
            	//sort
            	Collections.sort(mPolicemanList, new PoliceItemComparator());
            	//set closest addr
            	if(mPolicemanList.size() > 0){
            		String sClosestAddr = mPolicemanList.get(0).GetAddres();
            		mTextClosestAddr.setText(sClosestAddr);
            	}

            	mListAdapter.notifyDataSetChanged();
            };
        };	    
	    
    }
    
	public class PoliceItemComparator implements Comparator<PolicemanItem>
	{
	    public int compare(PolicemanItem left, PolicemanItem right) {
	    	double dfDeltaDist =  right.GetDistance() - left.GetDistance();

	    	if (dfDeltaDist < 0)
	    		return 1;
	    	else if (dfDeltaDist > 0)
	    		return -1;
	    	else
	    		return 0;
	    }
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
    		PolicemanDatabaseHelper dbHelper = new PolicemanDatabaseHelper(View.this);
    		SQLiteDatabase PolicemanDB = dbHelper.getReadableDatabase(); 
	    	try{
	            double dfCoeff = Math.cos(dfLat * Math.PI / 180);
	            double dfDist = 1000;
	            double dfR = 6378137;
	            //
	            double dfDeltaLat = dfDist * 180 / (dfR * Math.PI);
	            double dfDeltaLon = dfDeltaLat * dfCoeff;
	            Log.d(MainActivity.TAG, "delta lat:" + dfDeltaLat + "delta lat:" + dfDeltaLon);
	            double dfXmin = dfLon - dfDeltaLon;
	            double dfXmax = dfLon + dfDeltaLon; 
	            double dfYmin = dfLat - dfDeltaLat;
	            double dfYmax = dfLat + dfDeltaLat;
	            Log.d(MainActivity.TAG, "lat:" + dfLat + " lon:" + dfLon);
	            String sSQL = "SELECT pname, prank, pphone, pid, paddr, lat, lon from oppo WHERE lon <= " + dfXmax + " AND lon >= " + dfXmin + " AND lat <= " + dfYmax + " AND lat >= " + dfYmin;
	            Log.d(MainActivity.TAG, sSQL);        	    

	    		Cursor cursor = PolicemanDB.rawQuery(sSQL, null);
	    		Log.d(MainActivity.TAG, "db open success " + cursor.getCount());
	    		
	    		if(cursor.getCount() > 0)
	    		{
		    		cursor.moveToFirst();
		    		do
		    		{
		                String sName = cursor.getString(0);
		                String sRank = cursor.getString(1);
		                String sPhone = cursor.getString(2);
		                String sPic = cursor.getString(3) + ".jpg";
		                String sAddr = cursor.getString(4);
		                double dfCurrentLat = cursor.getDouble(5);
		                double dfCurrentLon = cursor.getDouble(6);

		                double dfCurrentDist = Math.sqrt((dfCurrentLat - dfLat) * (dfCurrentLat - dfLat) + (dfCurrentLon - dfLon) * (dfCurrentLon - dfLon));
		                
		                //if(dfCurrentDist > dfDist)
		                //	continue;
		                	
		                if(!list.contains(sPhone + sName)){
		                	list.add(sPhone + sName);
		                	
		                	Bundle bundle1 = new Bundle();
		                    bundle1.putString("name", sRank + " " + sName);
		                    bundle1.putString("phone", sPhone);
		                    bundle1.putString("pic", sPic);
		                    bundle1.putString("addr", sAddr);
		                    bundle1.putInt("type", 1);
		                    bundle1.putDouble("dist", dfCurrentDist);
		                    
		                    Message msg1 = new Message();
		                    msg1.setData(bundle1);
		                    
		                    if(mEventReceiver != null){
		                    	mEventReceiver.sendMessage(msg1);
		                    }
		                }
		                else{
		                	//update dist
		                	Bundle bundle1 = new Bundle();
		                    bundle1.putString("name", sRank + " " + sName);
		                    bundle1.putString("phone", sPhone);
		                    bundle1.putString("addr", sAddr);
		                    bundle1.putInt("type", 2);
		                    bundle1.putDouble("dist", dfCurrentDist);
		                    
		                    Message msg1 = new Message();
		                    msg1.setData(bundle1);
		                    
		                    if(mEventReceiver != null){
		                    	mEventReceiver.sendMessage(msg1);
		                    }		                
	                    }
		    			
		    		}while(cursor.moveToNext());
		    	}
	    		
	    		
            	Bundle bundle1 = new Bundle();
                bundle1.putInt("type", 1);
                
                Message msg1 = new Message();
                msg1.setData(bundle1);
                
                if(mEventReceiver != null){
                	mEventReceiver.sendMessage(msg1);
                }

                
	    		PolicemanDB.close();
	    	}
	    	catch(SQLiteException e){
	    		Log.d(MainActivity.TAG, e.getLocalizedMessage());
	    	}

			Message msg = new Message();
            if(mEventReceiver != null){
            	mEventReceiver.sendMessage(msg);
            }
            
			mbFilled = true;
			
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
				mDownloadDialog = null;
			}	
		}
		
		public void Abort(){
			DismissDowloadDialog();
			this.cancel(true);
		}
	}
}
