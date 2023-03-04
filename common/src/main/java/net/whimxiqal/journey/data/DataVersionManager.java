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
