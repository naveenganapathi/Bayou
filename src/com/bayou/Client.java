package com.bayou;

public class Client extends Process {

	public Client(Main env,String procId) {
		this.main = env;
		this.processId = procId;
	}

	@Override
	public void body() throws Exception {
		// TODO Auto-generated method stub

	}

}
