package com.bayou.common;

public class UserRequest {
	UserRequestEnum operation;
	String srcId;
	String destId;
	int timeout;
	public UserRequestEnum getOperation() {
		return operation;
	}
	public void setOperation(UserRequestEnum operation) {
		this.operation = operation;
	}
	public String getSrcId() {
		return srcId;
	}
	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}
	public String getDestId() {
		return destId;
	}
	public void setDestId(String destId) {
		this.destId = destId;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	
}
