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
public  class AsyncTasksScheduler {

    int  tasksCompleted= 0;
    // Общая информация по итогам написания
    // AtomicInteger не особо пригоден как счетчик. Иногда по итогам тестов он изменяется
    // из иного аппаратного потока. Использовать  лучше обычную синхронизацию. Далее:
    // в блок синхронизации кроме доступа КО ВСЕМ объектам, способным изменяться из разных
    // потоков надо включать и все операции собственно их изменения. ВСЕ сравнения и проверки
    // значений надо так же производить в том же блоке. В качестве мутекса следует использовать
    // финальную переменную метода или финальное поле класса, или имя нестатического метода
    // (лучше последнее, причем  для РАЗНЫХ блоков синхронизации следует по возможности
    // использовать РАЗНЫЕ объекты  Использование внешнего this для синхронизации внутри
    // анонимного класса, а так же иного внутреннего класса включая Callable/
    // Runnable может привести непредсказуемому поведению. Использовать надо внешние к блоку объекты,
    // точно не изменяемые другими тредами. Можно использовать имя самого внешнего класса
    // как самое надежное = this внутри класса КРОМЕ вложенных и анонимных, где
    // полностью квалифицированное имя внешнего класса

    final Collection<Object> results= Collections.synchronizedList(new ArrayList<>());
    int totalTasks = 0;

    final Object innerLock = new Object();
    ThreadPoolExecutor currentExecutor=null;


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
     * onCompletedListener,  - исполняется один раз после полного исполнения всех переданных задач.
     * получает список объектов, содержащий результаты исполнения каждой задачи в порядке исполнения
     * Если необходимо получить и порядковые номера преданных задач - возвращать  следует кортеж или
     * иной составной объект. При появлении исключения оно будет включено в качестве результата задачи.
     * Аргумент null - не вызывает слушатель.
     *
     * @param
     * onEachCompletedListener,  вызывается строго по порядку исполения задач, возвращает ее порядковые
     *  номера по порядку поступления и порядку исполнения, результат исполнения задачи (или исключение)
     *  а так же процент исполнения = выполненные задачи / общее количество * 100
     *  Аргумент null - не вызывает слушатель.
     *
     * @param
     * tasks - Runnable, их массив или список, передаваемый на исполнение
     * @param
     * activity - при передаче сюда активности, методы-слушатели вызываются в ее потоке,
     * при передаче null = в отдельном. Передача активности позволяет вызвать методы изменения ее ui
     * внутри задач (следует выполнять проверку на null при этом)
     * @return возвращает  ThreadPoolExecutor, у которого можно в любой момент  запросить
     * внутренними методами, к примеру, данные о числе выполненных и выполняемых задач, или
     * сбросить все и остановить
     *
     *
     * Таймауты исполнения потоков   не устанавливаются и не используются,
     * но на возвращенный экзекютор можно повесить блокирующий метод awaitTermination(время)
     *
     * Метод нереентерабельный, второй submit работать не будет и бросит исключение
     * Создавайте новый инстанс!
     *
     *Пример передаваемой задачи:
     *           final Callable callable = new Callable() {
     *
     *          //@Override
     *
     *          public Object call() throws Exception {
     *          // тут находится код, не требующий синхронизации, в том числе блокирующий
     *
     *           synchronized  (mutex){
     *
     *           // в синхронизированном блоке размещается код, работающий с общими данными
     *
     *           if (Math.random() > 0.5f) return "from callable 1";
     *           else  i=42/0; // <=== пример точки возникновения исключения
     *           если исключение обработать, то оно не попадет в результат слушателя каждой
     *           операции, и в итоговую коллекцию результатов не попадет
     *
     *           return "from callable 1";
     *              }
     *          }
     *          };
     */



    public ThreadPoolExecutor submitTasks(int numberOfThreads,
                                          OnCompletedListener onCompletedListener,
                                          OnEachCompletedListener onEachCompletedListener,
                                          Activity activity,
                                          Callable... tasks)  {

        totalTasks=tasks.length;

        if (currentExecutor!=null)
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





    /* метод  при необходимости обрамляет переданную задачу переданными  в него слушателями  и вызовами */
     private Callable boxTask(final Callable nextTask,
                             final int currentTaskNumber,
                             final OnCompletedListener onCompletedListener,
                             final OnEachCompletedListener onEachCompleted,
                             final Activity activity,
                             final ThreadPoolExecutor currentExecutor){


            final Callable boxedTask =new Callable() {
            @Override
            public Object call() throws Exception {

                Object result = null;

                try{
                result = nextTask.call();
               }

                catch (  Exception exception) {

                    result=exception;
                }
                /* когда мы попадаем сюда, у нас:
                В result будее результат кода, либо объект Exception если была ошибка.
                */

                // число выполненных задач считается с единицы, не с 0
                //обработка ошибок - если получен Exception - он возвращается в качестве результата,
                // получатель сам анализирует тип результата

                final Object finalResult = result;

                synchronized (innerLock) {
                    results.add(finalResult);

                    final float completionPercent = (float) tasksCompleted+1 / (float) totalTasks * 100f;

                    if (onEachCompleted != null) {
                        if (activity == null) {
                            // счет выполненных  задач начинается с единицы
                            onEachCompleted.runAfterEach(currentTaskNumber, finalResult,
                                    tasksCompleted+1,
                                    totalTasks,
                                    currentExecutor,completionPercent
                                    );
                        } else {

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onEachCompleted.runAfterEach(currentTaskNumber,
                                            finalResult, tasksCompleted+1,
                                            totalTasks,
                                            currentExecutor,
                                            completionPercent);
                                }
                            });
                        }
                    }

                        // обеспечение вызова завершающего кода

                        tasksCompleted++;
                        if (tasksCompleted < totalTasks) return null;

                    }
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

            return null;
            }
         };
        return boxedTask;
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
