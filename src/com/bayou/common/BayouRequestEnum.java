package com.bayou.common;

public enum BayouRequestEnum {
	ADD("add"),
	EDIT("edit"),
	DELETE("delete");
	String messageLabel;
	BayouRequestEnum(String message) {
            this.messageLabel=message;
    }
    public String getMessageLabel() {
            return messageLabel;
    }
    public void setMessageLabel(String messageLabel) {
            this.messageLabel = messageLabel;
    }
}
