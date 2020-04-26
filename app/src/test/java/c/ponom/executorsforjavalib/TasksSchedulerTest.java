package c.ponom.executorsforjavalib;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import c.ponom.executorsforjavalib.TasksScheduler.ResultedRecord;

import static org.junit.Assert.*;

public class TasksSchedulerTest {

    private static final int TASKS_NUMBER = 100;
    private static final long TIMEOUT =2000 ;
    private static final int THREAD_NUMBER = 20;
    private Task[] tasksOneArgument= new Task[TASKS_NUMBER];
    private Task[] tasksTwoArguments= new Task[TASKS_NUMBER];
    private List<Task>  tasksOneArgumentList=new ArrayList<>();
    private List<Task>  tasksTwoArgumentsList=new ArrayList<>();
    private ArrayList<ResultedRecord> resultedRecordsByExecution =new ArrayList<>();
    private ArrayList<ResultedRecord> resultedRecordsByTaskOrder =new ArrayList<>();
    private ArrayList<Object> resultsByOnEach = new ArrayList<>();
    private Collection<Object> resultsByOnEachSynchronized =
            Collections.synchronizedCollection(resultsByOnEach);
    private ThreadPoolExecutor currentExecutor;



    @Test
    public void submitTasksTest() throws Exception {
        prepareTasks();

        TasksScheduler tasksScheduler = new TasksScheduler();
        currentExecutor
                = tasksScheduler.submitTasks(THREAD_NUMBER,
                onCompleted,
                onEachCompleted,
                null,
                tasksOneArgument);
        currentExecutor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);

        // проверяем собранные после вызовов слушателей коллекции:


        tasksScheduler = new TasksScheduler();
        currentExecutor= tasksScheduler.submitTasks(10,
                onCompleted,
                onEachCompleted,
                null,
                tasksOneArgument);
        currentExecutor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);

        tasksOneArgumentList = Arrays.asList(tasksOneArgument);
        tasksTwoArgumentsList = Arrays.asList(tasksTwoArguments);
        containsExceptions(resultsByOnEachSynchronized,new Exception());
        isInAscendingOrder(resultedRecordsByTaskOrder);
        isArgumentTripled(resultedRecordsByExecution);

        // проверяем собранные после вызовов слушателей коллекции (для одного аргумента):



    }


    @Test
    public void submitTasksAsListList() {
        prepareTasks();

    }


    private void prepareTasks(){


        for (int i = 0; i <TASKS_NUMBER; i++)
            tasksOneArgument[i] = createTaskOneArgument(i);
        // результат д.б. равен i в квадрате * 2


        for (int i = 0; i <TASKS_NUMBER; i++)
            tasksTwoArguments[i] = createTaskTwoArguments(i,3);
        // результат д.б. равен произведению первого аргумента на 3 (фиксированный второй аргумент)

        // ДЛЯ ВСЕХ АРГУМЕНТОВ каждый 10й бросает исключение


       tasksOneArgumentList=new ArrayList<>(Arrays.asList(tasksOneArgument));
       tasksTwoArgumentsList=new ArrayList<>(Arrays.asList(tasksOneArgument));

    }


      /* Тестировать
      1. Число фактических вызовов OnEachCompleted на исполнение каждой задачи от числа задач,
      однократность вызова OnCompleted на каждый набор задач
      2. Правильность всех возвращаемых результатов OnEachCompleted,
      то что в нем указан правильный  и восходящий номер задачи по порядку выполнения, что тестовая коллекция
      содержит исключения и что результаты и аргументы непустые (и правильные)
      3. Полученная в OnCompleted коллекция тестируется на число записей в ней,
      правильность результатов, наличие исключений, восходящие номера записей
      4. Прием как результатов массивов И списков задач
      // todo - че то я затупил, что нам не дает в основном методе проверить тип Object...arguments
        как списка и преобразовать именно там, а не в вспомогательном?
         Протестить это
      */


    // проверка успешности проброса исключений в результирующую коллекцию и результаты сборной коллекции из
    // onEach
    private void containsExceptions(Collection collection, Exception exception){
        boolean result = false;
        for (Object record:collection)
            if (record instanceof Exception) result =true;

            assertTrue(result);
            }


    private void isInAscendingOrder(ArrayList<ResultedRecord> collection){
        // номера записей в итоговой коллекции должны идти последовательно через единицу
        long lastNumber=-1; // номера коллекций начинаются с 0,
                            // это решит проблему с чем  сравнивать запись [0]
        for (ResultedRecord record:collection){
            {assertEquals(record.taskNumber, lastNumber + 1);
                lastNumber=record.taskNumber;}

        }
    }

    private void isArgumentTripled(ArrayList<ResultedRecord> results) {
        for (int i = 0; i < TASKS_NUMBER; i++)
            assertEquals(i*3,(int)results.get(i).arguments[0]);
    }





    private Task createTaskTwoArguments(final Object... arguments){
        return new Task(arguments){
            @Override
            public Object doTask(final Object...arguments) throws Exception {

                // Полученный аргумент должен быть приведен к необходимому виду,
                // если мы используем vararg как аргументы при создании задач
                // - то к массиву соответствующих объектов, иначе к типу
                // переданного единственного объекта




                /// todo - посмотреть как сюда можно приделать сохранение типов, что бы не
                //   кастить все каждый раз из объектов, это может вести к ошибкам


                // для тестирования  что исключения правильно передаются -
                // каждый 10й объект бросит исключение
                if ((int)arguments[0] %10 ==9 ) throw new ArithmeticException();
                int argument1 = (int) arguments[0];
                int argument2 = (int) arguments[1];


                // в данном случае мы умножаем первый на второй, равный в данном случае  трем
                return argument1*argument2;

            }
        };
    }


    private Task createTaskOneArgument(final Object... arguments){
        return new Task(arguments){
            @Override
            public Object doTask(final Object...argument) throws Exception {


                // Полученный аргумент должен быть приведен к необходимому виду,
                // если мы используем vararg как аргументы при создании задач
                // - то к массиву соответствующих объектов, иначе к типу
                // переданного единственного объекта

                Integer finalArgument = (Integer) argument[0];
                // put your code for doing task here
                // в данном случае мы умножаем аргумент на 3

                // для тестирования  что исключения правильно передаются -
                // каждый 10й объект бросит исключение
                if ((int)arguments[0] %10 ==9 ) throw new ArithmeticException();

                return finalArgument *  finalArgument *2;

            }
        };

    }




    private TasksScheduler.OnEachCompleted onEachCompleted =
            new TasksScheduler.OnEachCompleted(){

                @Override
                public void runAfterEach(long currentTaskNumber,
                                         Object result,
                                         long totalTasks,
                                         long currentTaskByExecutionNumber, ThreadPoolExecutor currentExecutor,
                                         double completion, Object... argument) {


                    resultsByOnEachSynchronized.add(result);


                }

            };




    private TasksScheduler.OnCompleted onCompleted = new TasksScheduler.OnCompleted(){


        @Override
        public void runAfterCompletion(Collection<ResultedRecord> resultsByExecutionOrder,
                                       Collection<ResultedRecord> resultsByTaskOrder) {
            resultedRecordsByExecution.addAll(resultsByExecutionOrder);
            resultedRecordsByTaskOrder.addAll(resultsByTaskOrder);

        }
    };



}