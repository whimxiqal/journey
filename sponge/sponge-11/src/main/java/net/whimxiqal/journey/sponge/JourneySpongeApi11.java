package net.whimxiqal.journey.sponge;

import com.google.inject.Inject;
import java.nio.file.Path;
import net.whimxiqal.journey.AssetVersion;
import net.whimxiqal.journey.command.JourneyConnectorProvider;
import net.whimxiqal.mantle.common.CommandRegistrar;
import net.whimxiqal.mantle.sponge9.Sponge9RegistrarProvider;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("journey")
public class JourneySpongeApi11 extends JourneySponge {

  @Inject
  JourneySpongeApi11(Logger logger, PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDir, Metrics.Factory metricsFactory) {
    super(logger, pluginContainer, configDir, metricsFactory);
  }

  @Listener
  public void onStartedServer(final StartedEngineEvent<Server> event) {
    super.onStartedServer(event, AssetVersion.MINECRAFT_17);
  }

  @Listener
  public void onServerStopping(StoppingEngineEvent<Server> event) {
    super.onServerStopping(event);
  }

  @Listener
  public void onRegisterCommand(RegisterCommandEvent<Command.Raw> event) {
    // Register command
    CommandRegistrar registrar = Sponge9RegistrarProvider.get(pluginContainer, event);
    registrar.register(JourneyConnectorProvider.connector());
  }

}
