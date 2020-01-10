package com.hsbc.eep.hackslack.transforms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.eep.hackslack.data.SlackMessage;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.transforms.DoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ParseSlackMessagesFromStringFn extends DoFn<String, KV<Integer, SlackMessage>> {

    private final static Logger LOG = LoggerFactory.getLogger(ParseSlackMessagesFromStringFn.class);

    @ProcessElement
    public void processElement(@Element String in, OutputReceiver<KV<Integer, SlackMessage>> out) {
        try {
            System.out.println("ParseSlackMessageFromStringFn received " + in);
            final ArrayList<SlackMessage> messages = new ObjectMapper().readValue(in, new TypeReference<ArrayList<SlackMessage>>() {
            });
            for (final SlackMessage message : messages) {
                LOG.warn("Sending: " + message);
                out.output(KV.of(message.getTimestamp().hashCode(), message));
            }
        } catch (Exception ex) {
            System.out.println("ParseSlackMessageFromStringFn caught " + ex.getMessage());
            LOG.error(ex.getMessage());
        }
    }
}
