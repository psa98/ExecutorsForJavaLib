package c.ponom.executorsforjavalib;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import c.ponom.executorsforjavalib.TaskScheduler.ResultedRecord;

import static java.lang.Math.floor;


public class TaskSchedulerExample extends AppCompatActivity
{

    TaskScheduler currentExecutor;
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
                Snackbar.make(view, "See resultsByExecutionOrder", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                Task[] tasks = new Task[TASKS_NUMBER];
                for (int i = 0; i <TASKS_NUMBER; i++)
                    tasks[i] = createTaskTwoArguments(i,2);


                currentExecutor = new TaskScheduler();
                currentExecutor.submitTasks(10,
                        onCompleted,
                        onEachCompleted,
                        tasks);


            }
        });
    }





    Task createTaskTwoArguments (final Object...arguments){
        //noinspection RedundantThrows
        return new Task(arguments){
            @Override
            public Object doTask(final Object...arguments) throws Exception {

                // Полученный аргумент должен быть приведен к необходимому виду,
                // если мы используем vararg как аргументы при создании задач
                // - то к массиву соответствующих объектов, иначе к типу
                // переданного единственного объекта

                /// todo - посмотреть как сюда можно приделать сохранение типов, что бы не
                //   кастить в  doTask все каждый раз из объектов, это может вести к ошибкам


                // тестируем что исключения правильно передаются
                if (arguments[0].equals(10)) throw new ArithmeticException();
                int argument1 = (int) arguments[0];
                int argument2 = (int) arguments[1];


                // в данном случае мы перемножаем два аргумента
                return argument1*argument2;

            }
        };
    }







    TaskScheduler.OnEachCompleted onEachCompleted =
            new TaskScheduler.OnEachCompleted(){

        @Override
        public void runAfterEach(int currentTaskNumber,
                                 Object result,
                                 int totalTasks,
                                 int currentTaskByExecutionNumber, ThreadPoolExecutor currentExecutor,
                                 double completion, Object... argument) {

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




    TaskScheduler.OnCompleted onCompleted = new TaskScheduler.OnCompleted(){


        @Override
        public void runAfterCompletion(Collection<ResultedRecord> resultedRecordCollection, Collection<ResultedRecord> resultsByTaskOrder) {
            System.out.println("Done with tasks. We got result for  " +
                    "onFinished with array size  ="+resultedRecordCollection.size());
            System.out.println("Current thread ="+Thread.currentThread().getName());
        }
    };



}
