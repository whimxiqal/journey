package net.whimxiqal.journey.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class UUIDUtil {

  public static byte[] uuidToBytes(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

  public static UUID bytesToUuid(byte[] bytes) {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    long mostSignificant = bb.getLong();
    long leastSignificant = bb.getLong();
    return new UUID(mostSignificant, leastSignificant);
  }

}
