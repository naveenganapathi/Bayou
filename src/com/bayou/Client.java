package com.bayou;

import java.io.BufferedReader;
import java.io.FileReader;

import org.omg.CORBA.Request;

import com.bayou.common.BayouMessage;
import com.bayou.common.BayouMessageEnum;
import com.bayou.common.BayouRequest;
import com.bayou.common.BayouRequestEnum;

public class Client extends Process {

	int clientCommandId=0;

	public Client(Main env,String procId) {
		this.main = env;
		this.processId = procId;
	}

	@Override
	public void body() throws Exception {
		while(true) {
			BayouMessage bMessage = getNextMessage();
			if(BayouMessageEnum.CLIENT_INPUT.equals(bMessage.getMessageType())) {
				BufferedReader br = new BufferedReader(new FileReader(this.processId.replaceAll(":", "_")+".txt"));
				String temp = null;
				while((temp = br.readLine()) != null) {
					Thread.sleep(5);
					String[] vals = temp.split(",");
					BayouRequest r = new BayouRequest(BayouRequestEnum.valueOf(vals[0]),vals[1],vals[2],clientCommandId++);
					BayouMessage m = new BayouMessage();
					m.setMessageType(BayouMessageEnum.REQUEST);
					m.setRequest(r);
					sendMessage("REPLICA:"+Integer.parseInt(vals[3]),m);
				}
				br.close();
			} 
		}
	}

}
