package fr.plop.contexts.game.instance.event.domain;

import java.util.concurrent.*;

public class GameEventQueue {
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    public GameEventQueue() {
        ExecutorService queueExecutor = Executors.newSingleThreadExecutor(r -> { //BUG IS TRY WITH RESOURCES
            Thread t = new Thread(r, "EventQueue-Thread");
            t.setDaemon(false);
            return t;
        });

        queueExecutor.submit(this::processQueue);
    }

    public void enqueue(Runnable task) {
        try {
            queue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Event queuing interrupted", e);
        }
    }

    private void processQueue() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Runnable task = queue.take();
                try {
                    task.run();
                } catch (Exception e) {
                    System.err.println("Error processing queued event: " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int queueSize() {
        return queue.size();
    }
}
