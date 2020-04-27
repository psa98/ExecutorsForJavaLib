package c.ponom.executorsforjavalib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("WeakerAccess")
public  class SingleTaskScheduler {


    /*
    аналог упрощенного старого АсинкТаска. Ограничения и особенности:
    1. Принимается ОДНА задача и используется один тред
    2. По завершении ее вызывается предоставленный коллбэк или ничего не вызывается если передан null.
    3. В его результатах будет объект с результатом (в т.ч. с полученным эксепшном при ошибке)
    4. Возвращается экзекьютор, с которым получатель может делать что хочет - к примеру отменить задание
    5. Для получения сведений об исключениях отправленный на вызов Callable должен иметь
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

     * @return возвращает * ThreadPoolExecutor, у которого можно в любой момент  запросить его
     * внутренними методами, к примеру, данные о числе выполненных и выполняемых задач, или
     * сбросить все и остановить
     *
     * Метод не реентерабельный, для передачи следущей задачи сознайте новый инстанс
     *
     */



    ThreadPoolExecutor currentExecutor;

    public ThreadPoolExecutor submitAsyncTask(@NonNull final Callable task,
                                              @Nullable final AsyncCallBack asyncCallBack) {
      if (currentExecutor != null)
            throw new IllegalStateException("This scheduler is in shutdown  mode," +
                    " make a new instance");
        else  currentExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

        final Callable boxedTask = new Callable() {
            @Override
            public Object call() {
                Object result;
                try {
                    result = task.call();
                } catch (Exception exception) {
                    result=exception;
                }
                final Object finalResult = result;
                if (asyncCallBack != null)  asyncCallBack.asyncResult(finalResult);
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