package c.ponom.executorsforjavalib;

import android.content.Context;
import android.util.Log;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;


import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static c.ponom.executorsforjavalib.SimpleAsyncsTesting.TAG;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ExampleInstrumentedTest {

    static int unitTestCounter;




    @Test
    public void testLaunchTasksRunnable() throws InterruptedException {
        unitTestCounter=0;
        int taskCounter=1000;

        //убеждаемся что отрабатывают все 10000 посланных заданий за разумное
        // время, причем эксепшны не влияют на это (обработанные в run или ушедшие наверх в call)


        Runnable[] testRunnableArray = new Runnable[taskCounter];
        Arrays.fill(testRunnableArray,unitTestRunnable);

        Callable[] testCallableArray = new Callable[taskCounter];
        Arrays.fill(testCallableArray,unitTestCallable);

      int i = 1;

            // проверяем для потоков от 5 до 50

            SimpleAsyncs.launchTasks(i*5, testRunnableArray);
            SimpleAsyncs.launchTasks(i*5, testCallableArray);
            SimpleAsyncs.launchTasks(i*5, testRunnableArray);
            SimpleAsyncs.launchTasks(i*5, testCallableArray);





        Thread.sleep(15000);
        System.out.println("count "+ unitTestCounter);

    }




    private  final Runnable unitTestRunnable= new Runnable() {
        @Override
        public void run() {
            unitTestCounter++;
            int i;
            try {
                if (Math.random() < 0.1f)  i=42/0;

            } catch (Exception e) {
              e.printStackTrace();
            }

        }
    };

    private  final Callable unitTestCallable= new Callable() {
        @Override
        public Object call() throws Exception {
            unitTestCounter++;
            // тестируем влияние на работу эксепшнов
            if (Math.random()<0.01) throw new Exception();
            return null;
        }

    };
}
