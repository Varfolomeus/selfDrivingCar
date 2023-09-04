package selfDrivingCar;

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
      this.NNlevels[i] = new NNLevel(this.neuronCounts[i], this.neuronCounts[i + 1], this.activationValue);
    }
  }

  public void feedForward() {
    for (int i = 0; i < NNlevels.length; i++) {
      NNlevels[i].levelOutputs = NNlevels[i].feedForward(NNlevels[i], i == NNlevels.length - 1);
      if (i < NNlevels.length - 1) {
        NNlevels[i + 1].levelInputs = NNlevels[i].levelOutputs;
      }
    }
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
    NNetwork MutatedBrain = null;
    try {
      MutatedBrain = (NNetwork) nnNetwork.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    Random random = new Random();
    for (NNLevel level : MutatedBrain.NNlevels) {

      for (int i = 0; i < level.biases.length; i++) {
        level.biases[i] = Utils.lerp(level.biases[i], random.nextDouble() * 2 - 1, amount);
      }
      for (int i = 0; i < level.weights.length; i++) {
        for (int j = 0; j < level.weights[i].length; j++) {
          level.weights[i][j] = Utils.lerp(level.weights[i][j], random.nextDouble() * 2 - 1, amount);
        }
      }
    }
    return MutatedBrain;
  }
}
