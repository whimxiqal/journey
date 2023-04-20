package net.whimxiqal.journey.util;

import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UUIDUtilTest {

  @Test
  public void serializeUuid() {
    UUID uuid = UUID.randomUUID();
    Assertions.assertEquals(uuid, UUIDUtil.bytesToUuid(UUIDUtil.uuidToBytes(uuid)));
  }

}