package com.bayou;

import java.util.HashMap;
import java.util.Map;

import com.bayou.common.BayouMessage;

public class Main {
	public Map<String, Process> processes=new HashMap<String, Process>();
	public final static int nClients = 2;
	public final static int nReplicas = 2;

	synchronized public void sendMessage(String srcProcessId,String destProcessId, BayouMessage msg) throws Exception{
		Process p = processes.get(destProcessId);        
		if(p!=null) {
			p.deliver(msg);
		}
	}

	synchronized public void addProcess(String processId, Process process) {
		if(processes.get(processId)!=null){
			System.err.println("Process Id ("+processId+") already present in the list of processes");
			return;
		}
		processes.put(processId,process);
		process.start();
	}

	synchronized public void removeProcess(String processId) {
		processes.remove(processId);
	}

	void run() throws Exception {

	}

	public static void main(String args[]) throws Exception {
		Main main = new Main();
		main.run();
	}
}
