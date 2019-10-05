package seakers.explorerserver.structs;

import java.util.List;
/*
    struct BinaryInputArchitecture
    {
        1: int id,
        2: list<bool> inputs,
        3: list<double> outputs
    }
 */
public class BinaryInputArchitecture
{
    public int id;
    public List<Boolean> inputs;
    public List<Double> outputs;

    public BinaryInputArchitecture(int id, List<Boolean> inputs, List<Double> outputs)
    {
        this.id      = id;
        this.inputs  = inputs;
        this.outputs = outputs;
    }
}
