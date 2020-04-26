package c.ponom.executorsforjavalib;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;

@SuppressWarnings("WeakerAccess")
public  abstract class Task {


    private Object[] argument = null;


    final public Callable getCallableForExecutor() {
        return callableForExecutor;
    }

    final private Callable callableForExecutor = new Callable() {
        @Override
        final public Object call() throws Exception {

            return  doTask(argument);
        }
    };

    private Task() {

    }



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
}
