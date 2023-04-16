package org.mcphackers.mcp.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TaskExecutor implements ExecutorService {
    private final List<Task> tasks = new ArrayList<>();
    private final ExecutorService pool;

    public TaskExecutor(int threads, Task... tasks) {
        this.pool = Executors.newFixedThreadPool(threads);
        this.tasks.addAll(Arrays.asList(tasks));
    }

    public TaskExecutor(int threads, List<Task> tasks) {
        this.pool = Executors.newFixedThreadPool(threads);
        this.tasks.addAll(tasks);
    }

    public TaskExecutor(ExecutorService pool, Task... tasks) {
        this.pool = pool;
        this.tasks.addAll(Arrays.asList(tasks));
    }

    public TaskExecutor(ExecutorService pool, List<Task> tasks) {
        this.pool = null;
        this.tasks.addAll(tasks);
    }

    /**
     * Returns all tasks in this executor.
     *
     * @return List of tasks
     */
    public List<Task> tasks() {
        return tasks;
    }

    /**
     * Creates a new TaskExecutor for tasks that match the given side.
     *
     * @param side Side to filter by
     * @return New TaskExecutor
     */
    public TaskExecutor side(Task.Side side) {
        return new TaskExecutor(pool, this.tasks.stream().filter(task -> task.side == side || task.side == Task.Side.ANY).collect(Collectors.toList()));
    }

    public void run() throws InterruptedException, ExecutionException {
        if (pool.isShutdown()) throw new IllegalStateException("ExecutorService is shutdown.");
        List<Future<?>> futures = new ArrayList<>();
        for (Task task : tasks) futures.add(pool.submit(task));
        for (Future<?> future : futures) future.get();
        pool.shutdown();
    }


    @Override
    public void shutdown() {
        pool.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return pool.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return pool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return pool.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return pool.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        throw new UnsupportedOperationException("Not available in TaskExecutor.");
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        throw new UnsupportedOperationException("Not available in TaskExecutor.");
    }

    @Override
    public Future<?> submit(Runnable task) {
        throw new UnsupportedOperationException("Not available in TaskExecutor.");
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw new UnsupportedOperationException("Not available in TaskExecutor.");
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not available in TaskExecutor.");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("Not available in TaskExecutor.");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("Not available in TaskExecutor.");
    }

    @Override
    public void execute(Runnable command) {
        throw new UnsupportedOperationException("Not available in TaskExecutor.");
    }
}
