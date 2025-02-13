import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GamePanel extends JPanel {
    private JLabel flagLabel;
    private JButton[] choiceButtons;
    private JLabel scoreLabel;
    private JLabel timerLabel;
    private int score;
    private int questionsAnswered;
    private String difficulty;
    private FlagDatabase flagDatabase;
    private List<Flag> currentFlags;
    private Flag currentFlag;
    private volatile boolean running;
    private MainPanel mainPanel;
    private Timer timer;
    private int timeRemaining;
    private List<Flag> incorrectFlags; // List to store incorrect flags
    private SoundEffect buttonSound;

    public GamePanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        this.buttonSound = new SoundEffect("src/click.wav");

        setLayout(new BorderLayout());

        flagDatabase = new FlagDatabase();
        score = 0;
        questionsAnswered = 0;
        incorrectFlags = new ArrayList<>(); // Initialize the list

        // Main panel with gradient background
        JPanel mainInnerPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color color1 = Color.decode("#7AD2EA");
                Color color2 = Color.decode("#0F597E");
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        mainInnerPanel.setOpaque(false);

        // White panel with black outline
        JPanel innerPanel = new JPanel();
        innerPanel.setPreferredSize(new Dimension(650, 450));
        innerPanel.setLayout(new BorderLayout());
        innerPanel.setBackground(Color.decode("#2592AF"));
        innerPanel.setOpaque(false);
        innerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        flagLabel = new JLabel("", SwingConstants.CENTER);
        flagLabel.setPreferredSize(new Dimension(300, 200));
        flagLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        innerPanel.add(flagLabel, BorderLayout.CENTER);

        // Choice buttons
        JPanel choicePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        choicePanel.setOpaque(false);
        choiceButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            choiceButtons[i] = new JButton();
            choiceButtons[i].setFont(new Font("Arial", Font.BOLD, 24));
            choiceButtons[i].setBackground(Color.decode("#2592AF"));
            choiceButtons[i].setForeground(Color.WHITE);
            choiceButtons[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 2),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)));
            choiceButtons[i].setFocusPainted(false);
            choiceButtons[i].setContentAreaFilled(false);
            choiceButtons[i].setOpaque(true);
            choiceButtons[i].addActionListener(new ChoiceButtonListener());
            choicePanel.add(choiceButtons[i]);
        }

        choicePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(choicePanel, BorderLayout.CENTER);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        innerPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Timer and score panel with margin
        JPanel timerScorePanel = new JPanel(new BorderLayout());
        timerScorePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add margin here
        timerScorePanel.setBackground(Color.lightGray); // Example background color

        timerLabel = new JLabel("Time: 10", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Example: set font size to 18
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Example: set font size to 18
        timerScorePanel.add(timerLabel, BorderLayout.WEST);
        timerScorePanel.add(scoreLabel, BorderLayout.EAST);

        innerPanel.add(timerScorePanel, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mainInnerPanel.add(innerPanel, gbc);

        add(mainInnerPanel, BorderLayout.CENTER);

        // Add the "Go Back" label at the top
        JLabel goBackLabel = new JLabel("< Go Back");
        goBackLabel.setFont(new Font("Arial", Font.BOLD, 18));
        goBackLabel.setForeground(Color.white); // Set to the same blue color as the buttons
        goBackLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        goBackLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                buttonSound.play();
                mainPanel.showDifficultyPanel();
            }
        });

        // Position "Go Back" label
        GridBagConstraints backGbc = new GridBagConstraints();
        backGbc.gridx = 0;
        backGbc.gridy = 0;
        backGbc.anchor = GridBagConstraints.WEST;
        backGbc.insets = new Insets(20, 10, 10, 0); // Add some padding
        mainInnerPanel.add(goBackLabel, backGbc);
    }

    private boolean analysisShown = false;

    public void startGame(String difficulty) {
        this.difficulty = difficulty;
        score = 0;
        questionsAnswered = 0;
        incorrectFlags.clear(); // Clear incorrect flags list at the start of the game
        scoreLabel.setText("Score: 0/10");
        currentFlags = flagDatabase.getFlags(difficulty);
        loadNextFlag();
        running = true;
        analysisShown = false; // Reset the flag when the game starts
    }

    private void loadNextFlag() {
        SwingUtilities.invokeLater(() -> {
            // Reset button colors
            for (JButton button : choiceButtons) {
                button.setBackground(Color.decode("#2592AF")); // Set button color to default
            }

            if (questionsAnswered > 0 && questionsAnswered < 10) {
                try {
                    Thread.sleep(2000); // 2-second delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (questionsAnswered < 10 && !currentFlags.isEmpty()) {
                int index = (int) (Math.random() * currentFlags.size());
                currentFlag = currentFlags.remove(index);
                flagLabel.setIcon(new ImageIcon(new ImageIcon(currentFlag.getImagePath()).getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH)));
                setChoiceButtons();
                resetTimer();
            } else if (!analysisShown) { // Ensure analysis is shown only once
                showAnalysis();
                running = false;
                analysisShown = true; // Mark analysis as shown
            }
        });
    }

    private void setChoiceButtons() {
        int correctIndex = (int) (Math.random() * 4);
        choiceButtons[correctIndex].setText(currentFlag.getName());

        // Create a list of incorrect flag names
        List<Flag> incorrectFlags = new ArrayList<>(currentFlags);
        incorrectFlags.remove(currentFlag);

        // Ensure the incorrect flags are unique and not already guessed
        Collections.shuffle(incorrectFlags);
        int incorrectFlagIndex = 0;
        for (int i = 0; i < 4; i++) {
            if (i != correctIndex) {
                choiceButtons[i].setText(incorrectFlags.get(incorrectFlagIndex).getName());
                incorrectFlagIndex++;
            }
        }
    }

    private void checkAnswer(String selectedAnswer) {
        if (!selectedAnswer.equals(currentFlag.getName())) {
            incorrectFlags.add(currentFlag); // Add incorrect flag to the list
            highlightCorrectAnswer();
        } else {
            score++;
        }
        questionsAnswered++;
        scoreLabel.setText("Score: " + score + "/10");
        loadNextFlag();
    }

    private void highlightCorrectAnswer() {
        for (JButton button : choiceButtons) {
            if (button.getText().equals(currentFlag.getName())) {
                button.setBackground(Color.GREEN); // Highlight correct answer
            }
        }
    }

    private void showAnalysis() {
        String analysis = PerformanceAnalyzer.getDetailedFeedback(score, incorrectFlags);
        if (difficulty.equals("Easy") && score >= 7) {
            mainPanel.unlockMedium();
        } else if (difficulty.equals("Medium") && score >= 8) {
            mainPanel.unlockHard();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame analysisFrame = new JFrame("Performance Analysis");
            analysisFrame.setSize(500, 400);
            analysisFrame.setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    int width = getWidth();
                    int height = getHeight();
                    Color color1 = Color.decode("#7AD2EA");
                    Color color2 = Color.decode("#0F597E");
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, width, height);
                }
            };
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            contentPanel.setOpaque(false); // Ensure the gradient is visible

            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.setBackground(Color.WHITE);
            textPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            JTextArea analysisArea = new JTextArea(analysis);
            analysisArea.setFont(new Font("Arial", Font.PLAIN, 16)); // Example: set font size to 16
            analysisArea.setLineWrap(true);
            analysisArea.setWrapStyleWord(true);
            analysisArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(analysisArea);
            textPanel.add(scrollPane, BorderLayout.CENTER);

            contentPanel.add(textPanel, BorderLayout.CENTER);

            JButton closeButton = new JButton("Close");
            closeButton.setFont(new Font("Arial", Font.BOLD, 18));
            closeButton.setBackground(Color.decode("#2592AF"));
            closeButton.setForeground(Color.WHITE);
            closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            closeButton.setFocusPainted(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setOpaque(true);
            closeButton.addActionListener(e -> {
                analysisFrame.dispose();
                mainPanel.showDifficultyPanel(); // Navigate back to DifficultyPanel
            });
            contentPanel.add(closeButton, BorderLayout.SOUTH);

            analysisFrame.add(contentPanel);
            analysisFrame.setLocationRelativeTo(this);
            analysisFrame.setVisible(true);
        });
    }

    private void resetTimer() {
        if (timer != null) {
            timer.stop();
        }
        timeRemaining = 10;
        timerLabel.setText("Time: " + timeRemaining);
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;
                if (timeRemaining >= 0) {
                    timerLabel.setText("Time: " + timeRemaining);
                }
                if (timeRemaining == 0) {
                    timer.stop();
                    incorrectFlags.add(currentFlag); // Add current flag to incorrect list
                    highlightCorrectAnswer();
                    questionsAnswered++;
                    scoreLabel.setText("Score: " + score + "/10");
                    loadNextFlag();
                }
            }
        });
        timer.start();
    }

    private class ChoiceButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttonSound.play();
            if (running) {
                JButton clickedButton = (JButton) e.getSource();
                String selectedAnswer = clickedButton.getText();
                clickedButton.setBackground(selectedAnswer.equals(currentFlag.getName()) ? Color.GREEN : Color.RED);
                checkAnswer(selectedAnswer);
            }
        }
    }
}
