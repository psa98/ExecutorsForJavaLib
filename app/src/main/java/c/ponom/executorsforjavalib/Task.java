package c.ponom.executorsforjavalib;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;

@SuppressWarnings({"WeakerAccess", "rawtypes", "unused"})
public  abstract class Task  {


    private Object[] argument = null;


    final public   Callable getCallableForExecutor() {
        return callableForExecutor;
    }

    final private Callable callableForExecutor = new Callable() {
        @Override
        final public Object call() throws Exception {
            return  doTask(argument);
        }
    };

    private Task() {
        // приватный конструктор блокирует возможность создания задачи без установки аргументов.
        // для таких простых случаев можно обойтись простыми Callable.
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
     *                      включены в соответвующий результат в качестве объекта Exception
     */
    abstract public Object doTask(Object... argument)  throws Exception;




    public Task(@NonNull Object...argument) {
        this.argument = argument;
    }



    final public Object[] getArguments() {
        return argument;
    }

    final public void setArguments(Object...arguments) {
        this.argument = arguments;
    }
    //для экзотических случаев если нам надо как то поменять аргументы после создания задачи
}
