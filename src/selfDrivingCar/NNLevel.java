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
        this.randomize();
    }

    public void randomize() {
        Random random = new Random();
        for (int i = 0; i < this.levelInputs.length; ++i) {
            for (int j = 0; j < this.levelOutputs.length; ++j) {
                this.weights[i][j] = random.nextDouble() * 2 - 1;
            }
        }
        for (int i = 0; i < this.biases.length; ++i) {
            this.biases[i] = random.nextDouble() * 2 - 1;
        }
        ;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        NNLevel newNNlevel = (NNLevel) super.clone();
        // NNLevel newNNlevel = new NNLevel(this.levelInputs.length,
        // this.levelOutputs.length, this.activationValue);
        newNNlevel.levelInputs = this.levelInputs.clone();
        newNNlevel.levelOutputs = this.levelOutputs.clone();
        newNNlevel.biases = this.biases.clone();
        newNNlevel.weights = new double[this.weights.length][];
        for (int i = 0; i < this.weights.length; i++) {
            newNNlevel.weights[i] = this.weights[i].clone();
        }
        return newNNlevel;
    }

    public double[] feedForward(double[] givenInputs, NNLevel level, boolean lastLevel) {
        System.arraycopy(givenInputs, 0, level.levelInputs, 0, givenInputs.length);

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
