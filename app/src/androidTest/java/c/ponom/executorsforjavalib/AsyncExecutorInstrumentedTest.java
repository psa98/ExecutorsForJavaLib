package c.ponom.executorsforjavalib;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class AsyncExecutorInstrumentedTest {

    static int unitTestCounter;





    @Test
    public void testLaunchTasksRunnable() throws InterruptedException {
        unitTestCounter=0;
        int taskCounter=2000;
        //// ИТОГИ тестирования - по непонятным причинам, вероятно из-за того что не удается
        // дождаться завершения всех тестов, из, скажем, 4000 прогонов получается 3987
        // вероятно как то связано с подачей шатдауна до завершения

        //убеждаемся что отрабатывают все 1000 посланных заданий за разумное
        // время, причем эксепшны не влияют на это (обработанные в run или ушедшие наверх в call)


        Runnable[] testRunnableArray = new Runnable[taskCounter];
        Arrays.fill(testRunnableArray,unitTestRunnable);

        Callable[] testCallableArray = new Callable[taskCounter];
        Arrays.fill(testCallableArray,unitTestCallable);





          SimpleAsyncs.launchTasks(1 , testRunnableArray);
          SimpleAsyncs.launchTasks(1 , testCallableArray);
          SimpleAsyncs.launchTasks(1 , testRunnableArray);
          SimpleAsyncs.launchTasks(1 , testCallableArray);





        Thread.sleep(10000);
        System.out.println("count "+ unitTestCounter);

    }




    private  final Runnable unitTestRunnable= new Runnable() {
        @Override
        public void run() {
            unitTestCounter++;
            int i;
            try {

                Thread.sleep(3);
                if (Math.random() < 0.05f)  i=42/0;

            } catch (Exception e) {
              e.printStackTrace();
            }

        }
    };

    private  final Callable unitTestCallable= new Callable() {
        @Override
        public Object call() throws Exception {
            unitTestCounter++;
            Thread.sleep(1);
            // тестируем влияние на работу эксепшнов
            if (Math.random()<0.05) throw new Exception();
            return null;
        }

    };
}
