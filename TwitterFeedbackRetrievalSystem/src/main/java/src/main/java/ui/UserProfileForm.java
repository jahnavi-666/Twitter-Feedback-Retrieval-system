package src.main.java.ui;
import src.main.java.db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProfileForm extends JFrame {
    private int userID;

    public UserProfileForm(int userID) {
        this.userID = userID;
        setTitle("User Profile");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel postsPanel = new JPanel();
        postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(postsPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadUserPosts(postsPanel);
    }

    private void loadUserPosts(JPanel postsPanel) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM TwitterPosts WHERE UserID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int postID = rs.getInt("PostID");
                String postText = rs.getString("PostText");
                String postType = getPostType(rs.getInt("TypeID"));
                String postURL = rs.getString("PostURL");
                String postDate = rs.getString("PostDate");

                JPanel postPanel = new JPanel(new BorderLayout());
                postPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                JLabel postLabel = new JLabel("<html><body>" +
                        "<b>Post ID:</b> " + postID + "<br/>" +
                        "<b>Post Type:</b> " + postType + "<br/>" +
                        "<b>Post Date:</b> " + postDate + "<br/>" +
                        "<b>Post Text:</b> " + postText + "<br/>" +
                        "<b>Post URL:</b> " + postURL + "<br/>" +
                        "</body></html>");
                postPanel.add(postLabel, BorderLayout.CENTER);

                postsPanel.add(postPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getPostType(int typeID) {
        switch (typeID) {
            case 1:
                return "Image";
            case 2:
                return "Video";
            case 3:
                return "Text";
            default:
                return "Unknown";
        }
    }
}
