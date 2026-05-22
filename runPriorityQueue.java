import java.util.List;
import java.util.PriorityQueue;

public class runPriorityQueue {
    public static void run(List<main.Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No tasks to schedule.");
            return;
        }

        System.out.println("\n── Priority Queue Scheduler ──");

        PriorityQueue<main.Task> pq = new PriorityQueue<>();
        for (main.Task t : tasks) pq.add(t.copy()); // fresh copies

        int time = 0;
        while (!pq.isEmpty()) {
            main.Task task = pq.poll();

            System.out.println("[t=" + time + "] Running " + task.id
                    + " | priority=" + task.priority
                    + " | burst=" + task.burst + "ms");

            // Give full burst at once — non-preemptive
            while (task.remainingBurst > 0) {
                task.giveQuantum(task.remainingBurst);
            }

            task.waitUntilDone();

            System.out.println("  → " + task.id + " TERMINATED (state=" + task.getState() + ")");
            time += task.burst;
        }

        System.out.println("── Priority Queue complete ──\n");
    }
}