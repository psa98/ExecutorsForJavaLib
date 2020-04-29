package c.ponom.executorsforjavalib;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SingleTaskSchedulerTest_Done {



    private static final float PERCENT_OF_ERRORS = 0.1f;
    private static final int TIMEOUT = 500; // mseconds
    private static final int NUMBER_OF_TESTS=50;
    private static final int NUMBER_OF_TASKS=100;
    private int completionTestCounter;





    private static final Object mutexCallback =new Object();
    private static final Object mutexTask =new Object();


    private final Collection<Object> resultedCollection =
            Collections.synchronizedList(new ArrayList<>());


     /* сравниваем число фактических вызовов коллбэка от запрошенного,
      а так же проверяем итоговую  коллекцию возвращенных результатов  */




    @Test
     public void testSubmitTasks() throws InterruptedException {

        final Callable[] testCallableArray = new Callable[NUMBER_OF_TASKS];
        Arrays.fill(testCallableArray,unitTestCallable);


        for (int testBatch = 0; testBatch < NUMBER_OF_TESTS; testBatch++) {


                completionTestCounter = 0;
                resultedCollection.clear();


                for (int taskNumber = 0; taskNumber <NUMBER_OF_TASKS; taskNumber++) {
                SingleTaskScheduler currentExecutor = new SingleTaskScheduler();
                currentExecutor.submitAsyncTask(testCallableArray[taskNumber],asyncCallBack)
                        .awaitTermination(TIMEOUT,TimeUnit.MILLISECONDS);
                }
                // если поставить медленную задачу, то до завершения таймаута
                // она может  не быть исполненной. В таком случае тестировать
                // выполнение всего тестового пакета задач надо по завершению
                // последнего коллбэка
                System.out.println("TasksCompleted " + completionTestCounter);
                System.out.println("Size " + resultedCollection.size());
                System.out.println("batch " + (testBatch + 1));
                assertEquals(NUMBER_OF_TASKS,resultedCollection.size());
                assertTrue(containsExceptions(resultedCollection));


            }

        }
        private final Callable unitTestCallable = new Callable() {
            @SuppressWarnings("RedundantThrows")
            @Override
            public Object call() throws Exception {

                // тестируем так же влияние на работу эксепшнов - их надо либо обрабатывать внутри,
                // либо они прерывают исполнение и остаток кода после не исполняется.
                // это нормально, главное что вылета программы при этом не происходит

                // До этого блока можно выполнить код, не нуждающийся в синхронизации,
                // в т.ч. долгий и/или  блокирующий
                Object result;

                synchronized (mutexTask) {

                result=new Object();
                int i=0;
                double random =random();
                // проверяем как бросается исключение в результат
                try {
                    if (random  < PERCENT_OF_ERRORS) i = 42 / 0;
                    else result= random;

                } catch (Exception exception){
                    result=exception;
                }
                // возвращаем либо результат к примеру, как пару чисел, либо как исключение.
                // Возможны, конечно, и другие варианты, скажем просто проброска исключений выше
                return result;
                }
            }
        };


    private boolean containsExceptions(Collection collection) {

        for (Object record : collection) {
            if (record instanceof Exception) {
                return  true;

            }
        }
        return false; //если все просмотрели, а исключений нет

    }




    private SingleTaskScheduler.AsyncCallBack asyncCallBack = new SingleTaskScheduler.AsyncCallBack() {
        @Override
         public void asyncResult(Object result) {
            synchronized (mutexCallback) {
                resultedCollection.add(result);
                completionTestCounter++;
            }

        }
    };
}