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

package net.whimxiqal.journey.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import net.whimxiqal.journey.Journey;

public enum DataVersion implements Comparable<DataVersion> {


  ERROR(-1),
  V000(0),
  V001(1);

  public static final String VERSION_FILE_NAME = "journeydb.ver";
  private static final Map<Integer, DataVersion> VERSIONS = new HashMap<>();

  static {
    Arrays.stream(DataVersion.values()).forEach(version -> VERSIONS.put(version.internalVersion, version));
  }

  private final int internalVersion;

  DataVersion(int version) {
    if (version > 9999) {
      throw new IllegalArgumentException("We don't support versions above 9999");
    }
    this.internalVersion = version;
  }

  public static DataVersion fromString(String versionString) {
    int parsedVersion;
    try {
      parsedVersion = Integer.parseInt(versionString);
    } catch (NumberFormatException e) {
      Journey.logger().error("The database version file is in the incorrect format.");
      return DataVersion.ERROR;
    }
    DataVersion version = VERSIONS.get(parsedVersion);
    if (version == null) {
      Journey.logger().error("Journey doesn't support a database version " + parsedVersion);
      return DataVersion.ERROR;
    }
    return version;
  }

  /**
   * Returns the non-zero version of the database, or negative if error.
   *
   * @return the version
   */
  static DataVersion version() {
    File versionFile = Journey.get().proxy().dataFolder().resolve(VERSION_FILE_NAME).toFile();
    if (!versionFile.exists()) {
      writeVersion(DataVersion.V000);
    }
    return readVersion();
  }

  static void writeVersion(DataVersion version) {
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
      } else {
        Journey.logger().info("Journey database version file created.");
      }
    }
    try {
      FileWriter writer = new FileWriter(versionFile);
      writer.write(version.printVersion());
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static DataVersion readVersion() {
    File versionFile = Journey.get().proxy().dataFolder().resolve(VERSION_FILE_NAME).toFile();
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

  public static DataVersion latest() {
    return DataVersion.V001;
  }

  public boolean hasError() {
    return internalVersion < 0;
  }

  public String printVersion() {
    if (hasError()) {
      throw new IllegalStateException("The version is in an error state");
    }
    return String.format("%04d", internalVersion);
  }

  @Override
  public String toString() {
    return "DataVersion{" + (hasError() ? "ERROR" : printVersion()) + '}';
  }

}
