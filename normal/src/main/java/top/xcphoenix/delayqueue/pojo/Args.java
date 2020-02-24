package top.xcphoenix.delayqueue.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xuanc
 * @version 1.0
 * @date 2020/2/9 下午5:13
 */
@Data
public class Args {

    @Data
    @AllArgsConstructor
    public static class Arg<V> {
        private Class<V> clazz;
        private V obj;
    }

    @SuppressWarnings("rawtypes")
    private List<Arg> argList = new ArrayList<>();

    public int size() {
        return argList.size();
    }

    @SuppressWarnings("rawtypes")
    public Arg get(int index) {
        return argList.get(index);
    }

    public <T> void put(Class<T> clazz, T obj) {
        argList.add(new Arg<>(clazz, obj));
    }

    public void remove(int index) {
        argList.remove(index);
    }

    @SuppressWarnings("rawtypes")
    public Class[] getClasses() {
        return argList.stream().map(arg -> arg.clazz)
                .collect(Collectors.toList()).toArray(Class[]::new);
    }

    public Object[] getObjs() {
        return argList.stream().map(arg -> arg.obj)
                .collect(Collectors.toList()).toArray(Object[]::new);
    }

}
