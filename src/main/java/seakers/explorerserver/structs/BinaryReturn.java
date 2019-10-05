package seakers.explorerserver.structs;

import java.util.ArrayList;

public class BinaryReturn
{
    public ArrayList<ArrayList<Boolean>> inputs;
    public ArrayList<ArrayList<Double>> outputs;

    public BinaryReturn(ArrayList<ArrayList<Boolean>> inputs, ArrayList<ArrayList<Double>> outputs)
    {
        this.inputs  = inputs;
        this.outputs = outputs;
    }
}
