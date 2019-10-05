package seakers.explorerserver.structs;

import java.util.List;
/*
        struct DiscreteInputArchitecture
        {
            1: int id,
            2: List<Integer> inputs,
            3: List<Double> outputs
        }
 */
public class DiscreteInputArchitecture
{
    public int id;
    public List<Integer> inputs;
    public List<Double> outputs;

    public DiscreteInputArchitecture(int id, List<Integer> inputs, List<Double> outputs)
    {
        this.id      = id;
        this.inputs  = inputs;
        this.outputs = outputs;
    }
}
