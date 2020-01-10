package com.hsbc.eep.hackslack.data;

import com.google.gson.annotations.SerializedName;
import org.junit.Test;

import java.io.Serializable;
import java.util.Date;

public class SlackMessage implements Serializable {

    @SerializedName("timestamp")
    private final Double timestamp;

    @SerializedName("channel")
    private final String channel;

    @SerializedName("text")
    private final String text;

    public SlackMessage() {
        timestamp = null;
        channel = null;
        text = null;
    }

    public SlackMessage(Double timestamp, String channel, String message) {
        this.timestamp = timestamp;
        this.channel = channel;
        this.text = message;
    }

    public Double getTimestamp() { return timestamp; }

    public String getChannel() { return channel; }

    public String getText() { return text; }

    @Override
    public String toString() {
        return text;
    }
}
