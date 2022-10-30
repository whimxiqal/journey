package me.pietelite.journey.common.integration;

import me.pietelite.mantle.common.CommandSource;

public interface Integrator {

  String name();

  Scope scope(CommandSource src);

}
