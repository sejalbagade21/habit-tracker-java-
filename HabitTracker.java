import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;

public class HabitTracker {
    private static String username = null;
    private static final String PROGRESS_FILE_PREFIX = "progress-";
    private static final String PROGRESS_FILE_SUFFIX = ".txt";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HabitTracker::showLogin);
    }

    private static void showLogin() {
        JFrame loginFrame = new JFrame("Login - Daily Habit Tracker");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 180);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel userLabel = new JLabel("Enter your username:");
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField userField = new JTextField();
        userField.setMaximumSize(new Dimension(200, 30));
        JButton loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(userLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(userField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(loginButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(errorLabel);

        loginButton.addActionListener(e -> {
            String user = userField.getText().trim();
            if (user.isEmpty() || user.contains(" ")) {
                errorLabel.setText("Invalid username.");
            } else {
                username = user;
                loginFrame.dispose();
                createAndShowGUI();
            }
        });

        loginFrame.setContentPane(panel);
        loginFrame.setVisible(true);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Daily Habit Tracker - " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 500);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Date label
        LocalDate now = LocalDate.now();
        String today = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        JLabel dateLabel = new JLabel("Today: " + today);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(dateLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Streak counter
        JLabel streakLabel = new JLabel();
        streakLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        streakLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(streakLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Task checkboxes
        JCheckBox leetcodeBox = new JCheckBox("1 Leetcode Problem");
        JCheckBox projectBox = new JCheckBox("1 Project Step");
        JCheckBox exerciseBox = new JCheckBox("Exercise");
        JCheckBox studyBox = new JCheckBox("Study (College Subject)");
        JCheckBox[] checkBoxes = {leetcodeBox, projectBox, exerciseBox, studyBox};

        panel.add(leetcodeBox);
        panel.add(projectBox);
        panel.add(exerciseBox);
        panel.add(studyBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, checkBoxes.length);
        progressBar.setStringPainted(true);
        panel.add(progressBar);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Motivational label
        JLabel motivationLabel = new JLabel("Let's crush your goals today!");
        motivationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        motivationLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        panel.add(motivationLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Manual reset button
        JButton resetButton = new JButton("Reset for Today");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(resetButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Show history button
        JButton historyButton = new JButton("Show Daily History");
        historyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(historyButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Persistence: Load progress and history
        Properties props = new Properties();
        boolean loaded = false;
        String progressFile = PROGRESS_FILE_PREFIX + username + PROGRESS_FILE_SUFFIX;
        Map<String, String> history = new TreeMap<>(); // date -> "1" (all done) or "0"
        try (FileInputStream fis = new FileInputStream(progressFile)) {
            props.load(fis);
            String savedDate = props.getProperty("date");
            if (savedDate != null && savedDate.equals(now.toString())) {
                for (int i = 0; i < checkBoxes.length; i++) {
                    String val = props.getProperty("task" + i);
                    checkBoxes[i].setSelected("1".equals(val));
                }
                loaded = true;
            }
            // Load history
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("history-")) {
                    history.put(key.substring(8), props.getProperty(key));
                }
            }
        } catch (IOException ignored) {}
        if (!loaded) {
            // If not loaded, ensure all unchecked
            for (JCheckBox box : checkBoxes) box.setSelected(false);
        }

        // Calculate streak
        int streak = 0;
        java.util.List<String> sortedDates = new java.util.ArrayList<>(history.keySet());
        Collections.sort(sortedDates, Comparator.reverseOrder());
        LocalDate streakDate = now.minusDays(1);
        for (String date : sortedDates) {
            if (history.get(date).equals("1") && date.equals(streakDate.toString())) {
                streak++;
                streakDate = streakDate.minusDays(1);
            } else if (!date.equals(streakDate.toString())) {
                break;
            }
        }
        // If today is complete, add to streak
        boolean todayComplete = false;
        int completed = 0;
        for (JCheckBox box : checkBoxes) if (box.isSelected()) completed++;
        if (completed == checkBoxes.length) {
            todayComplete = true;
            streak++;
        }
        streakLabel.setText("Current Streak: " + streak + " day" + (streak == 1 ? "" : "s"));

        // Listener to update progress, message, save, and streak
        Runnable updateProgress = () -> {
            int completed2 = 0;
            for (JCheckBox box : checkBoxes) {
                if (box.isSelected()) completed2++;
            }
            progressBar.setValue(completed2);
            boolean allDone = completed2 == checkBoxes.length;
            if (allDone) {
                motivationLabel.setText("You did it! 🎉");
            } else {
                motivationLabel.setText("Let's crush your goals today!");
            }
            // Save progress and update history
            Properties saveProps = new Properties();
            saveProps.setProperty("date", now.toString());
            for (int i = 0; i < checkBoxes.length; i++) {
                saveProps.setProperty("task" + i, checkBoxes[i].isSelected() ? "1" : "0");
            }
            // Update history
            for (Map.Entry<String, String> entry : history.entrySet()) {
                saveProps.setProperty("history-" + entry.getKey(), entry.getValue());
            }
            saveProps.setProperty("history-" + now.toString(), allDone ? "1" : "0");
            try (FileOutputStream fos = new FileOutputStream(progressFile)) {
                saveProps.store(fos, "Daily Habit Progress");
            } catch (IOException ignored) {}
            // Update streak
            int newStreak = 0;
            java.util.List<String> sortedDates2 = new java.util.ArrayList<>(history.keySet());
            sortedDates2.add(now.toString());
            Set<String> uniqueDates = new HashSet<>(sortedDates2);
            java.util.List<String> sortedUniqueDates = new java.util.ArrayList<>(uniqueDates);
            Collections.sort(sortedUniqueDates, Comparator.reverseOrder());
            LocalDate streakDate2 = now;
            for (String date : sortedUniqueDates) {
                String val = date.equals(now.toString()) ? (allDone ? "1" : "0") : history.get(date);
                if (val != null && val.equals("1") && date.equals(streakDate2.toString())) {
                    newStreak++;
                    streakDate2 = streakDate2.minusDays(1);
                } else if (!date.equals(streakDate2.toString())) {
                    break;
                }
            }
            streakLabel.setText("Current Streak: " + newStreak + " day" + (newStreak == 1 ? "" : "s"));
        };

        for (JCheckBox box : checkBoxes) {
            box.addActionListener(e -> updateProgress.run());
        }

        // Reset button logic
        resetButton.addActionListener(e -> {
            for (JCheckBox box : checkBoxes) box.setSelected(false);
            updateProgress.run();
        });

        // Show history panel
        historyButton.addActionListener(e -> {
            JDialog historyDialog = new JDialog(frame, "Daily History", true);
            historyDialog.setSize(350, 350);
            historyDialog.setLocationRelativeTo(frame);
            String[] columns = {"Date", "Completed"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            for (String date : history.keySet()) {
                data.add(new String[]{date, history.get(date).equals("1") ? "Yes" : "No"});
            }
            // Add today
            int completed3 = 0;
            for (JCheckBox box : checkBoxes) if (box.isSelected()) completed3++;
            data.add(new String[]{now.toString(), (completed3 == checkBoxes.length) ? "Yes" : "No"});
            String[][] tableData = data.toArray(new String[0][]);
            JTable table = new JTable(tableData, columns);
            JScrollPane scrollPane = new JScrollPane(table);
            historyDialog.add(scrollPane);
            historyDialog.setVisible(true);
        });

        // Initialize progress
        updateProgress.run();

        frame.setContentPane(panel);
        frame.setVisible(true);
    }
} 
