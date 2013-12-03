package com.bayou;
import java.util.List;
import java.util.Scanner;

import com.bayou.common.BayouMessage;
import com.bayou.common.BayouMessageEnum;
import com.bayou.common.UserRequest;
import com.bayou.common.UserRequestEnum;
public class UserInputListener extends Process {

	private Main main;
	public  UserInputListener(Main main, String myProcessId) {
		// TODO Auto-generated method stub
		this.main = main;
		this.processId = myProcessId;

	}

	public UserRequest getUserRequest(String s) {
		UserRequest r = new UserRequest();
		String arr[] = s.split(",");		
		if(UserRequestEnum.ISOLATE.equals(UserRequestEnum.valueOf(arr[0]))) {
			r.setOperation(UserRequestEnum.ISOLATE);
			r.setSrcId(arr[1]);			
		} else if(UserRequestEnum.PAUSE.equals(UserRequestEnum.valueOf(arr[0]))) {
			r.setOperation(UserRequestEnum.PAUSE);
		} else if(UserRequestEnum.CONTINUE.equals(UserRequestEnum.valueOf(arr[0]))) {
			r.setOperation(UserRequestEnum.CONTINUE);
		} else if (UserRequestEnum.RECONNECT.equals(UserRequestEnum.valueOf(arr[0]))) {
			r.setOperation(UserRequestEnum.RECONNECT);
			r.setSrcId(arr[1]);
		} else if (UserRequestEnum.BREAK_CONNECTION.equals(UserRequestEnum.valueOf(arr[0]))) {
			r.setOperation(UserRequestEnum.BREAK_CONNECTION);
			r.setSrcId(arr[1]);
			r.setDestId(arr[2]);
		} else if (UserRequestEnum.RECOVER_CONNECTION.equals(UserRequestEnum.valueOf(arr[0]))) {
			r.setOperation(UserRequestEnum.RECOVER_CONNECTION);
			r.setSrcId(arr[1]);
			r.setDestId(arr[2]);
		} else if (UserRequestEnum.JOIN.equals(UserRequestEnum.valueOf(arr[0]))) {
			r.setOperation(UserRequestEnum.JOIN);
			r.setSrcId(arr[1]);
			r.setDestId(arr[2]);
		} else if (UserRequestEnum.LEAVE.equals(UserRequestEnum.valueOf(arr[0]))) {
			r.setOperation(UserRequestEnum.LEAVE);
			r.setSrcId(arr[1]);
		}

		// to include other stuff as and when necessary.
		return r;
	}

	public void performUserRequest(UserRequest r) throws Exception {
		switch(r.getOperation()) {
			case BREAK_CONNECTION:
				changeConnectionState(r.getSrcId(),r.getDestId(),false);
				break;
			case CONTINUE:
				System.out.println("Continuing");
				this.main.pause = false;
				break;
			case ISOLATE:				
				alterNeighborStates(r.getSrcId(),false);
				break;
			case JOIN:
				joinReplica(r.getSrcId(),r.getDestId());
				break;
			case LEAVE:
				retire(r.getSrcId());
				break;
			case PAUSE:
				this.main.pause = true;
				break;
			case PRINT_ALL_LOG:
				break;
			case PRINT_LOG:
				break;
			case RECONNECT:
				alterNeighborStates(r.getSrcId(), true);
				break;
			case RECOVER_CONNECTION:
				changeConnectionState(r.getSrcId(),r.getDestId(),true);
				break;			
		}
	}

	private void retire(String srcId) {
		((Replica)this.main.processes.get(srcId)).isRetired=true;
	}

	private void joinReplica(String srcId, String destId) throws Exception {
		if(!destId.equals("-1") && main.processes.get("REPLICA:"+destId)==null) {
			System.err.println("MAIN: REPLICA:"+destId+" is not yet created");
			return;
		}
		if(main.processes.get("REPLICA:"+srcId)==null) {
			//System.out.println("creating replica - "+vals[0]);
			Replica r = new Replica(this.main, "REPLICA:"+srcId, Integer.parseInt(destId));
			//System.out.println("Created replica - "+vals[0]);
			r.start();
			//addProcess("REPLICA:"+vals[0],r);
		} else {
			Replica r0 = (Replica)main.processes.get("REPLICA:"+srcId);
			Replica r1 = (Replica)main.processes.get("REPLICA:"+destId);
			r0.neighbors.put(r1.replicaId, true);
			r1.neighbors.put(r0.replicaId, true);
		}
	}

	public void alterNeighborStates(String pid, boolean val) {
		System.out.println("Pid neighbou sate"+pid);
		Replica r = (Replica) main.processes.get(pid);
		
		//remove all neighbours from src.
		for(List<Long> key : r.neighbors.keySet()) {
			r.neighbors.put(key, val);
		}
		
		//remove src from all other replicas.
		for(Process p : main.processes.values()) {
			if(p instanceof Replica) {
				Replica a = (Replica) p;
				if(a.neighbors.containsKey(r.replicaId)) {
					a.neighbors.put(r.replicaId, val);
				}
			}
		}		
	}
	
	public void changeConnectionState(String src,String dest, boolean val) {
		Replica r1 = (Replica) main.processes.get(src);
		Replica r2 = (Replica) main.processes.get(dest);
		if(r1.neighbors.containsKey(r2.replicaId)) {
			r1.neighbors.put(r2.replicaId, val);
		}
		if(r2.neighbors.containsKey(r1.replicaId)) {
			r2.neighbors.put(r1.replicaId, val);
		}
	}
	
	public void body() throws Exception {
		Scanner reader = new Scanner(System.in);
		while(true) {
			String input = reader.nextLine();
			if(input != null) {
				UserRequest r = getUserRequest(input);
				performUserRequest(r);
			}
		}
	}

}
