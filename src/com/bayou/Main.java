package com.bayou;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bayou.common.BayouMessage;
import com.bayou.common.BayouMessageEnum;

public class Main {
	public Map<String, Process> processes=new HashMap<String, Process>();
	public final static int nClients = 2;
	public final static int nReplicas = 4;
	public Map<List<Long>,String> processMap =  new HashMap<List<Long>,String>();
	public boolean pause = false;

	synchronized public void sendMessage(String srcProcessId,String destProcessId, BayouMessage msg) throws Exception{
		msg.setSrcId(srcProcessId);
		Process p = processes.get(destProcessId);
		Process s = processes.get(srcProcessId);
		System.out.println(srcProcessId+" "+destProcessId+" "+(s==null)+" "+msg.getMessageType());
		System.out.println("msg"+msg);
		System.out.println("Source ID for you"+srcProcessId);
		if(srcProcessId.equals("MAIN") || s.canSend(destProcessId)) {
		if(p!=null) {
			//System.out.println("delivering"+msg+" to "+destProcessId);
			p.deliver(msg);
		}
		} else {
			System.out.println("sending to"+destProcessId+" failed for "+msg);
		}
	}

	synchronized public void addProcess(String processId, Process process) {
		if(processes.get(processId)!=null){
			System.err.println("Process Id ("+processId+") already present in the list of processes");
			return;
		}
		System.out.println("Adding process - "+processId);
		processes.put(processId,process);
		process.start();
		//System.out.println("started process");
	}

	synchronized public void removeProcess(String processId) {
		processes.remove(processId);
	}

	void run() throws Exception {
		//spawning replicas
		BufferedReader br = new BufferedReader(new FileReader("TOPOLOGY.txt"));
		String temp = null;
		while((temp = br.readLine()) != null) {
			String[] vals = temp.split(" ");
			if(!vals[1].equals("-1") && processes.get("REPLICA:"+vals[1])==null) throw new Exception("MAIN: REPLICA:"+vals[1]+" is not yet created");
			if(processes.get("REPLICA:"+vals[0])==null) {
				//System.out.println("creating replica - "+vals[0]);
				Replica r = new Replica(this, "REPLICA:"+vals[0], Integer.parseInt(vals[1]));
				//System.out.println("Created replica - "+vals[0]);
				r.start();
				//addProcess("REPLICA:"+vals[0],r);
			} else {
				
				Thread.sleep(1000);
				Replica r0 = (Replica)processes.get("REPLICA:"+vals[0]);
				Replica r1 = (Replica)processes.get("REPLICA:"+vals[1]);
				r0.neighbors.put(r1.replicaId, true);
				r1.neighbors.put(r0.replicaId, true);
				
//				BayouMessage msg = new BayouMessage();
//				msg.setMessageType(BayouMessageEnum.ADD_NEIGHBOR);
//				msg.setReplicaId(((Replica)processes.get("REPLICA:"+vals[0])).replicaId);
//				sendMessage("MAIN", "REPLICA:"+vals[1], msg);
//				
//			    msg = new BayouMessage();
//				msg.setMessageType(BayouMessageEnum.ADD_NEIGHBOR);
//				msg.setReplicaId(((Replica)processes.get("REPLICA:"+vals[1])).replicaId);
//				System.out.println("Processes list - "+processes);
//				System.out.println("REPLICA:"+vals[0]+"adding neighbour replica id set as"+((Replica)processes.get("REPLICA:"+vals[1])).replicaId);
//				sendMessage("MAIN", "REPLICA:"+vals[0], msg);
				
			}
			Thread.sleep(2);
		}
		br.close();

		//spawning clients
		for(int i=0;i<nClients;i++) {
			Client c = new Client(this, "CLIENT:"+i);
			addProcess("CLIENT:"+i,c);
		}

		for(int i=0;i<nClients;i++) {
			BayouMessage m = new BayouMessage();
			m.setMessageType(BayouMessageEnum.CLIENT_INPUT);
			sendMessage("MAIN","CLIENT:"+i, m);
		}
		
		//spawning user input listener
		UserInputListener ul = new UserInputListener(this, "UIL");
        addProcess("UIL", ul);
	}

	public static void main(String args[]) throws Exception {
		Main main = new Main();
		main.run();
	}
}
