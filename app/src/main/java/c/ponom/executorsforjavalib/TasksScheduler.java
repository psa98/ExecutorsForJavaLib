package c.ponom.executorsforjavalib;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


@SuppressWarnings("WeakerAccess")
public  class TasksScheduler {




    final Collection<ResultedRecord> resultsByTaskOrder =
            Collections.synchronizedList(new ArrayList<ResultedRecord>());
    int totalTasks = 0;

    final Object innerLock = new Object();
    ThreadPoolExecutor currentExecutor=null;
    int  tasksCompleted= 0;

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
     * onCompleted,  - исполняется один раз после полного исполнения всех переданных задач.
     * получает список объектов, содержащий результаты исполнения каждой задачи в порядке исполнения
     * Если необходимо получить и порядковые номера преданных задач - возвращать  следует кортеж или
     * иной составной объект, соответсвенно изменив тип возвращаемого в переопределенном методе
     * doTask() задачи. При появлении исключения оно будет включено в качестве результата задачи.
     * Если аргумент = null -  вызовы не осуществляются.
     *
     * @param
     * onEachCompleted,  вызывается строго по порядку исполения задач, возвращает ее порядковые
     *  номера по порядку поступления и порядку исполнения, результат исполнения задачи (или исключение)
     *  а так же процент исполнения = выполненные задачи / общее количество * 100
     * Если аргумент = null -  вызовы не осуществляются.
     *
     * @param
     * tasks - Callable, их массив или список, передаваемый на исполнение
     * @param
     * activity - при передаче сюда активности, методы-слушатели вызываются в ее потоке,
     * при передаче null = в отдельном. Передача параметра в качестве параметра текущей активности
     * позволяет вызывать методы изменения ее ui внутри onCompleted/nEachCompleted.
     * (перед этим следует проверить существование активности на момент вызова)
     *
     * @return возвращает  ThreadPoolExecutor, у которого можно в любой момент  запросить
     * внутренними методами, к примеру, данные о числе выполненных и выполняемых задач, получить
     * очередь текущих задач или сбросить все оставшиеся задачи и остановить работу
     *
     * Таймауты исполнения потоков   не устанавливаются и не используются,
     * но на возвращенный экзекютор можно повесить блокирующий метод awaitTermination(время)
     *
     * Метод нереентерабельный, второй submit работать не будет и бросит исключение
     * Создавайте новый инстанс!
     *
     */


    // todo - сделать возможность передачи аргумента в виде списка, а не только массива
    public ThreadPoolExecutor submitTasks(int numberOfThreads,
                                          OnCompleted onCompleted,
                                          OnEachCompleted onEachCompleted,
                                          Activity activity,
                                          Task... tasks)  {



        if (currentExecutor!=null)
            throw new IllegalStateException("This scheduler has tasks or is in shutdown  mode," +
                " make a new instance");
        currentExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);
        tasksCompleted= 0;
        totalTasks=tasks.length;
        for (int taskNumber=0;taskNumber<totalTasks; taskNumber++) {
            Task boxedTask= boxTask( tasks[taskNumber],
                    taskNumber,
                    onCompleted,
                    onEachCompleted,
                    activity,
                    currentExecutor);
            currentExecutor.submit(boxedTask.getCallableForExecutor());
        }
    currentExecutor.shutdown();
    return currentExecutor;
    }






    /* метод  при необходимости обрамляет переданную задачу переданными  в него обратными
    вызовами */

    private Task boxTask(final Task nextTask,
                             final int currentTaskNumber,
                             final OnCompleted onCompleted,
                             final OnEachCompleted onEachCompleted,
                             final Activity activity,
                             final ThreadPoolExecutor currentExecutor){


            final Task boxedTask =new Task(nextTask.getArguments()) {
                @Override
                public Object doTask(final Object...arguments) throws Exception {


                Object result = null;

                try{
                result = nextTask.doTask(arguments);
               }

                catch (  Exception exception) {

                    result=exception;
                }
                /* когда мы попадаем сюда, у нас:
                В result будет результат кода, либо объект Exception если была ошибка.
                Если Exception  возвращается в качестве результата, получатель сам
                анализирует причины появления
                */



                final Object finalResult = result;

                synchronized (innerLock) {
                    final ResultedRecord resultedRecord = new ResultedRecord(tasksCompleted,arguments,result);
                    resultsByTaskOrder.add(resultedRecord);
                    // число выполненных задач считается с единицы, не с 0
                    tasksCompleted++;
                    final double completionPercent = (((float) tasksCompleted )/( (float) totalTasks)) * 100f;
                    final long currentTaskByExecutionOrder=tasksCompleted;
                    if (onEachCompleted != null) {
                        if (activity == null) {
                            // счет выполненных  задач начинается с единицы
                            onEachCompleted.runAfterEach(currentTaskNumber, finalResult,
                                    totalTasks,currentTaskByExecutionOrder ,
                                    currentExecutor,
                                    completionPercent, arguments);
                        } else {

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onEachCompleted.runAfterEach(currentTaskNumber,
                                            finalResult,
                                            totalTasks,
                                            currentTaskByExecutionOrder,
                                            currentExecutor,
                                            completionPercent,
                                            arguments);
                                }
                            });
                        }
                    }



                        if (tasksCompleted < totalTasks) return null;
                    } // конец блока синхронизации

                    // обеспечение вызова завершающего кода
                        if (onCompleted == null) return null;
                        else if (activity == null) {
                            onCompleted.runAfterCompletion(resultsByTaskOrder);
                            return null;
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onCompleted.runAfterCompletion(resultsByTaskOrder);
                                }
                            });
                        }

            return null;
            }
         };
        return boxedTask;
    }




    interface OnCompleted {

        /**
         * @param results - по завершению исполнения всех задач возвращается коллекция, содержащая
         * результаты их исполения в порядке постановки.
         */

        void runAfterCompletion(Collection<ResultedRecord> results);
        // по завершению исполнения всех задач возвращается коллекция, содержащая
        // их результаты в порядке постановки.

    }

    interface OnEachCompleted {



        /**
         * @param currentTaskNumber - соответствует номеру переданной задачи по порядку их передачи
         * в массиве заданий Task (...) в метод submitTasks, и позиции соотв.массива
         *
         * @param result    - возврашает объект, содержащий результат операции doTask(..)
         * Объект следует привести к необходимому типу. Следует учитывать, что объект МОЖЕТ
         * содержать Exception в качестве результата, таким образом следует обязательно проводить
         * проверку типа полученного объекта перед дальнейшей обработкой
         *
         * @param totalTasks - общее количество переданных задач
         *
         * @param currentTaskByExecutionNumber - номер переданной задачи по порядку исполнения,
         * при использовании более чем одного треда порядок их исполнения межет быть любым.
         * Показатель считается от единицы и монотонно растет до размера пакета переданных заданий
         * @param currentExecutor - ссылка на текущий ThreadPoolExecutor, у  которого можно в любой
         * момент  запросить его  внутренними методами, к примеру, данные о числе выполненных и
         * выполняемых задач, или  сбросить все задачи  и остановить работу
         * @param completion - отношение числа выполнных задач к общему числу задач пакета, в процентах
         * @param argument Аргументы, переданные текущей  исполненной задаче
         */
        void runAfterEach(long currentTaskNumber,
                          Object result,
                          long totalTasks,
                          long currentTaskByExecutionNumber,
                          ThreadPoolExecutor currentExecutor,
                          double completion,
                          Object...argument);
    }

    public class ResultedRecord {
        int recordNumber;
        Object[] arguments;
        Object result;

        public ResultedRecord(int recordNumber, Object[] arguments, Object result) {
            this.recordNumber = recordNumber;
            this.arguments = arguments;
            this.result = result;
        }

}

}
