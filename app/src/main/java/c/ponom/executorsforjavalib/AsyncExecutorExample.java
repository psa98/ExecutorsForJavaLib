package c.ponom.executorsforjavalib;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

import c.ponom.executorsforjavalib.AsyncTasksExecutor.OnEachCompletedListener;


public class AsyncExecutorExample extends AppCompatActivity
{
    final static String TAG="AsyncTestCompat";
    AsyncTasksExecutor myExecutor;
    static int counter;
    static Runnable[] tasks;
    static Callable[] taskLists;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);

        tasks= new Runnable[]{testRunnable,testRunnable,
                testRunnable,testRunnable,testRunnable,
                testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,};



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Go!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                myExecutor = new AsyncTasksExecutor();
                launchThreads();
            }
        });
    }



    private void launchThreads() {
        taskLists = new Callable[]{callable,callable2,callable3,
                    callable, callable,callable2,callable3, callable,
                    callable,callable2,callable3, callable, callable,
                    callable2,callable3, callable};


        myExecutor.submitTasks(1,onCompletedListener,
                onEachCompletedListener,
                        null,
                        taskLists);

    }


    private void launchThreads2() {
        Callable[] taskLists = new Callable[]{callable,callable2};

        myExecutor.submitTasks(5,
                onCompletedListener,
                onEachCompletedListener,
                null,
                taskLists);
    }

    // на пробу было переделано под лямбды.
    // сама библиотека не планируется к исп. версии J8 для совместимости с API<24

    final Runnable testRunnable= new Runnable() {
        @Override

        public void run() {
            try {
                Thread.sleep((long) (3000 * Math.random()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counter++;
            Log.e(TAG, "+++++++++++from testRunnable "+counter);;

        }
    };




    final Callable callable = new Callable() {
        @Override
        public Object call() throws Exception {
            int i;
            Log.e(TAG, "from test callable+++++++++++++++++++++++++++++++" );

            try {
                Thread.sleep((long) (8000 * Math.random()));
                if (Math.random() > 0.5f) return "from callable 1"; else
                    i=42/0;

            } catch (Exception exception){
                return     exception;
            }
            return       "from callable 1";
        }
    };


    final Callable callable2 = new Callable() {
        @Override
        synchronized public Object call() throws Exception {
            Thread.sleep((long) (5000 * Math.random()));
            int i;
            if (Math.random() > 0.5f) return "from callable 1"; else
                i=42/0;
            return "from callable 2";

        }
    };


    final Callable callable3 = new Callable() {
        @Override
        public Object call() throws Exception {
            Thread.sleep((long) (5000 * Math.random()));
            return  42;
        }
    };


    AsyncTasksExecutor.OnCompletedListener onCompletedListener = new AsyncTasksExecutor.OnCompletedListener() {
        @Override
        public void runAfterCompletion(Collection<Object> results) {

        }
    };

    OnEachCompletedListener onEachCompletedListener=new AsyncTasksExecutor.OnEachCompletedListener() {
        @Override
        public void runAfterEach(long currentTaskNumber, Object result, long tasksCompleted, long totalTasks, ThreadPoolExecutor currentExecutor, float completion) {

        }
    };



}
