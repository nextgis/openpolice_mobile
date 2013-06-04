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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;


public class DownloadAndUnzipAsync extends AsyncTask<String, String, String> {
    private Context mContext;
    private boolean m_bSucces = true;
    private ProgressDialog mDownloadDialog;
    private String mDownloadDialogMsg;
    private Handler mEventReceiver;
    private String  m_sTmpOutFile;
    private String  m_sExtractPath;
	
    public DownloadAndUnzipAsync(Context c, String sExtractPath, String sMsg, Handler eventReceiver) {        
        super();
        mContext = c;
       	mDownloadDialog = null;
        mEventReceiver = eventReceiver;
        mDownloadDialogMsg = sMsg;  
        m_sExtractPath = sExtractPath;
        
        File dir = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File zipFile = new File(dir, "tmp.zip");		

        m_sTmpOutFile = zipFile.getAbsolutePath();
    }
    
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
		mDownloadDialog = new ProgressDialog(mContext);
		mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mDownloadDialog.setMessage(mDownloadDialogMsg);
		mDownloadDialog.setCancelable(false);
		mDownloadDialog.show();
    } 
    
    @Override
    protected String doInBackground(String... aurl) {
    	int count;
    	try {
    		URL url = new URL(aurl[0]);
    		URLConnection connetion = url.openConnection();
    		connetion.connect();
    		int lenghtOfFile = connetion.getContentLength();
    		InputStream input = new BufferedInputStream(url.openStream());
    		OutputStream output = new FileOutputStream(m_sTmpOutFile);
    		byte data[] = new byte[1024];
    		long total = 0;
    		while ((count = input.read(data)) != -1) {
    			total += count;
    			publishProgress(""+(int)((total*100)/lenghtOfFile));
    			output.write(data, 0, count);
    		}
    		output.close();
    		input.close();
    	} catch (Exception e) {
    		m_bSucces = false;
    		
            Bundle bundle = new Bundle();
            bundle.putBoolean("error", true);
            bundle.putString("err_msq", e.getLocalizedMessage());
            
            Message msg = new Message();
            msg.setData(bundle);
            if(mEventReceiver != null){
            	mEventReceiver.sendMessage(msg);
            }
    		
    		
    	}
    	return null;
	}
    
    protected void onProgressUpdate(String... progress) {
    	mDownloadDialog.setProgress(Integer.parseInt(progress[0]));
    }
    
    @Override
    protected void onPostExecute(String unused) {
    	mDownloadDialog.dismiss();
    	if(m_bSucces){
    		try {
    			unzip();                	
    		} catch (IOException e) {
    			e.printStackTrace();
    			
                Bundle bundle = new Bundle();
                bundle.putBoolean("error", true);
                bundle.putString("err_msq", e.getLocalizedMessage());
                
                Message msg = new Message();
                msg.setData(bundle);
                if(mEventReceiver != null){
                	mEventReceiver.sendMessage(msg);
                }    			
    		}
    	}
	}
    
    public void unzip() throws IOException {
    	mDownloadDialog = new ProgressDialog(mContext);
    	mDownloadDialog.setMessage(mContext.getResources().getString(R.string.stZipExtractionProcess));
    	mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	mDownloadDialog.setCancelable(false);
    	mDownloadDialog.show();
    	new UnZipTask().execute(m_sTmpOutFile, m_sExtractPath);
    }
    
    private class UnZipTask extends AsyncTask<String, Void, Boolean> {

    	@Override
    	protected Boolean doInBackground(String... params) {
    
    		String filePath = params[0];
    		String destinationPath = params[1];
    		File archive = new File(filePath);
    		try {
    			ZipFile zipfile = new ZipFile(archive);
    			for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements();) {
    				ZipEntry entry = (ZipEntry) e.nextElement();
    				unzipEntry(zipfile, entry, destinationPath);
    			}
    			zipfile.close();
    			archive.delete();
    		} catch (Exception e) {
    			return false;
    		}
    		
            Bundle bundle = new Bundle();
            bundle.putBoolean("error", false);
            bundle.putString("path", destinationPath);
            
            Message msg = new Message();
            msg.setData(bundle);
            if(mEventReceiver != null){
            	mEventReceiver.sendMessage(msg);
            }   		
    		return true;
    	}
    	
    	@Override
    	protected void onPostExecute(Boolean result) {
    		mDownloadDialog.dismiss();
    	}
    	
    	private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {
    		if (entry.isDirectory()) {
    			createDir(new File(outputDir, entry.getName()));
    			return;
    		}
    		File outputFile = new File(outputDir, entry.getName());
    		if (!outputFile.getParentFile().exists()) {
    			createDir(outputFile.getParentFile());
    		}

    		BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
    		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
    		try {
    			byte[] _buffer = new byte[1024];
    			copyStream(inputStream, outputStream, _buffer, 1024);
    		} finally {
    			outputStream.flush();
    			outputStream.close();
    			inputStream.close();
    		}
    	}
    	
    	private void copyStream( InputStream is, OutputStream os, byte[] buffer, int bufferSize ) throws IOException {
			try {
				for (;;) {
					int count = is.read( buffer, 0, bufferSize );
					if ( count == -1 ) { break; }
					os.write( buffer, 0, count );
				}
			} catch ( IOException e ) {
				throw e;
			}
		}    	
    	
    	private void createDir(File dir) {
    		if (dir.exists()) {
    			return;
    		}
    		if (!dir.mkdirs()) {
    			throw new RuntimeException("Can not create dir " + dir);
    		}
    	}
    }
}
