package de.c4vxl.app.page;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.c4vxl.app.App;
import de.c4vxl.engine.training.Datasets;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatasetDownload extends JFrame {
    private App parent;
    private JPanel list = new JPanel();

    @SuppressWarnings("unchecked")
    public DatasetDownload(App parentApp) {
        parent = parentApp;

        this.setTitle("Download datasets");
        this.setSize(600, 500);
        this.getContentPane().setBackground(App.background);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setIconImage(App.icon.getImage());

        // home button
        JButton homeButton = App.createButton("Home");
        App.setSize(homeButton, this.getWidth(), homeButton.getHeight());
        homeButton.addActionListener(e -> {
            this.setVisible(false);
            parentApp.setVisible(true);
        });


        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(App.background);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;

        panel.add(homeButton, gbc);

        gbc.insets = new Insets(10, 0, 0, 0);

        gbc.gridy = 1;
        panel.add(datasetSearchBar(), gbc);

        this.add(panel, BorderLayout.PAGE_START);


        list.setBackground(App.background);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        this.add(list, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); // hide scrollbar by setting width and height to 0
        this.add(scrollPane);

        // load list on other thread so the main thread won't have to wait
        new Thread(() -> {
            loadList("");
        }).start();
    }

    public JPanel datasetSearchBar() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        searchPanel.setBackground(App.background);

        // search bar
        JTextField bar = new JTextField(20);
        App.setSize(bar, 200, 50);
        bar.setLayout(new FlowLayout());
        bar.setForeground(Color.GRAY);
        bar.setBackground(App.background);
        bar.setText("Search");
        bar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.GRAY, 1, true), // outside border
                new EmptyBorder(0, 10, 0, 0) // border for inner spacing
        ));
        bar.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (bar.getText().equals("Search")) {
                    bar.setText("");
                    bar.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {}
        });
        bar.addActionListener((e) -> loadList(e.getActionCommand()));

        JButton button = App.createButton("Go");
        App.setSize(button, 100, 50);
        button.addActionListener((e) -> loadList(bar.getText()));
        searchPanel.add(bar);
        searchPanel.add(button);

        return searchPanel;
    }

    private JLabel createLabel(String text, Integer size) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Inter", Font.PLAIN, size));
        return label;
    }

    @SuppressWarnings("unchecked")
    public void loadList(String query) {
        List<List<String>> datasets = Datasets.HuggingFaceAPI.getDatasetList(query); // search for datasets matching the query

        list.removeAll();
        for (List<String> dataset : datasets) {
            String id = dataset.get(0).replace("\"", "");
            String author = dataset.get(1);
            String sha = dataset.get(2);
            String date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault()).format(Instant.parse(dataset.get(4).replace("\"", "")));

            JPanel panel = new JPanel();
            App.setSize(panel, this.getWidth(), 50);
            panel.setToolTipText("Click to download");
            panel.setBackground(App.background);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            namePanel.setBackground(App.background);
            namePanel.add(createLabel(id + "  -  ", 18));
            namePanel.add(createLabel("By: " + author, 14));

            JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            descriptionPanel.setBackground(App.background);
            descriptionPanel.add(createLabel(date, 14));
            descriptionPanel.add(createLabel("   -   SHA: " + sha, 14));

            panel.add(namePanel);
            panel.add(descriptionPanel);

            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    panel.setBackground(new Color(57, 53, 53));
                    namePanel.setBackground(new Color(57, 53, 53));
                    descriptionPanel.setBackground(new Color(57, 53, 53));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    panel.setBackground(App.background);
                    namePanel.setBackground(App.background);
                    descriptionPanel.setBackground(App.background);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    handleDownload(id);
                }
            });

            list.add(panel);
        }

        list.updateUI();
    }

    public void handleDownload(String id) {
        String[] files = Datasets.HuggingFaceAPI.getDatasetFiles(id)
                .toArray(String[]::new);

        // return if dataset cannot be downloaded
        if (files.length == 0) {
            JOptionPane.showMessageDialog(this, "This dataset cannot be downloaded!");
            return;
        }

        String file = null;
        if (files.length > 1)
            file = JOptionPane.showInputDialog(this,
                    "Select a file to download",
                    "Select a file",
                    JOptionPane.PLAIN_MESSAGE, null,
                    files, null).toString();

        file = file == null ? files[0] : file;

        System.out.println("Downloading file: " + file + " from " + id);

        // export
        String outputPath = JOptionPane.showInputDialog(this,
                "Save to: ", "dataset/" + (id + "_" + file).replace("/", "_"));
        File datasetFile = Datasets.HuggingFaceAPI.downloadDataset(id, file, new File(App.getRunPath(), outputPath).getPath());

        // show dataset file
        try {
            File openPath = datasetFile.getParentFile() == null ? datasetFile : datasetFile.getParentFile();
            Desktop.getDesktop().open(openPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}