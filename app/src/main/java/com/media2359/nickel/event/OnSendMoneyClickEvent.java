package com.media2359.nickel.event;

/**
 * Created by Xijun on 24/3/16.
 */
public class OnSendMoneyClickEvent {
    private int position;

    public OnSendMoneyClickEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
