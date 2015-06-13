package cn.com.common.util.concurrent.queuing;

import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 排队机制接口的代理实现<br/>
 * 应用场景：<br/>
 * a、队列人数已达到最大限制数，新访问用户无法加入队列；<br/>
 * b、队列中等待用户，等待时间超出最长等待时间，移出队列
 *
 * @author wuliwei
 */
final public class QueuingProxy implements Queuing {
    private Semaphore sem;
    private int maxWaitCount;
    private long maxWaitTime;

    /**
     * @param permits      信号量初始的可用许可数目
     * @param fair         信号量保证在争用时按先进先出的顺序授予许可，则为 true；否则为 false。
     * @param maxWaitCount 正在等待获取可用许可的线程最大数目
     * @param maxWaitTime  正在等待获取可用许可的线程最大等待时间，单位为毫秒
     */
    public QueuingProxy(int permits, boolean fair, int maxWaitCount,
                        long maxWaitTime) {
        this.sem = new Semaphore(permits, fair);
        this.maxWaitCount = maxWaitCount;
        this.maxWaitTime = maxWaitTime;
    }

    /**
     * @param target     需要排队的目标方法的所属对象，不能为null
     * @param method     需要排队的目标方法的名称，不能为null
     * @param paramTypes 需要排队的目标方法的参数类型，不能为null，必须与params长度保持一致。注：基本类型 int的
     *                   Class实例为Integer.TYPE、基本类型 long的 Class实例为Long.TYPE，其他基本类型同理
     * @param params     需要排队的目标方法的参数（参数的数目可变），不能为null，必须与paramTypes长度保持一致
     * @return QueuingResult
     * 排队操作的执行结果，包括：本次排队操作的执行结果、当前可用许可的大小、当前等待队列的大小、排队过程中发生的异常
     * 、目标方法的执行返回结果
     */
    public QueuingResult invoke(Object target, String method,
                                Class<?>[] paramTypes, Object[] params) {
        Object o = null;
        Throwable t = null;
        int ap = sem.availablePermits(), ql = sem.getQueueLength();
        if (ql >= maxWaitCount) {
            return new ProxyResult(FAIL, ap, ql, t, o);
        }
        try {
            if (!sem.tryAcquire(maxWaitTime, TimeUnit.MILLISECONDS)) {
                ap = sem.availablePermits();
                ql = sem.getQueueLength();
                return new ProxyResult(TIME_OUT, ap, ql, t, o);
            }
        } catch (Throwable tx) {
            // 正常情况下不会抛出InterruptedException
            // 如果抛出异常，以此次资源不可用处理，直接返回
            ap = sem.availablePermits();
            ql = sem.getQueueLength();
            t = tx;
            return new ProxyResult(ERROR, ap, ql, t, o);
        }
        try {
            Method m = null;
            try {
                m = target.getClass().getMethod(method, paramTypes);
            } catch (NoSuchMethodException e) {
                m = target.getClass().getDeclaredMethod(method, paramTypes);
            }
            m.setAccessible(true);
            o = m.invoke(target, params);
        } catch (Throwable tx) {
            t = tx;
        } finally {
            sem.release();
        }
        ap = sem.availablePermits();
        ql = sem.getQueueLength();
        return new ProxyResult(SUCCESS, ap, ql, t, o);
    }

    /**
     * 排队操作的执行结果——包括：本次排队操作的执行结果、当前可用许可的大小、当前等待队列的大小、排队过程中发生的异常 、目标方法的执行返回结果
     *
     * @author wuliwei
     */
    final private class ProxyResult implements QueuingResult {
        private String result;
        private int curPermits;
        private int curQueueSize;
        private Throwable throwable;
        private Object invokeReturn;

        private ProxyResult(String result, int curPermits, int curQueueSize,
                            Throwable t, Object invokeReturn) {
            setResult(result);
            setCurPermits(curPermits);
            setCurQueueSize(curQueueSize);
            setThrowable(t);
            setInvokeReturn(invokeReturn);
        }

        /**
         * @return String 本次排队操作的执行结果
         */
        public String getResult() {
            return result;
        }

        private void setResult(String pResult) {
            result = pResult;
        }

        /**
         * @return int 当前可用许可的大小
         */
        public int getCurPermits() {
            return curPermits;
        }

        private void setCurPermits(int pCurPermits) {
            curPermits = pCurPermits;
        }

        /**
         * @return int 当前等待队列的大小
         */
        public int getCurQueueSize() {
            return curQueueSize;
        }

        private void setCurQueueSize(int pCurQueueSize) {
            curQueueSize = pCurQueueSize;
        }

        /**
         * @return Throwable 本次排队操作过程中发生的异常，如果整个过程没有异常发生，则该值为null
         */
        public Throwable getThrowable() {
            return throwable;
        }

        private void setThrowable(Throwable pThrowable) {
            throwable = pThrowable;
        }

        /**
         * @return Object
         * 需要排队的目标方法的执行返回结果，如果该方法返回类型为void，则该值为null；如果返回类型为基础类型int
         * ，则该值为Integer类型；其他返回基础类型同理
         */
        public Object getInvokeReturn() {
            return invokeReturn;
        }

        private void setInvokeReturn(Object pInvokeReturn) {
            invokeReturn = pInvokeReturn;
        }

    }
}
