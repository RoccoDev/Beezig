package eu.beezig.core.util;

import java.util.Arrays;

public class ArrayUtils {
    public static <T> T[] getPage(T[] src, int pageNo, int pageSize) {
        int from = pageNo * pageSize;
        if(from >= src.length) return null;
        int to = from + pageSize;
        if(to > src.length) to = src.length;
        return Arrays.copyOfRange(src, from, to);
    }
}
