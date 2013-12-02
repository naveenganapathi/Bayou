package com.bayou;

import java.io.FileWriter;
import java.io.PrintWriter;

import com.bayou.common.BayouMessage;
import com.bayou.common.Queue;

public abstract class Process extends Thread{
	public String processId;
	public Main main;
	public Queue<BayouMessage> messages=new Queue<BayouMessage>();
	public PrintWriter writer;

	public void run(){
		try {			
			body();
		} catch (Exception e) {
			writeToLog(this.processId+" caught exception. finishing execution."+e);
			e.printStackTrace();
		}
		System.out.println("Removing processId - "+processId);
		main.removeProcess(processId);
	}

	public boolean canSend(String p) {
		return true;
	}

	abstract public void body() throws Exception;

	public void clearAndWriteFile(String s, String fileExt) {
		String temp = processId.replace(":", "_").replace(",","_");
		try {
			this.writer = new PrintWriter(new FileWriter(temp+fileExt+".txt", false));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(processId+":"+s);
		writer.println(s);
		writer.close(); 
	}
	
	public void writeToLog(String s) {
		String temp = processId.replace(":", "_").replace(",","_");
		try {
			this.writer = new PrintWriter(new FileWriter(temp+".txt", true));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(processId+":"+s);
		writer.println(s);
		writer.close();                
	}
	public void sendMessage(String destProcessId, BayouMessage msg) throws Exception{
		main.sendMessage(this.processId,destProcessId, msg);
	}


	public void deliver(BayouMessage msg) throws Exception {
		while(this.main.pause) {Thread.sleep(100);}
		messages.enqueue(msg);
	}

	public BayouMessage getNextMessage() throws Exception{
		while(this.main.pause) {Thread.sleep(100);}
		return messages.dequeue();
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}}
