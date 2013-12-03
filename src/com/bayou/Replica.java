package com.bayou;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.bayou.common.BayouMessage;
import com.bayou.common.BayouMessageEnum;
import com.bayou.common.BayouRequest;
import com.bayou.common.BayouRequestEnum;

public class Replica extends Process {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	public boolean isRetired = false;
	boolean isPrimary = false;
	List<Long> replicaId = new ArrayList<Long>();
	Map<List<Long>,Long> versionVector =  new HashMap<List<Long>,Long>();
	int CSN = 0;
	Map<String,String> playList = new HashMap<String,String>();
	private List<BayouMessage> commitedWrites = new ArrayList<BayouMessage>();
	List<BayouMessage> tentativeWrites = new ArrayList<BayouMessage>();
	Map<List<Long>,Boolean> neighbors = new HashMap<List<Long>,Boolean>();

	Runnable startEntropy = new Runnable() {
		public void run() { 
			if(!isRetired) {
				//move writes from tentative to commited if primary.
				if(isPrimary) {
					
					commitTentativeWrites();
					//System.out.println("comitted :"+commitedWrites.size()+" writes!");
				}			
				System.out.println(processId+": Initiating Entropy for "+neighbors.size()+ " processes.");
				for(Entry<List<Long>,Boolean> neighborEntry : neighbors.entrySet()) {
					if(neighborEntry.getValue() == false) {
						System.out.println("value set to false;");
						continue;
					}			
					List<Long> neighbor = neighborEntry.getKey();
					BayouMessage msg = new BayouMessage();
					System.out.println(processId+" sending init entropy to :"+main.processMap.get(neighbor));
					msg.setReplicaId(replicaId);
					msg.setMessageType(BayouMessageEnum.INIT_ENTROPY);
					try {
						System.out.println("sending msg");
						sendMessage(main.processMap.get(neighbor), msg);
						System.out.println("returning from send msg");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	};

	public  void commitTentativeWrites() {
		synchronized(tentativeWrites) {
			synchronized(commitedWrites) {
				System.out.println("committing tentative writes of size"+tentativeWrites.size());
				for(BayouMessage m : tentativeWrites) {
					commitedWrites.add(m);
					CSN++;
					m.setCSN(CSN);				
				}
				tentativeWrites.clear();
			}
		}
	}


	@Override
	public boolean canSend(String pid) {
		Process p = main.processes.get(pid);
		if(p instanceof Replica) {
			Replica r = (Replica) p;
			if(neighbors.get(r.replicaId) != null &&
					neighbors.get(r.replicaId) == false)
				return false;
		}
		return true;
	}

	public Replica(Main env, String procId, int addUnder) throws Exception {
		this.main = env;
		this.processId = procId;
		scheduler.scheduleWithFixedDelay(startEntropy, 5, 3, TimeUnit.SECONDS);
		this.main.processes.put(procId, this);
		if(addUnder==-1) { 
			replicaId.add(System.currentTimeMillis());
			this.isPrimary = true;
			this.versionVector.put(this.replicaId, System.currentTimeMillis());
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
			String s = null;
			if(bm.getRequest().getOperation() != null) {
				s =bm.getReplicaId()+","+bm.getRequest().getAcceptStamp()+":"+bm.getRequest().getOperation()
						+","+bm.getRequest().getKey()
						+","+bm.getRequest().getValue();
			} else {
				s =bm.getReplicaId()+","+bm.getRequest().getAcceptStamp()+":"+
						bm.getMessageType();
			}						
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

	boolean hasReceiverRetired(Map<List<Long>,Long> vector,List<Long> key) {
		List<Long> parent = key.subList(1, key.size());
		List<Long> oldParent;
		int cnt = 0;
		while(parent.size() > 0) {
			if(vector.get(parent) == null) {
				cnt++;
			} else {
				if(vector.get(parent) < key.get(0)) {
					return false;
				} else {
					return true;
				}
			}
			oldParent = parent;
			if(1+cnt >= key.size())
				return false;
			parent = key.subList(1+cnt, key.size());
			key = oldParent;
		}
		return false;
	}
	@Override
	public void body() throws Exception {
		boolean isCreated = false;
		BayouMessage toRemove = null;
		while(!isCreated && !isPrimary) {
			if(!messages.list.isEmpty()) {
				List<BayouMessage> msgs = new ArrayList<BayouMessage>(messages.list);
				for(BayouMessage mesg : msgs) {
					if(BayouMessageEnum.CREATE_WRITE_RESP.equals(mesg.getMessageType())) {
						this.replicaId = mesg.getReplicaId();
						this.neighbors.put(mesg.getParentReplicaId(),true);
						this.versionVector.put(this.replicaId, System.currentTimeMillis());
						main.processMap.put(replicaId, processId);
						toRemove = mesg;
						isCreated = true;
						break;
					}
				}
			}
			Thread.sleep(100);
		}
		messages.list.remove(toRemove);

		while(!isRetired) {
			BayouMessage bMessage = getNextMessage();
			if(BayouMessageEnum.ADD_NEIGHBOR.equals(bMessage.getMessageType())) {				
				neighbors.put(bMessage.getReplicaId(),true);
			} else if(BayouMessageEnum.CREATE_WRITE.equals(bMessage.getMessageType())) {
				List<Long> newReplicaId = new ArrayList<Long>();
				long acceptStamp = System.currentTimeMillis();
				newReplicaId.add(acceptStamp);
				newReplicaId.addAll(this.replicaId);
				BayouMessage msg = new BayouMessage();
				msg.setMessageType(BayouMessageEnum.CREATE_WRITE_RESP);
				msg.setReplicaId(newReplicaId);
				msg.setParentReplicaId(this.replicaId);
				neighbors.put(newReplicaId,true);
				sendMessage(bMessage.getSrcId(), msg);
				// new
				versionVector.put(newReplicaId, acceptStamp);
				bMessage.setRequest(new BayouRequest());
				bMessage.getRequest().setAcceptStamp(acceptStamp);
				bMessage.setReplicaId(newReplicaId);
				tentativeWrites.add(bMessage);
				//System.out.println(this.processId+": Added "+bMessage.getReplicaId()+" to the neighbor list");
			} else if(BayouMessageEnum.REQUEST.equals(bMessage.getMessageType())) {
				//long clock = versionVector.get(this.replicaId);
			    BayouMessage clientResponse = new BayouMessage();
			    clientResponse.setResponseMessage("your operation was performed.");
				long tval = System.currentTimeMillis();
				if(!BayouRequestEnum.READ.equals(bMessage.getRequest().getOperation())) {
					versionVector.put(this.replicaId, tval);
					bMessage.getRequest().setAcceptStamp(versionVector.get(this.replicaId));
					bMessage.setReplicaId(replicaId);
					tentativeWrites.add(bMessage);
					writeListToLog(tentativeWrites, "TENTATIVE");
					performWrites();
					executeRequest(bMessage.getRequest());
					System.out.println(this.processId+"Tentative List:");
					for(BayouMessage mesg: tentativeWrites) {
						System.out.println(this.processId+":"+mesg.getRequest().toString());
					}		
					
					Replica cReplica = null;
					if(bMessage.getCsid() != null) {
						cReplica = (Replica) main.processes.get(bMessage.getCsid());
					}
					if(bMessage.getCsid() != null && !this.processId.equals(bMessage.getCsid()) &&( 
							versionVector.get(cReplica.replicaId) == null
							|| versionVector.get(cReplica.replicaId) < bMessage.getTs())) {
						clientResponse.setResponseMessage("you might have operated on stale data"
//								+"\n my id is"+replicaId
//								+"\nyour server id is:"+bMessage.getCsid()
//								+"\nyour server s replica id is:"+cReplica.replicaId
//								+"\nyour server ids timestamp is"+bMessage.getTs()
//								+"\n main process map is"+main.processMap
//								+"\nmy timestamp for you server id is"+versionVector.get(cReplica.replicaId)
//								+"\nmy version vector is"+versionVector
								);								
					}
					if(cReplica!= null)
					clientResponse.setResponseMessage(clientResponse.getResponseMessage()
							+"\n my id is"+replicaId
							+"\nyour server id is:"+bMessage.getCsid()
							+"\nyour server s replica id is:"+cReplica.replicaId
							+"\nyour server ids timestamp is"+bMessage.getTs()
							+"\n main process map is"+main.processMap
							+"\nmy timestamp for you server id is"+versionVector.get(cReplica.replicaId)
							+"\nmy version vector is"+versionVector
							);
				}
				
				//send response to client.
			    clientResponse.setCsid(this.processId);
			    clientResponse.setTs(BayouRequestEnum.READ.equals(bMessage.getRequest().getOperation()) ? 0 : tval);
			    clientResponse.setMessageType(BayouMessageEnum.CLIENT_OUTPUT);
			    clientResponse.setPlayList(playList);
			    sendMessage(bMessage.getSrcId(), clientResponse);
						
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

				
				//add necessary entries into bMessage version vector.
				Map<List<Long>,Long> tVector = new HashMap<List<Long>,Long>(bMessage.getVersionVector());
				for(Entry<List<Long>,Long> e : versionVector.entrySet()) {
					if(tVector.get(e.getKey()) == null) {
						//to decide whether to send everything or send nothing.
						//get keys parent.											
						if(hasReceiverRetired(tVector,e.getKey()) == false) {
							tVector.put(e.getKey(), 0L);	
						} else {
							tVector.put(e.getKey(), System.currentTimeMillis()+10000000000L);
						}						
					}
				}
				for(BayouMessage message : tentativeWrites) {
					Long rAcceptCount = tVector.get(message.getReplicaId());
					if(rAcceptCount!=null && message.getRequest().getAcceptStamp() > rAcceptCount) {
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
				System.out.println(this.processId+": commit Writes Received:"+bMessage.getCommitMessages().size());				
				if(this.processId.equals("REPLICA:1")) {
					System.err.println("versionvector:\n"+versionVector);
					System.err.println(this.processId+" current tentative write size:"+tentativeWrites.size()+",no of tentative writes received:"+bMessage.getTentativeMessages().size());
				} else {
					System.out.println(this.processId+" current tentative write size:"+tentativeWrites.size()+",no of tentative writes received:"+bMessage.getTentativeMessages().size());
				}

				synchronized(tentativeWrites) {
					synchronized(commitedWrites) {
						updateCommittedWrites(bMessage.getCommitMessages());
						//deleteCommitted(bMessage.getTentativeMessages());
						updateTentativeWrites(bMessage.getTentativeMessages());
						performWrites();
						if(bMessage.isBecomePrimary()) {
							isPrimary = true;
							System.out.println(this.processId+"just became primary!!");
						}
					}
				}

			}
		}
		
		//retirement
		BayouMessage msg = new BayouMessage();
		msg.setReplicaId(replicaId);
		msg.setRequest(new BayouRequest());
		msg.setMessageType(BayouMessageEnum.RETIRE);
		msg.getRequest().setAcceptStamp(System.currentTimeMillis());
		tentativeWrites.add(msg);
		
		for(Entry<List<Long>,Boolean> neighborEntry : neighbors.entrySet()) {
			BayouMessage mesg = new BayouMessage();
			mesg.setReplicaId(replicaId);
			mesg.setMessageType(BayouMessageEnum.INIT_ENTROPY);
			sendMessage(this.main.processMap.get(neighborEntry.getKey()), mesg);
			break;
		}
		
		boolean isEntropyCompleted = false;
		//wait for init entropy response from neighbor1
		while(!isEntropyCompleted) {
			if(!messages.list.isEmpty()) {
				List<BayouMessage> msgs = new ArrayList<BayouMessage>(messages.list);
				for(BayouMessage mesg : msgs) {
					if(mesg.getMessageType().equals(BayouMessageEnum.INIT_ENTROPY_RESP)) {

						System.out.println(this.processId+": Init Entropy Response Received");
						List<BayouMessage> tentative = new ArrayList<BayouMessage>();
						List<BayouMessage> toCommit = new ArrayList<BayouMessage>();
						if(CSN > mesg.getCSN()) {
							int num = mesg.getCSN()-1 < 0? 0 : mesg.getCSN();
							toCommit.addAll(commitedWrites.subList(num, CSN));
						}
						for(BayouMessage message : tentativeWrites) {
							Long rAcceptCount = mesg.getVersionVector().get(message.getReplicaId());
							if(rAcceptCount==null || message.getRequest().getAcceptStamp() > rAcceptCount) {
								tentative.add(message);
							}
						}
						BayouMessage msg1 = new BayouMessage();
						msg1.setBecomePrimary(isPrimary);
						msg1.setMessageType(BayouMessageEnum.ENTROPY_ADD_WRITES);
						msg1.setTentativeMessages(tentative);
						System.out.println(this.processId+"tocommit size:"+toCommit.size());
						msg1.setCommitMessages(toCommit);
						msg1.setReplicaId(replicaId);
						sendMessage(mesg.getSrcId(),msg1);
						isEntropyCompleted = true;
						break;
					}
				}
			}
		}
		
		// time pass
		while(true) {Thread.sleep(100);}
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
		List<BayouMessage> old = new ArrayList<BayouMessage>(tentativeWrites);
		int s1,s2,i,j;
		i=j=0;		
		for(BayouMessage msg : tentativeWrites) {
			toInclude.remove(msg);
		}
		s1 = tentativeWrites.size(); s2 = toInclude.size();
		while(i<s1 && j < s2) {
			//System.err.println("MY TS - "+tentativeWrites.get(i).getRequest().getAcceptStamp()+"; NEW MSG TS - "+toInclude.get(j).getRequest().getAcceptStamp());
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
			writeToLog(this.processId+"tempTent1:i,j"+i+","+j+","+res);
		}

		while(i < s1) {
			res.add(tentativeWrites.get(i));
			i++;
			writeToLog(this.processId+"tempTent2:i,j"+i+","+j+","+res);
		}
		while(j < s2) {
			res.add(toInclude.get(j));
			j++;
			writeToLog(this.processId+"tempTent3:i,j"+i+","+j+","+res);
		}
		tentativeWrites = res;

		for(BayouMessage msg : tentativeWrites) {
			if(versionVector.get(msg.getReplicaId()) == null ||
					versionVector.get(msg.getReplicaId()) < msg.getRequest().getAcceptStamp() ) {
				versionVector.put(msg.getReplicaId(), msg.getRequest().getAcceptStamp());
			}
		}

		System.out.println(this.processId+"New Tentative Writes:");
		for(BayouMessage msg : tentativeWrites) {
			System.out.println(msg);
		}

		//delete msgs from tentative writes that were already committed
		for(BayouMessage m : commitedWrites) {
			deleteFromList(tentativeWrites, m);
		}

		writeListToLog(tentativeWrites, "TENTATIVE");
	}

	private synchronized void performWrites() {
		playList.clear();
		executeRequest(commitedWrites);
		executeRequest(tentativeWrites);
		clearAndWriteFile(playList.toString(), "PLAYLIST");
	}

	private void executeRequest(BayouRequest request) {
		if(BayouRequestEnum.ADD.equals(request.getOperation())) {
			playList.put(request.getKey(), request.getValue());
		} else if(BayouRequestEnum.EDIT.equals(request.getOperation())) {
			playList.put(request.getKey(), request.getValue());
		} else if(BayouRequestEnum.DELETE.equals(request.getOperation())){
			playList.remove(request.getKey());
		} 
	}

	private void executeRequest(List<BayouMessage> messages) {
		for(BayouMessage mesg: messages) {
			if(BayouMessageEnum.RETIRE.equals(mesg.getMessageType())) {
				versionVector.remove(mesg.getReplicaId());
				continue;
			}
			executeRequest(mesg.getRequest());
		}
	}

}
