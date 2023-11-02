package selfDrivingCar;

import java.util.Random;

public class NNLevel implements Cloneable {
    public double[] levelInputs;
    public double[] levelOutputs;
    public double[] biases;
    public double[][] weights;
    public double activationValue;

    public NNLevel(int levelInputs, int levelOutputs, double activationValue) {
        this.levelInputs = new double[levelInputs];
        this.levelOutputs = new double[levelOutputs];
        this.biases = new double[levelOutputs];
        this.activationValue = activationValue;
        this.weights = new double[levelInputs][levelOutputs];
        randomize();
    }

    public void randomize() {
        Random random = new Random();
        for (int i = 0; i < levelInputs.length; ++i) {
            for (int j = 0; j < levelOutputs.length; ++j) {
                weights[i][j] = random.nextDouble() * 2 - 1;
            }
        }
        for (int i = 0; i < biases.length; ++i) {
            biases[i] = random.nextDouble() * 2 - 1;
        }
        ;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        NNLevel newNNlevel = (NNLevel) super.clone();
        newNNlevel.levelInputs = levelInputs.clone();
        newNNlevel.levelOutputs = levelOutputs.clone();
        newNNlevel.biases = biases.clone();
        newNNlevel.weights = new double[weights.length][];
        for (int i = 0; i < weights.length; i++) {
            newNNlevel.weights[i] = weights[i].clone();
        }
        return newNNlevel;
    }

    public double[] feedForward(NNLevel level, boolean lastLevel) {

        for (int i = 0; i < level.levelOutputs.length; ++i) {
            double sum = 0;
            for (int j = 0; j < level.levelInputs.length; ++j) {
                sum += level.levelInputs[j] * level.weights[j][i];
            }
            if (!lastLevel) {
                double utyuri = Utils.hypeTthan(sum + level.biases[i]) * 2;
                level.levelOutputs[i] = utyuri;
            } else {
                if (sum + level.biases[i] >= level.activationValue) {
                    level.levelOutputs[i] = 1;
                } else {
                    level.levelOutputs[i] = 0;
                }
            }
        }
        return level.levelOutputs;
    }
}
