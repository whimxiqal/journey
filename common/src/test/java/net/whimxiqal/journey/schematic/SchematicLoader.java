package net.whimxiqal.journey.schematic;


import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.function.Supplier;

public class SchematicLoader implements Supplier<Clipboard> {

  private Clipboard clipboard;

  @Override
  public Clipboard get() {
    return clipboard;
  }

  public void printLevel(int y, int minX, int maxX, int minZ, int maxZ) {
    System.out.printf("Printing altitude %d, x: [%d,%d), z: [%d,%d)", y, minX, maxX, minZ, maxZ);
    for (int z = minZ - 1; z <= maxZ; z++) {
      for (int x = minX - 1; x <= maxX; x++) {
        if (x == minX - 1 || x == maxX || z == minZ - 1 || z == maxZ) {
          System.out.print("+");
          continue;
        }
        BlockType type = clipboard.getBlock(BlockVector3.at(x, y, z)).getBlockType();
        System.out.print(type.equals(BlockTypes.AIR) ? " " : "O");
      }
      System.out.println();
    }
  }

  boolean load(String schematic) {
    String resourceLocation = "/schematics/" + schematic;
    URL resourceUrl = SchematicLoader.class.getResource(resourceLocation);
    if (resourceUrl == null) {
      System.out.println("Could not find resource at location: " + resourceLocation);
      return false;
    }
    File file = new File(resourceUrl.getFile());
    ClipboardFormat format = ClipboardFormats.findByFile(file);
    if (format == null) {
      System.out.println("WorldEdit could not find the format of file: " + file.getPath());
      return false;
    }

    try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
      clipboard = reader.read();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    if (clipboard == null) {
      System.out.println("The read clipboard was null");
      return false;
    }
    return true;
  }

}
