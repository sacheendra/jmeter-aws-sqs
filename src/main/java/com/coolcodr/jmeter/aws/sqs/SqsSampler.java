package com.coolcodr.jmeter.aws.sqs;

import java.io.Serializable;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.auth.BasicAWSCredentials;

public class SqsSampler extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 1L;

    // set up default arguments for the JMeter GUI
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("ACCESS_KEY", "");
        defaultParameters.addArgument("SECRET_KEY", "");
        defaultParameters.addArgument("REGION", "");
        defaultParameters.addArgument("QUEUE_NAME", "");
        defaultParameters.addArgument("MSG", "");
        return defaultParameters;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        // pull parameters
        String accessKey = context.getParameter( "ACCESS_KEY" );
        String secretKey = context.getParameter( "SECRET_KEY" );
        String region = context.getParameter( "REGION" );
        String queueName = context.getParameter( "QUEUE_NAME" );
        String msg = context.getParameter( "MSG" );

        SampleResult result = new SampleResult();
        result.sampleStart(); // start stopwatch

        try {
            AWSCredentials credentials = new BasicAWSCredentials( accessKey, secretKey);
            AmazonSQS sqs = new AmazonSQSClient(credentials);
            // Region awsRegion = Region.getRegion(Regions.valueOf("AP_SOUTHEAST_1"));
            // Region awsRegion = Region.getRegion(Regions.AP_SOUTHEAST_1);
            Region awsRegion = Region.getRegion(Regions.valueOf(region));
            sqs.setRegion(awsRegion);

            CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
            String queueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

            SendMessageResult sqsResult = sqs.sendMessage(new SendMessageRequest(queueUrl, msg));

            result.sampleEnd(); // stop stopwatch
            result.setSuccessful( true );
            result.setResponseMessage( "Successfully performed action" );
            result.setResponseCodeOK(); // 200 code
        } catch (Exception e) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful( false );
            result.setResponseMessage( "Exception: " + e );

            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace( new java.io.PrintWriter( stringWriter ) );
            result.setResponseData( stringWriter.toString() );
            result.setDataType( org.apache.jmeter.samplers.SampleResult.TEXT );
            result.setResponseCode( "500" );
        }
        return result;
    }
}
