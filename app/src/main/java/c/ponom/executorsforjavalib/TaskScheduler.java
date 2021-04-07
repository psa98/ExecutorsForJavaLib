package c.ponom.executorsforjavalib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;



@SuppressWarnings({"UnusedReturnValue", "unused","WeakerAccess"})
public  class TaskScheduler {

    final Collection<ResultedRecord> resultsByExecutionOrder =
            Collections.synchronizedList(new ArrayList<ResultedRecord>());

    int totalTasks = 0;

    final Object innerLock = new Object();
    ThreadPoolExecutor currentExecutor=null;
    int  tasksCompleted= 0;
    Comparator<ResultedRecord> comparator = new Comparator<ResultedRecord>() {
        @Override
        public int compare(ResultedRecord resultedRecord1, ResultedRecord resultedRecord2) {
            return Integer.compare(resultedRecord1.taskNumber, resultedRecord2.taskNumber);
        }



    };


    /* TODO - пожелания на будущее:
    1. Возможный переход на Java 1.8 - позволит использвать лямбды и более гибкий newWorkStealingPool
    2. Тестирование с реальными задачачами на обмен с сетью, в т.ч. с имитацией таймаутов, сетевых ошибок
    и с файловой системой.
    3. Нагрузочное тестирование на реальном аппарате, управление приоритетами потоков
    (поднять приоритет исполнения потока реально, я это делал)
     */




    /**
     * <p> Метод исполняет переданный ему список Tasks, в указанном числе потоков,  с вызовом
     * переданных в него слушателей на завершающие события исполнения потоков
     *
     * @param
     * numberOfThreads - число создаваемых потоков. Минимальное число -  1, для одного потока
     * обеспечивается последовательное исполнение переданных задач, для большего количества
     * порядок исполнения может быть любым.
     * @param
     * onCompleted,  - исполняется один раз после полного исполнения всех переданных задач,
     * возвращает список переданных задач по порядку, соответствующему исходному в пакете задач.
     * Необработанные исключения будет включены в результат задачи как объект подтипа Exception.
     * Если аргумент = null -  вызовы не осуществляются.
     *
     * @param
     * onEachCompleted,  вызывается строго по порядку исполения задач, возвращает ее порядковые
     *  номера по порядку поступления и порядку исполнения, результат исполнения задачи (или исключение)
     *  а так же процент исполнения = выполненные задачи / общее количество * 100
     * Если аргумент = null -  вызовы не осуществляются.
     *
     * @param
     * tasks - Tasks, их массив или список, передаваемый на исполнение
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
     */



    @SuppressWarnings("UnusedReturnValue")
    public ThreadPoolExecutor submitTasks(int numberOfThreads,
                                          OnCompleted onCompleted,
                                          OnEachCompleted onEachCompleted,
                                          Task... tasks)  {


        if (tasks.length==0) throw new IllegalArgumentException("Tasks list is empty");
        if (currentExecutor!=null)
            throw new IllegalStateException("This scheduler has tasks or is in shutdown  mode," +
                " make a new instance");
        currentExecutor = (ThreadPoolExecutor) Executors.
                newFixedThreadPool(numberOfThreads);
        tasksCompleted= 0;
        totalTasks=tasks.length;
        for (int taskNumber=0;taskNumber<totalTasks; taskNumber++) {
            Task boxedTask= boxTask( tasks[taskNumber],
                    taskNumber,
                    onCompleted,
                    onEachCompleted,
                    currentExecutor);
            currentExecutor.submit(boxedTask.getCallableForExecutor());
        }
    currentExecutor.shutdown();
    return currentExecutor;
    }



    public ThreadPoolExecutor submitTasks(int numberOfThreads,
                                          OnCompleted onCompleted,
                                          OnEachCompleted onEachCompleted,
                                          List<Task> listOfTasks)  {
        final Task[] arrayOfTasks =  listOfTasks.toArray(new Task[]{});
        return submitTasks(numberOfThreads,onCompleted,
        onEachCompleted, arrayOfTasks);
    }


    /* метод  "обрамляет" переданную задачу переданными  в него обратными
    вызовами  и передает полученные результаты в собираемые коллекции*/

    private Task boxTask(final Task nextTask,
                             final int currentTaskNumber,
                             final OnCompleted onCompleted,
                             final OnEachCompleted onEachCompleted,
                             final ThreadPoolExecutor currentExecutor){


            @SuppressWarnings("UnnecessaryLocalVariable")
            final Task boxedTask =new Task(nextTask.getArguments()) {
                @Override
                public Object doTask(final Object...arguments) {


                    Object result;

                    try {
                        result = nextTask.doTask(arguments);
                    } catch (Exception exception) {

                        result = exception;
                    }
                /* когда мы попадаем сюда, у нас в result будет результат кода, либо объект
                Exception если была ошибка.
                Если Exception  возвращается в качестве результата, получатель сам
                анализирует причины ее появления
                */


                    final Object finalResult = result;

                    synchronized (innerLock) {

                        final ResultedRecord resultedRecord =
                                new ResultedRecord(currentTaskNumber, arguments, result);
                        resultsByExecutionOrder.add(resultedRecord);
                        // число выполненных задач для передачи в onEach считается с единицы, не с 0
                        tasksCompleted++;
                        final double completionPercent = (((float) tasksCompleted) / ((float) totalTasks)) * 100f;
                        final int currentTaskByExecutionOrder = tasksCompleted;
                        if (onEachCompleted != null) {
                            // счет выполненных  задач начинается с единицы
                            onEachCompleted.runAfterEach(currentTaskNumber, finalResult,
                                    totalTasks, currentTaskByExecutionOrder,
                                    currentExecutor,
                                    completionPercent, arguments);
                        }
                        if (tasksCompleted < totalTasks) return null;
                    }   // конец блока синхронизации. Синхронизируем инкремент счетчика и проверку его
                        // значения, иначе будут твориться чудеса

                    // обеспечение вызова завершающего кода
                    if (onCompleted == null) return null;
                    final ArrayList<ResultedRecord> resultsByTaskOrder =
                            new ArrayList<>(resultsByExecutionOrder);
                    Collections.sort(resultsByTaskOrder, comparator);
                    onCompleted.runAfterCompletion(resultsByExecutionOrder, resultsByTaskOrder);
                    return null;
                }};

        return boxedTask;
    }




    interface OnCompleted {

        /**
         * @param resultsByExecutionOrder - по завершению исполнения всех задач возвращается коллекция,
         * содержащая результаты их исполнения в порядке исполнения, порядковый номер и переданные в задачу
         * аргументы.
         *
         * @param resultsByTaskOrder - та же коллекция результатов, отсортированная по порядку переданных
         * в метод submitTasks(...) задач
         */

        void runAfterCompletion(Collection<ResultedRecord> resultsByExecutionOrder,
                                Collection<ResultedRecord>resultsByTaskOrder);
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
         *
         *
         * Если мы в данном методе используем, к примеру, обновление элементов UI, то следует учитывать что
         * первые  numberOfThreads задач будут исполняться одновременно, и ранее завершения исполнения
         * первой задачи этот метод не вызывается, а потом будет вызван почти одновременно для numberOfThreads
         * задач. Это может вызвать, к примеру, резкое движение progressBar в начале исполнения задач.
         */
        void runAfterEach(int currentTaskNumber,
                          Object result,
                          int totalTasks,
                          int currentTaskByExecutionNumber,
                          ThreadPoolExecutor currentExecutor,
                          double completion,
                          Object...argument);
    }

    public class ResultedRecord {
        public int taskNumber;
        public Object[] arguments;
        public Object result;
        public ResultedRecord(int recordNumber, Object[] arguments, Object result) {
            this.taskNumber = recordNumber;
            this.arguments = arguments;
            this.result = result;
        }
    }
}
