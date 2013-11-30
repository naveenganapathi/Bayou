package com.bayou.common;

public class BayouMessage {
	BayouMessageEnum messageType;
	String srcId;
	BayouRequest request;

	public BayouMessageEnum getMessageType() {
		return messageType;
	}

	public void setMessageType(BayouMessageEnum messageType) {
		this.messageType = messageType;
	}

	public String getSrcId() {
		return srcId;
	}

	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}

	public BayouRequest getRequest() {
		return request;
	}

	public void setRequest(BayouRequest request) {
		this.request = request;
	}
}
