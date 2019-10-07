package seakers.explorerserver.server;




// ------------------------------
// AWS Imports
// ------------------------------
import jmetal.encodings.variable.Binary;
import org.json.simple.parser.ParseException;
import seakers.explorerserver.structs.BinaryInputArchitecture;
import seakers.explorerserver.structs.BinaryReturn;
import seakers.explorerserver.structs.DiscreteInputArchitecture;
import seakers.explorerserver.structs.DiscreteReturn;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.core.exception.*;
//import com.amazonaws.services.sqs.model.AmazonSQSException;


// ------------------------------
// Other Imports
// ------------------------------
import seakers.explorerserver.ga.GASearch;



import java.io.*;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;






public class JavaServer
{

	public static GASearch       ga;
	public SqsAsyncClient sqsClient;


	// This main function will be continuously checking to see if a GA request was sent to the AWS Queue
	// When a message is recieved, is will call a function that will start an instance of the GA
	public void main(String [] args) throws ParseException {

		//--> Object that performs the GA search algorithm
		String vassarControllerQueue = "queue.URL";
		ga = new GASearch("./VASSAR_resources", vassarControllerQueue);

		//--> SQS Client
		this.sqsClient                              = SqsAsyncClient.builder().region(Region.US_WEST_2).build();
		String         gaRequestQueue               = "ga_request_queue";
		String         evaluatedArchitecturesQueue  = "evaluated_architectures_queue";

		//--> Queue Details
		int            messagesInQueue              = getQueueMessageVolume(gaRequestQueue);
		List<Message>  messagesReceived             = getQueueMessages(gaRequestQueue, 1);


		//--> Iterate over the messages received - process - delete
		for(Message runRequest: messagesReceived)
		{
			Map<String, MessageAttributeValue> messageAttributes = runRequest.messageAttributes();

			//--> This will either be assignation (binary) or partition (discrete)
			MessageAttributeValue problemTypeAttr  = messageAttributes.get("problem_type");
			String                problemType      = problemTypeAttr.stringValue();
			System.out.println(problemType);

			//--> Username
			MessageAttributeValue usernameAttr     = messageAttributes.get("username");
			String                username         = usernameAttr.stringValue();
			System.out.println(username);

			//--> Problem Name
			MessageAttributeValue problemNameAttr  = messageAttributes.get("problem_name");
			String                problemName      = problemNameAttr.stringValue();
			System.out.println(problemName);

			//--> Max Evaluations
			MessageAttributeValue maxEvalsAttr      = messageAttributes.get("maxEvals");
			int                   maxEvals          = Integer.parseInt(maxEvalsAttr.stringValue());
			System.out.println(maxEvals);

			//--> Crossover Probability
			MessageAttributeValue crossoverProbAttr = messageAttributes.get("crossoverProbability");
			double                crossoverProb     = Double.parseDouble(crossoverProbAttr.stringValue());
			System.out.println(crossoverProb);

			//--> Mutation Probability
			MessageAttributeValue mutationProbAttr  = messageAttributes.get("mutationProbability");
			double                mutationProb      = Double.parseDouble(mutationProbAttr.stringValue());
			System.out.println(mutationProb);

			//--> this List<String> will become List<BinaryInputArchitecture> or List<DiscreteInputArchitecture>
			MessageAttributeValue datasetAttr       = messageAttributes.get("dataset");
			List<String>          dataset           = datasetAttr.stringListValues();

			//--> Delete the message
			DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(gaRequestQueue).receiptHandle(runRequest.receiptHandle()).build();
			this.sqsClient.deleteMessage(deleteMessageRequest);


			//--> Evaluate the appropriate problem
			if(problemType == "assignation")
			{
				List<BinaryInputArchitecture> architectures = getBinaryArchitectures(dataset);


				//BinaryReturn results = ga.runBinaryInputGATask(username, problemName, architectures, maxEvals, crossoverProb, mutationProb);
			}
			else if(problemType == "partition")
			{
				List<DiscreteInputArchitecture> architectures = getDiscreteArchitectures(dataset);
				//DiscreteReturn results = ga.runDiscreteInputGATask(username, problemName, architectures, maxEvals, crossoverProb, mutationProb);
			}
		}
	}





	//--> Parse a list of strings into a list of BinaryInputArchitecture objs
	public List<BinaryInputArchitecture> getBinaryArchitectures(List<String> dataset) throws ParseException {
		JSONParser                    parser    = new JSONParser();
		List<BinaryInputArchitecture> to_return = new ArrayList<>();
		for(String arch: dataset)
		{
			//--> Get JSON object
			JSONObject arch_json = (JSONObject) parser.parse(arch);

			//--> Get id
			int        id        = Integer.parseInt(arch_json.get("id").toString());

			//--> Get inputs
			List<Boolean> inputs = new ArrayList<>();
			JSONArray inputs_array = (JSONArray) arch_json.get("inputs");
			for(int x = 0; x < inputs_array.size(); x++)
			{
				String input = inputs_array.get(x).toString();
				inputs.add(Boolean.parseBoolean(input));
			}

			//--> Get outputs
			List<Double> outputs = new ArrayList<>();
			JSONArray outputs_array = (JSONArray) arch_json.get("outputs");
			for(int x = 0; x < outputs_array.size(); x++)
			{
				String output = outputs_array.get(x).toString();
				outputs.add(Double.parseDouble(output));
			}
			BinaryInputArchitecture binaryArch = new BinaryInputArchitecture(id, inputs, outputs);
			to_return.add(binaryArch);
		}
		return to_return;
	}

	//--> Parse a list of strings into a list of DiscreteInputArchitecture objs
	public List<DiscreteInputArchitecture> getDiscreteArchitectures(List<String> dataset) throws ParseException {
		JSONParser                    parser    = new JSONParser();
		List<DiscreteInputArchitecture> to_return = new ArrayList<>();
		for(String arch: dataset)
		{
			//--> Get JSON object
			JSONObject arch_json = (JSONObject) parser.parse(arch);

			//--> Get id
			int        id        = Integer.parseInt(arch_json.get("id").toString());

			//--> Get inputs
			List<Integer> inputs = new ArrayList<>();
			JSONArray inputs_array = (JSONArray) arch_json.get("inputs");
			for(int x = 0; x < inputs_array.size(); x++)
			{
				String input = inputs_array.get(x).toString();
				inputs.add(Integer.parseInt(input));
			}

			//--> Get outputs
			List<Double> outputs = new ArrayList<>();
			JSONArray outputs_array = (JSONArray) arch_json.get("outputs");
			for(int x = 0; x < outputs_array.size(); x++)
			{
				String output = outputs_array.get(x).toString();
				outputs.add(Double.parseDouble(output));
			}
			DiscreteInputArchitecture binaryArch = new DiscreteInputArchitecture(id, inputs, outputs);
			to_return.add(binaryArch);
		}
		return to_return;
	}















	//--> Send a message to a message queue
	public void sendMessage(String messageBody, Map<String, MessageAttributeValue> messageAttributes, String queueURL)
	{
		// Map<String, MessageAttributeValue>
		SendMessageRequest request = SendMessageRequest.builder().messageBody(messageBody).messageAttributes(messageAttributes).queueUrl(queueURL).build();
		this.sqsClient.sendMessage(request);
	}





	// This functions returns the number of messages in the queue -- waits until the future object has returned
	public int getQueueMessageVolume(String queue_url)
	{
		try {
			GetQueueAttributesRequest          desired_attributes = GetQueueAttributesRequest.builder().queueUrl(queue_url).attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES).build();
			Future<GetQueueAttributesResponse> future_queue_attrs = this.sqsClient.getQueueAttributes(desired_attributes);
			GetQueueAttributesResponse         queue_attrs        = future_queue_attrs.get();
			String                             number_of_messages = queue_attrs.attributes().get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES);
			return                             Integer.parseInt(number_of_messages);
		}
		catch (Exception x) {
			x.printStackTrace();
			return -1;
		}
	}



	// This function gets x number of messages in the queue -- waits until the future object has returned
	public List<Message> getQueueMessages(String queue_url, int max_messages)
	{
		try {
			ReceiveMessageRequest          receiveRequest          = ReceiveMessageRequest.builder().queueUrl(queue_url).maxNumberOfMessages(max_messages).build();
			Future<ReceiveMessageResponse> future_message_response = this.sqsClient.receiveMessage(receiveRequest);
			ReceiveMessageResponse         message_response        = future_message_response.get();
			List<Message>                  messages                = message_response.messages();
			return                         messages;
		}
		catch (Exception x) {
			x.printStackTrace();
			return new ArrayList<>();
		}
	}






	//--> This function will create the queue that each GA instance will look for results in
	public String createEvaluatedArchitectureQueue()
    {
    	//--> How will we choose the queue url?
		String queueURL = "queue.url";

		try {
			CreateQueueRequest createQueueRequest = CreateQueueRequest.builder().queueName(queueURL).build();
			this.sqsClient.createQueue(createQueueRequest);
		}
		catch (AwsServiceException e) {
			throw e;
		}

		return queueURL;
    }





	// This function will start the instance of the GA class
	public static void start_ga(String problem, String username)
	{







	}









}
