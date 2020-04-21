package c.ponom.executorsforjavalib;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Callable;


public class AsyncExecutorTesting extends AppCompatActivity
{
    final static String TAG="AsyncTestCompat";
    AsyncTaskScheduler myExecutor;
    static int counter;


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
                Snackbar.make(view, "Go!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //myExecutor = new AsyncTaskScheduler();
                //launchThreads();
                //myExecutor = new AsyncTaskScheduler();
                //launchThreads2();
                //myExecutor =new AsyncTaskScheduler();
                //myExecutor.asyncTask(callable,myExecutor.asyncCallBack,null);

                //myExecutor =new AsyncTaskScheduler();
                //myExecutor.asyncTask(callable,null,null);
                //launchTestAsync();
                //myExecutor =new AsyncTaskScheduler();
                //myExecutor.asyncTaskSimple(callable);
                //Executor =new AsyncTaskScheduler();
                //myExecutor.
                //myExecutor.asyncTaskSimple(testRunnable);
                Runnable[] tasks= new Runnable[]{testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,};

                SimpleAsyncs.launchTasks(30, tasks);
                //for (int i=1;i<100;i++) SimpleAsyncs.launchTasks(2,tasks);


            }
        });
    }

    private void launchTestAsync(){
        myExecutor =new AsyncTaskScheduler();
        myExecutor.asyncTask(callable,myExecutor.asyncCallBack,this);

    }

    private void launchThreads() {
        Callable[] taskLists = new Callable[]{callable,callable2,callable3, callable, callable,callable2,callable3, callable, callable,callable2,callable3, callable, callable,callable2,callable3, callable};

        myExecutor.submitTasks(3,
                myExecutor.onCompletedListener,
                myExecutor.onEachCompletedListener,
                this,
                taskLists);


    }


    private void launchThreads2() {
        Callable[] taskLists = new Callable[]{callable,callable2};

        myExecutor.submitTasks(5,
                myExecutor.onCompletedListener,
                myExecutor.onEachCompletedListener,
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

    final Runnable testRunnableRND = new Runnable() {
        @Override
        public void run() {

             if (Math.random()<0.01f)Log.e(TAG, "+++++++++++from testRunnableRND");

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
        public Object call() throws Exception {
            Thread.sleep((long) (5000 * Math.random()));
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



}
