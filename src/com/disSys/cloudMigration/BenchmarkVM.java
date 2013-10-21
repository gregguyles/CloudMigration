package com.disSys.cloudMigration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.Timestamp;
import java.util.Date;
 
public class BenchmarkVM {
 
	public static void main(String[] args) {
		long startTime = new Date().getTime();
		long endTime = startTime + Long.parseLong(args[0])*(60 * 1000);    //(24 * 60 * 60 * 1000)
		Pattern linePattern = Pattern.compile("(.+:\\s*((\\d+\\.?\\d*)/?(\\d+\\.?\\d*)?))");
	    File file = new File("sysBench_cpu.txt");
	    String writeLine;
	    String line;
//	    int[] maxPrime = new int[] {10000,20000,40000,80000,160000};
//	    int[] numThreads = new int[] {1,2,4};
	    
	    int[] maxPrime = new int[] {1000,2000};
	    int[] numThreads = new int[] {1,2};
	    
	    try {
			if (!file.exists()) {
				file.createNewFile();
			}	
	 	    FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			if (args.length == 1 || !args[1].equalsIgnoreCase("--noHeader")){
				bw.write("Timestamp,Num_Threads,Max_Prime,Total_Time,Num_Events,Execution_Time,"
				+ "Min,Avg,Max,Approx_95,Events_Avg,Events_Stddev,Execution_Avg,Execution_Stddev\n");
			}
		Process process;
		try {
			while(new Date().getTime() < endTime){
				for (int primeIndex = 0; primeIndex < maxPrime.length; primeIndex++){
					for (int threadIndex = 0; threadIndex < numThreads.length; threadIndex++){
						process = Runtime.getRuntime().exec("sysbench --num-threads=" + numThreads[threadIndex] 
								+ " --test=cpu --cpu-max-prime=" + maxPrime[primeIndex] +  " run");
						process.waitFor();
						writeLine = (new Timestamp(new Date().getTime()).toString());
						BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
						while ((line = reader.readLine()) !=null) {
							Matcher lineMatcher = linePattern.matcher(line);
							if (lineMatcher.find()){
								if (lineMatcher.group(2).contains("/")){
									writeLine = writeLine + "," + lineMatcher.group(3);
									writeLine = writeLine + "," + lineMatcher.group(4);
								}
								else {
									writeLine = writeLine + "," + lineMatcher.group(3);
								}
							}
						}
						writeLine = writeLine + "\n";
						bw.write(writeLine);
					}
				}	
			}
		bw.close();		
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}