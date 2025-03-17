import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;

public class NumberGuessingGame extends JFrame {

    private static final int MAX_LEVEL = 10;
    private static final int BASE_UPPER_BOUND = 50;
    private static final int LEVEL_INCREASE = 50;
    private static final int TIME_LIMIT = 30;

    private int randomNumber;
    private int numberOfTries;
    private int currentLevel = 1;
    private int lowerBound;
    private int upperBound;
    private int totalScore = 0;
    private int highScore = Integer.MAX_VALUE;
    private boolean hardMode = false; // Default to Normal Mode

    // UI Components
    private JTextField guessField;
    private JLabel messageLabel;
    private JLabel attemptsLabel;
    private JLabel levelLabel;
    private JLabel scoreLabel;
    private JLabel timerLabel;
    private JProgressBar progressBar;
    private Timer gameTimer;
    private int timeLeft;
    private Random random = new Random();

    public NumberGuessingGame() {
        super("Enhanced Number Guessing Game");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        showModeSelectionDialog();
        setLocationRelativeTo(null);
    }

    private void showModeSelectionDialog() {
        String[] options = {"Normal Mode", "Hard Mode"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select Game Mode:",
                "Game Mode",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        // Set hardMode based on user choice
        hardMode = (choice == 1); // 1 is Hard Mode
        showUniquePreLoader();
    }

    private void showUniquePreLoader() {
        JPanel preLoaderPanel = new JPanel(new GridBagLayout());
        preLoaderPanel.setBackground(new Color(30, 30, 30));

        // Pre-loader label to display random numbers
        JLabel preLoaderLabel = new JLabel();
        preLoaderLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        preLoaderLabel.setForeground(Color.WHITE);

        // Add pre-loader label to the panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        preLoaderPanel.add(preLoaderLabel, gbc);

        // Add the pre-loader panel to the frame
        add(preLoaderPanel);

        // Timer to flash random numbers
        Timer numberTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int randomNum = random.nextInt(100);
                preLoaderLabel.setText(String.format("%02d", randomNum)); // Display as 2-digit number
            }
        });
        numberTimer.start();

        // Timer to stop flashing and reveal "Game Ready"
        Timer revealTimer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                numberTimer.stop();
                preLoaderLabel.setText("Game Ready!");
                preLoaderLabel.setFont(new Font("Impact", Font.BOLD, 42));
                preLoaderLabel.setForeground(Color.YELLOW);

                // Simulate a delay before transitioning to the main game
                Timer transitionTimer = new Timer(2000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        remove(preLoaderPanel);
                        createUI();
                        showWelcomeMessage();
                        initializeGame();
                    }
                });
                transitionTimer.setRepeats(false);
                transitionTimer.start();
            }
        });
        revealTimer.setRepeats(false);
        revealTimer.start();
    }

    private void showWelcomeMessage() {
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(new Color(30, 30, 30));

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome to the Number Guessing Game!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        welcomeLabel.setForeground(Color.WHITE);

        // Add welcome label to the panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        welcomePanel.add(welcomeLabel, gbc);

        // Add the welcome panel to the frame
        add(welcomePanel, BorderLayout.CENTER);

        // Simulate a delay for the welcome message
        Timer timer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(welcomePanel);
                createUI();
                initializeGame();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void createUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header: Stylized title with custom font and drop shadow effect
        JLabel titleLabel = new JLabel("Number Guessing Challenge");
        titleLabel.setFont(new Font("Impact", Font.BOLD, 42));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.YELLOW));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Game Panel for input and actions – using semi-transparent background
        JPanel gamePanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        gamePanel.setOpaque(false);
        gamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Input field with custom styling
        guessField = new JTextField(10);
        guessField.setFont(new Font("Arial", Font.PLAIN, 24));
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkGuess();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gamePanel.add(guessField, gbc);

        // "Guess" button styled with bold colors (inspired by game UI buttons)
        JButton submitButton = createStyledButton("Guess", new Color(220, 20, 60));
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkGuess();
            }
        });
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gamePanel.add(submitButton, gbc);

        // "Hint (Power-up)" button with icon or vibrant style
        JButton hintButton = createStyledButton("Hint (Power-up)", new Color(255, 140, 0));
        hintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                giveHint();
            }
        });
        gbc.gridx = 1;
        gamePanel.add(hintButton, gbc);

        // Message label for feedback
        messageLabel = new JLabel("Guess a number!");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gamePanel.add(messageLabel, gbc);

        mainPanel.add(gamePanel, BorderLayout.CENTER);

        // Stats Panel for displaying levels, score, and timer, with transparent background
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        levelLabel = new JLabel("Level: 1/" + MAX_LEVEL);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 18));
        levelLabel.setForeground(Color.WHITE);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoreLabel.setForeground(Color.WHITE);

        attemptsLabel = new JLabel("Attempts: 0");
        attemptsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        attemptsLabel.setForeground(Color.WHITE);

        timerLabel = new JLabel("Time: N/A");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        timerLabel.setForeground(Color.WHITE);

        progressBar = new JProgressBar(1, MAX_LEVEL);
        progressBar.setValue(1);
        progressBar.setForeground(new Color(30, 144, 255));
        progressBar.setStringPainted(true);

        statsPanel.add(levelLabel);
        statsPanel.add(scoreLabel);
        statsPanel.add(attemptsLabel);
        statsPanel.add(progressBar);
        statsPanel.add(timerLabel);

        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        add(mainPanel);
        addPowerUps(); // Add Power-Ups to the UI
    }

    private void addPowerUps() {
        JPanel powerUpPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        powerUpPanel.setOpaque(false);

        // Time Freeze Button
        JButton timeFreezeButton = createStyledButton("Time Freeze", new Color(0, 191, 255));
        timeFreezeButton.addActionListener(e -> freezeTime());
        powerUpPanel.add(timeFreezeButton);

        // Range Narrowing Button
        JButton rangeNarrowButton = createStyledButton("Narrow Range", new Color(34, 139, 34));
        rangeNarrowButton.addActionListener(e -> narrowRange());
        powerUpPanel.add(rangeNarrowButton);

        // Extra Attempts Button
        JButton extraAttemptsButton = createStyledButton("+1 Attempt", new Color(255, 215, 0));
        extraAttemptsButton.addActionListener(e -> addExtraAttempt());
        powerUpPanel.add(extraAttemptsButton);

        // Save Game Button
        JButton saveButton = createStyledButton("Save Game", new Color(75, 0, 130));
        saveButton.addActionListener(e -> saveGame());
        powerUpPanel.add(saveButton);

        // Load Game Button
        JButton loadButton = createStyledButton("Load Game", new Color(139, 0, 139));
        loadButton.addActionListener(e -> loadGame());
        powerUpPanel.add(loadButton);

        // Add Power-Up Panel to the Main UI
        add(powerUpPanel, BorderLayout.NORTH);
    }

    private void freezeTime() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
            timerLabel.setText("Time Frozen!");
            new Timer(5000, e -> gameTimer.start()).start(); // Resume after 5 seconds
        }
    }

    private void narrowRange() {
        int range = upperBound - lowerBound;
        upperBound = randomNumber + range / 4;
        lowerBound = randomNumber - range / 4;
        messageLabel.setText("New Range: " + lowerBound + " to " + upperBound);
    }

    private void addExtraAttempt() {
        numberOfTries--;
        attemptsLabel.setText("Attempts: " + numberOfTries);
    }

    private void saveGame() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("savegame.dat"))) {
            out.writeInt(currentLevel);
            out.writeInt(totalScore);
            out.writeInt(numberOfTries);
            out.writeInt(randomNumber);
            out.writeInt(lowerBound);
            out.writeInt(upperBound);
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
        }
    }

// Previous imports and class definition remain the same

    private void loadGame() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("savegame.dat"))) {
            currentLevel = in.readInt();
            totalScore = in.readInt();
            numberOfTries = in.readInt();
            randomNumber = in.readInt();
            lowerBound = in.readInt();
            upperBound = in.readInt();
            initializeGame();
        } catch (IOException e) {
            System.err.println("Error loading game: " + e.getMessage());
        }
    }

// Rest of the code remains the same

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        return button;
    }

    private void initializeGame() {
        lowerBound = 1;
        upperBound = BASE_UPPER_BOUND + (currentLevel - 1) * LEVEL_INCREASE;
        if (upperBound <= lowerBound) {
            upperBound = lowerBound + 1;
        }
        randomNumber = random.nextInt(upperBound - lowerBound + 1) + lowerBound;
        numberOfTries = 0;
        attemptsLabel.setText("Attempts: 0");
        levelLabel.setText("Level: " + currentLevel + "/" + MAX_LEVEL);
        progressBar.setValue(currentLevel);
        messageLabel.setText("Guess a number between " + lowerBound + " and " + upperBound);
        if (hardMode) {
            startTimer();
        } else {
            stopTimer();
        }
    }

    private void startTimer() {
        timeLeft = TIME_LIMIT;
        timerLabel.setText("Time: " + timeLeft + "s");
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText("Time: " + timeLeft + "s");
                if (timeLeft <= 0) {
                    gameOver();
                }
            }
        });
        gameTimer.start();
    }

    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        timerLabel.setText("Time: N/A");
    }

    private void checkGuess() {
        try {
            int guess = Integer.parseInt(guessField.getText());
            numberOfTries++;
            if (guess < randomNumber) {
                flashMessage("Too low! Try again.", Color.RED);
            } else if (guess > randomNumber) {
                flashMessage("Too high! Try again.", Color.RED);
            } else {
                playSound("resources/correct.wav");
                flashMessage("Congratulations! You got it!", Color.GREEN);
                completeLevel();
            }
            attemptsLabel.setText("Attempts: " + numberOfTries);
            guessField.setText("");
        } catch (NumberFormatException ex) {
            flashMessage("Please enter a valid number!", Color.RED);
        }
    }

    private void completeLevel() {
        totalScore += numberOfTries;
        if (currentLevel < MAX_LEVEL) {
            currentLevel++;
            initializeGame();
        } else {
            if (totalScore < highScore) {
                highScore = totalScore;
            }
            JOptionPane.showMessageDialog(this, "You won the game! High Score: " + highScore);
            saveScore(totalScore);
            displayLeaderboard();
            currentLevel = 1;
            totalScore = 0;
            initializeGame();
        }
        scoreLabel.setText("Score: " + totalScore);
    }

    private void saveScore(int score) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("leaderboard.txt", true))) {
            writer.write(score + "\n");
        } catch (IOException e) {
            System.err.println("Error saving score: " + e.getMessage());
        }
    }

    private void displayLeaderboard() {
        try (BufferedReader reader = new BufferedReader(new FileReader("leaderboard.txt"))) {
            List<Integer> scores = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                scores.add(Integer.parseInt(line));
            }
            scores.sort(null);
            JOptionPane.showMessageDialog(this, "Top Scores:\n" + scores.subList(0, Math.min(5, scores.size())));
        } catch (IOException e) {
            System.err.println("Error reading leaderboard: " + e.getMessage());
        }
    }

    private void giveHint() {
        if (randomNumber % 2 == 0) {
            flashMessage("Hint: The number is even!", new Color(30, 144, 255));
        } else {
            flashMessage("Hint: The number is odd!", new Color(30, 144, 255));
        }
    }

    private void flashMessage(String text, Color color) {
        messageLabel.setText(text);
        messageLabel.setForeground(color);
        Timer flasher = new Timer(100, new ActionListener() {
            int count = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count >= 6) {
                    ((Timer) e.getSource()).stop();
                    messageLabel.setForeground(Color.WHITE);
                } else {
                    messageLabel.setForeground(count % 2 == 0 ? color : Color.WHITE);
                    count++;
                }
            }
        });
        flasher.start();
    }

    private void gameOver() {
        playSound("resources/gameover.wav");
        JOptionPane.showMessageDialog(this, "Time's up! Game over.");
        currentLevel = 1;
        totalScore = 0;
        initializeGame();
    }

    private void playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NumberGuessingGame game = new NumberGuessingGame();
                game.setVisible(true);
            }
        });
    }
}