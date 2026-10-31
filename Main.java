import src.User;
import src.HomeFrame;
import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Load local user database
        Map<String, User> userDatabase = new HashMap<>();
        String fileName = "users.txt";
        File file = new File(fileName);
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (!line.isEmpty()) {
                        String[] parts = line.split(",");
                        if (parts.length == 2) {
                            userDatabase.put(parts[0], new User(parts[0], parts[1]));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        if (userDatabase.isEmpty()) {
            userDatabase.put("admin", new User("admin", "admin123"));
        }

        SwingUtilities.invokeLater(() -> {
            new HomeFrame(userDatabase, fileName).setVisible(true);
        });
    }
}