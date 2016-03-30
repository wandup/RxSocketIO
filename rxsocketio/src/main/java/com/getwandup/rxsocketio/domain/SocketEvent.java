package com.getwandup.rxsocketio.domain;

/**
 * @author manolovn
 */
public class SocketEvent {

    private String type;
    private Object[] args;

    public SocketEvent(String type, Object[] args) {
        this.type = type;
        this.args = args;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
