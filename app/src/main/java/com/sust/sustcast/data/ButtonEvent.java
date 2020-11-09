package com.sust.sustcast.data;

public class ButtonEvent {

    public boolean state;

    public ButtonEvent(boolean state) {
        this.state = state;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
