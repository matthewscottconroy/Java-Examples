public class Main {
    public static void main(String[] args) {
        Storage dbStorage = new DatabaseStorage();
        Storage fileStorage = new FileStorage("backup.txt");

        BackupManager manager = new BackupManager();

        manager.performBackup(dbStorage, "User data: Alice, score=93");
        manager.performBackup(fileStorage, "User data: Bob, score=88");
    }
}

