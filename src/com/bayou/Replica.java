package com.bayou;

public class Replica extends Process {

	boolean isPrimary;
	
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
