package gra_test;


import java.util.List;

public class clientRunnable implements Runnable{

    public ExperimentRunner runner;
    public List<List<LogRecord>> logs;
    public int operationInterval;
    public int intervalVariance;
    public double writeProbability;
    public int experimentTime;
    public clientRunnable(ExperimentRunner runner, List<List<LogRecord>> logs, int operationInterval, int intervalVariance, double writeProbability, int experimentTime){
        this.runner = runner;
        this.logs = logs;
        this.experimentTime = experimentTime;
        this.intervalVariance = intervalVariance;
        this.operationInterval = operationInterval;
        this.writeProbability = writeProbability;
    }


    @Override
    public void run() {
        logs.add(runner.Run(operationInterval,intervalVariance,writeProbability,experimentTime));
    }
}
