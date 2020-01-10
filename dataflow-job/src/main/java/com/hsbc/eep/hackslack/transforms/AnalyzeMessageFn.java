package com.hsbc.eep.hackslack.transforms;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.hsbc.eep.hackslack.data.NLPResult;
import com.hsbc.eep.hackslack.data.SlackMessage;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

public class AnalyzeMessageFn extends DoFn<KV<Integer, SlackMessage>, KV<Integer, NLPResult>> {

    private final static Logger LOG = LoggerFactory.getLogger(AnalyzeMessageFn.class);

    @ProcessElement
    public void processElement(@Element KV<Integer, SlackMessage> in, DoFn.OutputReceiver<KV<Integer, NLPResult>> out) {
        try {
            System.out.println("AnalyzeMessageFn received " + in);
            final SlackMessage message = in.getValue();
            LOG.info("Received message: " + message);

            final LanguageServiceClient language = LanguageServiceClient.create();

            Document doc = Document.newBuilder().setContent(message.getText()).setType(Document.Type.PLAIN_TEXT).build();
            Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();

            final Date timestamp = new Date((long) (message.getTimestamp() * 1000));
            out.output(KV.of(timestamp.hashCode(), new NLPResult(timestamp, message.getChannel(), message.getText(), sentiment.getScore(), sentiment.getMagnitude())));

            language.shutdown();
            //language.awaitTermination(30, TimeUnit.SECONDS);
            language.close();

            LOG.info("Processed message: " + message);
        } catch (Exception ex) {
            System.out.println("AnalyzeMessageFn caught " + ex.getMessage());
            LOG.error(ex.getMessage());
        }
    }


    @Test
    public static void test() throws IOException {
        // Instantiates a client
        LanguageServiceClient language = LanguageServiceClient.create();

        // The text to analyze
        String text = "Stupid idiot!";
        Document doc = Document.newBuilder()
                .setContent(text).setType(Document.Type.PLAIN_TEXT).build();

        // Detects the sentiment of the text
        Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();

        System.out.printf("Text: %s%n", text);
        System.out.printf("Sentiment: %s, %s%n", sentiment.getScore(), sentiment.getMagnitude());
    }
}
