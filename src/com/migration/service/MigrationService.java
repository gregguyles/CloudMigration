package com.migration.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;

import org.migration.service.client.Scheduler;

@Path("/migrate")
public class MigrationService {
	
	// Local
//	String baseDir = "/home/gregor/googleDrive/workspace/org.migration.service/";
//	String logDir = baseDir + "migrateData.log";
	
	// Remote
	static String baseDir = "/usr/local/migrateData/";
	static String logDir = baseDir + "data.log";
	
	BufferedReader logReader;

	/*****************************************************
	* leatRecord return the oldest record of the datalog *
	******************************************************/
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String lastRecord(){
		String currentLine, lastLine = null;
		String[] strArr = null;
		try {
			logReader = new BufferedReader(new FileReader(logDir));
			while ((currentLine = logReader.readLine()) != null)
	    		lastLine = currentLine;
			if (lastLine != null)
				strArr = lastLine.split(",");
	    } catch (IOException e) {
			e.printStackTrace();
	    }
	if (strArr != null)
		return strArr[0];
	else return "0";
  }
  
	/*************************************************************
	* updateData receives a String and appends it to the datalog *
	**************************************************************/
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public String updateData(String newRecords){
		try {
	   		FileWriter fileWritter = new FileWriter(logDir, true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	        bufferWritter.write(newRecords);
	        bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "OK";
	}

	
	/*************************************************
	 * activate receives the oldest record value and *
	 * starts the local service with the next numnber */ 
	 //@throws IOException 
	 //@throws InterruptedException *
	 //throws IOException, InterruptedException
	 /*************************************************/
	@Path("/activate")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public String activate(String startingArgs) {
		
		/********************************************
		* Start data collection with startingRecord *
		*********************************************/
		try{
			Thread scheduler = new Thread(new Scheduler(startingArgs));
			scheduler.start();
		} catch (Exception ie){
			return "E";
		}
		return "OK1";
	}
	
	/*****************************************************
	* print Scheduler debug                              *
	******************************************************/
	@Path("/debugSch")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String printDebug(){
		String debugSchDir = baseDir + "debugSch.log";
		String currentLine, output = "";
		try {
			logReader = new BufferedReader(new FileReader(debugSchDir));
			while ((currentLine = logReader.readLine()) != null){
				output += currentLine + "\n";
			}
	    } catch (IOException e) {
			e.printStackTrace();
	    }
		return output;
	}
	
	/*****************************************************
	* print Scheduler data dump                          *
	******************************************************/
	@Path("/data-dump")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String printData(){
		String dataDir = baseDir + "data.log";
		String currentLine, output = "";
		try {
			logReader = new BufferedReader(new FileReader(dataDir));
			while ((currentLine = logReader.readLine()) != null){
				output += currentLine + "\n";
			}
	    } catch (IOException e) {
			e.printStackTrace();
	    }
		return output;
  }
	
}