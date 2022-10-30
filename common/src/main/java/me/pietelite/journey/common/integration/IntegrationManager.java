package me.pietelite.journey.common.integration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import me.pietelite.journey.common.command.JourneyExecutor;

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
