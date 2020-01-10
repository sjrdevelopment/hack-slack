package com.hsbc.eep.hackslack;

// Imports the Google Cloud client library
import com.google.api.services.bigquery.model.TableRow;
import com.hsbc.eep.hackslack.data.NLPResult;
import com.hsbc.eep.hackslack.transforms.AnalyzeMessageFn;
import com.hsbc.eep.hackslack.transforms.ParseSlackMessagesFromStringFn;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.joda.time.Duration;

public class StreamingJob {

    public interface Options extends PipelineOptions {
        String getBigQueryOutputTable();
        void setBigQueryOutputTable(String value);

        String getSlackSubscription();
        void setSlackSubscription(String value);

        @Default.Integer(5)
        Integer getWindowSize();
        void setWindowSize(Integer value);
    }

    public static void main(String[] args) {
        // Parse the options
        PipelineOptionsFactory.register(Options.class);
        final Options options = (Options)PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

        // Instantiate the pipeline
        final Pipeline p = Pipeline.create(options);

        // Read the slack messages
        PCollection<KV<Integer, NLPResult>> nlpResult = p
                .apply("Read PubSub Messages", PubsubIO.readStrings().fromSubscription(options.getSlackSubscription()))
                .apply("Slice messages per period of time", Window.into(FixedWindows.of(Duration.standardSeconds(options.getWindowSize()))))
                .apply("Parse messages", ParDo.of(new ParseSlackMessagesFromStringFn()))
                .apply("Analyze message sentiments", ParDo.of(new AnalyzeMessageFn()));

        // Public into BigQuery
        nlpResult.apply("Prepare data for BigQuery",
                MapElements.into(TypeDescriptor.of(TableRow.class))
                        .via((KV<Integer, NLPResult> elem) ->
                                new TableRow()
                                        .set("timestamp", elem.getValue().getTimestamp())
                                        .set("channel", elem.getValue().getChannel())
                                        .set("text", elem.getValue().getText())
                                        .set("score", elem.getValue().getScore())
                                        .set("magnitude", elem.getValue().getMagnitude())
                        ))
                .apply("Write data into " + options.getBigQueryOutputTable(),
                        BigQueryIO.writeTableRows()
                                .to(options.getBigQueryOutputTable())
                                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
                                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));

        // Run the pipeline
        p.run();
    }
}
