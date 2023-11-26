

## pom.xml

jsr250-api - часть аннотаций вынесена из Java Core. Например, @PostConstruct.

reflection - расширяет возможности Reflection.

## Конспект

```java
import Room;

public class CoronaDesinfector {
    public void start(Room room) {
        //todo сообщить всем присутствующим в комнате он начале дезинфекции и попросить их выйти
        //todo разогнать всех, кто не вышел после объявления
        desinfect(room);
        //todo сообщить всем присутствующим в комнате, что они могут вернутся обратно
    }

    private void desinfect(Room room) {
        System.out.println("Зачитывается молитва: - корона изыди! и вирус низвергнут в ад");
    }
}
```
Будем инкапсулировать логику, которую можно использовать в других местах.

S - Single Responsibility<br>
O - Open Close Principle<br>
L - <br>
I - <br>
D - Dependency Inversion

ClassLoader в Java не умеет работать с версиями.

Правила кодописания:
- Flexible
- Reusable
- Readable

```java
import com.edu.*;
import org.edu.*;

public class CoronaDesinfector {

    private Announcer announcer = new ConsoleAnnouncer();
    private Policeman policeman = new PolicemanImpl();

    public void start(Room room) {
        announcer.announce("Начинаем дезинфекцию - все вон!");
        policeman.makePeopleLeaveRoom();
        desinfect(room);
        announcer.announce("Рискните войти обратно!");
    }
}
```
Создаём интерфейсы и их имплементации

```java
public interface Announcer {
    void announce(String message);
}

public class ConsoleAnnouncer implements Announcer{
    @Override
    public void announce(String message) {
        System.out.println(message);
    }
}

```
Нарушаем Simple Responsibility.<br>
Класс CoronaDesinfector: 
- решает какие нужно выбирать имплементации при создании объектов Announcer
- должен уметь создавать объекты Announcer, пока это пустой конструктор, а может быть с параметрами в будущем
- так же он должен уметь настраивать Announcer

То же и про Policeman.

```java
import Announcer;
import ConsoleAnnouncer;
import Policeman;
import PolicemanImpl;

public class CoronaDesinfector {

    private Announcer announcer = new ConsoleAnnouncer();
    private Policeman policeman = new PolicemanImpl();
    ...
}
```

В начале Java, когда не было версифиционированности (возможности продукту развиваться в дальнейшем) было удобно создавать объекты с помощью new.

Таким образом появился паттерн Фабрика<br>
По сути произошло смещение плохого кода в другое место. Вместо того чтобы создавать объекты, нужно создавать фабрики.

Тогда придумали ObjectFactory, это Singleton, она умеет создавать настраивать объект.

```java
public class ObjectFactory {
    private static ObjectFactory ourInstance = new ObjectFactory();

    public static ObjectFactory getInstance() {
        return ourInstance;
    }

    private ObjectFactory() {
    }

    public <T> T createObject(Class<T> type) {
        ...
    }
}
```

Теперь объекты создаются так и CoronaDesinfector не должен ничего знать об объекта Announcer и Policeman

При создании объекта в методе createObject, если передан интерфейс, то нужно найти правильную имплементацию.<br>
Эту функцию в соответствии с принципом Single Responsibility нужно передать в отдельный класс.

```java
public interface Config {
    <T> Class <? extends T> getImplClass(Class<T> ifc);
}
```

Для начала будем считать, что имплементация одна.

```java
import Config;

public class JavaConfig implements Config {

    private Reflections scanner;
    // Задаём пакет для сканирования
    public JavaConfig(String packageToScan) {
        this.scanner = new Reflections(packageToScan);
    }

    @Override
    public <T> Class<? extends T> getImplClass(Class<T> ifc) {
        // Получаем все субклассы интерфейса
        Set<Class<? extends T>> classes = scanner.getSubTypesOf(ifc);
        if(classes.size() != 1) {
            throw new RuntimeException(ifc + "has 0 or more then 1 impl");
        }
        return classes.iterator().next();
    }
}
```
Тогда ObjectFactory будет выглядет так
```java
public class ObjectFactory {

    private static ObjectFactory ourInstance = new ObjectFactory();
    private Config config = new JavaConfig("com.edu");

    public static ObjectFactory getInstance() {
        return ourInstance;
    }

    private ObjectFactory() {
    }

    @SneakyThrows
    public <T> T createObject(Class<T> type) {
        Class<? extends T> implClass = type;
        if(implClass.isInterface()) {
            implClass = config.getImplClass(type);
        }
        return implClass.getDeclaredConstructor().newInstance();
    }
}
```
Что делать, если клиент захочет написать свою имплементацию Policeman, но при этом он не может удалить текущую так как она зашита в jarнике.

Чтобы не заставлять клиента лезть и исправлять код, или исключать классы, в классе Config можно завести Map, в которой будут храниться интерфейсы и их имплементации

```java
public class JavaConfig implements Config{

    private Reflections scanner;
    private Map<Class, Class> ifc2ImplClass;
    
    public JavaConfig(String packageToScan, Map<Class, Class> ifc2ImplClass) {
        this.scanner = new Reflections(packageToScan);
        this.ifc2ImplClass = ifc2ImplClass;
    }

    @Override
    public <T> Class<? extends T> getImplClass(Class<T> ifc) {

        return ifc2ImplClass.computeIfAbsent(ifc, aClass -> {
            Set<Class<? extends T>> classes = scanner.getSubTypesOf(ifc);
            if(classes.size() != 1) {
                throw new RuntimeException(ifc + "has 0 or more then 1 impl please check your config");
            }
            return classes.iterator().next();
        });
    }
}
```

