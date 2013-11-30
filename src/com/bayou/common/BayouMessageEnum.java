package com.bayou.common;

public enum BayouMessageEnum {
	CREATE_WRITE("create-write");
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
