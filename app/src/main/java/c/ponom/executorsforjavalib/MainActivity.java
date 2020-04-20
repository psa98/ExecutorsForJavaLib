package c.ponom.executorsforjavalib;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import java.util.EmptyStackException;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {
    final static String TAG="AsyncTestCompat";
    TaskScheduler myExecutor;


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
                myExecutor = new TaskScheduler();
                launchThreads();
                myExecutor = new TaskScheduler();
                launchThreads2();

            }
        });
    }

    private void launchThreads() {
        Callable[] taskLists = new Callable[]{callable1,callable2,callable3,callable1,callable1,callable2,callable3,callable1,callable1,callable2,callable3,callable1,callable1,callable2,callable3,callable1};

        myExecutor.submitTasks(3,
                myExecutor.onCompletedListener,
                myExecutor.onEachCompletedListener,
                this,
                taskLists);


    }


    private void launchThreads2() {
        Callable[] taskLists = new Callable[]{callable1,callable2,callable3,callable1,callable1,callable2,callable3,callable1,callable1,callable2,callable3,callable1};

        myExecutor.submitTasks(5,
                myExecutor.onCompletedListener,
                myExecutor.onEachCompletedListener,
                null,
                taskLists);


    }



    // на пробу было переделано под лямбды.
    // сама библиотека не планируется к исп. версии J8 для совместимости с API<24



    final Callable callable1 = new Callable() {
        @Override
        public Object call() throws Exception {

               Thread.sleep((long) (5000 * Math.random()));
              if (Math.random()>0.3f) return "from callable 1"; else

                    return      new IllegalStateException();


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
