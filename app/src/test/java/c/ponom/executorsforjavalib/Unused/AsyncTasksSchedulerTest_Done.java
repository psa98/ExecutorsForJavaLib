package c.ponom.executorsforjavalib.Unused;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class AsyncTasksSchedulerTest_Done {



    private static final float PERCENT_OF_ERRORS = 0.1f;
    private static final int TASKS_NUMBER = 5;
    private static final int TIMEOUT = 5000; // mseconds
    private static final int NUMBER_OF_TESTS=2;


    private static int onEachTestCounter;

    private static final Object lockOnComplete =new Object();
    private static final Object lockOnUnitTestCounter =new Object();

    private final Collection<Object> resultedCollection =
            Collections.synchronizedList(new ArrayList<>());
    private final Collection<Long> resultedCollectionByOrder =
            Collections.synchronizedList(new ArrayList<Long>());
    private final Collection<Long> resultedCollectionByTaskNumber =
            Collections.synchronizedList(new ArrayList<Long>());

    private int completionTestCounter;







     /* Тестируется:
      1. Число фактических вызовов OnEachCompleted на исполнение каждой задачи от числа задач,
      однократность вызова OnCompleted на каждый набор задач
      2. Правильность всех возвращаемых результатов OnEachCompleted,
      то что в нем указан правильныый номер задачи по порядку выполнения
      3. Полученная в OnCompleted коллекция на число записей в ней.
       */
/*
    @Test
     public void testSubmitTasksCallable() throws InterruptedException {


        for (int testBatch = 0; testBatch <NUMBER_OF_TESTS ; testBatch++) {

            completionTestCounter = 0;
            onEachTestCounter = 0;

            resultedCollectionByOrder.clear();
            resultedCollection.clear();


            Callable[] testCallableArray = new Callable[TASKS_NUMBER];
            Arrays.fill(testCallableArray, unitTestCallable);


            AsyncTasksScheduler myExecutor = new AsyncTasksScheduler();
            myExecutor.submitTasks((int) (10f * random() + 1),
                    onCompletedListener, onEachCompletedListener, null, testCallableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);


            myExecutor = new AsyncTasksScheduler();
            myExecutor.submitTasks((int) (30f * random() + 1),
                    onCompletedListener, onEachCompletedListener, null, testCallableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);

            myExecutor = new AsyncTasksScheduler();
            myExecutor.submitTasks((int) (100f * random() + 1),
                    onCompletedListener, onEachCompletedListener, null, testCallableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);


            myExecutor = new AsyncTasksScheduler();
            myExecutor.submitTasks((int) (100f * random() + 1),
                    onCompletedListener, onEachCompletedListener, null, testCallableArray)
                    .awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);




            System.out.println("count " + completionTestCounter);
            System.out.println("Size " + resultedCollection.size());
            System.out.println("batch " + (testBatch + 1));

            // проверка того что в коллекции не одни null, то есть что
            // исключения в нее тоже попадают из результата.
            boolean containsException = false;

            for (Object record :
                    resultedCollection) {
                if (record instanceof Exception) containsException = true;


            }
            assertTrue(containsException);


            // плюс проверка того что все вызовы onFinished реально приходят по порядку
            // исполнения, а не произвольно в первой тысяче вызовов как минимум
            boolean byOrder = false;

            // resultedCollectionByOrder не имеет get, плюс
            // оно не кастится в Array, создаем новую коллекцию
            ArrayList<Long> listByOrder = new ArrayList<>(resultedCollectionByOrder);


            for (int j = 0; j < TASKS_NUMBER/4; j++) {

                // проверяем что номера выполненных задач (для первой пачки)
                // по порядку выполнения идут с 1 по возрастающей и что всегда
                // правильно передается порядковый номер задачи в onEachComplete

                if (listByOrder.get(j) == j+1)
                    byOrder = true;
                else {
                    byOrder = false;
                    break;
                }
            }
            assertTrue(byOrder);

            // увеличение счетчика за цикл происходит дважды, в собственнно задаче
            // и в колбэке вызываемом после выполнения каждой
            assertEquals (TASKS_NUMBER * 4, resultedCollection.size());
            assertEquals(TASKS_NUMBER * 4 * 2, onEachTestCounter);


            //коллбэк по исполнению пакета задач исполняется 4 раза в каждом цикле
            assertEquals(4, completionTestCounter);
        }
    }










    private final Callable unitTestCallable = new Callable() {
        @Override
         public Object call() throws Exception {

            // тестируем так же влияние на работу эксепшнов - их надо либо обрабатывать внутри,
            // либо они прерывают исполнение и остаток кода после не исполняется.
            // это нормально, главное что вылета программы при этом не происходит

            // До этого блока можно выполнить код, не нуждающийся в синхронизации,
            // в т.ч. долгий и/или  блокирующий

            synchronized (lockOnUnitTestCounter){
                onEachTestCounter++;
                // этот счетчик увеличивается дважды - тут и в onEach.
                // но лучше на 2 разных разделить
            }
            int i;
            double result;
            if ((result = random()) < PERCENT_OF_ERRORS) i = 42 / 0;
            // в данном случае исключение пробрасывается в результат onEach. Но его можно и тут обработать,
            // выдав в этом случае альтернативный результат
            return result;
        }
    };


     private AsyncTasksScheduler.OnCompleted onCompletedListener
            = new AsyncTasksScheduler.OnCompletedListener() {
        @Override
        public void runAfterCompletion(Collection<Object> results) {

            // До этого блока можно выполнить код, не нуждающийся в синхронизации,
            // в т.ч. долгий и/или  блокирующий
            synchronized (lockOnComplete) {
                    completionTestCounter++;
                    resultedCollection.addAll(results);


            }
        }

    };


    private AsyncTasksScheduler.OnEachCompletedListener onEachCompletedListener
            = new AsyncTasksScheduler.OnEachCompletedListener() {
        @Override
        public void runAfterEach(long currentTaskNumber,
                                 Object result,
                                 long tasksCompleted,
                                 long totalTasks,
                                 ThreadPoolExecutor currentExecutor,
                                 float completion) {


            synchronized (lockOnUnitTestCounter){
                onEachTestCounter++;

                System.out.println("currentTaskNumber = [" + currentTaskNumber + "], result = [" + result + "], tasksCompleted = [" + tasksCompleted + "], totalTasks = [" + totalTasks + "], currentExecutor = [" + currentExecutor + "], completion = [" + completion + "]");
                resultedCollectionByOrder.add(tasksCompleted);
                resultedCollectionByTaskNumber.add(currentTaskNumber);
            }

        }
    };



*/

}