/* *****************************************************************************
 *
 *
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package edu.whimc.journey.common.ml;

import edu.whimc.journey.common.data.PathReportManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * Example: Train a network to reproduce certain mathematical functions, and plot the results.
 * Plotting of the network output occurs every 'plotFrequency' epochs. Thus, the plot shows the accuracy of the network
 * predictions as training progresses.
 * A number of mathematical functions are implemented here.
 * Note the use of the identity function on the network output layer, for regression
 *
 * @author Alex Black
 */
public class NeuralNetwork implements Serializable {

  //Batch size: i.e., each epoch has nSamples/batchSize parameter updates
  public static final int batchSize = 100;
  //Network learning rate
  public static final double learningRate = 0.01;
  public static final int numInputs = 8;
  public static final String CACHE_FILE_NAME = "nn.ser";
  private static final int numOutputs = 1;
  public static final transient Random rng = new Random();
  //Number of data points
  private final MultiLayerNetwork net;
  private final transient Lock lock = new ReentrantLock();

  public NeuralNetwork() {

    final int numHiddenNodes = 50;
    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .weightInit(WeightInit.XAVIER)
        .updater(new Nesterovs(learningRate, 0.9))
        .list()
        .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
            .activation(Activation.SIGMOID).build())
        .layer(new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
            .activation(Activation.SIGMOID).build())
        .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
            .activation(Activation.IDENTITY)
            .nIn(numHiddenNodes).nOut(numOutputs).build())
        .build();

    //Create the network
    this.net = new MultiLayerNetwork(conf);
    this.net.init();
    this.net.setListeners(new ScoreIterationListener(1));
  }

  public boolean train(Collection<PathReportManager.PathTrialRecord> records, int duration) {
    if (lock.tryLock()) {
      //Generate the training data
      List<PathReportManager.PathTrialCellRecord> cellList = records.stream()
          .flatMap(record -> record.cells().stream())
          .collect(Collectors.toCollection(ArrayList::new));

      final INDArray features = Nd4j.zeros(cellList.size(), 8);
      for (int i = 0; i < cellList.size(); i++) {
        // Distance to destination
        features.put(i, 0, cellList.get(i).distance());
        // Y distance to destination
        features.put(i, 1, cellList.get(i).distanceY());
        // Y location
        features.put(i, 2, cellList.get(i).y());
        // Destination y location
        features.put(i, 3, cellList.get(i).record().destinationY());
        // Dimensions (s)
        features.put(i, 4, cellList.get(i).dimension() == 0 ? 1 : 0);
        features.put(i, 5, cellList.get(i).dimension() == 1 ? 1 : 0);
        features.put(i, 6, cellList.get(i).dimension() == 2 ? 1 : 0);
        features.put(i, 7, cellList.get(i).dimension() == 3 ? 1 : 0);
      }

      final INDArray labels = Nd4j.zeros(cellList.size(), 1);
      for (int i = 0; i < cellList.size(); i++) {
        features.put(i, 0, cellList.get(i).deviation());
      }

      final DataSet allData = new DataSet(features, labels);

      final List<DataSet> list = allData.asList();
      Collections.shuffle(list, rng);
      final DataSetIterator iterator = new ListDataSetIterator<>(list, batchSize);

      //Train the network on the full data set, and evaluate in periodically
      long endTime = System.currentTimeMillis() + duration;
      while (System.currentTimeMillis() < endTime) {
        iterator.reset();
        net.fit(iterator);
      }
      lock.unlock();
      return true;
    } else {
      return false;
    }
  }

  public double getDeviationOf(double distance,
                            int distanceY,
                            int locationY,
                            int destinationY,
                            int dimension) {
    return net.output(Nd4j.createFromArray(distance,
            distanceY, locationY, destinationY, dimension == 0 ? 1 : 0,
            dimension == 1 ? 1 : 0,
            dimension == 2 ? 1 : 0,
            dimension == 3 ? 1 : 0), false)
        .getDouble(0);
  }

}