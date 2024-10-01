package de.c4vxl.app;

import de.c4vxl.app.page.Benchmark;
import de.c4vxl.app.page.CreateDataset;
import de.c4vxl.app.page.Prediction;
import de.c4vxl.app.page.Train;
import de.c4vxl.engine.nn.MLP;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

public class App extends JFrame {
    private int initialX, initialY;
    public static Color background = new Color(32, 30, 30);

    public static String getRunPath() {
        try {
            return new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public MLP model;

    // pages
    public Prediction prediction_page = new Prediction(this);
    public CreateDataset dataset_page = new CreateDataset(this);
    public Benchmark benchmark_page = new Benchmark(this);
    public Train train_page = new Train(this);

    private JButton pageButton(String name) {
        JButton button = new JButton(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("app/home/" + name + ".png"))));
        button.setPreferredSize(new Dimension(113, 144));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.addActionListener(e -> open(name));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setContentAreaFilled(true);
                button.setBackground(new Color(57, 53, 53));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setContentAreaFilled(false);
                button.setBackground(background);
            }
        });

        return button;
    }

    public static JButton createButton(String name) {
        JButton button = new JButton(name);
        setSize(button, 260, 40);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(57, 53, 53));
        return button;
    }

    public static void setSize(JComponent panel, int width, int height) {
        panel.setPreferredSize(new Dimension(width, height));
        panel.setSize(new Dimension(width, height));
        panel.setMaximumSize(new Dimension(width, height));
        panel.setMinimumSize(new Dimension(width, height));
    }

    public static ImageIcon icon = new ImageIcon(Objects.requireNonNull(App.class.getClassLoader().getResource("app/home/logo.png")));

    public App(String modelPath) {
        this.model = (MLP) new MLP(784, 10, 2, 16).load(modelPath);

        String[] modelPathParts = modelPath.split("/");
        String modelName = modelPathParts[modelPathParts.length - 1];

        this.setTitle("JavaNN");
        this.setSize(650, 370);
        this.getContentPane().setBackground(background);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setIconImage(icon.getImage());

        // remove toolbar
        this.setUndecorated(true);
        this.setLocationRelativeTo(null);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialX = e.getX();
                initialY = e.getY();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int newX = getLocation().x + e.getX() - initialX;
                int newY = getLocation().y + e.getY() - initialY;
                setLocation(newX, newY);
            }
        });

        // display title
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(new EmptyBorder(40, 0, 0, 320));
        titlePanel.setBackground(background);
        JLabel title = new JLabel("JavaNN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Inter", Font.ITALIC, 25));
        titlePanel.add(title);
        JLabel subtitle = new JLabel("- v1.0 - " + modelName);
        subtitle.setForeground(Color.WHITE);
        subtitle.setFont(new Font("Inter", Font.ITALIC, 15));
        titlePanel.add(subtitle);

        // display buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(background);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 40, 10);

        buttonPanel.add(pageButton("predict"), gbc);
        buttonPanel.add(pageButton("create_dataset"), gbc);
        buttonPanel.add(pageButton("benchmark"), gbc);
        buttonPanel.add(pageButton("train"), gbc);

        this.setLayout(new BorderLayout());
        this.add(titlePanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);

        this.setVisible(true);
    }

    private void open(String name) {
        this.setVisible(false);

        if (name.equalsIgnoreCase("predict"))
            prediction_page.setVisible(true);
        else if (name.equalsIgnoreCase("create_dataset"))
            dataset_page.setVisible(true);
        else if (name.equalsIgnoreCase("benchmark"))
            benchmark_page.setVisible(true);
        else if (name.equalsIgnoreCase("train"))
            train_page.setVisible(true);
        else
            this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String model = JOptionPane.showInputDialog(null, "Select a model file:", "model.mdl");
            model = model == null ? "model.mdl" : model;
            new App(new File(App.getRunPath(), model).getPath());
        });
    }
}
