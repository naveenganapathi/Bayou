package com.bayou.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BayouMessage {
	BayouMessageEnum messageType;
	String srcId;
	int CSN;
	List<Long> replicaId;
	List<Long> parentReplicaId;
	Map<List<Long>,Long> versionVector;
	BayouRequest request;
	boolean becomePrimary = false;
	List<BayouMessage> tentativeMessages;
	List<BayouMessage> commitMessages;
	String responseMessage;
	Map<String,String> playList;
	long ts;
	String csid;
	
	
	
	
	

	
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

	public Map<List<Long>, Long> getVersionVector() {
		return versionVector;
	}

	public void setVersionVector(Map<List<Long>, Long> versionVector) {
		this.versionVector = versionVector;
	}

	public List<BayouMessage> getTentativeMessages() {
		return tentativeMessages;
	}

	public void setTentativeMessages(List<BayouMessage> writeMessages) {
		this.tentativeMessages = writeMessages;
	}

	@Override
	public String toString() {
		return "BayouMessage [messageType=" + messageType + ", srcId=" + srcId
				+ ", replicaId=" + replicaId + ", parentReplicaId="
				+ parentReplicaId + ", versionVector=" + versionVector
				+ ", request=" + request + ", writeMessages=" + tentativeMessages
				+ "]";
	}

	public int getCSN() {
		return CSN;
	}

	public void setCSN(int cSN) {
		CSN = cSN;
	}

	public List<BayouMessage> getCommitMessages() {
		return commitMessages;
	}

	public void setCommitMessages(List<BayouMessage> commitMessages) {
		this.commitMessages = commitMessages;
	}

	public boolean isBecomePrimary() {
		return becomePrimary;
	}

	public void setBecomePrimary(boolean becomePrimary) {
		this.becomePrimary = becomePrimary;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public Map<String, String> getPlayList() {
		return playList;
	}

	public void setPlayList(Map<String, String> playList) {
		this.playList = playList;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public String getCsid() {
		return csid;
	}

	public void setCsid(String csid) {
		this.csid = csid;
	}
}
