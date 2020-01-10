package com.hsbc.eep.hackslack;

// Imports the Google Cloud client library
import com.google.api.services.bigquery.model.TableRow;
import com.hsbc.eep.hackslack.data.NLPResult;
import com.hsbc.eep.hackslack.transforms.AnalyzeMessageFn;
import com.hsbc.eep.hackslack.transforms.ParseSlackMessagesFromFileFn;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;

public class BatchJob {

    public interface Options extends PipelineOptions {
        String getBigQueryOutputTable();
        void setBigQueryOutputTable(String value);

        String getSlackDumpsRoot();
        void setSlackDumpsRoot(String value);
    }

    public static void main(String[] args) {
        // Parse the options
        PipelineOptionsFactory.register(Options.class);
        final Options options = (Options)PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

        // Instantiate the pipeline
        final Pipeline p = Pipeline.create(options);

        // Read the slack messages
        PCollection<KV<Integer, NLPResult>> nlpResult = p
                .apply("Find the Slack messages", FileIO.match().filepattern(options.getSlackDumpsRoot()))
                .apply("Load messages", FileIO.readMatches())
                .apply("Parse messages", ParDo.of(new ParseSlackMessagesFromFileFn()))
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
                                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE));

        // Run the pipeline
        p.run();
    }
}
