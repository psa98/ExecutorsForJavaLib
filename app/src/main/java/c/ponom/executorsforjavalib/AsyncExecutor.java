package c.ponom.executorsforjavalib;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("WeakerAccess")
public  class AsyncExecutor {

    int  tasksCompleted= 0;
    // что узнал полезного - AtomicInteger непригоден как счетчик. иногда он изменяется
    // из иного аппаратного потока. Использовать обычную синхронизацию. Далее:
    // в блок синхронизации кроме доступа КО ВСЕМ объектам, способным изменяться из разных
    // потоков надо включать и все операции собственно изменения. ВСЕ сравнения и проверки
    // значений надо так же производить в том же блоке. В качестве ЗАМКА следует использовать
    // финальную переменную метода или финальное поле класса (лучше последнее, причем
    // для РАЗНЫХ блоков синхронизации следует по возможности использовать РАЗНЫЕ объекты
    // Использование this для синхронизации внутри анонимного класса, включая Callable/Runnable
    // ведет к непредсказуемому поведению. Использовать МАКСИМАЛЬНО внешние к блоку объекты,
    // точно не изменяемые другими тредами. Можно использовать имя самого внешнего класса
    // как самое надежное = this внутри класса КРОМЕ вложенных и анонимных, где только
    // полностью квалифицированное имя
    final Collection<Object> results= Collections.synchronizedList(new ArrayList<>());
    int totalTasks = 0;;

     ThreadPoolExecutor currentExecutor=null;

    //ThreadPoolExecutor - тут используется именно он, поскольку  у него больше методов,
    // и он более управляем чем обычный Executors.newFixedThreadPool(n)


    /**
     *
     * <p> Метод исполняет переданный ему список Callable, в указанном числе потоков,  с вызовом
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
     * tasks - Runnable, их массив или список, передаваемый на исполнение
     * @param
     * activity - при передаче сюда активности, методы-слушатели вызываются в ее потоке,
     * при передаче null = в отдельном. Передача активности позволяет вызвать методы изменения ее ui
     * @return возвращает * ThreadPoolExecutor, у которого можно в любой момент  запросить
     * внутренними функциями, к примеру, данные о числе выполненных и выполняемых задач, или
     * сбросить все и остановить
     *
     *
     * Таймауты исполнения потоков   не устанавливаются и не используются,
     * но на возвращенный экзекютор можно повесить блокирующий метод awaitTermination(время)
     * ВАЖНО:
     * экзекьютор нереентерабельный, второй submit работать не будет и бросит исключение
     * Создавайте новый!
     *
     * Пример передаваемой задачи:
     *
     * final Callable callable = new Callable() {
     *
     *         //@Override
     *         public Object call() throws Exception {
     *             int i;
     *
     *
     *           Thread.sleep((long) (8000 * Math.random()));
     *           if (Math.random() > 0.5f) return "from callable 1";
     *           else  i=42/0; // <=== пример точки возникновения исключения
     *           если исключение обработать, то оно не попадет в результат слушателя каждой
     *           операции, и в итоговую коллекцию результатов не попадет
     *
     *           return "from callable 1";
     *              }
     *          };
     *
     *
     *
     *
     */






    public ThreadPoolExecutor submitTasks(int numberOfThreads,
                                          OnCompletedListener onCompletedListener,
                                          OnEachCompletedListener onEachCompletedListener,

                                          Activity activity,
                                          Callable... tasks)  {



        totalTasks=tasks.length;

        if (currentExecutor!=null&&currentExecutor.isTerminating())
            throw new IllegalStateException("This scheduler has tasks or is in shutdown  mode," +
                " make a new instance");


        currentExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);

        for (int taskNumber=0;taskNumber<tasks.length; taskNumber++) {

            Callable boxedTask= boxTask( tasks[taskNumber],
                    taskNumber,
                    onCompletedListener,
                    onEachCompletedListener,
                    activity,
                    currentExecutor);
            currentExecutor.submit(boxedTask);

        }
    currentExecutor.shutdown();
    return currentExecutor;

    }



    /*
    удобный метод с сокращенным набором параметров, вызов только завершающего кода
    */
    public void submitTasks(int numberOfThreads,
                            OnCompletedListener onCompleted,
                            Activity activity,
                            Callable... tasks){

        submitTasks(numberOfThreads,
                onCompleted,
                null,
                activity,
                tasks);

    }




    /*удобный метод с минимальным набором параметров, ничего не вызываем*/
    public void submitTasks(int numberOfThreads, Activity activity,Callable... tasks){

        submitTasks(numberOfThreads,
                null,
                null,
                activity,
                tasks);
    }

    /*
    аналог упрощенного старого АсинкТаска. Ограничения и особенности:
    1. Принимается ОДНА задача и используется один тред
    2. По завершении ее вызывается предоставленный коллбэк или ничего не вызывается если передан null.
    3. В его результатах будет объект с результатом (в т.ч. с полученным эксепшном при ошибке)
    4. Возвращается экзекьютор, с которым получатель может делать что хочет - к примеру отменить задание
    5. Для получения сведений об исключении отправленный на вызов Callable должен иметь
    строку "return exception;" в catch секции. Исключение придет в коллбэк в качестве результата
   */



    public ThreadPoolExecutor asyncTask(@NonNull final Callable task,
                                        @Nullable final AsyncCallBack asyncCallBack,
                                        @Nullable final Activity activity) {

        if (currentExecutor != null && currentExecutor.isTerminating())
            throw new IllegalStateException("This scheduler is in shutdown  mode," +
                    " make a new instance");
        else  currentExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);


        final Callable boxedTask = new Callable() {
            @Override
            public Object call() {

                Object result = null;
                try {
                    result = task.call();
                } catch (Exception exception) {
                    //exception.printStackTrace();
                    result=exception;

                }
                /* когда мы попадаем сюда, у нас:
                В result будее результат кода, либо объект Exception если была ошибка.
                обработка ошибок - если получен Exception - он возвращается из перепределенной
                задачи в качестве результата,
                 получатель сам анализирует тип результата
                */
                final Object finalResult = result;
                if (asyncCallBack != null) {
                    if (activity == null) {
                        asyncCallBack.asyncResult(finalResult);
                    } else {

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                asyncCallBack.asyncResult(finalResult);
                            }
                        });
                    }
                }
                return result;
            }
         };
        currentExecutor.submit(boxedTask);
        currentExecutor.shutdown();
        return currentExecutor;
    }



    /* метод  при необходимости обрамляет переданную задачу переданными  в него слушателями */
    private Callable boxTask(final Callable nextTask,
                             final int currentTaskNumber,
                             final OnCompletedListener onCompletedListener,
                             final OnEachCompletedListener onEachCompleted,
                             final Activity activity,
                             final ThreadPoolExecutor currentExecutor){


        //TODo - это крайне плохой стиль программирования, такое количество вложенных
        // if надо разбивать все это минимум на пять разных методов, кторые можно по
        // отдельности покрыть тестами

         final Callable boxedTask =new Callable() {
            @Override
            public Object call() throws Exception {

                Object result = null;

                try{
                result = nextTask.call();
               }

                catch (  Exception exception) {
                    //exception.printStackTrace();
                    result=exception;
                }
                /* когда мы попадаем сюда, у нас:
                В result будее результат кода, либо объект Exception если была ошибка.
                */

                // число выполненных задач считается с единицы, не с 0
                //обработка ошибок - если получен Exception - он возвращается в качестве результата,
                // получатель сам анализирует тип результата

                final Object finalResult = result;

                synchronized (AsyncExecutor.class) {
                    results.add(finalResult);

                }
                    if (onEachCompleted != null) {
                        if (activity == null) {
                            onEachCompleted.runAfterEach(currentTaskNumber, finalResult,
                                    tasksCompleted,
                                    totalTasks,
                                    currentExecutor,
                                    (float) tasksCompleted / (float) totalTasks * 100f);
                        } else {

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onEachCompleted.runAfterEach(currentTaskNumber, finalResult, tasksCompleted,
                                            totalTasks,
                                            currentExecutor,
                                            (float) tasksCompleted / (float) totalTasks * 100f);
                                }
                            });
                        }
                    }
                    // обеспечение вызова завершающего кода

                    synchronized (AsyncExecutor.class) {
                        tasksCompleted++;
                       if (tasksCompleted < totalTasks) return null;

                        if (onCompletedListener == null) return null;


                        else if (activity == null) {
                            onCompletedListener.runAfterCompletion(results);
                            return null;
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onCompletedListener.runAfterCompletion(results);
                                }
                            });
                        }

                    }
            return null;
            }


         };

        return boxedTask;
    }



    interface   AsyncCallBack{

            void asyncResult(Object result);
        }


    interface   OnCompletedListener{

        void runAfterCompletion(Collection<Object> results);

    }

    interface   OnEachCompletedListener{

        // переданные параметры могут быть использованы для оценки состояния исполнения
        void runAfterEach(long currentTaskNumber,
                          Object result, long tasksCompleted,
                          long totalTasks,
                          ThreadPoolExecutor currentExecutor,
                          float completion); // число выполненных задач от общего, в %
    }
    /*
    при переопредении данной функции следует внутри структуры catch (каждой) в конце выполнить
    return exception.

     */

}
