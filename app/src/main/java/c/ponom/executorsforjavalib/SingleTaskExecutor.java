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

import static java.lang.Math.random;

@SuppressWarnings("WeakerAccess")
public  class SingleTaskExecutor {


    /*
    аналог упрощенного старого АсинкТаска. Ограничения и особенности:
    1. Принимается ОДНА задача и используется один тред
    2. По завершении ее вызывается предоставленный коллбэк или ничего не вызывается если передан null.
    3. В его результатах будет объект с результатом (в т.ч. с полученным эксепшном при ошибке)
    4. Возвращается экзекьютор, с которым получатель может делать что хочет - к примеру отменить задание
    5. Для получения сведений об исключении отправленный на вызов Callable должен иметь
    строку "return exception;" в catch секции. Исключение придет в коллбэк в качестве результата
   */





    /**
     *
     * <p> Метод исполняет переданный ему список Callable, в одном потоке,  с вызовом
     * переданного в него коллбэка на завершающее событие исполнения
     *
     * @param
     * task - Callable, принимаемая на исполнение в отдельном потоке
     * @param
     * asyncCallBack - вызывается при завершении задачи с передачей туда результата задачи либо
     * полученного исключения
     * @param
     * activity - при передаче сюда активности, методы-слушатели вызываются в ее потоке,
     * при передаче null = в отдельном. Передача активности позволяет вызвать методы изменения ее ui
     *
     * @return возвращает * ThreadPoolExecutor, у которого можно в любой момент  запросить его
     * внутренними методами, к примеру, данные о числе выполненных и выполняемых задач, или
     * сбросить все и остановить
     *
     * Метод не реентерабельный, для передачи следущей задачи сознайте новый инстанс
     *
     */


     /*Пример передаваемой задачи:
     *           final Callable callable = new Callable() {
     *
     *         //@Override
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

    ThreadPoolExecutor currentExecutor;

    public ThreadPoolExecutor submitAsyncTask(@NonNull final Callable task,
                                              @Nullable final AsyncCallBack asyncCallBack,
                                              @Nullable final Activity activity) {
      if (currentExecutor != null)
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
                    result=exception;
                }
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



    interface   AsyncCallBack{

        void asyncResult(Object result);
    }




}