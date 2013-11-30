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
	List<BayouMessage> commitedWrites;
	List<BayouMessage> tentativeWrites;
	List<List<Long>> neighbors;
	
	
	public Replica(Main env, String procId, boolean isPrimary) {
		this.main = env;
		this.processId = procId;
		this.isPrimary = isPrimary;
	}
	
	@Override
	public void body() throws Exception {
		// TODO Auto-generated method stub

	}

}
