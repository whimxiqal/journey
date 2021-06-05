/*
 * MIT License
 *
 * Copyright (c) 2020 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package edu.whimc.indicator.config;

import edu.whimc.indicator.common.data.DataType;

/**
 * An enumeration of all Settings. No need to register anywhere, that's done dynamically.
 */
public final class Settings {

  public static final Setting<Integer> DEFAULT_SEARCH_TIMEOUT = new Setting<>("search.timeout", 15, Integer.class);
  public static final Setting<String> STORAGE_ADDRESS = new Setting<>("storage.address", "whimcproject.web.illinois.edu", String.class);
  public static final Setting<String> STORAGE_DATABASE = new Setting<>("storage.database", "db", String.class);
  public static final Setting<String> STORAGE_USERNAME = new Setting<>("storage.username", "username", String.class);
  public static final Setting<String> STORAGE_PASSWORD = new Setting<>("storage.password", "p@ssword", String.class);
  public static final Setting<DataType> CUSTOM_ENDPOINT_STORAGE_TYPE = new Setting<>("storage.custom_endpoint_type", DataType.SQLite, DataType.class);
  public static final Setting<DataType> SERVER_ENDPOINT_STORAGE_TYPE = new Setting<>("storage.server_endpoint_type", DataType.SQLite, DataType.class);


  private Settings() {
  }

}
