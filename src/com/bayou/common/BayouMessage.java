package com.bayou.common;

import java.util.List;

public class BayouMessage {
	BayouMessageEnum messageType;
	String srcId;
	List<Long> replicaId;
	List<Long> parentReplicaId;
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

	public List<Long> getReplicaId() {
		return replicaId;
	}

	public void setReplicaId(List<Long> replicaId) {
		this.replicaId = replicaId;
	}

	public List<Long> getParentReplicaId() {
		return parentReplicaId;
	}

	public void setParentReplicaId(List<Long> parentReplicaId) {
		this.parentReplicaId = parentReplicaId;
	}
}
