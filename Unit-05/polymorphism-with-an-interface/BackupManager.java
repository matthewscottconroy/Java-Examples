public class BackupManager {
    public void performBackup(Storage storage, String data) {
        System.out.println("Starting backup...");
        storage.save(data);
        System.out.println(storage.load());
        System.out.println("Backup complete.\n");
    }
}

