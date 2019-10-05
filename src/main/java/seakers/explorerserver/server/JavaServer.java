package seakers.explorerserver.server;




// ------------------------------
// AWS Imports
// ------------------------------
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;


// ------------------------------
// Other Imports
// ------------------------------
import seakers.explorerserver.ga.GASearch;



import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;






public class JavaServer
{

	public static GASearch       ga;
	public SqsAsyncClient sqsClient;


	// This main function will be continuously checking to see if a GA request was sent to the AWS Queue
	// When a message is recieved, is will call a function that will start an instance of the GA
	public void main(String [] args)
	{
		//--> Object that performs the GA search algorithm
		ga = new GASearch("./VASSAR_resources");



		//--> SQS Client
		this.sqsClient                    = SqsAsyncClient.builder().region(Region.US_WEST_2).build();

		//--> Queue URLs
		String         gaRequestQueue               = "ga_request_queue";
		String         evaluatedArchitecturesQueue  = "evaluated_architectures_queue";

		//--> Queue Details
		int            messagesInQueue              = getQueueMessageVolume(gaRequestQueue);
		List<Message>  messagesReceived             = getQueueMessages(gaRequestQueue, 1);




		//-->














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






	// This function will start the instance of the GA class
	public static void start_ga(String problem, String username)
	{







	}









}
