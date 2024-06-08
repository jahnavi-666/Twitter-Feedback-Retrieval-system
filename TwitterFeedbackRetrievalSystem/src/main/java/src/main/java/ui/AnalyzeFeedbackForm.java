package src.main.java.ui;
import javax.swing.*;

public class AnalyzeFeedbackForm extends JFrame {
    public AnalyzeFeedbackForm() {
        setTitle("Analyze Feedback");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Feedback analyzed as: Positive");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label);
    }
}
