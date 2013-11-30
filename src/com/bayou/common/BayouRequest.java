package com.bayou.common;

public class BayouRequest {
	BayouRequestEnum operation;
	String key;
	String value;
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
}
