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

package net.whimxiqal.journey.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.data.TunnelDataManager;
import net.whimxiqal.journey.data.TunnelType;
import net.whimxiqal.journey.navigation.NetherTunnel;
import net.whimxiqal.journey.util.UUIDUtil;

public class SqlTunnelDataManager extends SqlManager implements TunnelDataManager {

  public static final String NETHER_TUNNEL_TABLE_NAME = "journey_tunnels";

  public SqlTunnelDataManager(SqlConnectionController connectionController) {
    super(connectionController);
  }

  @Override
  public void addTunnel(Cell origin, Cell destination, double cost, TunnelType type) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
          NETHER_TUNNEL_TABLE_NAME,
          "origin_domain_id",
          "origin_x",
          "origin_y",
          "origin_z",
          "destination_domain_id",
          "destination_x",
          "destination_y",
          "destination_z",
          "tunnel_type"));

      statement.setBytes(1, UUIDUtil.uuidToBytes(Journey.get().domainManager().domainId(origin.domain())));
      statement.setInt(2, origin.blockX());
      statement.setInt(3, origin.blockY());
      statement.setInt(4, origin.blockZ());
      statement.setBytes(5, UUIDUtil.uuidToBytes(Journey.get().domainManager().domainId(destination.domain())));
      statement.setInt(6, destination.blockX());
      statement.setInt(7, destination.blockY());
      statement.setInt(8, destination.blockZ());
      statement.setInt(9, type.id());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public Collection<Tunnel> getTunnelsWithOrigin(Cell origin, TunnelType type) {
    return getPortsWithOneSide(origin, "origin", type);
  }

  @Override
  public Collection<Tunnel> getTunnelsWithDestination(Cell destination, TunnelType type) {
    return getPortsWithOneSide(destination, "destination", type);
  }

  private Collection<Tunnel> getPortsWithOneSide(Cell cell, String cellTypePrefix, TunnelType type) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? and %s = ?;",
          NETHER_TUNNEL_TABLE_NAME,
          cellTypePrefix + "_domain_id",
          cellTypePrefix + "_x",
          cellTypePrefix + "_y",
          cellTypePrefix + "_z",
          "tunnel_type"));

      statement.setBytes(1, UUIDUtil.uuidToBytes(Journey.get().domainManager().domainId(cell.domain())));
      statement.setInt(2, cell.blockX());
      statement.setInt(3, cell.blockY());
      statement.setInt(4, cell.blockZ());
      statement.setInt(5, type.id());

      return extractTunnels(statement.executeQuery());
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public Collection<Tunnel> getAllTunnels(TunnelType type) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s = ?;",
          NETHER_TUNNEL_TABLE_NAME,
          "tunnel_type"));

      statement.setInt(1, type.id());

      return extractTunnels(statement.executeQuery());
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  private Collection<Tunnel> extractTunnels(ResultSet resultSet) throws SQLException {
    List<Tunnel> tunnels = new LinkedList<>();
    while (resultSet.next()) {
      TunnelType type = TunnelType.MAP.get(resultSet.getInt("tunnel_type"));
      if (type == null) {
        throw new IllegalStateException("A tunnel with an invalid type was found in the database: " + resultSet.getInt("tunnel_type"));
      }
      switch (type) {
        case NETHER -> tunnels.add(new NetherTunnel(
            new Cell(resultSet.getInt("origin_x"),
                resultSet.getInt("origin_y"),
                resultSet.getInt("origin_z"),
                Journey.get().domainManager().domainIndex(UUIDUtil.bytesToUuid(resultSet.getBytes("origin_domain_id")))),
            new Cell(resultSet.getInt("destination_x"),
                resultSet.getInt("destination_y"),
                resultSet.getInt("destination_z"),
                Journey.get().domainManager().domainIndex(UUIDUtil.bytesToUuid(resultSet.getBytes("destination_domain_id"))))));
        default -> throw new RuntimeException(); // programmer error
      }
    }
    return tunnels;
  }

  @Override
  public void removeTunnels(Cell origin, Cell destination, TunnelType type) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          NETHER_TUNNEL_TABLE_NAME,
          "origin_domain_id",
          "origin_x",
          "origin_y",
          "origin_z",
          "destination_domain_id",
          "destination_x",
          "destination_y",
          "destination_z",
          "tunnel_type"));

      statement.setBytes(1, UUIDUtil.uuidToBytes(Journey.get().domainManager().domainId(origin.domain())));
      statement.setInt(2, origin.blockX());
      statement.setInt(3, origin.blockY());
      statement.setInt(4, origin.blockZ());
      statement.setBytes(5, UUIDUtil.uuidToBytes(Journey.get().domainManager().domainId(destination.domain())));
      statement.setInt(6, destination.blockX());
      statement.setInt(7, destination.blockY());
      statement.setInt(8, destination.blockZ());
      statement.setInt(9, type.id());

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public void removeTunnels(TunnelType type) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s = ?;",
          NETHER_TUNNEL_TABLE_NAME,
          "tunnel_type"));

      statement.setInt(1, type.id());

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }
}
