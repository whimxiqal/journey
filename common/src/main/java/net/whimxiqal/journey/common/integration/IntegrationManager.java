/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package net.whimxiqal.journey.common.integration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.whimxiqal.journey.common.command.JourneyExecutor;

public class IntegrationManager {

  private final Map<String, Integrator> integrators = new HashMap<>();

  public void register(Integrator integrator) {
    String id = integrator.name().toLowerCase(Locale.ENGLISH);
    if (integrators.containsKey(id)) {
      throw new IllegalArgumentException("An integrator with name " + integrator.name() + " already exists");
    }
    if (id.equals(JourneyExecutor.PERSONAL_WAYPOINT_SCOPE) || id.equals(JourneyExecutor.PUBLIC_WAYPOINT_SCOPE)) {
      throw new IllegalArgumentException("That scope name isn't allowed: " + integrator.name());
    }
    integrators.put(id, integrator);
  }

  public Optional<Integrator> integrator(String name) {
    Integrator integrator = integrators.get(name.toLowerCase(Locale.ENGLISH));
    if (integrator != null) {
      return Optional.of(integrator);
    }
    return Optional.empty();
  }

  public Collection<Integrator> integrators() {
    return integrators.values();
  }

}
