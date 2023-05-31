package net.whimxiqal.journey.real;


import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import net.whimxiqal.journey.Journey;

public class SchematicLoader {

  void doSomething() {
    Clipboard clipboard = load("blah");
    clipboard.getRegion().getWorld().getBlock(null).getBlockType();
  }

  Clipboard load(String schematic) {
    String resourceLocation = "/schematics/" + schematic;
    URL resourceUrl = SchematicLoader.class.getResource(resourceLocation);
    if (resourceUrl == null) {
      Journey.logger().error("Could not find resource at location: " + resourceLocation);
      return null;
    }
    File file = new File(resourceUrl.getFile());
    ClipboardFormat format = ClipboardFormats.findByFile(file);
    if (format == null) {
      Journey.logger().error("WorldEdit could not find the file: " + file.getPath());
      return null;
    }

    try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
      return reader.read();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
