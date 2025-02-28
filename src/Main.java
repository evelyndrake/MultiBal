import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class Main {
    private JPanel mainPanel;
    private JTable table1;
    private JButton installFromURLButton;
    private JButton installFromZipButton;
    private JButton openModFolderButton;
    private JButton openGameFolderButton;
    private JButton exportModsFolderToZip;
    private JButton showGithubLink;
    private ArrayList<Mod> modList;
    private Path downloadFolderPath;
    private Path modFolderPath;

    public Main() {
        // Add action listeners to buttons
        $$$setupUI$$$();
        installFromURLButton.addActionListener(new ActionListener() {
            // Open a dialog to enter a URL
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open a dialog to enter a URL
                String url = JOptionPane.showInputDialog("Enter the URL of the mod to install");
                if (url != null) {
                    // Download the mod from the URL
                    System.out.println("Downloading mod from " + url);
                    String zipUrl = url + "/archive/refs/heads/main.zip";
                    // Download the zip file to the Downloads folder
                    String zipFileName = Paths.get(downloadFolderPath.toString(), "mod.zip").toString();
                    try {
                        FileOperations.downloadFile(zipUrl, zipFileName);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // Extract the zip file
                    System.out.println("Extracting mod");
                    try {
                        FileOperations.unzip(zipFileName, modFolderPath.toString());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // Remove mod.zip if it exists
                    Path zipFilePath = Paths.get(downloadFolderPath.toString(), "mod.zip");
                    File zipFile = zipFilePath.toFile();
                    if (zipFile.exists()) {
                        zipFile.delete();
                    }
                    // Refresh the mod list
                    initializeModList();
                    refreshModTable();
                }
            }
        });
        installFromZipButton.addActionListener(new ActionListener() {
            // Open a dialog to select a zip file
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open a dialog to select a zip file
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                // Only show zip files
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    public boolean accept(File file) {
                        return file.getName().toLowerCase().endsWith(".zip") || file.isDirectory();
                    }

                    public String getDescription() {
                        return "Zip files";
                    }
                });
                fileChooser.setDialogTitle("Select the zip file to install");
                fileChooser.setApproveButtonText("Select");
                fileChooser.setApproveButtonMnemonic('s');
                fileChooser.setApproveButtonToolTipText("Select the zip file to install");
                fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                fileChooser.showDialog(null, "Select");
                File zipFile = fileChooser.getSelectedFile();
                if (zipFile != null) {
                    // Extract the zip file
                    System.out.println("Extracting mod");
                    try {
                        FileOperations.unzip(zipFile.getAbsolutePath(), modFolderPath.toString());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // Refresh the mod list
                    initializeModList();
                    refreshModTable();
                }
            }
        });
        openModFolderButton.addActionListener(new ActionListener() {
            // Open the mod folder
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(modFolderPath.toFile());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        openGameFolderButton.addActionListener(new ActionListener() {
            // Open the game folder
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(modFolderPath.getParent().toFile());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        exportModsFolderToZip.addActionListener(new ActionListener() {
            // Export the mods folder to a zip file
            @Override
            public void actionPerformed(ActionEvent e) {
                // Prompt for path to save the zip file
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setDialogTitle("Save the mods folder as a zip file");
                fileChooser.setApproveButtonText("Save");
                fileChooser.setApproveButtonMnemonic('s');
                fileChooser.setApproveButtonToolTipText("Save the mods folder as a zip file");
                fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                fileChooser.showDialog(null, "Save");
                File zipFile = fileChooser.getSelectedFile();
                // Add .zip
                if (!zipFile.getName().toLowerCase().endsWith(".zip")) {
                    zipFile = new File(zipFile.getAbsolutePath() + ".zip");
                }
                // Create a zip file of the mods folder
                System.out.println("Creating zip file");
                try {
                    FileOperations.pack(new File(modFolderPath.toString()), zipFile);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        showGithubLink.addActionListener(new ActionListener() {
            // Open the GitHub link to the project
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URL("https://github.com/evelyndrake/MultiBal").toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // Main method to run the application
    public static void main(String[] args) {
        JFrame frame = new JFrame("MultiBal");
        frame.setContentPane(new Main().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    // Initialize the mod list by loading mod folders from the Mods directory
    private void initializeModList() {
        // Find all mod folders
        modList = new ArrayList<>();
        for (File file : Objects.requireNonNull(modFolderPath.toFile().listFiles())) {
            if (file.isDirectory()) {
                modList.add(new Mod(file, file.getName(), true));
            }
        }
        Path disabledFolderPath = Paths.get(modFolderPath.getParent().toString(), "Disabled");
        // Add mod folders from the Disabled folder to the table
        for (File file : Objects.requireNonNull(disabledFolderPath.toFile().listFiles())) {
            if (file.isDirectory()) {
                modList.add(new Mod(file, file.getName(), false));
            }
        }
    }

    // Refresh the mod table with the current mod list
    private void refreshModTable() {
        // Add mod folders to the table
        String[] columnNames = {"Mod Name", "Enabled"};
        Object[][] data = new Object[modList.size()][2];
        for (int i = 0; i < modList.size(); i++) {
            data[i][0] = modList.get(i).getModName();
            data[i][1] = modList.get(i).isEnabled();
        }
        // Inside the createUIComponents method, after setting the table model
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            Class[] types = new Class[]{
                    String.class, Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 1;
            }

        };

        // Toggle enabled state when the checkbox column is clicked
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 1) {
                    modList.get(row).toggleEnabled();
                }
            }
        });
        table1.setModel(model);
    }

    // Prompt the user to select the mods folder
    private void findDirectories() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select the mods folder");
        fileChooser.setApproveButtonMnemonic('s');
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setApproveButtonToolTipText("Select the mods folder");
        fileChooser.showDialog(null, "Select");
        System.out.println("Selected mods folder: " + fileChooser.getSelectedFile());
        File selectedFile = fileChooser.getSelectedFile();
        // Load the mod folder path for testing
        // If the selected file ends with Mods/Mods, set the mod folder path to the parent directory
        if (selectedFile.getAbsolutePath().endsWith("Mods" + File.separator + "Mods")) {
            selectedFile = selectedFile.getParentFile();
        }
        modFolderPath = selectedFile.toPath();

        // Set Downloads folder path above Mods folder, create if it doesn't exist
        downloadFolderPath = Paths.get(modFolderPath.getParent().toString(), "Downloads");
        File downloadFolder = downloadFolderPath.toFile();
        if (!downloadFolder.exists()) {
            downloadFolder.mkdir();
        }
        // Create a Disabled folder in the directory above the mods folder if it doesn't exist
        File disabledFolder = new File(modFolderPath.getParent().toString(), "Disabled");
        if (!disabledFolder.exists()) {
            disabledFolder.mkdir();
        }
    }


    private void createUIComponents() {
        table1 = new JTable();
        mainPanel = new JPanel();
        findDirectories();
        initializeModList();
        refreshModTable();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setBackground(new Color(-1));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setBackground(new Color(-1));
        mainPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Manage Mods", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scrollPane1.setViewportView(table1);
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setBackground(new Color(-1));
        mainPanel.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-4473925)), "Install Mods", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-1));
        panel1.setForeground(new Color(-1));
        scrollPane2.setViewportView(panel1);
        installFromURLButton = new JButton();
        installFromURLButton.setText("Install from GitHub repo URL");
        panel1.add(installFromURLButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        installFromZipButton = new JButton();
        installFromZipButton.setText("Install from .zip");
        panel1.add(installFromZipButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        scrollPane3.setBackground(new Color(-1));
        scrollPane3.setForeground(new Color(-1));
        mainPanel.add(scrollPane3, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-4473925)), "Miscellaneous", TitledBorder.RIGHT, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, -1, scrollPane3.getFont()), null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setBackground(new Color(-1));
        scrollPane3.setViewportView(panel2);
        openModFolderButton = new JButton();
        openModFolderButton.setText("Open mods folder");
        panel2.add(openModFolderButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openGameFolderButton = new JButton();
        openGameFolderButton.setText("Open Balatro folder");
        panel2.add(openGameFolderButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportModsFolderToZip = new JButton();
        exportModsFolderToZip.setText("Export mods folder");
        panel2.add(exportModsFolderToZip, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showGithubLink = new JButton();
        showGithubLink.setText("View on GitHub");
        panel2.add(showGithubLink, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, Font.BOLD, 24, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("MultiBal");
        mainPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, Font.ITALIC, -1, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("A Balatro mod manager by Evelyn Drake.");
        mainPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
