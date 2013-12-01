package com.bayou;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.bayou.common.BayouMessage;
import com.bayou.common.BayouMessageEnum;
import com.bayou.common.BayouRequest;
import com.bayou.common.BayouRequestEnum;

public class Replica extends Process {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	Runnable startEntropy = new Runnable() {
		public void run() { 
			
			//move writes from tentative to commited if primary.
			if(isPrimary) {
				commitTentativeWrites();
				System.out.println("comitted :"+commitedWrites.size()+" writes!");
			}			
			System.out.println(processId+": Initiating Entropy");
			for(List<Long> neighbor : neighbors) {
				BayouMessage msg = new BayouMessage();
				System.out.println(processId+" sending init entropy to :"+main.processMap.get(neighbor));
				msg.setReplicaId(replicaId);
				msg.setMessageType(BayouMessageEnum.INIT_ENTROPY);
				try {
					sendMessage(main.processMap.get(neighbor), msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	boolean isPrimary = false;
	List<Long> replicaId = new ArrayList<Long>();
	Map<List<Long>,Long> versionVector =  new HashMap<List<Long>,Long>();
	int CSN = 0;
	Map<String,String> playList = new HashMap<String,String>();
	private List<BayouMessage> commitedWrites = new ArrayList<BayouMessage>();
	List<BayouMessage> tentativeWrites = new ArrayList<BayouMessage>();
	List<List<Long>> neighbors = new ArrayList<List<Long>>();

	public  void commitTentativeWrites() {
		synchronized(tentativeWrites) {
			for(BayouMessage m : tentativeWrites) {
				commitedWrites.add(m);
				CSN++;
				m.setCSN(CSN);				
			}
			tentativeWrites.clear();
		}
	}

	public Replica(Main env, String procId, int addUnder) throws Exception {
		this.main = env;
		this.processId = procId;
		scheduler.scheduleAtFixedRate(startEntropy, 500, 300, TimeUnit.MILLISECONDS);
		if(addUnder==-1) { 
			replicaId.add(System.currentTimeMillis());
			this.isPrimary = true;
			this.versionVector.put(this.replicaId, 0L);
			main.processMap.put(replicaId, processId);
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

	public boolean isGreater(List<Long> r,List<Long> r2) {
		int s1,s2,ms;
		s1 = r2.size();
		s2 = r.size();
		ms = s1 > s2? s2 : s1;
		for(int i=1;i<=ms;i++) {
			if(r.get(s2-i) < r2.get(s1-i))
				return false;
			if(r.get(s2-i) > r2.get(s1-i))
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
				List<Long> newReplicaId = new ArrayList<Long>();
				newReplicaId.add(System.currentTimeMillis());
				newReplicaId.addAll(this.replicaId);
				BayouMessage msg = new BayouMessage();
				msg.setMessageType(BayouMessageEnum.CREATE_WRITE_RESP);
				msg.setReplicaId(newReplicaId);
				msg.setParentReplicaId(this.replicaId);
				neighbors.add(newReplicaId);
				sendMessage(bMessage.getSrcId(), msg);
			} else if(BayouMessageEnum.CREATE_WRITE_RESP.equals(bMessage.getMessageType())) {
				this.replicaId = bMessage.getReplicaId();
				this.neighbors.add(bMessage.getParentReplicaId());
				this.versionVector.put(this.replicaId, 0L);
				main.processMap.put(replicaId, processId);
				//System.out.println("Node Created with ID - "+replicaId);
				//System.out.println("Neighbors of node is - "+neighbors);
			} else if(BayouMessageEnum.REQUEST.equals(bMessage.getMessageType())) {
				long clock = versionVector.get(this.replicaId);
				versionVector.put(this.replicaId, clock+1);
				bMessage.getRequest().setAcceptStamp(versionVector.get(this.replicaId));
				bMessage.setReplicaId(replicaId);
				tentativeWrites.add(bMessage);
				executeRequest(bMessage.getRequest());
				System.out.println(this.processId+"Tentative List:");
				for(BayouMessage mesg: tentativeWrites) {
					System.out.println(this.processId+":"+mesg.getRequest().toString());
				}				
			} else if(BayouMessageEnum.INIT_ENTROPY.equals(bMessage.getMessageType())) {
				System.out.println(this.processId+": Init Entropy Received");
				BayouMessage msg = new BayouMessage();
				msg.setMessageType(BayouMessageEnum.INIT_ENTROPY_RESP);
				msg.setVersionVector(versionVector);
				msg.setCSN(CSN);
				sendMessage(bMessage.getSrcId(), msg);
			} else if(BayouMessageEnum.INIT_ENTROPY_RESP.equals(bMessage.getMessageType())) {
				System.out.println(this.processId+": Init Entropy Response Received");
				List<BayouMessage> tentative = new ArrayList<BayouMessage>();
				List<BayouMessage> toCommit = new ArrayList<BayouMessage>();
				if(CSN > bMessage.getCSN()) {
					int num = bMessage.getCSN()-1 < 0? 0 : bMessage.getCSN();
					toCommit.addAll(commitedWrites.subList(num, CSN));
				}
				for(BayouMessage message : tentativeWrites) {
					Long rAcceptCount = bMessage.getVersionVector().get(message.getReplicaId());
					if(rAcceptCount==null || message.getRequest().getAcceptStamp() > rAcceptCount) {
						tentative.add(message);
					}
				}
				BayouMessage msg = new BayouMessage();
				msg.setMessageType(BayouMessageEnum.ENTROPY_ADD_WRITES);
				msg.setTentativeMessages(tentative);
				System.out.println(this.processId+"tocommit size:"+toCommit.size());
				msg.setCommitMessages(toCommit);
				msg.setReplicaId(replicaId);
				sendMessage(bMessage.getSrcId(),msg);
			} else if(BayouMessageEnum.ENTROPY_ADD_WRITES.equals(bMessage.getMessageType())) {
				System.out.println(this.processId+": Entropy Writes Received");
				synchronized(this) {
				updateCommittedWrites(bMessage.getCommitMessages());
				//deleteCommitted(bMessage.getTentativeMessages());
				updateTentativeWrites(bMessage.getTentativeMessages());
				}
			}
		}
	}

	private synchronized void updateCommittedWrites(List<BayouMessage> toCommit) {
		System.out.println(this.processId+"my current CSN:"+CSN);
		for(BayouMessage m : toCommit) {
			System.out.println(this.processId+"m csn"+m.getCSN()+", CSN:"+CSN);
			if(m.getCSN() > CSN) {				
				commitedWrites.add(m);
				deleteFromList(tentativeWrites,m);
				CSN++;
			}
		}
		writeListToLog(commitedWrites, "COMMIT");
		System.out.println(this.processId+" committed"+commitedWrites.size()+" writes");
	}
	
	private synchronized void deleteFromList(List<BayouMessage> tentative, BayouMessage m) {
		List<BayouMessage> toRemove = new ArrayList<BayouMessage>();
		for(BayouMessage msg : tentative) {
			if(msg.getReplicaId().equals(m.getReplicaId()) 
					&& msg.getRequest().getAcceptStamp() == m.getRequest().getAcceptStamp()) {
				toRemove.add(msg);
			}
		}		
		for(BayouMessage msg : toRemove) {
			tentative.remove(msg);
		}
	}
	
	private synchronized void updateTentativeWrites(List<BayouMessage> toInclude) {
		List<BayouMessage> res = new ArrayList<BayouMessage>();
		int s1,s2,i,j;
		i=j=0;
		s1 = tentativeWrites.size(); s2 = toInclude.size();
		System.out.println(this.processId+"Old Tentative Writes:");
		for(BayouMessage msg : tentativeWrites) {
			System.out.println(msg);
		}
		System.out.println(this.processId+"Writes Received:");
		for(BayouMessage msg : toInclude) {
			System.out.println(msg);
		}
		while(i<s1 && j < s2) {
			
			//data already present.
			if(
					(tentativeWrites.get(i).getRequest().getAcceptStamp() == toInclude.get(j).getRequest().getAcceptStamp())
			&&(tentativeWrites.get(i).getReplicaId().equals(toInclude.get(j).getReplicaId()) )
			){
			  res.add(toInclude.get(j));
			  i++;
			  j++;
			} else if (tentativeWrites.get(i).getRequest().getAcceptStamp() < toInclude.get(j).getRequest().getAcceptStamp()) {
				res.add(tentativeWrites.get(i));
				i++;
			} else if(tentativeWrites.get(i).getRequest().getAcceptStamp() > toInclude.get(j).getRequest().getAcceptStamp()) {
				res.add(toInclude.get(j));
				j++;
			} else {
				if(isGreater(tentativeWrites.get(i).getReplicaId(), toInclude.get(j).getReplicaId())) {
					res.add(toInclude.get(j));
					j++;
				} else {
					res.add(tentativeWrites.get(i));
					i++;
				}
			}
		}
		
		while(i < s1) {
			res.add(tentativeWrites.get(i));
			i++;
		}
		while(j < s2) {
			res.add(toInclude.get(j));
			j++;
		}
	    tentativeWrites = res;
	    System.out.println(this.processId+"New Tentative Writes:");
		for(BayouMessage msg : tentativeWrites) {
			System.out.println(msg);
		}
		
		//delete msgs from tentative writes that were already committed
		for(BayouMessage m : commitedWrites) {
			deleteFromList(tentativeWrites, m);
		}
		
		writeListToLog(tentativeWrites, "TENTATIVE");
	    performWrites();
	}
	
	private synchronized void performWrites() {
		playList.clear();
		executeRequest(tentativeWrites);
	}
	private void executeRequest(BayouRequest request) {
		if(BayouRequestEnum.ADD.equals(request.getOperation())) {
			playList.put(request.getKey(), request.getValue());
		} else if(BayouRequestEnum.EDIT.equals(request.getOperation())) {
			playList.put(request.getKey(), request.getValue());
		} else {
			playList.remove(request.getKey());
		}
	}

	private void executeRequest(List<BayouMessage> messages) {
		for(BayouMessage mesg: messages) {
			executeRequest(mesg.getRequest());
		}
	}

}
