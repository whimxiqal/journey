/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package me.pietelite.journey.common.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.data.PortDataManager;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.NetherPort;
import me.pietelite.journey.common.navigation.Port;

public class SqlPortDataManager extends SqlManager implements PortDataManager {

  public static final String PORT_TABLE_NAME = "journey_ports";

  public SqlPortDataManager(SqlConnectionController connectionController) {
    super(connectionController);
    createTables();
  }

  private void createTables() {
    try (Connection connection = getConnectionController().establishConnection()) {
      String tableStatement = "CREATE TABLE IF NOT EXISTS "
          + PORT_TABLE_NAME + " ("
          + "origin_domain_id char(36) NOT NULL, "
          + "origin_x int(7) NOT NULL, "
          + "origin_y int(7) NOT NULL, "
          + "origin_z int(7) NOT NULL, "
          + "destination_domain_id char(36) NOT NULL, "
          + "destination_x int(7) NOT NULL, "
          + "destination_y int(7) NOT NULL, "
          + "destination_z int(7) NOT NULL, "
          + "mode_type int(2) NOT NULL, "
          + "cost double(12, 5) NOT NULL"
          + ");";
      connection.prepareStatement(tableStatement).execute();

      String indexStatement = "CREATE INDEX IF NOT EXISTS origin_idx ON "
          + PORT_TABLE_NAME
          + " (origin_domain_id, origin_x, origin_y, origin_z);";
      connection.prepareStatement(indexStatement).execute();

      indexStatement = "CREATE INDEX IF NOT EXISTS destination_id ON "
          + PORT_TABLE_NAME
          + " (destination_domain_id, destination_x, destination_y, destination_z);";
      connection.prepareStatement(indexStatement).execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void addPort(ModeType type, Cell origin, Cell destination, double cost) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
          PORT_TABLE_NAME,
          "origin_domain_id",
          "origin_x",
          "origin_y",
          "origin_z",
          "destination_domain_id",
          "destination_x",
          "destination_y",
          "destination_z",
          "mode_type",
          "cost"));

      statement.setString(1, origin.domainId());
      statement.setInt(2, origin.getX());
      statement.setInt(3, origin.getY());
      statement.setInt(4, origin.getZ());
      statement.setString(5, destination.domainId());
      statement.setInt(6, destination.getX());
      statement.setInt(7, destination.getY());
      statement.setInt(8, destination.getZ());
      statement.setInt(9, type.ordinal());
      statement.setDouble(10, cost);

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public Collection<Port> getPortsWithOrigin(ModeType type, Cell origin) {
    return getPortsWithOneSide(type, origin, "origin");
  }

  @Override
  public Collection<Port> getPortsWithDestination(ModeType type, Cell destination) {
    return getPortsWithOneSide(type, destination, "destination");
  }

  private Collection<Port> getPortsWithOneSide(ModeType type, Cell cell, String cellTypePrefix) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          PORT_TABLE_NAME,
          cellTypePrefix + "_domain_id",
          cellTypePrefix + "_x",
          cellTypePrefix + "_y",
          cellTypePrefix + "_z",
          "mode_type"));

      statement.setString(1, cell.domainId());
      statement.setInt(2, cell.getX());
      statement.setInt(3, cell.getY());
      statement.setInt(4, cell.getZ());
      statement.setInt(5, type.ordinal());

      return extractPorts(statement.executeQuery());
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public Collection<Port> getPorts(ModeType type) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s = ?;",
          PORT_TABLE_NAME,
          "mode_type"));

      statement.setInt(1, type.ordinal());

      return extractPorts(statement.executeQuery());
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public Collection<Port> getAllPorts() {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s;",
          PORT_TABLE_NAME));

      return extractPorts(statement.executeQuery());
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  private Collection<Port> extractPorts(ResultSet resultSet) throws SQLException {
    List<Port> ports = new LinkedList<>();
    while (resultSet.next()) {
      ports.add(new NetherPort(
          new Cell(resultSet.getInt("origin_x"),
              resultSet.getInt("origin_y"),
              resultSet.getInt("origin_z"),
              resultSet.getString("origin_domain_id")),
          new Cell(resultSet.getInt("destination_x"),
              resultSet.getInt("destination_y"),
              resultSet.getInt("destination_z"),
              resultSet.getString("destination_domain_id"))));
    }
    return ports;
  }

  @Override
  public void removePorts(ModeType type, Cell origin, Cell destination) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          PORT_TABLE_NAME,
          "origin_domain_id",
          "origin_x",
          "origin_y",
          "origin_z",
          "destination_domain_id",
          "destination_x",
          "destination_y",
          "destination_z",
          "mode_type"));

      statement.setString(1, origin.domainId());
      statement.setInt(2, origin.getX());
      statement.setInt(3, origin.getY());
      statement.setInt(4, origin.getZ());
      statement.setString(5, destination.domainId());
      statement.setInt(6, destination.getX());
      statement.setInt(7, destination.getY());
      statement.setInt(8, destination.getZ());
      statement.setInt(9, type.ordinal());

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public void removePorts(ModeType type) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s = ?;",
          PORT_TABLE_NAME,
          "mode_type"));

      statement.setInt(1, type.ordinal());

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }
}
