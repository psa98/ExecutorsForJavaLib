package c.ponom.executorsforjavalib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import c.ponom.executorsforjavalib.TaskScheduler.ResultedRecord;

import static java.lang.Math.random;
import static java.lang.Thread.sleep;

public class TaskSchedulerProgressBarUseExample extends AppCompatActivity
{
    Activity activity;
    Handler handler;

    private static final int TASKS_NUMBER = 500;
    private static final long TIMEOUT =2000 ;
    ///  этот таймаут в примере работает на время _постановки всего пакета задач_не их завершения


    private static final int THREAD_NUMBER = 5;
    //Выделение излишнего числа потоков скорее замедлит работу - из-за переключений контекста,
    // перегрузки I|O каналов, работы сборщика после завершения заданий
    private Task[] tasksOneArgument= new Task[TASKS_NUMBER];
    boolean isDoing = false;


    private Collection<ResultedRecord> resultedRecordsByExecution =
            Collections.synchronizedCollection(new ArrayList<ResultedRecord>());



    private Collection<ResultedRecord> resultedRecordsByTaskOrder
            = Collections.synchronizedCollection(new ArrayList<ResultedRecord>());

    private Collection<Object> resultsByOnEachSynchronized =
            Collections.synchronizedCollection(new ArrayList<>());
    ProgressBar progressBar;
    TextView textView;
    TextView textViewTasks;
    //Integer[] array;
    ThreadPoolExecutor currentExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_indicator);
        activity= this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        progressBar= findViewById(R.id.progress_bar);
        textView=findViewById(R.id.percent);
        textViewTasks=findViewById(R.id.tasks);
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


        // если текущий экзекьютор еще не выполнил всю работу, новый не создается
        // Если необходимо что бы поставленные задачи переживали поворот экрана
        // шедулер и экзекьютор следует перенести во ViewModel или иной подобный класс
        //

        if (currentExecutor!=null&&currentExecutor.getCompletedTaskCount()<TASKS_NUMBER) return;
        TaskScheduler taskScheduler = new TaskScheduler();
        currentExecutor = taskScheduler.submitTasks(THREAD_NUMBER,
                onCompleted,
                onEachCompleted,
                tasksOneArgument);
        currentExecutor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
        isDoing = false;

    }





    private void prepareTasks(){


        for (int i = 0; i <TASKS_NUMBER; i++)
            tasksOneArgument[i] = createTaskOneArgument((int)(random()*100));

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


                sleep((long) (1000* random()));


                Integer finalArgument = (Integer) argument[0];

                // в данном случае мы возводим аргумент в квадрат

                // для тестирования  что исключения правильно передаются выше и
                // попадают в итоговые результаты каждый 10й объект бросит исключение

                if ((int)arguments[0] %10 ==9 ) throw new ArithmeticException();
              return finalArgument *  finalArgument;

            }
        };

    }




    private  TaskScheduler.OnEachCompleted onEachCompleted =
            new TaskScheduler.OnEachCompleted(){

                @Override
                public synchronized void runAfterEach(int currentTaskNumber,
                                         Object result,
                                         int totalTasks,
                                         final int currentTaskByExecutionNumber,
                                         ThreadPoolExecutor currentExecutor,
                                         double completion, Object... argument) {

                    // нагрузочное тестирование на выделение памяти  - gc хорошо справляется,
                    // хотя вызывается слишком часто и тормозит на 100-200 мс
                    //array = new Integer[500000];
                    //Arrays.fill(array,42);

                    String data;
                    data=currentTaskNumber+" / "+result.toString()+" /  "+argument [0].toString();
                    resultsByOnEachSynchronized.add(data);


                    /*код ниже использует данные о порядке исполнения из аргумента completion
                    if (progressBar!=null&&textView!=null) {
                        // проверяем существует ли еще активность
                        progressBar.setProgress(currentTaskByExecutionNumber);
                        textView.setText(String.format("Процент выполнения равен=%.1f%%", completion));
                    }
                    */


                    //альтернативный код иcпользует данные непосредственно из экзекьютора:

                    if (progressBar!=null&&textView!=null) {
                        // проверяем существует ли еще активность
                        int count= (int) currentExecutor.getCompletedTaskCount();

                        progressBar.setProgress((int) (count));
                        textView.setText(String.format("Процент выполнения равен=%.1f%%", completion));


                        textViewTasks.setText("Выполнено задач ="+count);

                    }



               }

            };




    private TaskScheduler.OnCompleted onCompleted = new TaskScheduler.OnCompleted(){


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


}
