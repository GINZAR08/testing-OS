import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class runMLFQ {
    public static void run(List<main.Task> tasks, int quantum) {
        if (quantum <= 0) { System.out.println("Quantum must be > 0."); return; }
        if (tasks.isEmpty()) { System.out.println("No tasks to schedule."); return; }

        System.out.println("\n── MLFQ Scheduler (quantum=" + quantum + "ms) ──");

        Queue<main.Task> young = new ArrayDeque<>();
        Queue<main.Task> old = new ArrayDeque<>();

        for (main.Task t : tasks) young.add(t.copy()); // all tasks start in young

        int time = 0;

        while (!young.isEmpty() || !old.isEmpty()) {

            if (!young.isEmpty()) {
                // ── Take from young, run it, send to old ──
                main.Task task = young.poll();

                if (task.getState() == Thread.State.TERMINATED) continue;

                int before = task.remainingBurst;
                task.giveQuantum(quantum);
                int ran = before - task.remainingBurst;

                System.out.println("[t=" + time + "] YOUNG: " + task.id
                        + " ran for " + ran + "ms"
                        + " | remaining=" + task.remainingBurst + "ms"
                        + " | state=" + task.getState());
                time += ran;

                if (task.remainingBurst <= 0) {
                    task.waitUntilDone();
                    System.out.println(  task.id + " TERMINATED");
                } else {
                    // Not done — move to old
                    System.out.println( task.id + " moved to OLD");
                    old.add(task);
                }

            } else {
                // ── Young is empty, take from old, run it, send back to young ──
                main.Task task = old.poll();

                if (task.getState() == Thread.State.TERMINATED) continue;

                int before = task.remainingBurst;
                task.giveQuantum(quantum);
                int ran = before - task.remainingBurst;

                System.out.println("[t=" + time + "] OLD:   " + task.id
                        + " ran for " + ran + "ms"
                        + " | remaining=" + task.remainingBurst + "ms"
                        + " | state=" + task.getState());
                time += ran;

                if (task.remainingBurst <= 0) {
                    task.waitUntilDone();
                    System.out.println( task.id + " TERMINATED");
                } else {
                    // Not done — move back to young
                    System.out.println(  task.id + " moved back to YOUNG");
                    young.add(task);
                }
            }
        }

        System.out.println("── MLFQ complete ──\n");
    }
}