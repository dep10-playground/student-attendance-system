package lk.ijse.dep10.sas;

import javafx.application.Application;
import javafx.stage.Stage;
import lk.ijse.dep10.sas.db.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (DBConnection.getInstance().getConnection() != null && !DBConnection.getInstance().getConnection().isClosed()) {
                    DBConnection.getInstance().getConnection().close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        generateSchemaIfNotExist();
    }

    private void generateSchemaIfNotExist() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SHOW TABLES ");

            HashSet<String> tableNameSet = new HashSet<>();
            while (rst.next()) {
                tableNameSet.add(rst.getString(1));
            }

            boolean tableExists = tableNameSet.containsAll(Set.of("Attendance", "Picture", "Student", "User"));
            if (!tableExists) {
                stm.execute(readDBScript());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String readDBScript() {
        InputStream is = getClass().getResourceAsStream("/schema.sql");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            StringBuilder dbScript = new StringBuilder();
            while ((line = br.readLine()) != null) {
                dbScript.append(line).append("\n");
            }
            return dbScript.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
