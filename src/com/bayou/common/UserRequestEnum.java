package com.bayou.common;

public enum UserRequestEnum {
	ISOLATE("isolate"),
	PAUSE("pause"),
	RESUME("resume"),
	ADD("add"),
	RETIRE("retire"),
	CONNECT("connect");
	String messageLabel;
	UserRequestEnum(String message) {
            this.messageLabel=message;
    }
    public String getMessageLabel() {
            return messageLabel;
    }
    public void setMessageLabel(String messageLabel) {
            this.messageLabel = messageLabel;
    }
}
