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

	public static GASearch ga;


	// This main function will be continuously checking to see if a GA request was sent to the AWS Queue
	// When a message is recieved, is will call a function that will start an instance of the GA
	public static void main(String [] args)
	{
		// SQS Client
		SqsAsyncClient sqs_client                    = SqsAsyncClient.builder().region(Region.US_WEST_2).build();

		// Queue URLs
		String         ga_request_queue              = "ga_request_queue";
		String         evaluated_architectures_queue = "evaluated_architectures_queue";

		// Queue Details
		int            messages_in_queue             = get_queue_message_volume(sqs_client, ga_request_queue);
		List<Message>  messages_receiced             = get_queue_messages(sqs_client, ga_request_queue, 1);









	}





	// This functions returns the number of messages in the queue -- waits until the future object has returned
	public static int get_queue_message_volume(SqsAsyncClient sqs_client, String queue_url)
	{
		try {
			GetQueueAttributesRequest          desired_attributes = GetQueueAttributesRequest.builder().queueUrl(queue_url).attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES).build();
			Future<GetQueueAttributesResponse> future_queue_attrs = sqs_client.getQueueAttributes(desired_attributes);
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
	public static List<Message> get_queue_messages(SqsAsyncClient sqs_client, String queue_url, int max_messages)
	{
		try {
			ReceiveMessageRequest          receiveRequest          = ReceiveMessageRequest.builder().queueUrl(queue_url).maxNumberOfMessages(max_messages).build();
			Future<ReceiveMessageResponse> future_message_response = sqs_client.receiveMessage(receiveRequest);
			ReceiveMessageResponse         message_response        = future_message_response.get();
			List<Message>                  messages                = message_response.messages();
			return                         messages;
		}
		catch (Exception x) {
			x.printStackTrace();
			List<Message> empty = new ArrayList<>();
			return        empty;
		}
	}














	// This function will start the instance of the GA class
	public static void start_ga(String problem, String username)
	{







	}









}
