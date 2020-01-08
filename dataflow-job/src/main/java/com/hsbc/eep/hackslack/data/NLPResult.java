package com.hsbc.eep.hackslack.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class NLPResult implements Serializable {

    private final static double SCORE_THRESHOLD = 0.02;

    @SerializedName("ts")
    private final String m_timestamp;

    @SerializedName("c")
    private final String m_channel;

    @SerializedName("t")
    private final String m_text;

    @SerializedName("s")
    private final double m_score;

    @SerializedName("m")
    private final double m_magnitude;

    public NLPResult() {
        m_timestamp = null;
        m_channel = null;
        m_text = null;
        m_score = 0.;
        m_magnitude = 0.;
    }

    public NLPResult(Date timestamp, String channel, String text, double score, double magnitude) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        m_timestamp = dateFormat.format(timestamp);
        m_channel = channel;
        m_text = text;
        m_score = score;
        m_magnitude = magnitude;
    }

    public String getTimestamp() { return m_timestamp; }

    public String getChannel() { return m_channel; }

    public String getText() { return m_text; }

    public double getScore() { return m_score; }

    public double getMagnitude() { return m_magnitude; }

    @Override
    public String toString() {
        return "Sentiment for '" + m_text + "' is " + m_score + " with a magnitude of " + m_magnitude;
    }
}
