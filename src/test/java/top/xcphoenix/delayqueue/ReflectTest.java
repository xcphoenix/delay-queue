package top.xcphoenix.delayqueue;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import top.xcphoenix.delayqueue.pojo.Args;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/2/9 下午4:48
 */
@Slf4j
public class ReflectTest {

    private static class Demo {
        private long id;
        private String name;

        public Demo(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public void run() {
            System.out.println("Demo{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}');
        }
    }

    @Test
    void reflectObject() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        Class<Demo> demoClass = Demo.class;
        log.info("name: " + demoClass.getName());
        log.info("simpleName: " + demoClass.getSimpleName());

        for (Field field : demoClass.getDeclaredFields()) {
            log.info(field.getType().getName() + ", " + field.getName());
        }

        Args args = new Args();
        args.put(long.class, 2020L);
        args.put(String.class, "demo");

        /*
         * 内部类（非static）构造器第一个参数是外部类class
         */
        @SuppressWarnings("JavaReflectionMemberAccess")
        Constructor<Demo> constructor = demoClass.getConstructor(args.getClasses());
        Demo demo = constructor.newInstance(args.getObjs());

        demo.run();
    }

    @Test
    void reflectInterfaceClassName() {
        class CallableDemo<T> implements Callable<T> {
            @Override
            public T call() throws Exception {
                return null;
            }
        }

        Callable<Void> callable = new CallableDemo<>();
        Class callableClass = callable.getClass();

        log.info(callableClass.getName());

    }


}
