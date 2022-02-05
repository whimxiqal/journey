/*
 * MIT License
 *
 * Copyright 2021 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package edu.whimc.journey.common.ml;

import edu.whimc.journey.common.data.PathRecordManager;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.input.WeightedSum;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.nnet.comp.neuron.BiasNeuron;
import org.neuroph.nnet.comp.neuron.InputNeuron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.util.ConnectionFactory;
import org.neuroph.util.LayerFactory;
import org.neuroph.util.NeuralNetworkFactory;
import org.neuroph.util.NeuronProperties;

public class ScoringNetwork extends NeuralNetwork<LMS> implements Serializable {

  public static final transient String CACHE_FILE_NAME = "nn.ser";
  @Serial
  private static final long serialVersionUID = 1L;

  private static final transient int HIDDEN_LAYERS = 4;
  private static final transient int HIDDEN_LAYER_NEURONS = 50;
  private static final transient double LEARNING_RATE = 0.000001;
  private static final transient double MIN_ERROR_CHANGE = 0.001;
  private static final transient int SEED = 123456;

  private final Lock lock = new ReentrantLock();
  private final Random random = new Random(SEED);

  private double error;

  public ScoringNetwork() {
    this(HIDDEN_LAYERS, HIDDEN_LAYER_NEURONS, LEARNING_RATE, MIN_ERROR_CHANGE, e -> {});
  }

  public ScoringNetwork(int hiddenLayers,
                        int hiddenLayerNeurons,
                        double learningRate,
                        double minErrorChange,
                        LearningEventListener listener) {

    error = Double.MAX_VALUE;

    // create input layer
    NeuronProperties inputNeuronProperties = new NeuronProperties(InputNeuron.class);
    Layer inputLayer = LayerFactory.createLayer(8, inputNeuronProperties);
    inputLayer.addNeuron(new BiasNeuron());
    for (int i = 0; i < inputLayer.getNeuronsCount(); i++) {
      inputLayer.getNeuronAt(i).setLabel("Input Neuron " + i);
    }
    this.addLayer(inputLayer);

    // Create hidden layer 1
    NeuronProperties hiddenNeuronProperties = new NeuronProperties(Neuron.class,
        WeightedSum.class,
        LeakyRectifiedLinear.class);

    Layer hiddenLayer1 = LayerFactory.createLayer(hiddenLayerNeurons, hiddenNeuronProperties);
    for (int i = 0; i < hiddenLayer1.getNeuronsCount(); i++) {
      hiddenLayer1.getNeuronAt(i).setLabel("Hidden (1) Neuron " + i);
    }
    this.addLayer(hiddenLayer1);

    // Connect input layer to hidden layer 1
    // Set the first input neuron to be 1 and other's to be zero weights,
    //  because this represents that the euclidean distance is the most important value
    ConnectionFactory.fullConnect(inputLayer, hiddenLayer1, 0);
    inputLayer.getNeurons().get(0).getOutConnections().forEach(connection ->
        connection.setWeight(new Weight<>((random.nextDouble() * 0.1) + 0.95)));
    inputLayer.getNeurons().subList(1, inputLayer.getNeuronsCount())
        .forEach(neuron -> neuron.getOutConnections()
            .forEach(connection -> connection.setWeight(new Weight<>(random.nextDouble() * 0.1 - 0.05))));

    // Create other hidden layers

    Layer prev = hiddenLayer1;
    for (int h = 0; h < hiddenLayers - 1; h++) {
      Layer hiddenLayer = LayerFactory.createLayer(hiddenLayerNeurons, hiddenNeuronProperties);
      for (int i = 0; i < hiddenLayer.getNeuronsCount(); i++) {
        hiddenLayer.getNeuronAt(i).setLabel("Hidden (" + (h + 2) + ") Neuron " + i);
      }
      this.addLayer(hiddenLayer);

      // Connect hidden layer 1 to hidden layer 2
      ConnectionFactory.fullConnect(prev, hiddenLayer, 0);
      prev.getNeurons()
          .forEach(neuron -> neuron.getOutConnections()
              .forEach(connection -> connection.setWeight(new Weight<>(random.nextDouble() * 1.4 - 0.7))));

      prev = hiddenLayer; // Set this new hidden layer as previous for next layer's preparations
    }
    // Create output layer
    NeuronProperties outputNeuronProperties = new NeuronProperties(Neuron.class,
        WeightedSum.class,
        LeakyRectifiedLinear.class);

    Layer outputLayer = LayerFactory.createLayer(1, outputNeuronProperties);
    outputLayer.getNeuronAt(0).setLabel("Output Neuron");
    this.addLayer(outputLayer);

    // Connect hidden layer 2 to output layer
    ConnectionFactory.fullConnect(prev, outputLayer, 0);
    hiddenLayer1.getNeurons()
        .forEach(neuron -> neuron.getOutConnections()
            .forEach(connection -> connection.setWeight(new Weight<>(random.nextDouble() * 1.4 - 0.7))));

    NeuralNetworkFactory.setDefaultIO(this);

    BackPropagation backPropagation = new CustomBackPropagation();
    backPropagation.setLearningRate(learningRate);
    backPropagation.setMinErrorChange(minErrorChange);
    backPropagation.setMinErrorChangeIterationsLimit(5);
    backPropagation.setErrorFunction(new MeanSquaredError());
    backPropagation.addListener(listener);
    backPropagation.addListener(event -> {
      BackPropagation lms = (BackPropagation) event.getSource();
      error = lms.getTotalNetworkError();
      if (lms.getCurrentIteration() % 1 == 0) {
//        System.out.println("Weights: ");
//        System.out.println("   " + Arrays.stream(lms.getNeuralNetwork().getWeights())
//            .map(String::valueOf)
//            .collect(Collectors.joining(", ")));
//        System.out.println("Current iteration: " + lms.getCurrentIteration());
//        System.out.println("Error: " + lms.getTotalNetworkError());
      }
    });
    this.setLearningRule(backPropagation);
//
//    System.out.println("Input weights: "
//        + this.getLayerAt(0).getNeurons().stream()
//            .flatMap(neuron -> Arrays.stream(neuron.getWeights()))
//            .map(String::valueOf)
//            .collect(Collectors.joining(", ")));
//
//    System.out.println("Hidden weights: "
//        + this.getLayerAt(1).getNeurons().stream()
//        .flatMap(neuron -> Arrays.stream(neuron.getWeights()))
//        .map(String::valueOf)
//        .collect(Collectors.joining(", ")));
//
//    System.out.println("Output weights: "
//        + this.getLayerAt(2).getNeurons().stream()
//        .flatMap(neuron -> Arrays.stream(neuron.getWeights()))
//        .map(String::valueOf)
//        .collect(Collectors.joining(", ")));

  }

  public void learn(Collection<PathRecordManager.PathTrialCellRecord> records) {
    System.out.println("Learning with " + records.size() + " data points.");
    DataSet dataSet = new DataSet(8, 1);
//    PathRecordManager.PathTrialRecord record = new ArrayList<>(records).get(0);
//    PathRecordManager.PathTrialCellRecord cell = new ArrayList<>(record.cells()).get(0);
//    double[] inputs = new double[]{
//        cell.distance(),
//        cell.distanceY(),
//        cell.y(),
//        record.destinationY(),
//        cell.dimension() == 0 ? 1 : 0,
//        cell.dimension() == 1 ? 1 : 0,
//        cell.dimension() == 2 ? 1 : 0,
//        cell.dimension() == 3 ? 1 : 0
//    };
//    System.out.println("Inputs: ");
//    Arrays.stream(inputs).forEach(System.out::println);
//    double[] output = new double[]{
//        cell.deviation()
//    };
//    System.out.println("Outputs: ");
//    Arrays.stream(output).forEach(System.out::println);
//
//    dataSet.add(inputs, output);
    records.forEach(cell -> {
      double[] inputs = new double[]{
          cell.distance(),
          cell.distanceY(),
          cell.y(),
          cell.record().destinationY(),
          cell.dimension() == 0 ? 1 : 0,
          cell.dimension() == 1 ? 1 : 0,
          cell.dimension() == 2 ? 1 : 0,
          cell.dimension() == 3 ? 1 : 0
      };
//        System.out.println("Inputs: ");
//        Arrays.stream(inputs).forEach(System.out::println);
      double[] output = new double[]{
          cell.deviation()
      };
//        System.out.println("Outputs: ");
//        Arrays.stream(output).forEach(System.out::println);
      dataSet.add(inputs, output);
    });
//    System.out.println("Data Set size: " + dataSet.size());
    dataSet.shuffle(random);
    this.learn(dataSet);
  }

  @Override
  public void learn(DataSet trainingSet) {
    if (lock.tryLock()) {
      super.learn(trainingSet);
      lock.unlock();
    } else {
      System.out.println("This network is already training.");
      return;
    }
  }

  @Override
  public void stopLearning() {
    super.stopLearning();
  }

  public double calculateOutputs(double distance,
                                 int distanceY,
                                 int locationY,
                                 int destinationY,
                                 int dimension) {
    lock.lock();
    this.setInput(distance,
        distanceY,
        locationY,
        destinationY,
        dimension == 0 ? 1 : 0,
        dimension == 1 ? 1 : 0,
        dimension == 2 ? 1 : 0,
        dimension == 3 ? 1 : 0);
    this.calculate();

    double output = this.getOutput()[0];
    lock.unlock();
    return output;
  }

  public double getError() {
    return error;
  }
}
