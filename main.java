import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.io.File;
import javax.swing.JFileChooser;

public class main {
    public static void main(String[] args) {

        String FilePath = "enrol.csv";
        List<Task> tasks = readCSVTasks(FilePath);

        try (Scanner console = new Scanner(System.in)) {
            boolean validChoice = false;

            while (!validChoice) {
                try {
                    System.out.println("Choose a scheduling option:");
                    System.out.println("1. Priority Queue");
                    System.out.println("2. Round Robin");
                    System.out.println("3. MLFQ");
                    System.out.println("4. Load new file");
                    System.out.println("5. Exit");
                    System.out.print("Enter choice: ");

                    int choice = console.nextInt();

                    if (choice < 1 || choice > 5) {
                        System.out.println("Invalid number. Please enter a number between 1 and 5.\n");
                        continue;
                    }

                    switch (choice) {
                        case 1:
                            runPriorityQueueTask(tasks);
                            validChoice = true;
                            break;
                        case 2:
                            System.out.print("Enter quantum: ");
                            int quantumRR = console.nextInt();
                            runRoundRobin(tasks, quantumRR);
                            validChoice = true;
                            break;
                        case 3:
                            // implament MLFQ
                            validChoice = true;
                            break;
                        case 4:
                            runSFC();
                            validChoice = true;
                            break;
                        case 5:
                            System.out.println("Exiting...");
                            return;
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number.\n");
                    console.nextLine();
                }
            }
        }
    }

    static class Task implements Comparable<Task> {
        String id;
        int burst;
        int remainingBurst;
        int priority;
        long insertionIndex;

        Task(String id, int burst, int priority, long insertionIndex) {
            this.id = id;
            this.burst = burst;
            this.remainingBurst = burst;
            this.priority = priority;
            this.insertionIndex = insertionIndex;
        }

        @Override
        public int compareTo(Task other) {
            if (this.priority != other.priority) {
                return Integer.compare(this.priority, other.priority);
            }
            return Long.compare(this.insertionIndex, other.insertionIndex);
        }
    }

    public static List<Task> readCSVTasks(String filePath) {
        List<Task> tasks = new ArrayList<>();
        long insertionIndex = 0;

        if (filePath == null || filePath.isEmpty()) {
            System.out.println("File path is null or empty.");
            return tasks;
        }
        try (FileReader fr = new FileReader(filePath);
             Scanner sc = new Scanner(fr)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.out.println("Invalid line format: " + line);
                    continue;
                }
                String id = parts[0].trim();
                int burst = Integer.parseInt(parts[1].trim());
                int priority = Integer.parseInt(parts[2].trim());
                tasks.add(new Task(id, burst, priority, insertionIndex++));
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error parsing numbers: " + e.getMessage());
        }
        return tasks;
    }

    public static void runSFC() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            List<Task> tasks = readCSVTasks(selectedFile.getAbsolutePath());

            if (tasks.isEmpty()) {
                System.out.println("No tasks found in the selected file.");
                return;
            }
        } else {
            System.out.println("No file selected.");
        }
    }

    public static void runMLFQ(List<Queue<Task>> queues, int quantum) {
        Queue<Task> young = queues.get(0);
        Queue<Task> old = queues.get(1);
        Queue<Task> oldest = queues.get(2);

        boolean isyoung = true;
        boolean isold = false;
        while (!young.isEmpty() || !old.isEmpty() || !oldest.isEmpty()) {
            if (isyoung && !young.isEmpty()) {
                Task current = young.poll();

                int slice = Math.min(current.remainingBurst, quantum);
                current.remainingBurst -= slice;
                System.out.println("" + current.id + " ran in young queue for " + slice + " units, remaining "
                        + current.remainingBurst);
                if (current.remainingBurst > 0) {
                    isyoung = false;
                    isold = true;
                    old.add(current);
                } else {
                    System.out.println(current.id + " completed.");
                }
            } else if (isold && !old.isEmpty()) {
                Task current = old.poll();
                int slice = Math.min(current.remainingBurst, quantum * 2);
                current.remainingBurst -= slice;
                System.out.println("" + current.id + "ran in old queue for "+ slice + " units, remaining"+ current.remainingBurst);
                if (current.remainingBurst > 0){
                    isold = false;
                    oldest.add(current);
                }
            }    
                
                else if (!oldest.isEmpty()){
                    Task current = oldest.poll();
                    int slice = Math.min(current.remainingBurst, quantum * 4);
                    current.remainingBurst -= slice;
                    System.out.println(current.id + " ran in oldest queue for " + slice + " units, remaining " + current.remainingBurst);
                    if (current.remainingBurst > 0){
                        oldest.add (current);
                    } else {
                        System.out.println(current.id + " completed.");
                    }
                }
        }

    }

    public static void runPriorityQueueTask(List<Task> tasks) {
        PriorityQueue<Task> pq = new PriorityQueue<>(tasks);
        if (pq.isEmpty()) {
            System.out.println("No tasks to schedule.");
            return;
        }
        System.out.println("Priority Queue execution order:");
        while (!pq.isEmpty()) {
            Task t = pq.poll();
            System.out.println(t.id + " with priority " + t.priority + " and burst " + t.burst);
        }
    }

    public static void runRoundRobin(List<Task> tasks, int quantum) {
        if (quantum <= 0) {
            System.out.println("Time quantum must be greater than 0.");
            return;
        }
        if (tasks.isEmpty()) {
            System.out.println("No tasks to schedule.");
            return;
        }
        Queue<Task> queue = new ArrayDeque<>();
        for (Task t : tasks) {
            queue.add(new Task(t.id, t.burst, t.priority, t.insertionIndex));
        }

        System.out.println("Round Robin execution order:");
        while (!queue.isEmpty()) {
            Task current = queue.poll();
            int slice = Math.min(current.remainingBurst, quantum);
            current.remainingBurst -= slice;

            System.out.println(current.id + " ran for " + slice + " units, remaining " + current.remainingBurst);

            if (current.remainingBurst > 0) {
                queue.add(current);
            } else {
                System.out.println(current.id + " completed.");

            }
        }
    }

}
