package com.hsbc.eep.hackslack.transforms;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.hsbc.eep.hackslack.data.NLPResult;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

public class PrintResults extends DoFn<KV<Integer, NLPResult>, String> {

    private final static Logger LOG = LoggerFactory.getLogger(PrintResults.class);

    @ProcessElement
    public void processElement(@Element KV<Integer, NLPResult> in, OutputReceiver<String> out) {
        try {
            final NLPResult result = in.getValue();
            LOG.info(result.toString());
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
