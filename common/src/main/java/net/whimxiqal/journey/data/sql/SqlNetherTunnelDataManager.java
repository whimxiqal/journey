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

package net.whimxiqal.journey.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.data.NetherTunnelDataManager;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.navigation.NetherTunnel;

public class SqlNetherTunnelDataManager extends SqlManager implements NetherTunnelDataManager {

  public static final String NETHER_TUNNEL_TABLE_NAME = "journey_nether_tunnels";

  public SqlNetherTunnelDataManager(SqlConnectionController connectionController) {
    super(connectionController);
    createTables();
  }

  private void createTables() {
    try (Connection connection = getConnectionController().establishConnection()) {
      String tableStatement = "CREATE TABLE IF NOT EXISTS "
          + NETHER_TUNNEL_TABLE_NAME + " ("
          + "origin_domain_id char(36) NOT NULL, "
          + "origin_x int(7) NOT NULL, "
          + "origin_y int(7) NOT NULL, "
          + "origin_z int(7) NOT NULL, "
          + "destination_domain_id char(36) NOT NULL, "
          + "destination_x int(7) NOT NULL, "
          + "destination_y int(7) NOT NULL, "
          + "destination_z int(7) NOT NULL"
          + ");";
      connection.prepareStatement(tableStatement).execute();

      String indexStatement = "CREATE INDEX IF NOT EXISTS origin_idx ON "
          + NETHER_TUNNEL_TABLE_NAME
          + " (origin_domain_id, origin_x, origin_y, origin_z);";
      connection.prepareStatement(indexStatement).execute();

      indexStatement = "CREATE INDEX IF NOT EXISTS destination_id ON "
          + NETHER_TUNNEL_TABLE_NAME
          + " (destination_domain_id, destination_x, destination_y, destination_z);";
      connection.prepareStatement(indexStatement).execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void addTunnel(Cell origin, Cell destination, double cost) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
          NETHER_TUNNEL_TABLE_NAME,
          "origin_domain_id",
          "origin_x",
          "origin_y",
          "origin_z",
          "destination_domain_id",
          "destination_x",
          "destination_y",
          "destination_z"));

      statement.setString(1, origin.domainId());
      statement.setInt(2, origin.blockX());
      statement.setInt(3, origin.blockY());
      statement.setInt(4, origin.blockZ());
      statement.setString(5, destination.domainId());
      statement.setInt(6, destination.blockX());
      statement.setInt(7, destination.blockY());
      statement.setInt(8, destination.blockZ());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public Collection<Tunnel> getTunnelsWithOrigin(Cell origin) {
    return getPortsWithOneSide( origin, "origin");
  }

  @Override
  public Collection<Tunnel> getTunnelsWithDestination(Cell destination) {
    return getPortsWithOneSide(destination, "destination");
  }

  private Collection<Tunnel> getPortsWithOneSide(Cell cell, String cellTypePrefix) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          NETHER_TUNNEL_TABLE_NAME,
          cellTypePrefix + "_domain_id",
          cellTypePrefix + "_x",
          cellTypePrefix + "_y",
          cellTypePrefix + "_z"));

      statement.setString(1, cell.domainId());
      statement.setInt(2, cell.blockX());
      statement.setInt(3, cell.blockY());
      statement.setInt(4, cell.blockZ());

      return extractTunnels(statement.executeQuery());
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public Collection<Tunnel> getAllTunnels() {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s;",
          NETHER_TUNNEL_TABLE_NAME));

      return extractTunnels(statement.executeQuery());
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  private Collection<Tunnel> extractTunnels(ResultSet resultSet) throws SQLException {
    List<Tunnel> tunnels = new LinkedList<>();
    while (resultSet.next()) {
      tunnels.add(new NetherTunnel(
          new Cell(resultSet.getInt("origin_x"),
              resultSet.getInt("origin_y"),
              resultSet.getInt("origin_z"),
              resultSet.getString("origin_domain_id")),
          new Cell(resultSet.getInt("destination_x"),
              resultSet.getInt("destination_y"),
              resultSet.getInt("destination_z"),
              resultSet.getString("destination_domain_id"))));
    }
    return tunnels;
  }

  @Override
  public void removeTunnels(Cell origin, Cell destination) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          NETHER_TUNNEL_TABLE_NAME,
          "origin_domain_id",
          "origin_x",
          "origin_y",
          "origin_z",
          "destination_domain_id",
          "destination_x",
          "destination_y",
          "destination_z"));

      statement.setString(1, origin.domainId());
      statement.setInt(2, origin.blockX());
      statement.setInt(3, origin.blockY());
      statement.setInt(4, origin.blockZ());
      statement.setString(5, destination.domainId());
      statement.setInt(6, destination.blockX());
      statement.setInt(7, destination.blockY());
      statement.setInt(8, destination.blockZ());

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public void removeTunnels() {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s;",
          NETHER_TUNNEL_TABLE_NAME));

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }
}
