package com.bayou;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bayou.common.BayouMessage;
import com.bayou.common.BayouMessageEnum;

public class Replica extends Process {

	boolean isPrimary = false;
	List<Long> replicaId = new ArrayList<Long>();
	Map<List<Long>,Integer> versionVector =  new HashMap<List<Long>,Integer>();
	int CSN = 0;
	Map<String,String> playList = new HashMap<String,String>();
	List<BayouMessage> commitedWrites = new ArrayList<BayouMessage>();
	List<BayouMessage> tentativeWrites = new ArrayList<BayouMessage>();
	List<List<Long>> neighbors = new ArrayList<List<Long>>();
	
	
	public Replica(Main env, String procId, int addUnder) throws Exception {
		this.main = env;
		this.processId = procId;
		if(addUnder==-1) { 
			replicaId.add(System.currentTimeMillis());
			this.isPrimary = true;
			//System.out.println("Primary Created with ID - "+replicaId);
		}
		else {
			BayouMessage msg = new BayouMessage();
			msg.setMessageType(BayouMessageEnum.CREATE_WRITE);
			sendMessage("REPLICA:"+addUnder, msg);
		}
	}
	
	public void writeListToLog(List<BayouMessage> bmList, String ext) {
		StringBuffer br = new StringBuffer();
		br.append(this.replicaId+"\n");
		for(BayouMessage bm : bmList) {
			String s = bm.getRequest().getOperation()
					   +","+bm.getRequest().getKey()+","
					   +","+bm.getRequest().getValue();			
			br.append(s+"\n");
		}		
		clearAndWriteFile(br.toString(), ext);
	}
	
	public boolean isGreater(List<Long> r) {
		int s1,s2,ms;
		s1 = this.replicaId.size();
		s2 = r.size();
		ms = s1 > s2? s2 : s1;
		for(int i=1;i<=ms;i++) {
			if(r.get(s2-i) < this.replicaId.get(s1-i))
				return false;
			if(r.get(s2-i) > this.replicaId.get(s1-i))
				return true;
		}
		if(s2 < s1)
			return false;
		return true;
   }
	
	@Override
	public void body() throws Exception {
		while(true) {
			BayouMessage bMessage = getNextMessage();
			if(BayouMessageEnum.ADD_NEIGHBOR.equals(bMessage.getMessageType())) {
				neighbors.add(bMessage.getReplicaId());
			} else if(BayouMessageEnum.CREATE_WRITE.equals(bMessage.getMessageType())) {
				neighbors.add(bMessage.getReplicaId());
				List<Long> newReplicaId = new ArrayList<Long>();
				newReplicaId.add(System.currentTimeMillis());
				newReplicaId.addAll(this.replicaId);
				BayouMessage msg = new BayouMessage();
				msg.setMessageType(BayouMessageEnum.CREATE_WRITE_RESP);
				msg.setReplicaId(newReplicaId);
				msg.setParentReplicaId(this.replicaId);
				sendMessage(bMessage.getSrcId(), msg);
			} else if(BayouMessageEnum.CREATE_WRITE_RESP.equals(bMessage.getMessageType())) {
				this.replicaId = bMessage.getReplicaId();
				this.neighbors.add(bMessage.getParentReplicaId());
				//System.out.println("Node Created with ID - "+replicaId);
				//System.out.println("Neighbors of node is - "+neighbors);
			}
		}
	}

}
