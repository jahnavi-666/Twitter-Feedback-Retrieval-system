package src.main.java.ui;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import src.main.java.db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.List;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.net.URL;
import java.net.MalformedURLException;


public class MainPageForm extends JFrame {
    private int loggedInUserID;
    private JPanel postsPanel;

    public MainPageForm(int userID) {
        loggedInUserID = userID;
        setTitle("Main Page");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JButton addPostButton = new JButton("Add Post");
        addPostButton.addActionListener(e -> openAddPostDialog());

        JButton userProfileButton = new JButton("User Profile");
        userProfileButton.addActionListener(e -> openUserProfile());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(addPostButton);
        buttonPanel.add(userProfileButton);
        add(buttonPanel, BorderLayout.NORTH);

        postsPanel = new JPanel();
        postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(postsPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadAllPosts();
    }
   



    private void loadAllPosts() {
        postsPanel.removeAll(); 
        try (Connection conn = DBConnection.getConnection()) {
            loadUserPosts(loggedInUserID);
            loadOtherUserPosts(loggedInUserID);
            postsPanel.revalidate(); 
            postsPanel.repaint();
            revalidate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserPosts(int userID) {
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

                JPanel postPanel = createPostPanel(postID, postText, postType, postURL, postDate);
                postsPanel.add(postPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadOtherUserPosts(int loggedInUserID) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM TwitterPosts WHERE UserID != ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, loggedInUserID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int postID = rs.getInt("PostID");
                String postText = rs.getString("PostText");
                String postType = getPostType(rs.getInt("TypeID"));
                String postURL = rs.getString("PostURL");
                String postDate = rs.getString("PostDate");

                JPanel postPanel = createPostPanel(postID, postText, postType, postURL, postDate);
                postsPanel.add(postPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getPostType(int typeID) {
        switch (typeID) {
            case 12346:
                return "Image";
            case 12347:
                return "Video";
            case 12345:
                return "Text";
            default:
                return "Unknown";
        }
    }

    private JPanel createPostPanel(int postID, String postText, String postType, String postURL, String postDate) {
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

    if (postType.equals("Image") || postType.equals("Video")) {
        JLabel mediaLabel = new JLabel();
        if (postURL != null && !postURL.isEmpty()) {
            ImageIcon icon = createImageIcon(postURL);
            if (icon != null) {
                mediaLabel.setIcon(icon);
            } else {
                mediaLabel.setText("Error loading media from URL");
            }
        }
        postPanel.add(mediaLabel, BorderLayout.SOUTH);
    } else if (postType.equals("Text")) {
        JLabel emptyLabel = new JLabel();
        postPanel.add(emptyLabel, BorderLayout.SOUTH);
    }

    JPanel commentsPanel = new JPanel();
    commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
    commentsPanel.setBorder(BorderFactory.createTitledBorder("Comments"));
    loadCommentsForPost(postID, commentsPanel);
    
    // Add text field and button for adding comments
    JTextField commentTextField = new JTextField();
    JButton commentButton = new JButton("Add Comment");
    commentButton.addActionListener(e -> addComment(postID, commentTextField.getText()));
    
    JPanel addCommentPanel = new JPanel(new BorderLayout());
    addCommentPanel.add(commentTextField, BorderLayout.CENTER);
    addCommentPanel.add(commentButton, BorderLayout.EAST);
    commentsPanel.add(addCommentPanel);
    
    postPanel.add(commentsPanel, BorderLayout.SOUTH);

    return postPanel;
}



    private void loadCommentsForPost(int postID, JPanel commentsPanel) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM Comments WHERE PostID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, postID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int userID = rs.getInt("UserID");
                String commentText = rs.getString("CommentText");
                String username = getUsername(userID);

                JPanel commentPanel = new JPanel(new BorderLayout());
                JLabel commentLabel = new JLabel(username + ": " + commentText);
                commentPanel.add(commentLabel, BorderLayout.CENTER);

                JButton analyzeButton = new JButton("Analyze Sentiment");
                analyzeButton.addActionListener(e -> {
                    SentimentResult sentimentResult = performSentimentAnalysis(commentText);
                    JOptionPane.showMessageDialog(this,
                            "Sentiment: " + sentimentResult.getSentiment() +
                                    "\nConfidence: " + sentimentResult.getConfidence(),
                            "Sentiment Analysis Result", JOptionPane.INFORMATION_MESSAGE);
                });
                commentPanel.add(analyzeButton, BorderLayout.EAST);

                commentsPanel.add(commentPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private SentimentResult performSentimentAnalysis(String text) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        String sentiment = "";
        double confidence = 0.0;
        if (sentences != null && !sentences.isEmpty()) {
            CoreMap sentence = sentences.get(0);
            sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            confidence = getSentimentConfidence(sentence.get(SentimentCoreAnnotations.SentimentClass.class));
        }

        return new SentimentResult(sentiment, confidence);
    }

    private double getSentimentConfidence(String sentiment) {
        switch (sentiment) {
            case "Very positive":
                return 0.9;
            case "Positive":
                return 0.7;
            case "Neutral":
                return 0.5;
            case "Negative":
                return 0.3;
            case "Very negative":
                return 0.1;
            default:
                return 0.0;
        }
    }

    private String getUsername(int userID) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT Username FROM Users WHERE UserID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void openAddPostDialog() {
    // Create a dialog for adding a post
    JDialog dialog = new JDialog(this, "Add Post", true);
    dialog.setLayout(new GridLayout(0, 1));
    dialog.setSize(400, 200);
    dialog.setLocationRelativeTo(this);

    // Radio buttons for selecting post type
    JRadioButton imageRadioButton = new JRadioButton("Image");
    JRadioButton videoRadioButton = new JRadioButton("Video");
    JRadioButton textRadioButton = new JRadioButton("Text");
    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(imageRadioButton);
    buttonGroup.add(videoRadioButton);
    buttonGroup.add(textRadioButton);

    // Text field for post text
    JTextField postTextField = new JTextField();
    postTextField.setPreferredSize(new Dimension(200, 30));

    // Text field for post URL (for image and video posts)
    JTextField urlTextField = new JTextField();
    urlTextField.setPreferredSize(new Dimension(200, 30));

    // Button to add the post
    JButton addButton = new JButton("Add Post");
    addButton.addActionListener(e -> {
        if (imageRadioButton.isSelected()) {
            addImagePost(urlTextField.getText(), postTextField.getText());
        } else if (videoRadioButton.isSelected()) {
            addVideoPost(urlTextField.getText(), postTextField.getText());
        } else if (textRadioButton.isSelected()) {
            addTextPost(postTextField.getText());
        } else {
            JOptionPane.showMessageDialog(dialog, "Please select a post type.");
            return;
        }
        dialog.dispose(); // Close the dialog after adding the post
    });

    // Add components to the dialog
    dialog.add(imageRadioButton);
    dialog.add(videoRadioButton);
    dialog.add(textRadioButton);
    dialog.add(new JLabel("Post Text:"));
    dialog.add(postTextField);
    dialog.add(new JLabel("URL (for Image/Video):"));
    dialog.add(urlTextField);
    dialog.add(addButton);

    dialog.setVisible(true);
}
    private void addPostToDatabase(String postURL,String postText) {
    try (Connection conn = DBConnection.getConnection()) {
        // Determine the type of post based on the presence of postURL
        int typeID = determinePostType(postURL);
        
        // Insert the post into the TwitterPosts table with the determined TypeID
        String query = "INSERT INTO TwitterPosts (UserID, PostText, PostDate, TypeID, PostURL) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, loggedInUserID);
        stmt.setString(2, postText);
        stmt.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        stmt.setInt(4, typeID);
        stmt.setString(5, postURL);
        stmt.executeUpdate();
        
        // Refresh posts after adding
        loadAllPosts();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
private int determinePostType(String postURL) {
    if (postURL != null && !postURL.isEmpty()) {
        // If postURL is present, it's an image or video post
        // You can implement additional logic here to distinguish between image and video posts
        return getPostTypeID("Image"); // Assuming postURL indicates an image post
    } else {
        // If postURL is not present, it's a text post
        return getPostTypeID("Text");
    }
}

private int getPostTypeID(String typeName) {
    // Retrieve the TypeID for the given post type from the PostTypes table
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
    return -1; // Return -1 if TypeID is not found (handle appropriately in your application)
}
private void addImagePost(String url, String postText) {
    addPostToDatabase(url, postText);
    loadAllPosts(); // Refresh posts after adding
}

private void addVideoPost(String url, String postText) {
    addPostToDatabase(url, postText);
    loadAllPosts(); // Refresh posts after adding
}

private void addTextPost(String postText) {
    addPostToDatabase(null, postText); // No URL for text post
    loadAllPosts(); // Refresh posts after adding
}



    private void openUserProfile() {
        UserProfileForm userProfilePage = new UserProfileForm(loggedInUserID);
        userProfilePage.setVisible(true);
    }

    private void addComment(int postID, String commentText) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO Comments (PostID, UserID, CommentText) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, postID);
            stmt.setInt(2, loggedInUserID);
            stmt.setString(3, commentText);
            stmt.executeUpdate();

            loadAllPosts();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ImageIcon createImageIcon(String urlString) {
        try {
            URL url = new URL(urlString);
            return new ImageIcon(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainPageForm mainPageForm = new MainPageForm(1);
            mainPageForm.setVisible(true);
        });
    }
}
