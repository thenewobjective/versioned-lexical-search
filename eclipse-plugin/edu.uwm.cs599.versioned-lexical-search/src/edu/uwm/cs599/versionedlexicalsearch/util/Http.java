package edu.uwm.cs599.versionedlexicalsearch.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;

public class Http {
	public static String httpPost(String strUrl,String data) {
	    URL url;
	    HttpURLConnection conn = null;  
	    try {
	      //Create connection
	      url = new URL(strUrl);
	      conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("POST");
	      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	      conn.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
	      conn.setRequestProperty("Content-Language", "en-US");  
	      conn.setUseCaches (false);
	      conn.setDoInput(true);
	      conn.setDoOutput(true);

	      //Send request
	      DataOutputStream wr = new DataOutputStream (conn.getOutputStream());
	      wr.writeBytes(data);
	      wr.flush();
	      wr.close();

	      //Get Response	
	      InputStream is = conn.getInputStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	      String line;
	      StringBuffer response = new StringBuffer(); 
	      while((line = rd.readLine()) != null) {
	        response.append(line).append("\r\n");
	      }
	      rd.close();
	      return response.toString();
	    } catch (Exception e) {
	    	//TODO: errors...
	        e.printStackTrace();
	        return null;
	    } finally {
	        if(conn != null)
	          conn.disconnect(); 
	    }
	}
	
	public static String httpGet(String strUrl) {
		StringBuffer response = new StringBuffer();
		try{
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);
	    	BufferedReader in = new BufferedReader(
	                new InputStreamReader(
	                conn.getInputStream())
	        );
	    	
	    	String inputLine;
			while ((inputLine = in.readLine()) != null)
				response.append(inputLine).append("\r\n");
	    	in.close();

		} catch (MalformedURLException e1) {
        	//TODO: message box
			e1.printStackTrace();
		} catch (IOException e1) {
			//TODO: message box
			e1.printStackTrace();
		}
		return response.toString();
	}
}
