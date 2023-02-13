package net.whimxiqal.journey.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import net.whimxiqal.journey.Journey;

public class DataVersionManager {

  public static final String VERSION_FILE_NAME = "journeydb.ver";

  /**
   * Returns the non-zero version of the database, or negative if error.
   * @return the version
   */
  static DataVersion version() {
    File versionFile = Journey.get().proxy().dataFolder().resolve(VERSION_FILE_NAME).toFile();
    if (!versionFile.exists()) {
      boolean created = false;
      try {
        created = versionFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (!created) {
        Journey.logger().error("Could not create database version file in data folder. Please contact the Journey team!");
        return DataVersion.ERROR;
      } else {
        Journey.logger().info("Journey database version file created.");
      }
      try {
        Journey.logger().info("Can write: " + versionFile.canWrite());
        FileWriter writer = new FileWriter(versionFile);
        writer.write(latest().printVersion());
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    Scanner scanner;
    try {
      scanner = new Scanner(versionFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return DataVersion.ERROR;
    }
    if (!scanner.hasNextLine()) {
      Journey.logger().error("The database version file is empty. Please delete your database files and restart.");
      return DataVersion.ERROR;
    }
    DataVersion version = DataVersion.fromString(scanner.nextLine());
    if (version == DataVersion.ERROR) {
      Journey.logger().error("Journey's database is in an error state. Please delete your database files and restart.");
    }
    return version;
  }

  static DataVersion latest() {
    return DataVersion.V001;
  }

}
