package c.ponom.executorsforjavalib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import c.ponom.executorsforjavalib.TasksScheduler.ResultedRecord;

import static java.lang.Math.random;
import static java.lang.Thread.sleep;

public class TaskSchedulerProgressBarUseExample extends AppCompatActivity
{
    Activity activity;
    Handler handler;

    private static final int TASKS_NUMBER = 500;
    private static final long TIMEOUT =2000 ;
    private static final int THREAD_NUMBER = 10;
    private Task[] tasksOneArgument= new Task[TASKS_NUMBER];


    private Collection<ResultedRecord> resultedRecordsByExecution =
            Collections.synchronizedCollection(new ArrayList<ResultedRecord>());



    private Collection<ResultedRecord> resultedRecordsByTaskOrder
            = Collections.synchronizedCollection(new ArrayList<ResultedRecord>());

    private Collection<Object> resultsByOnEachSynchronized =
            Collections.synchronizedCollection(new ArrayList<>());
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_indicator);
        activity= this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        progressBar= findViewById(R.id.progress_bar);
        progressBar.setMax(TASKS_NUMBER);
        handler=new Handler();



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "See resultsByExecutionOrder", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                progressBar.setProgress(0);

                 /* Если мы в onEachComplete используем, к примеру, обновление элементов UI,
                 то следует учитывать что первые  numberOfThreads задач будут исполняться
                 одновременно, и ранее завершения исполнения  первой задачи из пакета этот
                 метод не вызывается, а потом будет вызван почти одновременно для первых
                 numberOfThreads задач. Это может вызвать, к примеру, резкое движение progressBar
                 в начале исполнения задач.
                 Это не баг, а фича */


                try {
                    submitTasksTest();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw  new AssertionError();
                }


            }
        });
    }







    public void submitTasksTest() throws Exception {
        prepareTasks();



        TasksScheduler tasksScheduler = new TasksScheduler();
        ThreadPoolExecutor currentExecutor = tasksScheduler.submitTasks(THREAD_NUMBER,
                onCompleted,
                onEachCompleted,
                tasksOneArgument);
        currentExecutor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);




    }





    private void prepareTasks(){


        for (int i = 0; i <TASKS_NUMBER; i++)
            tasksOneArgument[i] = createTaskOneArgument((int)(random()*100));

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

    */


    private void assertEquals(long a, long b) {
        if (a!=b) throw new AssertionError();
    }




    private void isArgumentSquared(Collection<ResultedRecord> results) {
        for (ResultedRecord record:results)
        {
            if (record.result instanceof Exception) break;
            int argument = (int)record.arguments[0];
            assertEquals( argument*argument,(int)record.result);
        }
    }




    private Task createTaskOneArgument(final Object... arguments){
        return new Task(arguments){
            @SuppressWarnings("RedundantThrows")
            @Override
            public Object doTask(final Object...argument) throws Exception {


                // Полученный аргумент должен быть приведен к необходимому виду,
                // если мы используем vararg как аргументы при создании задач
                // - то к массиву соответствующих объектов, иначе к типу
                // переданного единственного объекта



                Integer finalArgument = (Integer) argument[0];

                // в данном случае мы возводим аргумент в квадрат
                // для тестирования  что исключения правильно передаются выше и
                // попадают в итоговые результаты каждый 10й объект бросит исключение

                if ((int)arguments[0] %10 ==9 ) throw new ArithmeticException();
              return finalArgument *  finalArgument;

            }
        };

    }




    private  TasksScheduler.OnEachCompleted onEachCompleted =
            new TasksScheduler.OnEachCompleted(){

                @Override
                public synchronized void runAfterEach(int currentTaskNumber,
                                         Object result,
                                         int totalTasks,
                                         final int currentTaskByExecutionNumber,
                                         ThreadPoolExecutor currentExecutor,
                                         double completion, Object... argument) {
                    try {
                        sleep((long) (100+ random()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String data;
                    data=currentTaskNumber+" / "+result.toString()+" /  "+argument [0].toString();
                    resultsByOnEachSynchronized.add(data);
                    progressBar.setProgress(currentTaskByExecutionNumber);

               }

            };




    private TasksScheduler.OnCompleted onCompleted = new TasksScheduler.OnCompleted(){


        @Override
        public void runAfterCompletion(Collection<ResultedRecord> resultsByExecutionOrder,
                                       Collection<ResultedRecord> resultsByTaskOrder) {
            resultedRecordsByExecution.addAll(resultsByExecutionOrder);
            resultedRecordsByTaskOrder.addAll(resultsByTaskOrder);
            isInAscendingOrder(resultedRecordsByTaskOrder);
            isArgumentSquared(resultsByExecutionOrder);


        }
    };


    private void isInAscendingOrder(Collection<ResultedRecord> collection){
        // номера записей в итоговой коллекции должны идти последовательно через единицу
        long lastNumber=-1; // номера коллекций начинаются с 0,
        // это решит проблему с чем  сравнивать запись [0]
        for (ResultedRecord record:collection){
            {assertEquals(record.taskNumber, lastNumber + 1);
                lastNumber=record.taskNumber;}

        }
    }


}
