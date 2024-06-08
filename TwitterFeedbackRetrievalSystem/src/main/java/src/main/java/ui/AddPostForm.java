package src.main.java.ui;
import src.main.java.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddPostForm extends JFrame {
    private int loggedInUserID;

    public AddPostForm(int loggedInUserID) {
        this.loggedInUserID = loggedInUserID;

        setTitle("Add New Post");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] postTypes = {"Image", "Video", "Text"};
        JComboBox<String> typeComboBox = new JComboBox<>(postTypes);
        mainPanel.add(new JLabel("Post Type:"));
        mainPanel.add(typeComboBox);

        JTextField urlField = new JTextField();
        mainPanel.add(new JLabel("URL (if applicable):"));
        mainPanel.add(urlField);

        JTextArea postTextArea = new JTextArea();
        JScrollPane textScrollPane = new JScrollPane(postTextArea);
        mainPanel.add(new JLabel("Post Text:"));
        mainPanel.add(textScrollPane);

        JButton addButton = new JButton("Add Post");
        addButton.addActionListener(e -> addNewPost(typeComboBox.getSelectedItem().toString(), urlField.getText(), postTextArea.getText()));
        mainPanel.add(addButton);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void addNewPost(String postType, String url, String postText) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO TwitterPosts (UserID, PostText, PostDate, TypeID, PostURL) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, loggedInUserID);
            stmt.setString(2, postText);
            stmt.setInt(3, getTypeID(postType));
            stmt.setString(4, url);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Post added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add post.");
        }
    }

    private int getTypeID(String typeName) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT TypeID FROM PostTypes WHERE TypeName = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, typeName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("TypeID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
