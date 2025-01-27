package dev.boarbot.bot;

import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.logging.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;

public class DatabaseUpdater {
    public static void updateDatabase() {
        try (Connection connection = DataUtil.getConnection()) {
            File[] scripts = new File("database/scripts").listFiles();

            if (scripts == null) {
                return;
            }

            Arrays.sort(scripts);

            for (File script : scripts) {
                String scriptName = script.getName();

                if (!isScriptExecuted(connection, scriptName)) {
                    Log.info(DatabaseUpdater.class, "Executing " + scriptName + "...");

                    executeScript(connection, script);
                    markScriptAsExecuted(connection, scriptName);
                }
            }
        }catch (SQLException exception) {
            Log.error(DatabaseUpdater.class, "Something went wrong when updatings database", exception);
            System.exit(-1);
        }
    }

    private static boolean isScriptExecuted(Connection connection, String scriptName) {
        String query = """
            SELECT 1
            FROM schema_version
            WHERE script_name = ?;
        """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, scriptName);

            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        } catch (SQLException exception) {
            Log.error(DatabaseUpdater.class, "Something went wrong when checking script execution state", exception);
            System.exit(-1);
        }

        return false;
    }

    private static void executeScript(Connection connection, File script) {
        StringBuilder scriptBuilder = new StringBuilder();

        try (FileReader reader = new FileReader(script)) {
            int ch;
            while ((ch = reader.read()) != -1) {
                scriptBuilder.append((char) ch);
            }
        } catch (IOException exception) {
            Log.error(
                DatabaseUpdater.class, "Something went wrong when reading script: " + script.getName(), exception
            );
            System.exit(-1);
        }

        try (Statement statement = connection.createStatement()) {
            String scriptSql = scriptBuilder.toString();
            scriptSql = scriptSql.replaceAll("\\s+", " ");

            statement.execute(scriptSql);
        } catch (SQLException exception) {
            Log.error(
                DatabaseUpdater.class, "Something went wrong when executing script: " + script.getName(), exception
            );
            System.exit(-1);
        }
    }

    private static void markScriptAsExecuted(Connection connection, String scriptName) {
        String sql = """
            INSERT INTO schema_version (script_name)
            VALUES (?);
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, scriptName);
            statement.executeUpdate();
        } catch (SQLException exception) {
            Log.error(DatabaseUpdater.class, "Something went wrong when marking script: " + scriptName, exception);
            System.exit(-1);
        }
    }
}
