package com.bayou.common;

public enum BayouMessageEnum {
	CREATE_WRITE("create-write"),
	CREATE_WRITE_RESP("create-write-response"),
	CLIENT_INPUT("client-input"),
	ADD_NEIGHBOR("add-neighbor"),
	REQUEST("request");
	String messageLabel;
	BayouMessageEnum(String message) {
            this.messageLabel=message;
    }
    public String getMessageLabel() {
            return messageLabel;
    }
    public void setMessageLabel(String messageLabel) {
            this.messageLabel = messageLabel;
    }
}
