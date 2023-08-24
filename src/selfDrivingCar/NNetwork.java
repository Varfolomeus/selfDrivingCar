package selfDrivingCar;

import java.util.ArrayList;
import java.util.Random;

public class NNetwork implements Cloneable {
  public int[] neuronCounts;
  public NNLevel[] NNlevels;
  public double activationValue;

  public NNetwork(int[] neuronCounts, double activationValue) {
    this.neuronCounts = neuronCounts;
    this.NNlevels = new NNLevel[this.neuronCounts.length - 1];
    this.activationValue = activationValue;
    for (int i = 0; i < this.neuronCounts.length - 1; ++i) {
      this.NNlevels[i] = new NNLevel(neuronCounts[i], neuronCounts[i + 1], activationValue);
    }
  }

  public int[] feedForward(double[] givenInputs, NNetwork NNetwork) {
    int[] outputs = null;
    ArrayList<double[]> middleLayerOutputs = new ArrayList<double[]>();

    for (int i = 0; i < NNetwork.NNlevels.length; i++) {
      if (i == 0) {
        double[] middleLayerOut = new double[NNetwork.NNlevels[i].levelOutputs.length];
        middleLayerOut = NNetwork.NNlevels[i].feedForward(givenInputs, NNetwork.NNlevels[i],
            i == NNetwork.NNlevels.length - 1);
        middleLayerOutputs.add(middleLayerOut);
      } else if (i > 0 && i < NNetwork.NNlevels.length - 1) {
        double[] middleLayerOut = new double[NNetwork.NNlevels[i].levelOutputs.length];
        middleLayerOut = NNetwork.NNlevels[i].feedForward(middleLayerOutputs.get(i - 1), NNetwork.NNlevels[i],
            i == NNetwork.NNlevels.length - 1);
        middleLayerOutputs.add(middleLayerOut);
      } else {
        double[] doubleNNOutputs = new double[NNetwork.NNlevels[i].levelOutputs.length];
        doubleNNOutputs = NNetwork.NNlevels[i].feedForward(middleLayerOutputs.get(i - 1), NNetwork.NNlevels[i],
            i == NNetwork.NNlevels.length - 1);
        outputs = new int[NNetwork.NNlevels[i].levelOutputs.length];
        for (int j = 0; j < doubleNNOutputs.length; ++j) {
          outputs[j] = (int) doubleNNOutputs[j];
        }
      }
    }
    return outputs;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    super.clone();
    NNetwork newNetwork = new NNetwork(this.neuronCounts, this.activationValue);
    for (int i = 0; i < NNlevels.length; ++i) {
      newNetwork.NNlevels[i] = (NNLevel) this.NNlevels[i].clone();
    }
    return newNetwork;
  }

  public NNetwork mutate(NNetwork nnNetwork, double amount) {
    Random random = new Random();
    NNetwork MutatedBrain = null;
    try {
      MutatedBrain = (NNetwork) nnNetwork.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    for (NNLevel level : MutatedBrain.NNlevels) {

      for (int i = 0; i < level.biases.length; i++) {
        double pp = Utils.lerp(level.biases[i], random.nextDouble() * 2 - 1, amount);
        level.biases[i] = pp;
      }
      for (int i = 0; i < level.weights.length; i++) {
        for (int j = 0; j < level.weights[i].length; j++) {
          double uu = Utils.lerp(level.weights[i][j], random.nextDouble() * 2 - 1, amount);
          level.weights[i][j] = uu;
        }
      }
    }
    return MutatedBrain;
  }
}
