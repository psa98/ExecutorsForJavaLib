package c.ponom.executorsforjavalib;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class AsyncExecutorSubmitTasksTest {


    /*
    Выводы по итогам тестов:
    1. Никакие операции с таким числом потоков над общими данными невозможны без использования конкурентных
    или иммутабельных данных. Атомики и специализированные коллекции должны нормально работать
    2. Тестирование всего многопоточного обязательно.
    3.
   */
    private static final float PERCENT_OF_ERRORS = 0.1f;
    private static final int TASK_COUNTER = 100000;
    private static final int TIME_OUT = 30; // seconds
    private static final int NUMBER_OF_TESTS=10;


    private static int unitTestCounter ;

    private static final Object lockOnComplete =new Object();
    private static final Object lockOnEach =new Object();

    // todo - интересно, проявится на реальных задачах та проблема, что вызываемый Callable
    //  возможно придется обеспечивать возможностью работать с данными вне его через поля
    //  класса или иной внешний источник? Иными словами, передать внутрь исполняемой
    //  задачи нужные данные через ее параметр нельзя, у call нет параметров,
    //   только если обернуть во что-то    или дать доступ к полю объемлющего класса,
    //  что не является чистой функцией
    //  более того, и в функции, реализующие интерфейсы передать  параметр норм. способом,
    //  извне, сложно




    /*
    TODO Протестировать:
    asyncTask метод, с callable - в отдельном тесте

    submitTasks метод - в трех вариантах и первый еще на работы с активностью - операция
     должна выполняться main потоке
    //
     */


    private final Collection<Object> resultedCollection =
             Collections.synchronizedCollection(new ArrayList<>());
    private int completionTestCounter;


    @Test
    public void testSubmitTasksCallableAndRunnable() throws InterruptedException {


        for (int i = 0; i <NUMBER_OF_TESTS ; i++) {

            completionTestCounter=0;
            unitTestCounter=0;


            resultedCollection.clear();



            /* тестируется число фактических вызовов слушателей от запрошенного,
            а так же полученые данные

             */


            Callable[] testCallableArray = new Callable[TASK_COUNTER];
            Arrays.fill(testCallableArray, unitTestCallable);

            AsyncExecutor myExecutor = new AsyncExecutor();
            myExecutor.submitTasks((int)(10f*random()+1),
                    onCompletedListener, onEachCompletedListener, null, testCallableArray)
                    .awaitTermination(TIME_OUT, TimeUnit.SECONDS);

            myExecutor = new AsyncExecutor();
            myExecutor.submitTasks((int)(30f*random()+1),
                    onCompletedListener, onEachCompletedListener, null, testCallableArray)
                    .awaitTermination(TIME_OUT, TimeUnit.SECONDS);


            myExecutor = new AsyncExecutor();
            myExecutor.submitTasks((int)(100f*random()+1),
                    onCompletedListener, onEachCompletedListener, null, testCallableArray)
                    .awaitTermination(TIME_OUT, TimeUnit.SECONDS);


            myExecutor = new AsyncExecutor();
            myExecutor.submitTasks((int)(400f*random()+1),
                    onCompletedListener, onEachCompletedListener, null, testCallableArray)
                    .awaitTermination(TIME_OUT, TimeUnit.SECONDS);


            System.out.println("count " + completionTestCounter);
            System.out.println("Size " + resultedCollection.size());
            System.out.println("batch " + (i + 1));


            // увеличение счетчика за цикл происходит дважды, в собственнно задаче
            // и в колбэке вызываемом после выполнения каждой
            assertEquals(TASK_COUNTER * 4 * 2, unitTestCounter);
            boolean containsException=false;
            for (Object record:
                 resultedCollection) {
                if (record instanceof Exception) containsException = true;

            }
            assertTrue(containsException);
            assertEquals(TASK_COUNTER * 4 ,resultedCollection.size());
            //коллбэк по исполнению пакета задач исполняется 4 раза в каждом цикле
            assertEquals(4, completionTestCounter);
        }
    }

    private final Callable unitTestCallable = new Callable() {
        @Override
        public Object call() throws Exception {

            // тестируем влияние на работу эксепшнов - их надо либо обрабатывать внутри,
            // либо они прерывают исполнение и остаток кода после не исполняется.
            // это нормально, главное что вылета программы при этом не происходит

            synchronized (AsyncExecutorSubmitTasksTest.class){
                unitTestCounter++;
            }
            int i;
            double result;
            if ((result = random()) < PERCENT_OF_ERRORS) i = 42 / 0;


            return result;

        }

    };


    private AsyncExecutor.OnCompletedListener onCompletedListener
            = new AsyncExecutor.OnCompletedListener() {
        @Override
        public void runAfterCompletion(Collection<Object> results) {

            synchronized (AsyncExecutorSubmitTasksTest.class) {
                //требуется для любой работы с элементами synchronizedCollection,
                // по самой коллекции или по this

                    completionTestCounter++;
                    resultedCollection.addAll(results);
            }
        }

    };


    private AsyncExecutor.OnEachCompletedListener onEachCompletedListener
            = new AsyncExecutor.OnEachCompletedListener() {
        @Override
        public void runAfterEach(long currentTaskNumber,
                                 Object result,
                                 long tasksCompleted,
                                 long totalTasks,
                                 ThreadPoolExecutor currentExecutor,
                                 float completion) {

            synchronized (AsyncExecutorSubmitTasksTest.class){
                unitTestCounter++;
            }

        }
    };





}