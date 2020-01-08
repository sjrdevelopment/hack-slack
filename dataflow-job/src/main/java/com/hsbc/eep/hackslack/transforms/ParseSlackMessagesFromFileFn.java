package com.hsbc.eep.hackslack.transforms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.eep.hackslack.data.SlackMessage;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class ParseSlackMessagesFromFileFn extends DoFn<FileIO.ReadableFile, KV<Integer, SlackMessage>> {

    private final static Logger LOG = LoggerFactory.getLogger(ParseSlackMessagesFromFileFn.class);

    @ProcessElement
    public void processElement(@Element FileIO.ReadableFile in, DoFn.OutputReceiver<KV<Integer, SlackMessage>> out) {
        try {
            System.out.println("ParseSlackMessagesFn received " + in);
            ReadableByteChannel chan = in.open();
            InputStream stream = Channels.newInputStream(chan);
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream));
            final ArrayList<SlackMessage> messages = new ObjectMapper().readValue(streamReader, new TypeReference<ArrayList<SlackMessage>>() {
            });
            for (final SlackMessage message : messages) {
                LOG.warn("Sending: " + message);
                out.output(KV.of(message.getTimestamp().hashCode(), message));
            }
        } catch (Exception ex) {
            System.out.println("ParseSlackMessagesFn caught " + ex.getMessage());
            LOG.error(ex.getMessage());
        }
    }
}
