import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class runRoundRobin {
    public static void run(List<main.Task> tasks, int quantum) {
        if (quantum <= 0) { System.out.println("Quantum must be > 0."); return; }
        if (tasks.isEmpty()) { System.out.println("No tasks to schedule."); return; }

        System.out.println("\n── Round Robin Scheduler (quantum=" + quantum + "ms) ──");

        Queue<main.Task> queue = new ArrayDeque<>();
        for (main.Task t : tasks) queue.add(t.copy()); // fresh copies

        int time = 0;
        while (!queue.isEmpty()) {
            main.Task current = queue.poll();

            if (current.getState() == Thread.State.TERMINATED) continue;

            int before = current.remainingBurst;
            current.giveQuantum(quantum);
            int ran = before - current.remainingBurst;

            System.out.println("[t=" + time + "] " + current.id
                    + " ran for " + ran + "ms"
                    + " | remaining=" + current.remainingBurst + "ms"
                    + " | state=" + current.getState());

            time += ran;

            if (current.remainingBurst > 0) {
                queue.add(current); // re-queue for next turn
            } else {
                current.waitUntilDone();
                System.out.println("  → " + current.id + " TERMINATED (state=" + current.getState() + ")");
            }
        }

        System.out.println("── Round Robin complete ──\n");
    }
}