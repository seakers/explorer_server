package seakers.explorerserver.structs;

import java.util.ArrayList;


public class DiscreteReturn
{
    public ArrayList<ArrayList<Integer>> inputs;
    public ArrayList<ArrayList<Double>> outputs;

    public DiscreteReturn(ArrayList<ArrayList<Integer>> inputs, ArrayList<ArrayList<Double>> outputs)
    {
        this.inputs  = inputs;
        this.outputs = outputs;
    }
}
