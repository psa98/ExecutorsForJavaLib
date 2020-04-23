package c.ponom.executorsforjavalib;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("WeakerAccess")
public class SimpleAsyncExecutor {

    /**
     * <p> Метод  создает экзекьютор с указанным числом потоков, которому
     * можно передать задачу или массив задач. Пример использования:
     * launchTasks(10,myRunnable);
     *
     * @param
     * threads - запрашиваемое количество потоков
     *
     * @param
     * tasks - Runnable/Callable к исполнению или их массив (список)
     * метод launchTask выполняет переданный ему массив Runnable,
     * в указанном числе потоков, без вызова коллбэков и возвращения результатов
     * При необходимости получения результатов каждой задачи их можно, к примеру,
     * получать путем изменения внешнего к задаче объекта, к примеру, внешней
     * синхронизированной коллекции, с учетом требований к многопоточному коду.
     *
     * @return
     * Экзекьютор, в котором при желании можно вызвать управляющие им методы, доступные после шатдауна
     * по крайней мере пока он жив - получить текущую очередь заданий, число выполненных и активных
     * заданий, вызвать shutdownNow() и тому подобное
     *
     * Напроминание пользователям:
     * Любая работа внутри многих потоков с общими данными или с влиянием на общие данные требует
     * использования конкурентных коллекций и атомарных переменных, волатильных переменных и
     * синхронизированных методов
     *
     * Дождаться завершения всех задач можно:
     * 1. Дождаться пока количество полученных коллбэков сравняется числу переданных
     * 2. Или выполнив блокирующий awaitingTermination (timeout) к полученному экзекьютору
     *
     */




    public static ThreadPoolExecutor launchTasks(int threads,Runnable...tasks){
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        for (Runnable task:tasks){
            executor.submit(task);
        }
        executor.shutdown();
        return executor;
    }

    public static ThreadPoolExecutor launchTasks(int threads,Callable...tasks){
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        for (Callable task:tasks){
            executor.submit(task);
        }
        executor.shutdown();
        return executor;
    }
}
