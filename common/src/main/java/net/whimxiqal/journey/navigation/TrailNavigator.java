/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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
 */

package net.whimxiqal.journey.navigation;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.math.Vector;
import net.whimxiqal.journey.navigation.option.NavigatorOption;
import net.whimxiqal.journey.navigation.option.NavigatorOptionValues;
import net.whimxiqal.journey.navigation.option.ParseNavigatorOptionException;
import net.whimxiqal.journey.search.SearchStep;
import net.whimxiqal.journey.Color;
import net.whimxiqal.journey.util.ColorUtil;
import net.whimxiqal.journey.util.Permission;

public class TrailNavigator implements Navigator {

  public static final String TRAIL_NAVIGATOR_ID = "trail";
  private static final double CACHED_JOURNEY_STEPS_LENGTH = 128;  // length of all journey steps to cache for showing their particles
  private static final int TICKS_PER_PARTICLE_CYCLE = 3;
  public static final String OPTION_ID_PARTICLE = "particle";
  public static final NavigatorOption<List<String>> OPTION_PARTICLE = NavigatorOption
      .builder(OPTION_ID_PARTICLE, (Class<List<String>>) (Object) List.class)
      .parser(val -> {
        if (!Journey.get().proxy().platform().isValidParticleType(val)) {
          throw new ParseNavigatorOptionException("Unknown particle: " + val, 0);
        }
        return Collections.singletonList(val);
      }) // Only support single values in lists for now
      .defaultValue(Settings.DEFAULT_TRAIL_PARTICLE::getValue)
      .valueSuggestions(() -> Journey.get().proxy().platform().particleTypes())
      .permission(Permission.FLAG_NAVIGATOR_TRAIL_PARTICLE_OPTION.path())
      .valuePermission(particles -> {
        if (particles.isEmpty()) {
          return Permission.FLAG_NAVIGATOR_TRAIL_PARTICLE_OPTION.path();
        } else {
          return Permission.FLAG_NAVIGATOR_TRAIL_PARTICLE_OPTION.path(particles.get(0));  // we assume only one particle
        }
      })
      .build();
  public static final String OPTION_ID_COLOR = "color";
  public static final NavigatorOption<List<Color>> OPTION_COLOR = NavigatorOption
      .builder(OPTION_ID_COLOR, (Class<List<Color>>) (Object) List.class)
      .parser(val -> {
        try {
          return Collections.singletonList(ColorUtil.fromHex(val));
        } catch (ParseException e) {
          throw new ParseNavigatorOptionException("Color must be an RGB hexadecimal value (ex. ac15db)", 0);
        }
      })
      .defaultValue(Settings.DEFAULT_TRAIL_COLOR::getValue)
      .permission(Permission.FLAG_NAVIGATOR_TRAIL_COLOR_OPTION.path())
      .build();
  public static final String OPTION_ID_WIDTH = "width";
  public static final NavigatorOption<Double> OPTION_WIDTH = NavigatorOption
      .doubleValueBuilder(OPTION_ID_WIDTH, 0.1, 5.0)
      .defaultValue(Settings.DEFAULT_TRAIL_WIDTH::getValue)
      .permission(Permission.FLAG_NAVIGATOR_TRAIL_WIDTH_OPTION.path())
      .build();
  public static final String OPTION_ID_DENSITY = "density";
  public static final NavigatorOption<Double> OPTION_DENSITY = NavigatorOption
      .doubleValueBuilder(OPTION_ID_DENSITY, 1.0, 10.0)
      .defaultValue(Settings.DEFAULT_TRAIL_DENSITY::getValue)
      .permission(Permission.FLAG_NAVIGATOR_TRAIL_DENSITY_OPTION.path())
      .build();
  private static final double PARTICLE_UNIT_DISTANCE = 0.5;  // number of blocks between which particles will be shown
  private static final double PI_TIMES_2 = Math.PI * 2;
  private static final double TRAIL_DENSITY_FACTOR = 0.1;  // arbitrary factor to tune how it actually looks in game
  private static final Vector RANDOM_VECTOR_1 = new Vector(1, 0, 0);
  private static final Vector RANDOM_VECTOR_2 = new Vector(0, 1, 0);

  private final Random random = new Random();
  private final JourneyAgent agent;
  private final NavigationProgress progress;
  private final List<String> trailParticles;
  private final List<Color> trailColors;
  private final double trailWidth;
  private final double trailDensity;
  private UUID illuminationTaskId;

  public TrailNavigator(JourneyAgent agent, NavigationProgress progress, NavigatorOptionValues optionValues) {
    this.agent = agent;
    this.progress = progress;
    this.trailParticles = optionValues.value(OPTION_PARTICLE);
    this.trailColors = optionValues.value(OPTION_COLOR);
    this.trailWidth = optionValues.value(OPTION_WIDTH);
    this.trailDensity = optionValues.value(OPTION_DENSITY);
  }

  @Override
  public boolean start() {
    // Set up illumination scheduled task for showing the paths
    illuminationTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
      // Illuminate destination of path
      List<? extends SearchStep> steps = progress.steps();

      // Illuminate the rest of the path
      final int firstStepIndex = Math.max(1, progress.currentStepIndex());
      int stepIndex = firstStepIndex;
      double illuminatedDistance = 0;
      while (illuminatedDistance <= CACHED_JOURNEY_STEPS_LENGTH
          && stepIndex < steps.size()
          && steps.get(stepIndex - 1).location().domain() == steps.get(stepIndex).location().domain()) {
        NavigationStep step = new NavigationStep(steps.get(stepIndex - 1).location(), steps.get(stepIndex).location());
        double stepProgress = stepIndex == firstStepIndex ? progress.currentStepProgress() : 0;
        illuminateStep(step, stepProgress);
        illuminatedDistance += step.length() * (1 - stepProgress);
        stepIndex++;
      }
    }, false, TICKS_PER_PARTICLE_CYCLE);
    return true;
  }

  @Override
  public boolean shouldStop() {
    return false;
  }

  @Override
  public void stop() {
    Journey.get().proxy().schedulingManager().cancelTask(illuminationTaskId);
  }

  private void illuminateStep(NavigationStep step, double startingPortion) {
    double distance = startingPortion * step.length();
    final double startDistance = distance;
    final Vector unitPath = step.path().unit();
    // Add 0.5 to all locations to put center of particle cylinders in the center of the cell
    double curX = step.startVector().x() + unitPath.x() * distance + 0.5;
    double curY = step.startVector().y() + unitPath.y() * distance + 0.5;
    double curZ = step.startVector().z() + unitPath.z() * distance + 0.5;
    double offPathVec;
    double offRadius;  // offset away from center of cross-section
    double offAngle;
    double offVec1;
    double offVec2;
    final double deltaX = unitPath.x() * PARTICLE_UNIT_DISTANCE;
    final double deltaY = unitPath.y() * PARTICLE_UNIT_DISTANCE;
    final double deltaZ = unitPath.z() * PARTICLE_UNIT_DISTANCE;

    final double countPerCycle = trailDensity * trailWidth * trailWidth * PARTICLE_UNIT_DISTANCE * TRAIL_DENSITY_FACTOR;
    final double countCeil = Math.ceil(countPerCycle);
    final double particleProbability = countPerCycle / countCeil;
    final double crossSectionRadius = trailWidth / 2;

    // calculate orthogonal vectors.

    // 1. choose vector most different from path vector to get accurate cross product
    Vector leastSimilarRandomVector;
    double randomVector1Dot = Math.abs(unitPath.dot(RANDOM_VECTOR_1));
    double randomVector2Dot = Math.abs(unitPath.dot(RANDOM_VECTOR_2));
    if (randomVector1Dot < randomVector2Dot) {
      leastSimilarRandomVector = RANDOM_VECTOR_1;
    } else {
      leastSimilarRandomVector = RANDOM_VECTOR_2;
    }
    Vector orthogonalUnit1 = unitPath.cross(leastSimilarRandomVector).unit();
    Vector orthogonalUnit2 = unitPath.cross(orthogonalUnit1).unit();

    // Stop if we reach the end OR the total distance we are displaying is longer than the supposed cached length size (so we're not showing unnecessary particles too far ahead)
    while (distance < step.length() && (distance - startDistance) < TrailNavigator.CACHED_JOURNEY_STEPS_LENGTH) {
      // for the number of times dictated by the input "density", spawn a particle at a random location,
      // spread out as far as the width dictates but only forward as far as the PARTICLE_UNIT_DISTANCE
      for (double i = 0; i < countCeil; i += 1.0) {
        if (random.nextDouble() > particleProbability) {
          // ignore this one, so on average we get countCeil * particleProbability = countPerCycle particles
          continue;
        }
        offPathVec = random.nextDouble() * PARTICLE_UNIT_DISTANCE;
        offRadius = crossSectionRadius * random.nextDouble();
        offAngle = random.nextDouble() * PI_TIMES_2;
        offVec1 = Math.sin(offAngle) * offRadius;
        offVec2 = Math.cos(offAngle) * offRadius;

        Journey.get().proxy().platform().spawnParticle(agent.uuid(),
            trailParticles.get(random.nextInt(trailParticles.size())),
            trailColors.get(random.nextInt(trailColors.size())),
            step.domain(),
            curX + (unitPath.x() * offPathVec) + (orthogonalUnit1.x() * offVec1) + (orthogonalUnit2.x() * offVec2),
            curY + (unitPath.y() * offPathVec) + (orthogonalUnit1.y() * offVec1) + (orthogonalUnit2.y() * offVec2),
            curZ + (unitPath.z() * offPathVec) + (orthogonalUnit1.z() * offVec1) + (orthogonalUnit2.z() * offVec2));
      }
      curX += deltaX;
      curY += deltaY;
      curZ += deltaZ;
      distance += PARTICLE_UNIT_DISTANCE;
    }
  }

}
