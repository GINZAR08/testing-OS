import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {

        String FilePath = "enrol.csv";
        List<Task> tasks = readCSVTasks(FilePath);

        try (Scanner console = new Scanner(System.in)) {


            while (true) {
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
                            runPriorityQueue.run(tasks);

                            break;
                        case 2:
                            System.out.print("Enter quantum: ");
                            int quantumRR = console.nextInt();
                            runRoundRobin.run(tasks, quantumRR);

                            break;
                        case 3:
                            System.out.print("Enter quantum: ");
                            int quantumMLFQ = console.nextInt();
                            runMLFQ.run(tasks, quantumMLFQ);
                            break;
                        case 4:
                            List<Task> newTasks = runSFC.run();
                            if (!newTasks.isEmpty()) {
                                tasks = newTasks;
                            }
                            /** SFC doesnt work**/
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

    static class Task extends Thread implements Comparable<Task> {
        String id;
        int burst;
        int remainingBurst;
        int priority;
        long insertionIndex;
        private final Object lock = new Object();

        Task(String id, int burst, int priority, long insertionIndex) {
            this.id = id;
            this.burst = burst;
            this.remainingBurst = burst;
            this.priority = priority;
            this.insertionIndex = insertionIndex;
            this.setName(id);
        }

        @Override
        public void run() {
            while (remainingBurst > 0) {
                // ── Pause: wait until the scheduler wakes us ──
                synchronized (lock) {
                    try {
                        lock.wait(); // WAITING state — scheduler calls interrupt via giveQuantum
                    } catch (InterruptedException e) {

                    }
                }
                if (remainingBurst <= 0) break;
                try {
                    Thread.sleep(20); // simulate work unit
                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                }
            }
            // Thread naturally reaches TERMINATED state here
        }
        //giveQuantum is the method that acts as the scheduler giving CPU time to a thread
        public void giveQuantum(int quantum) {
            Thread.State state = getState();

            if (state == Thread.State.NEW) {
                start(); // NEW → RUNNABLE, thread will immediately hit lock.wait()
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Deduct the quantum from remaining burst
            int slice = Math.min(remainingBurst, quantum);
            remainingBurst -= slice;

            // Wake the thread so it can do its sleep(20) work simulation
            synchronized (lock) {
                lock.notify();
            }

            // Wait for the thread to finish its sleep(25) work unit
            // (or terminate if it just finished)
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Waits for this thread to fully reach TERMINATED state.
         */
        public void waitUntilDone() {
            try {
                join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public int compareTo(Task other) {
            if (this.priority != other.priority) {
                return Integer.compare(this.priority, other.priority);
            }
            return Long.compare(this.insertionIndex, other.insertionIndex);
        }

        /**
         * Produce a fresh copy of this Task (new thread, same data).
         */
        public Task copy() {
            return new Task(id, burst, priority, insertionIndex);
        }

        @Override
        public String toString() {
            return id + "(pri=" + priority + ", burst=" + burst + ")";
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
}
