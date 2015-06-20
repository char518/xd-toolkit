package cn.com.common.util.concurrent.queuing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliwei on 2015/6/13.
 */
public class QueuingProxyTest {
    private static int handleUsedTime;
    private static long st;

    public static void handle() {
        try {
            Thread.sleep(handleUsedTime * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int handle(Integer p1) {
        handle();
        return p1;
    }

    private static List<Integer> handle(int p1, List<Integer> p2) {
        handle();
        p2.add(p1);
        return p2;
    }

    public static String handle(Integer p1, List<Integer> p2, List<String> p3) {
        handle();
        p2.add(p1);
        return "" + p2.size() + "," + p3.size();
    }

    private static void echo(String result, int ap, int ql, Throwable t,
                             Object r) {
        System.out.println("["
                + (System.currentTimeMillis() - st)
                / 1000
                + "]"
                + Thread.currentThread().getName()
                + " acquire "
                + result
                + ", available permits is "
                + ap
                + ", queue size is "
                + ql
                + (null == r ? "" : ", return type is " + r.getClass()
                + " and value is " + r));
        if (null != t) {
            t.printStackTrace();
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(10 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testInvoke() throws Exception {
        int permits = 2; //信号量初始的可用许可数
        boolean fair = true; //信号量保证在争用时按先进先出的顺序授予许可
        int maxWaitCount = 2; //正在等待获取可用许可的线程最大数目
        long maxWaitTime = 38 * 1000; //正在等待获取可用许可的线程最大等待时间，单位为毫秒
        final QueuingProxy proxy = new QueuingProxy(permits,
                fair, maxWaitCount, maxWaitTime);
        handleUsedTime = 60; //模拟真实处理业务的耗费时间，单位为秒
        st = System.currentTimeMillis();
        /**
         * 以下代码模拟场景：
         * 0号线程申请信号量资源成功
         * 1号线程申请信号量资源成功
         * 2号线程申请信号量资源超时
         * 3号线程申请信号量资源成功
         * 4号线程申请信号量资源失败
         * 5号线程申请信号量资源失败
         * 6号线程申请信号量资源成功
         * 7号线程申请信号量资源成功
         */
        int i = 0;
        new Thread("thread[" + (i++) + "]") {
            public void run() {
                QueuingResult pr = proxy.invoke(new QueuingProxyTest(),
                        "handle", new Class[]{}, new Object[]{});
                echo(pr.getResult(), pr.getCurPermits(), pr.getCurQueueSize(),
                        pr.getThrowable(), pr.getInvokeReturn());
            }
        }.start();
        System.out.println("第0号线程启动，占用1个信号量，剩余1个信号量");
        sleep();
        System.out.println("休眠10秒");
        System.out.println("第0号线程已执行10秒，还有50秒执行完");
        new Thread("thread[" + (i++) + "]") {
            public void run() {
                QueuingResult pr = proxy.invoke(new QueuingProxyTest(),
                        "handle", new Class[]{Integer.class},
                        new Object[]{1});
                echo(pr.getResult(), pr.getCurPermits(), pr.getCurQueueSize(),
                        pr.getThrowable(), pr.getInvokeReturn());
            }
        }.start();
        System.out.println("第1号线程启动，占用1个信号量，剩余0个信号量");
        sleep();
        System.out.println("休眠10秒");
        System.out.println("第0号线程已执行20秒，还有40秒执行完");
        System.out.println("第1号线程已执行10秒，还有50秒执行完");
        for (; i < 6; i++) {
            new Thread("thread[" + i + "]") {
                public void run() {
                    QueuingResult pr = proxy.invoke(new QueuingProxyTest(),
                            "handle", new Class[]{Integer.TYPE, List.class},
                            new Object[]{3, new ArrayList<Integer>()});
                    echo(pr.getResult(), pr.getCurPermits(), pr
                            .getCurQueueSize(), pr.getThrowable(), pr
                            .getInvokeReturn());
                }
            }.start();
            if (i == 2) {
                System.out.println("第2号线程启动，占用等待队列1个资源，等待队列剩余1个资源");
            } else if (i == 3) {
                System.out.println("第3号线程启动，占用等待队列1个资源，等待队列剩余0个资源");
            } else if (i == 4) {
                System.out.println("第4号线程启动");
            } else if (i == 5) {
                System.out.println("第5号线程启动");
            }
            sleep();
            System.out.println("休眠10秒");
            if (i == 2) {
                System.out.println("第0号线程已执行30秒，还有30秒执行完");
                System.out.println("第1号线程已执行20秒，还有40秒执行完");
                System.out.println("第2号线程已等待10秒，还要等待28秒");
            } else if (i == 3) {
                System.out.println("第0号线程已执行40秒，还有20秒执行完");
                System.out.println("第1号线程已执行30秒，还有30秒执行完");
                System.out.println("第2号线程已等待20秒，还要等待18秒");
                System.out.println("第3号线程已等待10秒，还要等待28秒");
            } else if (i == 4) {
                System.out.println("第0号线程已执行50秒，还有10秒执行完");
                System.out.println("第1号线程已执行40秒，还有20秒执行完");
                System.out.println("第2号线程已等待30秒，还要等待8秒");
                System.out.println("第3号线程已等待20秒，还要等待18秒");
                System.out.println("第4号线程申请信号量失败");
            } else if (i == 5) {
                System.out.println("第0号线程已执行60秒，执行完，释放1个信号量，剩余1个信号量");
                System.out.println("第1号线程已执行50秒，还有10秒执行完");
                System.out.println("第2号线程申请信号量超时，释放等待队列1个资源，等待队列剩余1个资源");
                System.out.println("第3号线程释放等待队列1个资源，等待队列剩余2个资源，占用1个信号量，剩余0个信号量");
                System.out.println("第5号线程申请信号量失败");
            }
        }
        new Thread("thread[" + (i++) + "]") {
            public void run() {
                QueuingResult pr = proxy.invoke(new QueuingProxyTest(),
                        "handle", new Class[]{Integer.class, List.class,
                                List.class}, new Object[]{6,
                                new ArrayList<Integer>(),
                                new ArrayList<String>()});
                echo(pr.getResult(), pr.getCurPermits(), pr.getCurQueueSize(),
                        pr.getThrowable(), pr.getInvokeReturn());
            }
        }.start();
        System.out.println("第6号线程启动，占用等待队列1个资源，等待队列剩余1个资源");
        sleep();
        System.out.println("休眠10秒");
        System.out.println("第1号线程已执行60秒，执行完，释放1个信号量，剩余1个信号量");
        System.out.println("第3号线程已执行10秒，还有50秒执行完");
        System.out.println("第6号线程释放等待队列1个资源，等待队列剩余2个资源，占用1个信号量，剩余0个信号量");
        sleep();
        System.out.println("休眠10秒");
        System.out.println("第3号线程已执行20秒，还有40秒执行完");
        System.out.println("第6号线程已执行10秒，还有50秒执行完");
        sleep();
        System.out.println("休眠10秒");
        System.out.println("第3号线程已执行30秒，还有30秒执行完");
        System.out.println("第6号线程已执行20秒，还有40秒执行完");
        new Thread("thread[" + (i++) + "]") {
            public void run() {
                QueuingResult pr = proxy.invoke(new QueuingProxyTest(),
                        "equals", new Class[]{Object.class},
                        new Object[]{6});
                echo(pr.getResult(), pr.getCurPermits(), pr.getCurQueueSize(),
                        pr.getThrowable(), pr.getInvokeReturn());
            }
        }.start();
        System.out.println("第7号线程启动，占用等待队列1个资源，等待队列剩余1个资源");
        sleep();
        System.out.println("休眠10秒");
        System.out.println("第3号线程已执行40秒，还有20秒执行完");
        System.out.println("第6号线程已执行30秒，还有30秒执行完");
        System.out.println("第7号线程已等待10秒，还要等待28秒");
        sleep();
        System.out.println("休眠10秒");
        System.out.println("第3号线程已执行50秒，还有10秒执行完");
        System.out.println("第6号线程已执行40秒，还有20秒执行完");
        System.out.println("第7号线程已等待20秒，还要等待18秒");
        sleep();
        System.out.println("休眠10秒");
        System.out.println("第3号线程已执行60秒，执行完，释放1个信号量，剩余1个信号量");
        System.out.println("第6号线程已执行50秒，还有10秒执行完");
        System.out.println("第7号线程释放等待队列1个资源，等待队列剩余2个资源，占用1个信号量，剩余0个信号量");
        System.out.println("第7号线程已执完，释放1个信号量，剩余1个信号量");
        sleep();
        System.out.println("休眠10秒");
        System.out.println("第6号线程已执行60秒，执行完，释放1个信号量，剩余2个信号量");
        sleep();
    }
}