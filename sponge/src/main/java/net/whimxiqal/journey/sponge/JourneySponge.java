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

package net.whimxiqal.journey.sponge;

import com.google.inject.Inject;
import java.nio.file.Path;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.ProxyImpl;
import net.whimxiqal.journey.command.JourneyConnectorProvider;
import net.whimxiqal.journey.sponge.listener.DeathListener;
import net.whimxiqal.journey.sponge.listener.NetherListener;
import net.whimxiqal.journey.sponge.listener.PlayerListener;
import net.whimxiqal.journey.sponge.util.SpongeLogger;
import net.whimxiqal.journey.sponge.util.SpongeSchedulingManager;
import net.whimxiqal.mantle.common.CommandRegistrar;
import net.whimxiqal.mantle.sponge9.Sponge9RegistrarProvider;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("journey")
public final class JourneySponge {

  private static JourneySponge instance;

  /**
   * Get the instance that is currently run on the Spigot server.
   *
   * @return the instance
   */
  public static JourneySponge get() {
    return instance;
  }

  @Inject
  private Logger logger;

  @Inject
  private PluginContainer pluginContainer;

  @Inject
  @ConfigDir(sharedRoot = false)
  private Path configDir;

  @Listener
  public void onStartedServer(final StartedEngineEvent<Server> event) {
    instance = this;
    logger.info("Initializing Journey...");

    if (configDir.toFile().mkdirs()) {
      logger.info("Journey data folder created");
    }

    // API
    JourneySpongeApiSupplier.set(new JourneySpongeApiImpl());

    Journey.create();
    // Set up Journey Proxy
    ProxyImpl proxy = new ProxyImpl();
    Journey.get().registerProxy(proxy);
    proxy.logger(new SpongeLogger());
    proxy.dataFolder(configDir);
    proxy.audienceProvider(new SpongeAudiences());
    proxy.configPath(configDir.resolve("config.yml"));
    proxy.schedulingManager(new SpongeSchedulingManager());
    proxy.platform(new SpongePlatformProxy());
    proxy.version(pluginContainer.metadata().version().toString());

    // Initialize common Journey (after proxy is set up)
    boolean failed = false;
    try {
      if (!Journey.get().init()) {
        failed = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
      failed = true;
    }

    if (failed) {
      Journey.get().shutdown();
      Journey.remove();
      instance = null;
      return;
    }

    Sponge.eventManager().registerListeners(pluginContainer, new NetherListener());
    Sponge.eventManager().registerListeners(pluginContainer, new PlayerListener());
    Sponge.eventManager().registerListeners(pluginContainer, new DeathListener());

    // TODO enable
//    if (Settings.EXTRA_CHECK_LATEST_VERSION_ON_STARTUP.getValue()) {
//      Request.evaluateVersionAge("sponge", proxy.version());
//    }
//    if (Settings.EXTRA_FIND_INTEGRATIONS_ON_STARTUP.getValue()) {
//      Request.checkForIntegrationPlugins("sponge",
//          Sponge.version?,
//          Arrays.stream(Sponge.pluginManager().plugins())
//              .map(Plugin::getName)
//              .collect(Collectors.toSet()));
//    }
  }

  @Listener
  public void onRegisterCommand(RegisterCommandEvent<Command.Raw> event) {
    // Register command
    CommandRegistrar registrar = Sponge9RegistrarProvider.get(pluginContainer, event);
    registrar.register(JourneyConnectorProvider.connector());
  }

  @Listener
  public void onServerStopping(StoppingEngineEvent<Server> event) {
    Journey.get().shutdown();
    Journey.remove();
    instance = null;
  }

  public Logger logger() {
    return logger;
  }

  public PluginContainer container() {
    return pluginContainer;
  }
}
