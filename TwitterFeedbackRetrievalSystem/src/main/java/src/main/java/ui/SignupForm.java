package src.main.java.ui;

import src.main.java.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignupForm extends JFrame {
    private JTextField usernameField;
    private JTextField fullNameField;
    private JTextField profileImageURLField;
    private JPasswordField passwordField;
    private JButton signupButton;
    private JButton backButton;

    public SignupForm() {
        setTitle("Sign Up");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Full Name:"));
        fullNameField = new JTextField();
        add(fullNameField);

        add(new JLabel("Profile Image URL:"));
        profileImageURLField = new JTextField();
        add(profileImageURLField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        signupButton = new JButton("Sign Up");
        signupButton.addActionListener(this::signupAction);
        add(signupButton);

        backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });
        add(backButton);
    }

    private void signupAction(ActionEvent e) {
        String username = usernameField.getText();
        String fullName = fullNameField.getText();
        String profileImageURL = profileImageURLField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO Users (Username, FullName, ProfileImageURL, Password) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, fullName);
            stmt.setString(3, profileImageURL);
            stmt.setString(4, password);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Signup successful! Please log in.");
            new LoginForm().setVisible(true);
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during signup. Please try again.");
        }
    }
}
