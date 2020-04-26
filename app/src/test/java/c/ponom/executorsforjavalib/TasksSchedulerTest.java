package c.ponom.executorsforjavalib;

import org.junit.Test;

import static org.junit.Assert.*;

public class TasksSchedulerTest {

    @Test
    public void submitTasksTest() {
    }

    @Test
    public void submitTasksAsListList() {
    }






    Task createTaskTwoArguments (final Object...arguments){
        return new Task(arguments){
            @Override
            public Object doTask(final Object...arguments) throws Exception {

                // Полученный аргумент должен быть приведен к необходимому виду,
                // если мы используем vararg как аргументы при создании задач
                // - то к массиву соответствующих объектов, иначе к типу
                // переданного единственного объекта




                /// todo - посмотреть как сюда можно приделать сохранение типов, что бы не
                //   кастить все каждый раз из объектов, это может вести к ошибкам


                // тестируем что исключения правильно передаются
                if (arguments[0].equals(10)) throw new ArithmeticException();
                int argument1 = (int) arguments[0];
                int argument2 = (int) arguments[1];


                // в данном случае мы перемножаем два аргумента
                return argument1*argument2;

            }
        };
    }


    Task createTaskOneArgument (final Object...arguments){
        return new Task(arguments){
            @Override
            public Object doTask(final Object...argument) throws Exception {


                // Полученный аргумент должен быть приведен к необходимому виду,
                // если мы используем vararg как аргументы при создании задач
                // - то к массиву соответствующих объектов, иначе к типу
                // переданного единственного объекта

                Integer finalArgument = (Integer) argument[0];
                // put your code for doing task here
                // в данном случае мы умножаем аргумент на 3


                if (finalArgument==10) throw new ArithmeticException();

                return finalArgument * 3;

            }
        };

    }



}