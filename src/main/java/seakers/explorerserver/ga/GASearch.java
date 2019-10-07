package seakers.explorerserver.ga;




import java.io.File;
import java.util.*;
import java.util.concurrent.*;


// MOEA Includes
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.ParetoObjectiveComparator;
import org.moeaframework.core.operator.*;
import org.moeaframework.core.operator.binary.BitFlip;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.util.TypedProperties;



// VASSAR Includes
import seakers.architecture.operators.IntegerUM;
import seakers.architecture.util.IntegerVariable;
import seakers.explorerserver.ga.problems.Assigning.AssigningArchitecture;
import seakers.explorerserver.ga.problems.Assigning.AssigningProblem;
import seakers.explorerserver.ga.problems.PartitioningAndAssigning.PartitioningAndAssigningArchitecture;
import seakers.explorerserver.ga.problems.PartitioningAndAssigning.PartitioningAndAssigningProblem;
import seakers.explorerserver.ga.problems.PartitioningAndAssigning.operators.PartitioningAndAssigningCrossover;
import seakers.explorerserver.ga.problems.PartitioningAndAssigning.operators.PartitioningAndAssigningMutation;
import seakers.explorerserver.structs.BinaryInputArchitecture;
import seakers.explorerserver.structs.BinaryReturn;
import seakers.explorerserver.structs.DiscreteInputArchitecture;
import seakers.explorerserver.structs.DiscreteReturn;
import seakers.vassar.BaseParams;
import seakers.vassar.Resource;
import seakers.vassar.Result;
import seakers.vassar.architecture.AbstractArchitecture;
import seakers.vassar.evaluation.AbstractArchitectureEvaluator;
import seakers.vassar.evaluation.ArchitectureEvaluationManager;
import seakers.vassar.problems.Assigning.*;
import seakers.vassar.problems.PartitioningAndAssigning.Decadal2017AerosolsParams;



import seakers.engineerserver.search.problems.PartitioningAndAssigning.PartitioningAndAssigningInitialization;







// This class will have two functions
// 1. Evaluate a Binary Input GA
// 2. Evaluate a Discrete Input GA
public class GASearch 
{


    private String resourcesPath;
    private String vassarControllerQueue;


    public GASearch(String pathToVassarResources, String vassarControllerQueue)
    {
        this.resourcesPath         = pathToVassarResources;
        this.vassarControllerQueue = vassarControllerQueue;
    }





    public BinaryReturn runBinaryInputGATask(String username,
                                              String problem,
                                              List<BinaryInputArchitecture> dataset,
                                              int    maxEvals,
                                              double crossoverProbability,
                                              double mutationProbability)
    {
        //--> Defaults
        //int maxEvals                = 3000;
        //double crossoverProbability = 1.0;
        //double mutationProbability  = 1. / 60.;


        //--> Search Parameters
        TypedProperties properties          = new TypedProperties();
        properties.setInt("maxEvaluations", maxEvals);
        properties.setInt("populationSize", dataset.size());
        properties.setDouble("crossoverProbability", crossoverProbability);
        properties.setDouble("mutationProbability",  mutationProbability);


        Variation      singlecross;
        Variation      bitFlip;
        Variation      intergerMutation;
        Initialization initialization;

        //--> MOEA Epsilon - Should we let the user set this?
        double[] epsilonDouble               = new double[]{0.001, 1};

        //--> Initialize Base Parameters
        BaseParams params                    = this.getProblemParameters(problem);

        //ArchitectureEvaluationManager AEM  = this.architectureEvaluationManagerMap.get(problem);
        ArchitectureHandler           AH     = new ArchitectureHandler(params, this.vassarControllerQueue);

        //--> Create the assinging problem for binary GA run
        Problem assignmentProblem            = new AssigningProblem(new int[]{1}, problem, AH, params);

        //--> Solution for each input architecture in the data set
        List<Solution> initial               = new ArrayList<>(dataset.size());


        //--> Create an Assinging Architecture for each BinaryInputArchitecture
        for (BinaryInputArchitecture arch : dataset)
        {
            //--> Create an assinging architecture
            AssigningArchitecture new_arch = new AssigningArchitecture(new int[]{1}, params.getNumInstr(), params.getNumOrbits(), 2);

            //--> Create and set binary variables for each in the Assinging Architecture created
            for (int j = 1; j < new_arch.getNumberOfVariables(); ++j)
            {
                BinaryVariable var         = new BinaryVariable(1);
                var.set(0, arch.inputs.get(j-1));
                new_arch.setVariable(j, var);
            }

            //--> Set objectives
            new_arch.setObjective(0, -arch.outputs.get(0));
            new_arch.setObjective(1, arch.outputs.get(1));
            new_arch.setAlreadyEvaluated(true);
            initial.add(new_arch);
        }



        //--> Initialization
        initialization                     = new InjectedInitialization(assignmentProblem, dataset.size(), initial);

        //--> Population
        Population population              = new Population();

        //--> Archive
        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

        //--> Selection
        ChainedComparator comp             = new ChainedComparator(new ParetoObjectiveComparator());
        TournamentSelection selection      = new TournamentSelection(2, comp);

        //--> Variation
        singlecross                        = new OnePointCrossover(crossoverProbability);
        bitFlip                            = new BitFlip(mutationProbability);
        intergerMutation                   = new IntegerUM(mutationProbability);
        CompoundVariation var              = new CompoundVariation(singlecross, bitFlip, intergerMutation);


        // ---------------------------
        // GA Object
        // ---------------------------
        Algorithm eMOEA                    = new EpsilonMOEA(assignmentProblem, population, archive, selection, var, initialization);



        //--> Record the start time of the GA
        long startTime = System.currentTimeMillis();

        //--> Step through the GA until it finishes!
        eMOEA.step();
        while (!eMOEA.isTerminated() && (eMOEA.getNumberOfEvaluations() < maxEvals))
        {
            eMOEA.step();
        }
        //--> GA has finished



        //--> Now we will get the results
        ArrayList<ArrayList<Boolean>> inputs   = new ArrayList<>();
        ArrayList<ArrayList<Double>> outputs   = new ArrayList<>();
        int                          numInputs = 10;



        //--> Iterate over the population solutions
        for(int i = 0; i < population.size(); i++)
        {
            //--> Get a population solution
            AssigningArchitecture sol = (AssigningArchitecture)population.get(i);

            //--> Store Results
            numInputs                = sol.getNumberOfVariables();
            ArrayList<Boolean> input = new ArrayList<>();
            ArrayList<Double> output = new ArrayList<>();
            for(int j = 0; j < sol.getNumberOfVariables(); j++)
            {
                input.add(((BinaryVariable) sol.getVariable(j)).get(0));
            }
            for(int j = 0; j < sol.getNumberOfObjectives(); j++)
            {
                output.add(sol.getObjective(j));
            }
            inputs.add(input);
            outputs.add(output);
        }



        BinaryReturn to_return = new BinaryReturn(inputs, outputs);
        return       to_return;
    }






    /*

    ---------- Parameters ----------
    String username: the username of the user
    List<int> inputs: the inputs
    List<double> outputs: the outputs
    int maxEvals: the number of architecture evaluations
    int id: the ID of the GA run
     */
    public DiscreteReturn runDiscreteInputGATask(String username,
                                                 String problem,
                                                 List<DiscreteInputArchitecture> dataset,
                                                 int    maxEvals,
                                                 double crossoverProbability,
                                                 double mutationProbability)
    {
        // ------- Defaults -------
        //int maxEvals                = 3000;
        //double crossoverProbability = 1.0;
        //double mutationProbability  = 1. / 6.;

        //--> Search Parameters
        TypedProperties properties             = new TypedProperties();
        properties.setInt("maxEvaluations",          maxEvals);
        properties.setInt("populationSize",          dataset.size());
        properties.setDouble("crossoverProbability", crossoverProbability);
        properties.setDouble("mutationProbability",  mutationProbability);


        //--> MOEA Epsilon -- should we let the user specify this?
        double[] epsilonDouble = new double[]{0.001, 1};



        //--> Initialize Base Parameters
        BaseParams params                       = this.getProblemParameters(problem);

        //ArchitectureEvaluationManager AEM     = this.architectureEvaluationManagerMap.get(problem);
        ArchitectureHandler           AH        = new ArchitectureHandler(params, this.vassarControllerQueue);

        //--> Create an appropriate problem
        Problem partitioningAndAssigningProblem = new PartitioningAndAssigningProblem(problem, AH, params);


        //--> Solution for each input architecture
        List<Solution> initial = new ArrayList<>(dataset.size());


        //--> For each input, create a PAA architecture and set objectives
        for (DiscreteInputArchitecture arch : dataset)
        {
            PartitioningAndAssigningArchitecture new_arch = new PartitioningAndAssigningArchitecture(params.getNumInstr(), params.getNumOrbits(), 2);

            int numPartitioningVariables = params.getNumInstr();
            int numAssignmentVariables   = params.getNumInstr();

            //--> Create Partitioning Variables
            for (int j = 0; j < numPartitioningVariables; ++j)
            {
                IntegerVariable var = new IntegerVariable(arch.inputs.get(j), 0, params.getNumInstr());
                new_arch.setVariable(j, var);
            }

            //--> Create Assignment Variables
            for (int j = numPartitioningVariables; j < numPartitioningVariables + numAssignmentVariables; ++j)
            {
                IntegerVariable var = new IntegerVariable(arch.inputs.get(j), -1, params.getNumOrbits());
                new_arch.setVariable(j, var);
            }

            new_arch.setObjective(0, -arch.outputs.get(0));
            new_arch.setObjective(1, arch.outputs.get(1));
            new_arch.setAlreadyEvaluated(true);
            initial.add(new_arch);
        }


        //--> Initialization
        Initialization             initialization   = new PartitioningAndAssigningInitialization(partitioningAndAssigningProblem, dataset.size(), initial, params);

        //--> Population (will contain results of GA)
        Population                 population       = new Population();

        //--> Archive
        EpsilonBoxDominanceArchive archive          = new EpsilonBoxDominanceArchive(epsilonDouble);

        //--> Selection
        ChainedComparator          comp             = new ChainedComparator(new ParetoObjectiveComparator());
        TournamentSelection        selection        = new TournamentSelection(2, comp);

        //--> Var
        Variation                  singlecross      = new PartitioningAndAssigningCrossover(crossoverProbability, params);
        Variation                  intergerMutation = new PartitioningAndAssigningMutation(mutationProbability, params);
        CompoundVariation          var              = new CompoundVariation(singlecross, intergerMutation);


        // ---------------------------
        // GA Object
        // ---------------------------
        Algorithm eMOEA = new EpsilonMOEA(partitioningAndAssigningProblem, population, archive, selection, var, initialization);


        //--> Record the start time of the GA
        long startTime = System.currentTimeMillis();

        //--> Step through the GA until it finishes!
        eMOEA.step();
        while (!eMOEA.isTerminated() && (eMOEA.getNumberOfEvaluations() < maxEvals))
        {
            eMOEA.step();
        }
        //--> GA has finished



        //--> Now we will get the results
        ArrayList<ArrayList<Integer>> inputs   = new ArrayList<>();
        ArrayList<ArrayList<Double>> outputs   = new ArrayList<>();
        int                          numInputs = 10;


        //--> Iterate over the population solutions
        for(int i = 0; i < population.size(); i++)
        {
            //--> Get a population solution
            PartitioningAndAssigningArchitecture sol = (PartitioningAndAssigningArchitecture)population.get(i);

            //--> Store Results
            numInputs                = sol.getNumberOfVariables();
            ArrayList<Integer> input = new ArrayList<>();
            ArrayList<Double> output = new ArrayList<>();
            for(int j = 0; j < sol.getNumberOfVariables(); j++)
            {
                input.add(((IntegerVariable) sol.getVariable(j)).getValue());
            }
            for(int j = 0; j < sol.getNumberOfObjectives(); j++)
            {
                output.add(sol.getObjective(j));
            }
            inputs.add(input);
            outputs.add(output);
        }

        DiscreteReturn to_return = new DiscreteReturn(inputs, outputs);
        return         to_return;
    }




    // This will return the BaseParams object when give the problem
    private BaseParams getProblemParameters(String problem)
    {
        BaseParams params;
        String     key;

        if (problem.equalsIgnoreCase("SMAP") || problem.equalsIgnoreCase("SMAP_JPL1") || problem.equalsIgnoreCase("SMAP_JPL2") || problem.equalsIgnoreCase("ClimateCentric"))
        {
            key = problem;

            if (problem.equalsIgnoreCase("SMAP"))
            {
                params = new SMAPParams(this.resourcesPath, "CRISP-ATTRIBUTES", "test", "normal");
            }
            else if (problem.equalsIgnoreCase("SMAP_JPL1"))
            {
                params = new SMAPJPL1Params(this.resourcesPath, "CRISP-ATTRIBUTES", "test", "normal");
            }
            else if (problem.equalsIgnoreCase("SMAP_JPL2"))
            {
                params = new SMAPJPL2Params(this.resourcesPath, "CRISP-ATTRIBUTES", "test", "normal");
            }
            else if (problem.equalsIgnoreCase("ClimateCentric"))
            {
                params = new ClimateCentricParams(this.resourcesPath, "CRISP-ATTRIBUTES", "test", "normal");
            }
            else {
                throw new RuntimeException();
            }
        }
        else if (problem.equalsIgnoreCase("Decadal2017Aerosols"))
        {
            key    = "Decadal2017Aerosols";
            params = new Decadal2017AerosolsParams(this.resourcesPath, "CRISP-ATTRIBUTES", "test", "normal");
        }
        else {
            throw new IllegalArgumentException("Unrecorgnizable problem type: " + problem);
        }
        return params;
    }



}







