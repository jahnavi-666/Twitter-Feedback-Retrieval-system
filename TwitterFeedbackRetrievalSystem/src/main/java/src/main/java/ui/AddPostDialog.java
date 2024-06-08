package src.main.java.ui;

import src.main.java.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddPostDialog extends JDialog {
    private int userID;
    private JTextField postTextField;
    private JComboBox<String> postTypeComboBox;
    private JTextField postURLField;

    public AddPostDialog(Frame owner, int userID) {
        super(owner, "Add New Post", true);
        this.userID = userID;
        setSize(400, 300);
        setLocationRelativeTo(owner);

        setLayout(new GridLayout(5, 2));

        add(new JLabel("Post Text:"));
        postTextField = new JTextField();
        add(postTextField);

        add(new JLabel("Post Type:"));
        postTypeComboBox = new JComboBox<>(new String[]{"Image", "Video", "Text"});
        add(postTypeComboBox);

        add(new JLabel("Post URL:"));
        postURLField = new JTextField();
        add(postURLField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewPost();
            }
        });
        add(submitButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        add(cancelButton);
    }

    private void addNewPost() {
        String postText = postTextField.getText();
        String postType = (String) postTypeComboBox.getSelectedItem();
        String postURL = postURLField.getText();

        int typeID;
        switch (postType) {
            case "Image":
                typeID = 1;
                break;
            case "Video":
                typeID = 2;
                break;
            case "Text":
                typeID = 3;
                break;
            default:
                typeID = 3; // Default to Text if something goes wrong
                break;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO TwitterPosts (UserID, PostText, TypeID, PostURL, PostDate) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userID);
            stmt.setString(2, postText);
            stmt.setInt(3, typeID);
            stmt.setString(4, postURL);
            stmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Post added successfully!");
            dispose(); // Close the dialog
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add post. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
