package c.ponom.executorsforjavalib;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;

@SuppressWarnings({"WeakerAccess", "unused"})
public  abstract class Task <T,R> {
    // типизация - тип аргумента, тип (R) результата

    private T argument = null;


    final public   Callable getCallableForExecutor() {
        return callableForExecutor;
    }

    final private Callable callableForExecutor = new Callable() {
        @Override
        final public R call() throws Exception {
            return  doTask(argument);
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




    public Task(@NonNull T argument) {
        this.argument = argument;
    }



    final public T getArguments() {
        return argument;
    }

    final public void setArguments(T arguments) {
        this.argument = arguments;
    }
}
