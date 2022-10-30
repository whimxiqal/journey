package me.pietelite.journey.common.search.event;

import java.util.UUID;
import me.pietelite.journey.common.Journey;

public final class SearchDispatcherImpl extends SearchDispatcher {
  private UUID dispatchTaskUuid;

  public void init() {
    dispatchTaskUuid = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
      while (!events.isEmpty()) {
        dispatchHelper(events.remove());
      }
    }, false, 1);
  }

  /**
   * A method used to dispatch search events throughout the operation of
   * the search method in this class. Ultimately, a counterpart to this
   * event will be dispatched to the appropriate event handling system implemented
   * in a given Minecraft mod, e.g. Bukkit/Spigot's event handling system.
   *
   * @param event a search event
   * @param <S>   the type of event
   */
  private <S extends SearchEvent> void dispatchHelper(S event) {
    if (eventConversions.containsKey(event.type())) {
      this.externalDispatcher.accept(eventConversions.get(event.type()).convert(event));
    } else {
      this.externalDispatcher.accept(event);
    }
  }

  public void shutdown() {
    Journey.get().proxy().schedulingManager().cancelTask(dispatchTaskUuid);
  }

}
