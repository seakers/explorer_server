package seakers.explorerserver.ga;



// VASSAR Imports
import seakers.vassar.BaseParams;
import seakers.vassar.Result;
import seakers.vassar.architecture.AbstractArchitecture;



import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;




import java.util.concurrent.Future;


/*
    This class will handle the architecture created by the GA and send them to VASSAR
 */
public class ArchitectureHandler
{


    private BaseParams params;
    private String     vassarControllerQueue;
    private String     receiveArchitectureQueue;



    public ArchitectureHandler(BaseParams params, String vassarControllerQueue)
    {
        this.params                   = params;
        this.vassarControllerQueue    = vassarControllerQueue;
    }




    //--> This function is called when a discrete architecture is to be evaluated
    public Future<Result> evaluateArchitectureAsync(AbstractArchitecture arch, String mode)
    {


        //--> Read the Abstract Architecture and determine what kind of VASSAR run it will be




        //--> Query the VASSAR master controller to get a queue to place this abstract architecture in --> send username_problem or session_problem
        String queueURL;







        //--> Create a message from the abstract architecture and send it to the queue specified by the VASSAR controller


        //--> Check evaluated architectures queue to see if any messages contain the appropriate ID, get message --> VASSAR instance will have this queue


        //--> Process message into Future<Result> object, return this object


    }



    public String getVassarInstanceQueue(AbstractArchitecture)






}
