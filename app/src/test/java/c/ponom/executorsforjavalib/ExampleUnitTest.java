package c.ponom.executorsforjavalib;

import android.util.Log;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static c.ponom.executorsforjavalib.SimpleAsyncsTesting.TAG;
import static c.ponom.executorsforjavalib.SimpleAsyncsTesting.counter;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class ExampleUnitTest {


    static int unitTestCounter;






    @Test
    public void testLaunchTasksRunnable() throws InterruptedException {

    }




     private  final Runnable unitTestRunnable= new Runnable() {
        @Override
        public void run() {
            unitTestCounter++;
            int i;
            try {
                if (Math.random() < 0.00)  i=42/0;

            } catch (Exception e) {

            }

        }
    };

    private  final Callable unitTestCallable= new Callable() {
        @Override
        public Object call() throws Exception {
            unitTestCounter++;
            // тестируем влияние на работу эксепшнов
            if (Math.random()<0.000) throw new Exception();
            return null;
        }

    };
}






/*

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



    public static ThreadPoolExecutor launchTasks(int threads, Runnable...tasks){
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        for (Runnable task:tasks){
            executor.submit(task);

        }
        executor.shutdown();
        return executor;
    }

    public static ThreadPoolExecutor launchTasks(int threads, Callable...tasks){
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        for (Callable task:tasks){
            executor.submit(task);
        }
        executor.shutdown();
        return executor;
    }






 */

