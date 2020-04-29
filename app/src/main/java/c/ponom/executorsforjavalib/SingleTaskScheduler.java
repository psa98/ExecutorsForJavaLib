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
    строку "return exception;" в catch секции,. Исключение придет в коллбэк в качестве результата
    6. Типичный юзкейс - просто бросить сюда лямбду, Runnable (тогда в коллбэке надо просто
    обработать исключение

   */
    // todo - добавить в примеры вторую активность для упрощенных методов, реализовав, к примеру,
    //  скачку 10 общедоступных файлов,
    // TODO  сделать опциональный прием Runnable  через обертку, при этом в коллбэк
    //  ничего не передается
    //  При портировании на Котлин вместо четырех  методов будет один с разным набором
    //  (именованных) параметров

    /**
     *
     * <p> Метод исполняет переданный ему список Callable, в одном потоке,  с вызовом
     * переданного в него коллбэка на завершающее событие исполнения.
     * Типичное
     *
     * @param
     * task - Callable, принимаемая на исполнение в отдельном потоке
     * @param
     * asyncCallBack, параметр необязательный, может быть опущен или передан null
     *  соответствующий метод вызывается при завершении задачи с передачей туда результата задачи либо
     *  полученного при ее выполнении исключения как результата, если оно не обработано в поданной
     *  на исполнении задаче.
     *                //TODO - протестировать юнит тестами оба метода передачи
     *                    исключений - отсутствие обработки и возврат как результа
     *
     * @return возвращает ThreadPoolExecutor, у которого можно в любой момент  запросить его
     * внутренними методами, к примеру, сбросить задачу и остановить исполнение через
     * shutdownNow(), или блокировать поток до исполнения/таймаута через
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

    public ThreadPoolExecutor submitAsyncTask(@NonNull final Callable task)
    {return submitAsyncTask(task,null);}







    interface   AsyncCallBack{
        void asyncResult(Object result);
    }




}