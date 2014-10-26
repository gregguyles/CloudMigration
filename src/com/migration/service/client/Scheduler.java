package com.migration.service.client;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class Scheduler implements Runnable{
	
	//	Local
//	static String baseDir = "/home/gregor/googleDrive/workspace/org.migration.service/";
//	static String botoDir = "/home/gregor/boto_scripts/";	
	
	//	Remote
	static String baseDir = "/usr/local/migrateData/";
	static String botoDir = "/usr/local/boto_scripts/";

	static String remoteIP = "";
	static String port = "8080";
	static String logDir = baseDir + "data.log";
	static String tempDir = baseDir + "temp.log";
	static String migrateTimes = baseDir + "migrationTimes";
	Thread runner;
	static Thread benchmarkProcess = null;
	
	static int numOfLocations = 0;
	static String instanceID;
	static String nextInstanceID;
	static int zoneID;
	static int nextZoneID;
	static int currentMoveId = 0;
	
	static int startingRecordID;
	static Date migrateTime = null;
	static Calendar migrateCalendar;
	static boolean debugInit = false;
	
	
	public Scheduler(String startingArgs) throws InterruptedException{
		debugMessage("Scheduler init");
		String[] strArr = startingArgs.split(",");
		int prefMoveId = Integer.parseInt(strArr[0]);
		int nextMoveId = 9999;
		Scheduler.startingRecordID = Integer.parseInt(strArr[1]);
        BufferedReader timeReader;
        String migrateHour = "";
        String currentLine;
        String currentLocation = "";
        String nextLocation = "";
        migrateCalendar  = new GregorianCalendar();
 		try {       
	        timeReader = new BufferedReader(new FileReader(migrateTimes));
	        while ((currentLine = timeReader.readLine()) != null){
				numOfLocations++;
			}
	        currentMoveId = (prefMoveId + 1) % numOfLocations;
	        nextMoveId = (currentMoveId + 1) % numOfLocations;
			timeReader = new BufferedReader(new FileReader(migrateTimes));
			while ((currentLine = timeReader.readLine()) != null){			
				strArr = currentLine.split(","); 
				if (Integer.toString(currentMoveId).equals(strArr[0])){
					migrateHour = strArr[1];
					Scheduler.zoneID = Integer.parseInt(strArr[2]);
					Scheduler.instanceID = strArr[3];
					currentLocation = strArr[4];
				}
				if (Integer.toString(nextMoveId).equals(strArr[0])){
					Scheduler.nextZoneID = Integer.parseInt(strArr[2]);
					Scheduler.nextInstanceID = strArr[3];
					nextLocation = strArr[4];
				}				
			}
		
	    } catch (IOException e) {
			e.printStackTrace();
	    }
 		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
        dateformat.setTimeZone(timeZone);
        migrateTime = migrateCalendar.getTime();
        migrateTime.setHours(Integer.parseInt(migrateHour));
        migrateTime.setMinutes(0);
        migrateTime.setSeconds(0);
        
        if (currentMoveId == 0) {
        	Calendar c = Calendar.getInstance();
        	c.setTime(migrateTime);
        	c.add(Calendar.DATE, 1);
        	migrateTime = c.getTime();
        }
        migrateCalendar.setTime(migrateTime);
        debugMessage("Current Move ID: " + currentMoveId + "    Current Location: " + currentLocation);
        debugMessage("Next Move ID:    " + nextMoveId + "    Next Location:    " + nextLocation);
        debugMessage("Migrate Time: " + migrateCalendar.getTime().toString());
	}
	
	public static void debugMessage(String message){
		message = message + "\n";
		FileWriter tempWritter = null;
		try {
			tempWritter = new FileWriter(baseDir + "debugSch.log", true);
		    BufferedWriter tempBuffer = new BufferedWriter(tempWritter);
		    if (!debugInit){
		    	tempBuffer.write("\n********** New Run **********\n");
		    	debugInit = true;
		    }
	        tempBuffer.write(message);
	        tempBuffer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

    private static URI getBaseURI() {
    	String uri = "http://" + remoteIP + ":" + port + "/org.migration.service";
    	//String uri = "http://localhost:8080/org.migration.service";
        return UriBuilder.fromUri(uri).build();
    }
    
    private static void migrate() throws InterruptedException, IOException{
    debugMessage("Initializing  Migrate");
    ClientConfig config = new DefaultClientConfig();
    Client client = Client.create(config);
    BufferedReader logReader;
    String currentLine, updateLine = "";
    int updateRecord = 0;
    int lineCounter = 0;
    	
    /**************************
     * Power up remote system *
     **************************/
  	Process process;
  	while(remoteIP == null || !remoteIP.matches("\\d+.\\d+.\\d+.\\d+"))
  	{
  		debugMessage("remote power up");
  		debugMessage("startCommand: " + "python " + botoDir + "start_aws_instance.py " + nextZoneID + " " + nextInstanceID);
		process = Runtime.getRuntime().exec(
				"python " + botoDir + "start_aws_instance.py " + nextZoneID + " " + nextInstanceID);
		process.waitFor();
		BufferedReader startReader = new BufferedReader(new InputStreamReader((process.getInputStream())));
		startReader.readLine();
		remoteIP = startReader.readLine();
		if (remoteIP == null || !remoteIP.matches("\\d+.\\d+.\\d+.\\d+"))
			Thread.sleep(30 * 1000);
	}
	
	debugMessage("remoteIP: "+ remoteIP);
	/*******************************************
	 * Set compile WebReasource using remoteIP *
	 *******************************************/
	WebResource service = client.resource(getBaseURI());
	debugMessage("Remote URL: " + getBaseURI().toString());
	
    /*********************************************
    * get the oldest record of the remote system *
    * *******************************************/
	debugMessage("Sleeping while remote initializes...");
	Thread.sleep(60 * 1000);
	debugMessage("Attempting to contact remote");
    ClientResponse response = service.path("migrate")
            .type(MediaType.TEXT_PLAIN)
            .get(ClientResponse.class);
    updateRecord = Integer.parseInt(response.getEntity(String.class));
    
    /****************************
    * compile records to update *
    * ***************************/
	try {
	    logReader = new BufferedReader(new FileReader(logDir));
	    while ((currentLine = logReader.readLine()) != null){
	    	if( lineCounter++ >= updateRecord){
	    		updateLine += "\n" + currentLine;
	    	}
	    }
	} catch (IOException e) {
	   	e.printStackTrace();
	    }
    
	/***************************
	* Post the updated records *
	****************************/
	debugMessage("Posting first update to remote");
    service.path("migrate").accept(MediaType.TEXT_PLAIN).post(String.class, updateLine);
    updateLine = "";
    
    /*********************
    * Stop Local Service *
    **********************/
	debugMessage("Stopping local Benchmarking");
    benchmarkProcess.interrupt();
    benchmarkProcess.join();
    
    /***************************
    * Write TempLog to Datalog *
    ****************************/  
	debugMessage("Writing temp to data.log");
	try {
	    logReader = new BufferedReader(new FileReader(tempDir));
	    while ((currentLine = logReader.readLine()) != null){
    		updateLine += "\n" + currentLine;
    		lineCounter++;
	    }
	} catch (IOException e) {
	   	e.printStackTrace();
	    }
	FileWriter tempWritter = new FileWriter(logDir, true);
    BufferedWriter tempBuffer = new BufferedWriter(tempWritter);
    tempBuffer.write(updateLine);
    tempBuffer.close();

    /***********************
    * Start Remote Service *
    ************************/ 
	debugMessage("Activate remote Benchmarking");
    String startRemoteArgs = currentMoveId + "," + Integer.toString(lineCounter);
    service.path("migrate").path("activate").accept(MediaType.TEXT_PLAIN).post(String.class, startRemoteArgs);
    
    /*********************************
    * Send TempLog to remote service *
    **********************************/
	debugMessage("Posting temp.log to remote");
    service.path("migrate").accept(MediaType.TEXT_PLAIN).post(String.class, updateLine);
    
    /*************************
    * Shutdown local machine *
    **************************/
    String shutMessage = "";
    Process shutProcess;
	debugMessage("Shutting down local machine");
    while(shutMessage == null || !shutMessage.equals(instanceID)){
		shutProcess = Runtime.getRuntime().exec(
				"python " + botoDir + "stop_aws_instance.py " + zoneID + " " + instanceID);
		debugMessage("python stop_aws_instance.py " + zoneID + " " + instanceID);
		shutProcess.waitFor();
		BufferedReader stopReader = new BufferedReader(new InputStreamReader((shutProcess.getInputStream())));
		shutMessage = stopReader.readLine();
		if (shutMessage == null || !shutMessage.equals(instanceID))
			Thread.sleep(30 * 1000);
    }
   }
	
    public void run(){
    	debugMessage("Launching new BenchmarkVM");
    	benchmarkProcess = new Thread(new BenchmarkVM(tempDir, startingRecordID));
    	benchmarkProcess.start();
     	debugMessage("Waiting while benchmarking..."); 
    	Calendar currentTime = new GregorianCalendar();
    	int counter = 0;
		while (migrateCalendar.compareTo(currentTime) > 0){
			if (counter % 20 == 0){
				debugMessage("    Current Time: " + currentTime.getTime()
					+ "   Migrate Time: " + migrateCalendar.getTime());
			}
	    	try {
				Thread.sleep(60 * 1000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			currentTime = new GregorianCalendar();
			counter++;
		}
//		try {											//****************//
//			Thread.sleep(240 * 1000);					// For testing on //
//		} catch (InterruptedException e) {				// local machine  //
//		e.printStackTrace();                            //                //
//		}                                               // ***************//
		try {
			migrate();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
    }
}