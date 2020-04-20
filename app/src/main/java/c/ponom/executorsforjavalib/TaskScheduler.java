package c.ponom.executorsforjavalib;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import static c.ponom.executorsforjavalib.MainActivity.TAG;

public class TaskScheduler extends AsyncExecutor{


    // здесь переопределяются методы, выполняемые
    // по ходу исполнения задач
    // интересно, возможен ли случай когда активость прибьют пока мы тут чего-то делаем, то есть до того
    // как мы попробуем вызывать ее методы? И, кстати, как сюда передавать активность для этого самого?
    // можно, конечно, как очередной параметр, из интерфейсного метода, но это выглядит издевательски -
    // из активности в AsyncExecutor, потом из шедулера сюда, потом кастим ее к нашей.
    // может интерфейсы какие затеять из активности или включать ее в альтернативный конструктор этого
    // класса и хранить в нем?

    OnCompletedListener onCompletedListener = new OnCompletedListener() {
        @Override
        public void runAfterCompletion(ArrayList<Object> results) {
            Log.e(TAG, "runAfterCompletion: Ready" );
            Log.e(TAG, "Thread: "+Thread.currentThread().getName() );
            Log.e(TAG, "+++++++++++++++++++++++++++++++++++++++++++++" +
                    "result size= "+results.size() );


        }
    };



    OnEachCompletedListener onEachCompletedListener = new OnEachCompletedListener() {
        @Override
        public void runAfterEach(long currentTask,
                                 Object result, long tasksCompleted,
                                 long totalTasks,
                                 ThreadPoolExecutor currentExecutor,
                                 float completion) {
            Log.e(TAG, String.format("onEachCompletedListener, " +
                            "задача №%d,готово %d из %d, процент:%s"+" result "+result ,
                    currentTask, tasksCompleted,totalTasks, completion));
            Log.e(TAG, "Thread: "+Thread.currentThread().getName() );
            Exception  exception;
            if (result instanceof Exception){
                exception = (Exception) result;
                Log.e(TAG, "Error "+ exception);}

        }
    };
}
