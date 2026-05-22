import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class runSFC {
    public static List<main.Task> run() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());

            List<main.Task> tasks = main.readCSVTasks(selectedFile.getAbsolutePath());

            if (tasks.isEmpty()) {
                System.out.println("No tasks found in the selected file.");
            }

            return tasks;
        } else {
            System.out.println("No file selected.");
            return new ArrayList<>();
        }
    }
}
