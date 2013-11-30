package com.bayou.common;

public class BayouRequest {
	BayouRequestEnum operation;
	String key;
	String value;
	Long acceptStamp;
	int clientCommandId;
	public BayouRequest(BayouRequestEnum operation, String key, String value, int clientCommandId) {
		this.operation = operation;
		this.key = key;
		this.value = value;
		this.clientCommandId = clientCommandId;
	}
	public BayouRequestEnum getOperation() {
		return operation;
	}
	public void setOperation(BayouRequestEnum operation) {
		this.operation = operation;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Long getAcceptStamp() {
		return acceptStamp;
	}
	public void setAcceptStamp(Long acceptStamp) {
		this.acceptStamp = acceptStamp;
	}
	public int getClientCommandId() {
		return clientCommandId;
	}
	public void setClientCommandId(int clientCommandId) {
		this.clientCommandId = clientCommandId;
	}
	@Override
	public String toString() {
		return "BayouRequest [operation=" + operation + ", key=" + key
				+ ", value=" + value + ", acceptStamp=" + acceptStamp
				+ ", clientCommandId=" + clientCommandId + "]";
	}
}
