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

package edu.whimc.journey.spigot.util;

import com.opencsv.CSVWriter;
import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.data.PathRecordManager;
import edu.whimc.journey.common.ml.ScoringNetwork;
import edu.whimc.journey.spigot.JourneySpigot;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Bukkit;
import org.neuroph.nnet.learning.BackPropagation;

public class NetworkTests {

  public static void train() {
    Bukkit.getScheduler().runTaskAsynchronously(JourneySpigot.getInstance(), () -> {
      JourneySpigot.getInstance().getLogger().info("Collecting data for training....");
      Collection<PathRecordManager.PathTrialCellRecord> data = JourneyCommon.getDataManager()
          .getPathRecordManager()
          .getAllCells();
      JourneySpigot.getInstance().getLogger().info("Collected " + data.size() + " data points");
      int[] hiddenLayersArray = new int[]{2, 3, 5, 10};
      int[] hiddenLayerNeuronArray = new int[]{20, 50, 200, 1000};
      double[] learningRateArray = new double[]{0.00001, 0.000001, 0.0000001, 0.00000001, 0.000000001};
      for (int hiddenLayers : hiddenLayersArray) {
        for (int hiddenLayerNeurons : hiddenLayerNeuronArray) {
          for (double learningRate : learningRateArray) {
            double minErrorChange = learningRate * 1000;  // e.g. 0.00001 -> 0.01
            JourneySpigot.getInstance().getLogger().info("Training new journey network for one hour......");
            JourneySpigot.getInstance().getLogger().info("    Hidden Layers: " + hiddenLayers);
            JourneySpigot.getInstance().getLogger().info("    Hidden Layer Neurons: " + hiddenLayerNeurons);
            JourneySpigot.getInstance().getLogger().info("    Learning Rate: " + learningRate);
            JourneySpigot.getInstance().getLogger().info("    Min Error Change: " + minErrorChange);
            final List<String[]> iterations = new LinkedList<>();
            AtomicReference<Integer> learningStopper = new AtomicReference<>();
            final long start = System.currentTimeMillis();
            ScoringNetwork network = new ScoringNetwork(hiddenLayers,
                hiddenLayerNeurons,
                learningRate,
                minErrorChange,
                event -> {
                  BackPropagation lms = (BackPropagation) event.getSource();
                  int i = lms.getCurrentIteration();
                  if (Double.isNaN(lms.getTotalNetworkError())) {
                    JourneySpigot.getInstance().getLogger().info("Found NaN Network Error");
                    iterations.add(new String[]{
                        String.valueOf(hiddenLayerNeurons),
                        String.valueOf(learningRate),
                        String.valueOf(i),
                        String.valueOf(System.currentTimeMillis() - start),
                        String.valueOf(lms.getTotalNetworkError())
                    });
                    Bukkit.getScheduler().cancelTask(learningStopper.get());
                    lms.stopLearning();
                  }
                  if ((i < 10) || (i < 100 && i % 10 == 0) || i % 100 == 0) {
                    iterations.add(new String[]{
                        String.valueOf(hiddenLayerNeurons),
                        String.valueOf(learningRate),
                        String.valueOf(i),
                        String.valueOf(System.currentTimeMillis() - start),
                        String.valueOf(lms.getTotalNetworkError())
                    });
                  }
                });
            learningStopper.set(Bukkit.getScheduler().scheduleSyncDelayedTask(JourneySpigot.getInstance(),
                network::stopLearning,
                20 * 60 * 60 /*1 hour*/));
            network.learn(data);
            JourneySpigot.getInstance().getLogger().info("Stopped training Journey network");
            JourneySpigot.getInstance().getLogger().info("Resulting network error: " + network.getError());
            try (CSVWriter writer = new CSVWriter(new FileWriter(JourneySpigot.getInstance()
                .getDataFolder()
                .getPath() + "/network-tests_"
                + hiddenLayers + "_"
                + hiddenLayerNeurons + "_"
                + learningRate + "_.csv"))) {
              writer.writeAll(iterations);
              JourneySpigot.getInstance().getLogger().info("Saved learning iteration data");
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    });
    JourneySpigot.getInstance().getLogger().info("Done training!");
  }

}
