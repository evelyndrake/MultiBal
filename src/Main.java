import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

public class Main {

    private JPanel test;
    private JTable table1;
    private JButton installFromURLButton;
    private JButton installFromZipButton;
    private ArrayList<Mod> modList;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Main");
        frame.setContentPane(new Main().test);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        table1 = new JTable();
        // Show a dialog to select the mods folder
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select the mods folder");
        fileChooser.setApproveButtonText("Select");
        fileChooser.setApproveButtonMnemonic('s');
        fileChooser.setApproveButtonToolTipText("Select the mods folder");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.showDialog(null, "Select");
        Path modFolderPath = fileChooser.getSelectedFile().toPath();
//        Path modFolderPath = Paths.get()
        System.out.println(modFolderPath);
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

        // Create a disabled folder in the directory above the mods folder if it doesn't exist
        File disabledFolder = new File(modFolderPath.getParent().toString(), "Disabled");
        if (!disabledFolder.exists()) {
            disabledFolder.mkdir();
        }

    }
}
