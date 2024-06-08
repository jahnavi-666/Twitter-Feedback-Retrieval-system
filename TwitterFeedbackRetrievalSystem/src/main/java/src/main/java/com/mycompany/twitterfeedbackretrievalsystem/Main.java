package src.main.java.com.mycompany.twitterfeedbackretrievalsystem;

import src.main.java.db.DBConnection;
import src.main.java.ui.LoginForm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        // Initialize the database
        initializeDatabase();

        // Launch the login form
        java.awt.EventQueue.invokeLater(() -> new LoginForm().setVisible(true));
    }

    private static void initializeDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            // Read the SQL script from the resources directory
            BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/db/TwitterDB.sql"));
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
            reader.close();

            // Execute the SQL script
            String[] sqlCommands = sql.toString().split(";");
            Statement stmt = conn.createStatement();
            for (String command : sqlCommands) {
                if (!command.trim().isEmpty()) {
                    stmt.execute(command);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
