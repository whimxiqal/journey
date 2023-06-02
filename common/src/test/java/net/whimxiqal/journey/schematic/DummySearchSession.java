package net.whimxiqal.journey.schematic;

import net.whimxiqal.journey.search.SearchSession;

public class DummySearchSession extends SearchSession {
  protected DummySearchSession() {
    super(SchematicSearchTests.PLAYER.uuid(), Caller.PLAYER);
  }

  @Override
  protected void asyncSearch() {
    // do nothing
  }
}
