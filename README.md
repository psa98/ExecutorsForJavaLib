# ExecutorsForJavaLib
Данный набор классов предназначен для замены устаревших AsynkTask.    

Особенности библиотеки:
Основной класс библиотеки после передачи ему задачи или  пакета задач может выполнять их в любом задаваемом числе потоков. 
Обеспечивается получение результата выполнения каждой задачи с обработкой исключений, а так же возможность получения состояния 
выполнения пакета задач, процента выполнения пакета задач, управления выполнением пакетом задач (прерывание исполнения, получение
состояния экзекьютора или очереди задач)  

По итогам выполнения задачи, пакета задач, или каждой задачи обеспечивается получение результата ее выполнения, 
включая исключения. Коллбэки по итогам выполнения каждой задачи или всего пакета могут быть вызваны в ui-потоке, что позволяет 
обновление интерфейса приложения.  
Отдельные классы и методы библиотеки принимают упрощенный или минимальный набор аргументов.
