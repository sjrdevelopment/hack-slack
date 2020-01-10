Open this project with intellij idea, as an existing maven project.
The credentials folder contains the GCP service account key in json format, that must be referenced in the environment variable GOOGLE_APPLICATION_CREDENTIALS when running a Dataflow job.

1. To analyze slack messages in batch mode from Google Cloud Storage:

Use the class com.hsbc.eep.hackslack.BatchJob with below options

--runner=DataflowRunner
--project=eep-hack-01
--gcpTempLocation=gs://hack_slack/tmp
--tempLocation=gs://hack_slack/tmp
--region=europe-west1
--zone=europe-west2-b
--serviceAccount=hs-sa-326@eep-hack-01.iam.gserviceaccount.com
--subnetwork=https://www.googleapis.com/compute/alpha/projects/eep-hack-01/regions/europe-west2/subnetworks/hack2
--workerMachineType=n1-standard-2
--numWorkers=1
--maxNumWorkers=1
--autoscalingAlgorithm=NONE
--bigQueryOutputTable=hs_test.slack_nlp_output
--slackDumpsRoot=gs://hack_slack/message-data/*.json


2. To analyze slack messages in streaming mode from Pub/Sub:

Use the class com.hsbc.eep.hackslack.StreamingJob with below options

--runner=DataflowRunner
--project=eep-hack-01
--gcpTempLocation=gs://hack_slack/tmp
--tempLocation=gs://hack_slack/tmp
--region=europe-west1
--zone=europe-west2-b
--serviceAccount=hs-sa-326@eep-hack-01.iam.gserviceaccount.com
--subnetwork=https://www.googleapis.com/compute/alpha/projects/eep-hack-01/regions/europe-west2/subnetworks/hack2
--workerMachineType=n1-standard-2
--numWorkers=1
--maxNumWorkers=1
--autoscalingAlgorithm=NONE
--bigQueryOutputTable=hs_test.slack_nlp_output
--slackSubscription=projects/eep-hack-01/subscriptions/hack_slack


3. Notes:

You may enable autoscaling with --autoscalingAlgorithm=THROUGHPUT_BASED
To run locally, just remove the --runner argument
In local batch mode, you may want to use --slackDumpsRoot=<some local search pattern> (e.g. c:/temp/*.json)
With Google Dataflow, when the autoscaline is disabled, maxNumWorkers does not matter
To test streaming job, you can use the script in realtime_messages, that sends messages to Pub/Sub
