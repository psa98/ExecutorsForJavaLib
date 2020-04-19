package c.ponom.executorsforjavalib;

import android.util.Log;

import java.util.concurrent.ThreadPoolExecutor;

import static c.ponom.executorsforjavalib.MainActivity.TAG;

public class TaskScheduler extends AsyncExecutor{


    // здесь переопределяются методы, выполняемые
    // по ходу исполнения задач


    OnCompletedListener onCompletedListener = new OnCompletedListener() {
        @Override
        public void runAfterCompletion() {
            Log.e(TAG, "runAfterCompletion: Ready" );
            Log.e(TAG, "Thread: "+Thread.currentThread().getName() );

        }
    };

    OnErrorListener onErrorListener = new OnErrorListener() {
        @Override
        public void runAfterError(int currentTaskNumber, Exception exception) {
            Log.e(TAG, "runAfterError, error: "+
                    exception.getLocalizedMessage() + " task "+currentTaskNumber );
            Log.e(TAG, "Thread: "+Thread.currentThread().getName() );

        }
    };


    OnEachCompletedListener onEachCompletedListener = new OnEachCompletedListener() {
        @Override
        public void runAfterEach(long currentTask,
                                 long tasksCompleted,
                                 long totalTasks,
                                 ThreadPoolExecutor currentExecutor,
                                 float completion) {
            Log.e(TAG, String.format("onEachCompletedListener, задача №%d,готово %d из %d, процент:%s",
                    currentTask, tasksCompleted,totalTasks, completion));
            Log.e(TAG, "Thread: "+Thread.currentThread().getName() );

        }
    };
}
