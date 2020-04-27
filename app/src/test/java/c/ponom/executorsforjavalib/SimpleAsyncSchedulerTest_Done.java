package c.ponom.executorsforjavalib;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.random;
import static org.junit.Assert.assertEquals;



public class SimpleAsyncSchedulerTest_Done {

    private static final int REPETITIONS_COUNTER =10;
    private static final int TARGET_TASKS_COUNT =100;
    private static final int TIMEOUT=100; // in ms
    private static final float PERCENT_OF_EXCEPTIONS =0.05f;


    private int callableTasksCounter=0;
    private int runnableTasksCounter=0;
    private final Object tasksLockRunnables = new Object();
    private final Object tasksLockCallables = new Object();
    private final ArrayList<Integer> commonArray= new ArrayList<>();


      /* Тестируется:
       1.   То, что отрабатывают все X посланных заданий за разумное
            время, причем исключения не влияют на это (обработанные в run или
            ушедшие наверх в call),
       2.    что в пределах синхронизированного блока можно работать с общими данными
       */

    @Test
    public void testLaunchTasksRunnable() throws InterruptedException {


        Runnable[] testRunnableArray = new Runnable[TARGET_TASKS_COUNT];
        Arrays.fill(testRunnableArray, unitTestRunnable);

        Callable[] testCallableArray = new Callable[TARGET_TASKS_COUNT];
        Arrays.fill(testCallableArray, unitTestCallable);

        /*
        1. убеждаемся что отрабатывают все X посланных заданий за разумное
        время, причем исключения  не влияют на это (обработанные в run или
        ушедшие наверх в call)
        2. что в пределах синхронизированных блоков
        задачи и коллбэка можно работать с общими данными

        */

        for (int batch = 1; batch != REPETITIONS_COUNTER; batch++) {

            runnableTasksCounter = 0;
            callableTasksCounter =0;
            commonArray.clear();


            SimpleAsyncScheduler.launchTasks((int) (10 * random() + 1), testRunnableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);

            SimpleAsyncScheduler.launchTasks((int) (20 * random() + 1), testRunnableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);

            SimpleAsyncScheduler.launchTasks((int) (50 * random() + 1), testRunnableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);


            SimpleAsyncScheduler.launchTasks((int) (100 * random() + 1), testRunnableArray).
                    awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);





            callableTasksCounter = 0;
            runnableTasksCounter = 0;


            SimpleAsyncScheduler.launchTasks((int) (10 * random() + 1), testCallableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);

            SimpleAsyncScheduler.launchTasks((int) (20 * random() + 1), testCallableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);


            SimpleAsyncScheduler.launchTasks((int) (50 * random() + 1), testRunnableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);


            SimpleAsyncScheduler.launchTasks((int) (100 * random() + 1), testRunnableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);

            System.out.println("Runnable count "+runnableTasksCounter);
            System.out.println("Callable count "+callableTasksCounter);
            assertEquals(TARGET_TASKS_COUNT * 2, callableTasksCounter);
            assertEquals(TARGET_TASKS_COUNT * 2, runnableTasksCounter);
            assertEquals(TARGET_TASKS_COUNT*4*2, commonArray.size());
            System.out.println("Batch "+batch);
            // todo -стоит добавить тестирование правильности итогов callable

        }
        System.out.println("Total "+REPETITIONS_COUNTER*TARGET_TASKS_COUNT*2*4);
    }





    private  final Runnable unitTestRunnable= new Runnable() {
        @Override
        public void run() {

            // тут может находиться тяжелый или блокирующий код,
            // не использующий общих данных
            //
            int i=0;
            synchronized (tasksLockRunnables) {

                commonArray.add( runnableTasksCounter);
                runnableTasksCounter++;
                // тут может находиться код, работающий с общими данными при использовании
                // общего мутекса
                try {
                    // тестируем влияние на работу эксепшнов - их надо обработать в Runnable
                    if (random() < PERCENT_OF_EXCEPTIONS) i = 42 / 0;
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    };

    private  final Callable unitTestCallable= new Callable () {
        @Override
        public Object call () throws Exception {

            // тут может находиться тяжелый или блокирующий код,
            // не использующий общих данных
            //
            int i=0;

                synchronized (tasksLockCallables) {
                callableTasksCounter++;
                commonArray.add(callableTasksCounter);
                // тут может находиться код, работающий с общими данными при использовании
                // общего мутекса
                double result =random();
                try {
                    // тестируем влияние на работу эксепшнов
                    //
                    if ( result< PERCENT_OF_EXCEPTIONS) i = 42 / 0;

                } catch (Exception e) {
                    /// e.printStackTrace(); // обработка исключения,
                    // или, возможно, возврат его как результата через return exception;
                }
            return result;}
       }
    };

}

