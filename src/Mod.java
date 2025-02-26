import java.io.File;

public class Mod {
    private final File modFolder;
    private final String modName;
    private boolean isEnabled;
    private final File disabledFolder;
    private final File modsFolder;
    public Mod(File modFolder, String modName, boolean isEnabled) {
        this.modFolder = modFolder;
        this.modName = modName;
        this.isEnabled = isEnabled;
        // Find the Disabled folder in the folder above the mods folder
        disabledFolder = new File(modFolder.getParentFile().getParentFile(), "Disabled");
        modsFolder = new File(modFolder.getParentFile().getParentFile(), "Mods");
    }

    public File getModFolder() {
        return modFolder;
    }

    public String getModName() {
        return modName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void toggleEnabled() {
        isEnabled = !isEnabled;
        System.out.println(modName + " is now " + (isEnabled ? "enabled" : "disabled"));
        if (isEnabled) {
            // Move the mod folder from the Disabled folder to the Mods folder
            File oldLocation = new File(disabledFolder, modFolder.getName());
            File newLocation = new File(modsFolder, modFolder.getName());
            oldLocation.renameTo(newLocation);
        } else {
            // Move the mod folder from the Mods folder to the Disabled folder
            File oldLocation = new File(modsFolder, modFolder.getName());
            File newLocation = new File(disabledFolder, modFolder.getName());
            oldLocation.renameTo(newLocation);
        }

    }
}
