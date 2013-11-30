package com.bayou;

import java.util.List;
import java.util.Map;

import com.bayou.common.BayouMessage;

public class Replica extends Process {

	boolean isPrimary;
	List<Long> replicaId;
	Map<List<Long>,Integer> versionVector;
	int CSN = 0;
	Map<String,String> playList;
	List<BayouMessage> committedWrites;
	List<BayouMessage> tentativeWrites;
	List<List<Long>> neighbors;
	
	
	public Replica(Main env, String procId, boolean isPrimary) {
		this.main = env;
		this.processId = procId;
		this.isPrimary = isPrimary;
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
		// TODO Auto-generated method stub

	}

}
