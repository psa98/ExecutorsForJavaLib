package c.ponom.executorsforjavalib.TypedTasks;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;

@SuppressWarnings({"WeakerAccess", "rawtypes"})
public  abstract class Task<T,R> {

    // типизация - тип (T) аргумента, тип (R) результата задач

    private T arguments = null;


    final public   Callable getCallableForExecutor() {
        return callableForExecutor;
    }

    final private Callable callableForExecutor = new Callable() {
        @Override
        final public R call() throws Exception {
            return  doTask(arguments);
        }
    };

    private Task() {
    }


    /**
     * @param argument      Переопределите эту задачу для выполнения каждого задания пакета,
     *                      аргументы следует при необходимости привести к нужному виду, а при
     *                      использовании varargs - проверить состав и размер получаемого массива
     *
     * @return              Верните результат выполнения задачи. В случае необходимости передачи
     *                      сообщения об ошибке - можно передать соответствущее исключение в качетстве
     *                      результата, включив в ветку catch  ...return exception;}
     *
     * @throws Exception    В случае если исключения не обрабатываются, они будут проброшены выше и
     *                      включены в соответствующий результат в качестве объекта Exception
     */
    abstract public R doTask(T argument)  throws Exception;




    public Task(@NonNull T arguments) {
        this.arguments = arguments;
    }



    final public T getArguments() {
        return arguments;
    }

    final public void setArguments(T arguments) {
        this.arguments = arguments;
    }
}
