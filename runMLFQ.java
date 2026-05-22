import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class runMLFQ {
    public static void run(List<main.Task> tasks, int quantum) {
        if (quantum <= 0) { System.out.println("Quantum must be > 0."); return; }
        if (tasks.isEmpty()) { System.out.println("No tasks to schedule."); return; }

        System.out.println("\n── MLFQ Scheduler (q0=" + quantum + "ms, q1=" + (quantum * 2) + "ms) ──");

        Queue<main.Task> level0 = new ArrayDeque<>(); // young — short quantum
        Queue<main.Task> level1 = new ArrayDeque<>(); // old   — longer quantum

        for (main.Task t : tasks) level0.add(t.copy()); // fresh copies

        int time = 0;

        while (!level0.isEmpty() || !level1.isEmpty()) {

            // ── Always service level 0 first ──
            if (!level0.isEmpty()) {
                main.Task task = level0.poll();

                if (task.getState() == Thread.State.TERMINATED) continue;

                int before = task.remainingBurst;
                task.giveQuantum(quantum);
                int ran = before - task.remainingBurst;

                System.out.println("[t=" + time + "] L0: " + task.id
                        + " ran for " + ran + "ms"
                        + " | remaining=" + task.remainingBurst + "ms"
                        + " | state=" + task.getState());
                time += ran;

                if (task.remainingBurst > 0) {
                    // Didn't finish — demote to level 1
                    System.out.println("  ↓ " + task.id + " demoted to Level 1");
                    level1.add(task);
                } else {
                    task.waitUntilDone();
                }
            }
        }
    }
}