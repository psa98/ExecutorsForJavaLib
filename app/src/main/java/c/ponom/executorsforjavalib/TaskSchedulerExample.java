package c.ponom.executorsforjavalib;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import c.ponom.executorsforjavalib.TasksScheduler.ResultedRecord;

import static java.lang.Math.floor;


public class TaskSchedulerExample extends AppCompatActivity
{
    final static String TAG="TaskTest";
    TasksScheduler currentExecutor;
    int TASKS_NUMBER= 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "See resultsByTaskOrder", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                Task[] tasks = new Task[TASKS_NUMBER];
                for (int i = 0; i <TASKS_NUMBER; i++)
                    tasks[i] = createTaskTwoArguments(i,2);

                //for (int i = 0; i <TASKS_NUMBER; i++)
                //    tasks[i] = createTaskOneArgument(i);


                currentExecutor = new TasksScheduler();
                currentExecutor.submitTasks(10,
                        onFinished,
                        onEachCompleted,
                        (Activity) view.getContext(),tasks);


            }
        });
    }





    Task createTaskTwoArguments (final Object...arguments){
        return new Task(arguments){
            @Override
            public Object doTask(final Object...arguments) throws Exception {

                // Полученный аргумент должен быть приведен к необходимому виду,
                // если мы используем vararg как аргументы при создании задач
                // - то к массиву соответствующих объектов, иначе к типу
                // переданного единственного объекта




                /// todo - посмотреть как сюда можно приделать сохранение типов, что бы не
                //   кастить все каждый раз из объектов, это может вести к ошибкам


                // тестируем что исключения правильно передаются
                if (arguments[0].equals(10)) throw new ArithmeticException();
                int argument1 = (int) arguments[0];
                int argument2 = (int) arguments[1];

                // в данном случае мы перемножаем два аргумента
                return argument1*argument2;

            }
        };
    }


    Task createTaskOneArgument (final Object...arguments){
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


                if (finalArgument==10) throw new ArithmeticException();

                return finalArgument * 2;

            }
        };

    }





    TasksScheduler.OnEachCompleted onEachCompleted =
            new TasksScheduler.OnEachCompleted(){

        @Override
        public void runAfterEach(long currentTaskNumber,
                                 Object result,
                                 long totalTasks,
                                 long currentTaskByExecutionNumber, ThreadPoolExecutor currentExecutor,
                                 double completion, Object... argument) {
            //todo - итоговую таблицу можно собрать и на кортежах = номер задачи, аргумент, результат.
            //Или на нормальных Pair<F;S> - они есть в коде Андроида, но не в стандартной Java
            int varargsSize=argument.length;
            String argument2= (varargsSize==2)? "argument 2 = ["  +argument[1]+"]":" none";

            System.out.println("currentTaskNumber = [" + currentTaskNumber + "], result = ["
                    + result +  "],\n totalTasks = [" + totalTasks
                    + "], currentTaskByExecutionNumber = [" + currentTaskByExecutionNumber +
                    "]\n, completion = [" + floor(completion*10f)/10f + "%] "+
                     "], argument.size = [" + varargsSize+
                    "], argument 1=["+argument[0]+"]argument 2["+argument2);

            System.out.println("Current thread ="+Thread.currentThread().getName());

        }

    };




    TasksScheduler.OnCompleted onFinished = new TasksScheduler.OnCompleted(){


        @Override
        public void runAfterCompletion(Collection<ResultedRecord> resultedRecordCollection) {
            System.out.println("Done with tasks. We got result for  " +
                    "onFinished with array size  ="+resultedRecordCollection.size());
            System.out.println("Current thread ="+Thread.currentThread().getName());
        }
    };



}
