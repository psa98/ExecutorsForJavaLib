package c.ponom.executorsforjavalib;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class SimpleAsyncs extends ThreadPoolExecutor {
    /// обойдемся без расширения может?


    public SimpleAsyncs(int corePoolSize,
                        int maximumPoolSize,
                        long keepAliveTime,
                        TimeUnit unit,
                        BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /**
     *
     * <p> Метод  создает экзекьютор с указанным числом потоков, которому
     * можно передать задачу. Пример использования:
     *launchTasks(10,myRunnable);
     *
     * @param
     * threads
     *
     *
     * @param
     * tasks - Runnable/Callable к исполнению или их массив (список)
     *
     * метод launchTask выполняет переданный ему массив Runnable,
     * в указанном числе потоков, без вызова любых коллбэков и возвращения результатов
     * прочие методы базового класса скрыты для упрощения работы
     *
     *
     * @return
     * Экзекьютор, в котором при желании можно вызвать управляющие им методы - прекращение исполнения,
     * заброс состояния
     */


    public static ThreadPoolExecutor launchTasks(int threads,Runnable...tasks){
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        for (Runnable task:tasks){
            executor.submit(task);

        }
        executor.shutdown();
        return executor;
    }

    public static ThreadPoolExecutor launchTasks(int threads,Callable...tasks){
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        for (Callable task:tasks){
            executor.submit(task);
        }
        executor.shutdown();
        return executor;
    }

    /*интересные фишки выявленные по итогам тестов.
    awaitTermination блокирует до того как последние потоки подали на выполнение, а не до исполнения последнего
    - пользоваться им надо с умом
    - никак не могу понять почему из 4000 запущенных на исполнение задач отпахивают скажем 3960.
    Возможно итог на экран  уходит до завершения последних заданий, и как добиться другого - я не очень понимаю
    -


     */
}
