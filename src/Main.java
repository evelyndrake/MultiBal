import org.zeroturnaround.zip.ZipUtil;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

public class Main {

    private JPanel test;
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
                ZipUtil.pack(new File(modFolderPath.toString()), zipFile);
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
        frame.setContentPane(new Main().test);
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
                    java.lang.String.class, java.lang.Boolean.class
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
        fileChooser.setApproveButtonText("Select");
        fileChooser.setApproveButtonMnemonic('s');
        fileChooser.setApproveButtonToolTipText("Select the mods folder");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.showDialog(null, "Select");
        modFolderPath = fileChooser.getSelectedFile().toPath();
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

    // Create UI components and initialize the mod list on application startup
    private void createUIComponents() {
        findDirectories();
        initializeModList();
        refreshModTable();
    }
}
