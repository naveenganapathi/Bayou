package com.bayou;
import java.util.Scanner;

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
		if(UserRequestEnum.ISOLATE.equals(arr[0])) {
			r.setOperation(UserRequestEnum.ISOLATE);
			r.setSrcId(arr[1]);
			r.setDestId(arr[2]);			
		}
		
		// to include other stuff as and when necessary.
		return r;
	}
	
	public void performUserRequest(UserRequest r) {
		
	}
	
	public void body() {
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
