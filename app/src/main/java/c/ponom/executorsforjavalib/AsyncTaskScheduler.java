package c.ponom.executorsforjavalib;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import static c.ponom.executorsforjavalib.SimpleAsyncsTesting.TAG;

public class AsyncTaskScheduler extends AsyncExecutor{


    // здесь переопределяются коллбэки, выполняемые
    // по ходу исполнения задач
    // интересно, возможен ли случай когда активость прибьют пока мы тут чего-то делаем, то есть до того
    // как мы попробуем вызывать ее методы? И, кстати, как сюда передавать активность для этого самого?
    // можно, конечно, как очередной параметр, из интерфейсного метода, но это выглядит издевательски -
    // из активности в AsyncExecutor, потом из шедулера сюда, потом кастим ее к нашей.
    // может интерфейсы какие затеять из активности или включать ее в альтернативный конструктор этого
    // класса и хранить в нем?

    //todo - этот класс будет основной рабочий, а его абстрактный родитель может и в другом пакете
    // лежать. Следует понять где будут макеты запускаемого в потоках кода для модификации.
    // todo - кстати, можно ли и абстрактный и рабочий клас в одном файле держать? а то как то сложно будет.
    // ну или абстрактный в утилитах, а этот - в model
    // todo - Вопрос!! Если мы сделаем один класс - то есть тот не будет абстрактный, мы сможем обойтись одним
    //  файлом? Передав содержимое этого туда,  через pull Up member


    OnCompletedListener onCompletedListener = new OnCompletedListener() {
        @Override
        public void runAfterCompletion(ArrayList<Object> results) {
            Log.e(TAG, "runAfterCompletion: Ready" );
            Log.e(TAG, "Thread: "+Thread.currentThread().getName() );
            Log.e(TAG, "+++++++++++++++++++++++++++++++++++++++++++++" +
                    "result size= "+results.size() );


        }
    };

    AsyncCallBack asyncCallBack =new AsyncCallBack() {
        @Override
        public void asyncResult(Object result) {
            Log.e(TAG, "++++++++++++++++async result+++++"+result);
            Log.e(TAG, " Thread = WTF"+Thread.currentThread().getName());
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
