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

import edu.whimc.journey.common.data.PathReportManager;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.input.WeightedSum;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.core.transfer.Linear;
import org.neuroph.core.transfer.RectifiedLinear;
import org.neuroph.nnet.comp.neuron.BiasNeuron;
import org.neuroph.nnet.comp.neuron.InputNeuron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.ConnectionFactory;
import org.neuroph.util.LayerFactory;
import org.neuroph.util.NeuralNetworkFactory;
import org.neuroph.util.NeuronProperties;

public class ScoringNetwork extends NeuralNetwork<LMS> implements Serializable {

  private static final long serialVersionUID = 3L;
  private static final transient int seed = 123456;
  public static final transient String CACHE_FILE_NAME = "nn.ser";
  private final transient Lock lock = new ReentrantLock();
  private final transient Random random = new Random(seed);

  public ScoringNetwork() {
    // create input layer
    NeuronProperties inputNeuronProperties = new NeuronProperties(InputNeuron.class, Linear.class);
    Layer inputLayer = LayerFactory.createLayer(8, inputNeuronProperties);
    inputLayer.addNeuron(new BiasNeuron());
    this.addLayer(inputLayer);

    NeuronProperties hiddenNeuronProperties = new NeuronProperties(Neuron.class,
        WeightedSum.class,
        RectifiedLinear.class);

    Layer hiddenLayer = LayerFactory.createLayer(50, hiddenNeuronProperties);
    hiddenLayer.addNeuron(new BiasNeuron());
    this.addLayer(hiddenLayer);

    // Set the first input neuron to be 1 and other's to be zero weights,
    //  because this represents that the euclidean distance is the most important value
    ConnectionFactory.fullConnect(inputLayer, hiddenLayer, 0);
    inputLayer.getNeurons().get(0).getOutConnections().forEach(connection ->
        connection.setWeight(new Weight<>((random.nextDouble() * 0.05) + 0.95)));
    inputLayer.getNeurons().subList(1, inputLayer.getNeuronsCount())
        .forEach(neuron -> neuron.getOutConnections()
            .forEach(connection -> connection.setWeight(new Weight<>(random.nextDouble() * 0.05))));

    NeuronProperties outputNeuronProperties = new NeuronProperties(Neuron.class,
        WeightedSum.class,
        RectifiedLinear.class);

    Layer outputLayer = LayerFactory.createLayer(1, outputNeuronProperties);
    this.addLayer(outputLayer);
    ConnectionFactory.fullConnect(hiddenLayer, outputLayer, 0);
    hiddenLayer.getNeurons()
        .forEach(neuron -> neuron.getOutConnections()
            .forEach(connection -> connection.setWeight(new Weight<>(random.nextDouble() * 0.7))));

    NeuralNetworkFactory.setDefaultIO(this);

    BackPropagation backPropagation = new MomentumBackpropagation();
    backPropagation.setLearningRate(0.001);
    backPropagation.setMaxError(1);
    backPropagation.setErrorFunction(new MeanSquaredError());

    this.setLearningRule(backPropagation);
    this.getLearningRule().addListener(event -> {
      BackPropagation lms = (BackPropagation) event.getSource();
      if (lms.getCurrentIteration() < 10 || lms.getCurrentIteration() % 10 == 0) {
        System.out.println("Weights: ");
        System.out.println("   " + Arrays.stream(lms.getNeuralNetwork().getWeights())
            .map(String::valueOf)
            .collect(Collectors.joining(", ")));
        System.out.println("Current iteration: " + lms.getCurrentIteration());
        System.out.println("Error: " + lms.getTotalNetworkError());
      }
    });

    System.out.println("Input weights: "
        + this.getLayerAt(0).getNeurons().stream()
            .flatMap(neuron -> Arrays.stream(neuron.getWeights()))
            .map(String::valueOf)
            .collect(Collectors.joining(", ")));

    System.out.println("Hidden weights: "
        + this.getLayerAt(1).getNeurons().stream()
        .flatMap(neuron -> Arrays.stream(neuron.getWeights()))
        .map(String::valueOf)
        .collect(Collectors.joining(", ")));

    System.out.println("Output weights: "
        + this.getLayerAt(2).getNeurons().stream()
        .flatMap(neuron -> Arrays.stream(neuron.getWeights()))
        .map(String::valueOf)
        .collect(Collectors.joining(", ")));

  }

  public void learn(Collection<PathReportManager.PathTrialRecord> records) {
    DataSet dataSet = new DataSet(8, 1);
//    PathReportManager.PathTrialRecord record = new ArrayList<>(records).get(0);
//    PathReportManager.PathTrialCellRecord cell = new ArrayList<>(record.cells()).get(0);
//    dataSet.add(new double[]{
//            cell.distance(),
//            cell.distanceY(),
//            cell.y(),
//            record.destinationY(),
//            cell.dimension() == 0 ? 1 : 0,
//            cell.dimension() == 1 ? 1 : 0,
//            cell.dimension() == 2 ? 1 : 0,
//            cell.dimension() == 3 ? 1 : 0
//        },
//        new double[]{
//            cell.deviation()
//        });
    records.forEach(record -> {
      record.cells().forEach(cell -> {
        dataSet.add(new double[]{
                cell.distance(),
                cell.distanceY(),
                cell.y(),
                record.destinationY(),
                cell.dimension() == 0 ? 1 : 0,
                cell.dimension() == 1 ? 1 : 0,
                cell.dimension() == 2 ? 1 : 0,
                cell.dimension() == 3 ? 1 : 0
            },
            new double[]{
                cell.deviation()
            });
      });
    });
    System.out.println("Data Set size: " + dataSet.size());
    this.learn(dataSet);
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

}
