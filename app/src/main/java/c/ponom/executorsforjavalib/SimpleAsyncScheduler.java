package c.ponom.executorsforjavalib;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("WeakerAccess")
public class SimpleAsyncScheduler {



    /**
     * <p> Метод  создает экзекьютор с указанным числом потоков, которому
     * можно передать задачу или массив задач. Пример использования:
     * launchTasks(10,myRunnable);
     *
     *
     * @param
     * threads - запрашиваемое количество потоков
     *
     * @param
     * tasks - Runnable/Callable к исполнению или их массив (список)
     * метод launchTask выполняет переданный ему массив Runnable/Callable,
     * в указанном числе потоков, без вызова коллбэков и возвращения результатов
     * При необходимости получения результатов каждой задачи их можно, к примеру,
     * получать путем изменения внешнего к задаче объекта, к примеру, внешней
     * синхронизированной коллекции, с учетом требований к многопоточному коду.
     *
     * @return
     * Экзекьютор, в котором при желании можно вызвать управляющие им методы, доступные после шатдауна,
     * по крайней мере пока он жив - получить текущую очередь заданий, число выполненных и активных
     * заданий, вызвать shutdownNow() и тому подобное
     *
     * Напроминание пользователям:
     * Любая работа внутри многих потоков с общими данными или с влиянием на общие данные требует
     * использования конкурентных коллекций и атомарных переменных, волатильных переменных и
     * синхронизированных методов
     *
     * Дождаться завершения всех задач можно реализовав внутри переданных Runnable/Callable
     * счетчик
     */


    public static ThreadPoolExecutor launchTasks(int threads,@NonNull Runnable...tasks){
        if (tasks.length==0) throw new IllegalArgumentException("List of tasks is empty");
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        for (Runnable task:tasks){
            executor.submit(task);
        }
        executor.shutdown();
        return executor;
    }

    public static ThreadPoolExecutor launchTasks(int threads,@NonNull Callable...tasks){
        if (tasks.length==0) throw new IllegalArgumentException("List of tasks is empty");
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        for (Callable task:tasks){
            executor.submit(task);
        }
        executor.shutdown();
        return executor;
    }


    public static ThreadPoolExecutor launchTasks(int threads,@NonNull List tasks){
        if (tasks.isEmpty()) throw new IllegalArgumentException("List of tasks is empty");

        //todo - ниже определение того имеем ли мы дело со списком Runnable/Callable определяется п
        // по первому элементу. надо дополнить проверкой того что в списке все одного типа и постороннего
        // ничего нет
        Callable[] callableList=new Callable[tasks.size()];
        Runnable[] runnableList=new Runnable[tasks.size()];
        if (tasks.get(0) instanceof Callable) {
            callableList = (Callable[]) tasks.toArray(callableList);
            // вообще Null  тут не может быть но IDE настаивает
            if (callableList != null) return launchTasks(threads, callableList);
        }
        if (tasks.get(0) instanceof Runnable) {
            runnableList = (Runnable[]) tasks.toArray(runnableList);
            // вообще Null  тут не может быть но IDE настаивает
            if (runnableList != null) return launchTasks(threads, runnableList);
        }
        throw new IllegalArgumentException("List of tasks can contain only Runnable/Callable");

        // перегнать на котлин заново
        }

}

