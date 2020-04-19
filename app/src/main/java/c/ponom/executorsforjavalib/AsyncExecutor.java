package c.ponom.executorsforjavalib;

import android.app.Activity;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static c.ponom.executorsforjavalib.MainActivity.TAG;

@SuppressWarnings("WeakerAccess")
public abstract class AsyncExecutor {

     long totalTasks;
     long tasksCompleted;




    /**
     *
     * <p> Метод исполняет переданный ему список Runable, в указанном числе потоков,  с вызовом
     * переданных в него слушателей на завершающие события исполнения потоков
     *
     * @param
     * numberOfThreads - число создаваемых потоков. Минимальное число -  1, для одного потока
     * обеспечивается последовательное исполнение переданных задач, для большего количества
     * порядок исполнения может быть любым.
     * @param
     * onCompletedListener,  -
     * @param
     * onEachCompletedListener,  -
     * @param
     * onErrorListener - * вызываются соответственно по завершении всех потоков, каждого потока,
     * и при возникновени любого исключения. При передаче значения null соответствующие вызовы
     * @param
     * tasks - Runnable, их массив или список, передаваемый на исполнение
     * @param
     * activity - Runnable, при передаче сюда активности методы-слушатели вызываются в ее потоке,
     * при передаче null = в другом
     * @return возвращает * ThreadPoolExecutor, у которого можно в любой момент  запросить
     * внутренними функциями, к примеру, данные о числе выполненных и выполняемых задач
     *
     * Exceptions - любые исключения, возникшие в процессе исполнения, возвращаются в onErrorListener,
     * если он установлен, иначе игнорируются.
     * таймауты исполнения потоков  в данный момент не устанавливаются и не используются
     *
     */

    // Внимание! все передаваемые в метод слушатели исполняются в основном (ui) потоке,
    // что позволяет использовать их, к примеру, для обновления шкалы исполнения или
    // иных элементов ui


    public ThreadPoolExecutor submitTasks(int numberOfThreads,
                                          OnCompletedListener onCompletedListener,
                                          OnEachCompletedListener onEachCompletedListener,
                                          OnErrorListener onErrorListener,
                                          Activity activity,
                                          Runnable... tasks)  {
        totalTasks=tasks.length;

        ThreadPoolExecutor currentExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);

        for (int taskNumber=0;taskNumber<tasks.length; taskNumber++) {

            Runnable boxedTask= boxTask( tasks[taskNumber],
                    taskNumber,
                    (taskNumber==tasks.length-1), // true для последней записи
                    onCompletedListener,
                    onEachCompletedListener,
                    onErrorListener,
                    activity,
                    currentExecutor);
            currentExecutor.submit(boxedTask);
        }
    currentExecutor.shutdown();
    return currentExecutor;
    }





    /*
    удобный метод с сокращенным набором параметров
    */
    public void submitTasks(int numberOfThreads,
                            OnCompletedListener onCompleted,
                            Activity activity,
                            Runnable... tasks){

        submitTasks(numberOfThreads,
                onCompleted,
                null,
                null,
                activity,
                tasks);

    }




    /*удобный метод с минимальным набором параметров*/
    public void submitTasks(int numberOfThreads, Activity activity,Runnable... tasks){

        submitTasks(numberOfThreads,
                null,
                null,
                null,
                activity,
                tasks);
    }



    interface   OnCompletedListener{

        void runAfterCompletion();

    }

    interface   OnEachCompletedListener{

        // переданные параметры могут быть использованы для оценки состояния исполнения
        void runAfterEach(long currentTaskNumber,
                          long tasksCompleted,
                          long totalTasks,
                          ThreadPoolExecutor currentExecutor,
                          float completion); // число выполненных задач от общего, в %
    }

    interface OnErrorListener{
        void runAfterError(int currentTaskNumber, Exception exception);

    }

    /* метод  при необходимости обрамляет переданную задачу переданными  в него слушателями */
    private Runnable boxTask(final Runnable nextTask,
                             final int currentTaskNumber,
                             final boolean isLastTask,
                             final OnCompletedListener onCompletedListener,
                             final OnEachCompletedListener onEachCompleted,
                             final OnErrorListener onErrorListener,
                             final Activity activity,
                             final ThreadPoolExecutor currentExecutor){


        final Runnable boxedTask =new Runnable() {
            @Override
            public void run() {

                try{
                nextTask.run();
                }
                catch ( Exception exception){
                    //todo - осталось разобраться с обработкой исключений, там вроде все сложно
                    Log.e(TAG, "run exception"+exception.getLocalizedMessage() );
                    if (activity==null) onErrorListener.runAfterError(currentTaskNumber, exception);

                else {activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onErrorListener.runAfterError(currentTaskNumber, exception);

                    }
                });}
                }
                // сюда попадаем вне зависимости от того была ошибка или нет.
                // При этом для последней по счету исполненной задачи вызывается onComplete
                tasksCompleted++;
                if (onEachCompleted!=null){
                    if (activity==null) onEachCompleted.runAfterEach(currentTaskNumber,
                            tasksCompleted,
                            totalTasks,
                            currentExecutor,
                            (float) tasksCompleted/(float) totalTasks*100f);
                    else {activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onEachCompleted.runAfterEach(currentTaskNumber,tasksCompleted,
                                    totalTasks,
                                    currentExecutor,
                                    (float) tasksCompleted/(float) totalTasks*100f);
                        }
                    });}
                }



                if (onCompletedListener!=null&&isLastTask){
                    if (activity==null)onCompletedListener.runAfterCompletion();
                    else {activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onCompletedListener.runAfterCompletion();
                        }
                    });}
                }



        };
    };
        return boxedTask;
    }
}
