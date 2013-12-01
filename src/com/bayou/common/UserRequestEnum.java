package com.bayou.common;

public enum UserRequestEnum {
	PAUSE("pause"),
	CONTINUE("continue"),
	PRINT_ALL_LOG("print-all-log"),
	PRINT_LOG("print-log"),
	ISOLATE("isolate"),
	RECONNECT("reconnect"),
	BREAK_CONNECTION("break-connection"),
	RECOVER_CONNECTION("recover-connection"),
	JOIN("join"),
	LEAVE("leave");
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
