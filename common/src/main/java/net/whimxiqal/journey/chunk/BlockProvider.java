package net.whimxiqal.journey.chunk;

import java.util.concurrent.ExecutionException;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.proxy.JourneyBlock;

/**
 * A provider that retrieves {@link JourneyBlock}s from the server engine given a
 * geometric grid {@link Cell}.
 */
public interface BlockProvider {

  /**
   * Convert a {@link JourneyBlock} into a {@link Cell} asynchronously by querying the server engine
   * in a thread-safe manner.
   *
   * @param cell the cell
   * @return the block
   * @throws ExecutionException   if an error occurred during the async operation to get the block
   * @throws InterruptedException if the async operation to get the block was interrupted
   */
  JourneyBlock getBlock(Cell cell) throws ExecutionException, InterruptedException;

}
