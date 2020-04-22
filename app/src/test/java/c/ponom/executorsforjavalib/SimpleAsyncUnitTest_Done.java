package c.ponom.executorsforjavalib;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;



public class SimpleAsyncUnitTest_Done {

    private static final AtomicInteger unitTestCounterAtomic=new AtomicInteger();
    private static final int taskCounter=1000;
    private static final int timeForTest=1000; // in ms
    private static final float percentOfErrors =0.01f;




    //todo - тесты:

    @Test
    public void testLaunchTasksRunnable() throws InterruptedException {

        unitTestCounterAtomic.set(0);
        // убеждаемся что отрабатывают все X*1000 посланных заданий за разумное
        // время, причем эксепшны не влияют на это (обработанные в run или ушедшие наверх в call)

        //   ИТОГИ тестирования  - непонятным причинам из-за чего из 4000 прогонов получалось 3987
        //   Потом разобрался - потоки одновременно дергали счетчик - поставил Atomic
        //   все заработало. TODO Но атомики в качестве счетчика не рекомендуются = лучше синхронизацию


        Runnable[] testRunnableArray = new Runnable[taskCounter];
        Arrays.fill(testRunnableArray,unitTestRunnable);

        Callable[] testCallableArray = new Callable[taskCounter];
        Arrays.fill(testCallableArray,unitTestCallable);

        SimpleAsyncs.launchTasks(3 , testRunnableArray);
        SimpleAsyncs.launchTasks(70 , testCallableArray);
        SimpleAsyncs.launchTasks(3 , testRunnableArray);
        SimpleAsyncs.launchTasks(70 , testCallableArray);

        Thread.sleep(timeForTest);

        assertEquals(taskCounter*4,unitTestCounterAtomic.intValue());
    }





    private  final Runnable unitTestRunnable= new Runnable() {
        @Override
        public void run() {

            unitTestCounterAtomic.incrementAndGet();
            int i;
            try {
                // тестируем влияние на работу эксепшнов
                Thread.sleep(1);
                if (Math.random() < percentOfErrors)  i=42/0;

            } catch (Exception e) {
                //e.printStackTrace();
            }

        }
    };

    private  final Callable unitTestCallable= new Callable() {
        @Override
        public Object call() throws Exception {

            unitTestCounterAtomic.incrementAndGet();
            Thread.sleep(1);

            // тестируем влияние на работу эксепшнов
            if (Math.random()<percentOfErrors) throw new Exception();
            return null;
        }

    };
}

