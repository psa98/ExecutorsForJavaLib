package c.ponom.executorsforjavalib;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import java.util.EmptyStackException;

public class MainActivity extends AppCompatActivity {
    final static String TAG="AsyncTestCompat";


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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                launchThreads();
                //simpleLaunch();
            }
        });
    }

    private void launchThreads() {
        Runnable[] taskLists = new Runnable[]{r1,r2,r3};
        TaskScheduler myExecutor = new TaskScheduler();
        myExecutor.submitTasks(2,
                myExecutor.onCompletedListener,
                myExecutor.onEachCompletedListener,
                myExecutor.onErrorListener,
                this,
                taskLists);


    }
    // проверяем то, что если вместо активности передать null,
    // вызываемые методы дергаются не в ui потоке, и что упрощенный метод работает
    private void simpleLaunch() {
        Runnable[] taskLists = new Runnable[]{r1,r2,r3,r1};
        TaskScheduler myExecutor = new TaskScheduler();
        myExecutor.submitTasks(3,
                myExecutor.onCompletedListener,
               this,
                taskLists);
    }


    // на пробу было переделано под лямбды.
    // сама библиотека не планируется к исп. J8 для совместимости с API<24

    Runnable r1= new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "run 1");
            try {
                Thread.sleep((long) (5000 * Math.random()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable r2= new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "run 2");
            try {
                Thread.sleep((long) (5000 * Math.random()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    final Runnable r3 = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "run 3");
            try {
                Thread.sleep((long) (5000 * Math.random()));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // тест ловца ошибок
            throw
                    new IllegalStateException();
        }
    };
}
