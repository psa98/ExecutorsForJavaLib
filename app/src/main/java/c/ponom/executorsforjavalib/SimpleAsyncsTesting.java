package c.ponom.executorsforjavalib;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Callable;



public class SimpleAsyncsTesting extends AppCompatActivity
{
    final static String TAG="AsyncTestCompat";
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

                Runnable[] tasks= new Runnable[]{testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,testRunnable,};
                Callable[] callables= new Callable[]{callable,callable2,callable,callable2,callable,callable2,callable,callable2,callable,callable2,callable,callable2,callable,callable2};


                SimpleAsyncs.launchTasks(5, tasks);
                SimpleAsyncs.launchTasks(5, callables);
                // todo протестировать и профилировать память на большое  и малое  число задач и
                //  потоков, так же на возможность исполнения отдельных методов возвращаемого
                //  экзекьютора, последствия сохранения ссылки на него в статиках, переменных
                //  и полях метода

                //for (int i=1;i<100;i++) SimpleAsyncs.launchTasks(2,tasks);
            }
        });
    }

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
        // проверка работы с исключениями
        @Override
        public Object call() throws Exception   {
            int i;
            Log.e(TAG, "from test callable+++++++++++++++++++++++++++++++" );

            try {
                if (Math.random() > 0.5f) return "from callable 1"; else
                    i=42/0;
                Thread.sleep((long) (5000 * Math.random()));
            } catch (Exception e) {
               Log.e(TAG, "exception! ") ;
            }

          // проверка реакции на проброс исключения - если убрать throw из call() k
                if (Math.random() > 0.5f) return "from callable 1"; else
                    i=42/0;
                Thread.sleep((long) (5000 * Math.random()));

       return null;};
    };


    final Callable callable2 = new Callable() {
        @Override
        public Object call() throws Exception {
            Thread.sleep((long) (5000 * Math.random()));
            return "from callable 2";

        }
    };


}