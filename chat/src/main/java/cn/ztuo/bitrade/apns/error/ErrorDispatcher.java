package cn.ztuo.bitrade.apns.error;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @description: ErrorDispatcher
 * @author: MrGao
 * @create: 2019/09/04 11:19
 */
public class ErrorDispatcher {
    private List<ErrorListener> list;

    private ErrorDispatcher() {
        this.list = new ArrayList();
    }

    public static ErrorDispatcher getInstance() {
        return ErrorDispatcher.Nested.instance;
    }

    public void addListener(ErrorListener errorListener) {
        this.list.add(errorListener);
    }

    public void removeListener(ErrorListener errorListener) {
        this.list.remove(errorListener);
    }

    public void dispatch(ErrorModel errorModel) {
        Iterator var2 = this.list.iterator();

        while(var2.hasNext()) {
            ErrorListener listener = (ErrorListener)var2.next();
            listener.handle(errorModel);
        }

    }

    private static class Nested {
        private static ErrorDispatcher instance = new ErrorDispatcher();

        private Nested() {
        }
    }
}
