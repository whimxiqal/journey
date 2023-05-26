package net.whimxiqal.journey.data.version;

import net.whimxiqal.journey.data.DataVersion;
import net.whimxiqal.journey.data.sql.SqlConnectionController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static net.whimxiqal.journey.data.DataManagerImpl.VERSION_TABLE_NAME;
import static net.whimxiqal.journey.data.DataManagerImpl.VERSION_COLUMN_NAME;

public abstract class SqlDataVersionHandler implements DataVersionHandler {

    SqlConnectionController controller;

    SqlDataVersionHandler(SqlConnectionController controller) {
        this.controller = controller;
    }

    @Override
    public void saveDataVersion(DataVersion version) {
        try (Connection connection = controller.establishConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    String.format("INSERT INTO %s (%s) VALUES (%o);",
                            VERSION_TABLE_NAME,
                            VERSION_COLUMN_NAME,
                            version.internalVersion
                    ));
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean runBatch(String filePath) {
        try (Connection connection = controller.establishConnection()) {
            Statement statement = connection.createStatement();
            addBatchesToStatement(filePath, statement);
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addBatchesToStatement(String queryResource, Statement statement) throws SQLException {
        InputStream resourceStream = this.getClass().getResourceAsStream(queryResource);
        if (resourceStream == null) {
            throw new NoSuchElementException("Cannot find resource at path " + queryResource);
        }
        for (String query : new BufferedReader(new InputStreamReader(resourceStream))
                .lines().collect(Collectors.joining("\n")).split(";")) {
            statement.addBatch(query);
        }
    }
}
