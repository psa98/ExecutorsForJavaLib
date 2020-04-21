package c.ponom.executorsforjavalib;

import android.util.Log;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static c.ponom.executorsforjavalib.SimpleAsyncsTesting.TAG;
import static c.ponom.executorsforjavalib.SimpleAsyncsTesting.counter;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class SimpleAsyncUnitTest {

    static int unitTestCounter =0;
    static AtomicInteger unitTestCounterAtomic=new AtomicInteger();
    int taskCounter=10000;
    int timeForTest=10000; // in ms
    float percentOfErrors =0.02f;




    @Test
    public void testLaunchTasksRunnable() throws InterruptedException {

        unitTestCounter=0;
        unitTestCounterAtomic.set(0);
        // убеждаемся что отрабатывают все X*1000 посланных заданий за разумное
        // время, причем эксепшны не влияют на это (обработанные в run или ушедшие наверх в call)

        //   ИТОГИ тестирования  - непонятным причинам из-за чего из 4000 прогонов получалось 3987
        //   Потом разобрался - потоки одновременно дергали счетчик - поставил Atomic
        //   все заработало .


        Runnable[] testRunnableArray = new Runnable[taskCounter];
        Arrays.fill(testRunnableArray,unitTestRunnable);

        Callable[] testCallableArray = new Callable[taskCounter];
        Arrays.fill(testCallableArray,unitTestCallable);

        SimpleAsyncs.launchTasks(3 , testRunnableArray);
        SimpleAsyncs.launchTasks(70 , testCallableArray);
        SimpleAsyncs.launchTasks(3 , testRunnableArray);
        SimpleAsyncs.launchTasks(70 , testCallableArray);


        Thread.sleep(timeForTest);
        System.out.println("count "+ unitTestCounter);

        assertEquals(taskCounter*4,unitTestCounter);
        assertEquals(taskCounter*4,unitTestCounterAtomic.intValue());
    }


    private synchronized void increment(){
        unitTestCounter++;

    }


    private  final Runnable unitTestRunnable= new Runnable() {
        @Override
        public void run() {
            increment();
            unitTestCounterAtomic.incrementAndGet();
            int i;
            try {

                Thread.sleep(1);
                if (Math.random() < percentOfErrors)  i=42/0;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private  final Callable unitTestCallable= new Callable() {
        @Override
        public Object call() throws Exception {
            increment();
            unitTestCounterAtomic.incrementAndGet();
            Thread.sleep(1);
            // тестируем влияние на работу эксепшнов
            if (Math.random()<percentOfErrors) throw new Exception();
            return null;
        }

    };
}

