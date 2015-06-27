package cn.com.common.util.concurrent.spmc.original;

import cn.com.common.util.concurrent.spmc.DataConsumer;
import cn.com.common.util.concurrent.spmc.DataProducer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wuliwei on 2015/6/20.
 *
 * @author wuliwei
 */
public class OriginalSpmcTest {
    private long tps = 0;

    @Test
    public void testAvgTps() {
        long allTps = 0;
        int loopTimes = 10;
        for (int i = 0; i < loopTimes; i++) {
            testTps();
            allTps += tps;
        }
        System.out.println("平均 tps " + (allTps / loopTimes));
    }

    @Test
    @Ignore
    public void testTps() {
        /**
         * producerCount 生产者个数<br/>
         * consumerCount 消费者个数<br/>
         * fetchDataInterval 内部自动抓取数据的时间间隔（为0时调用yield，否则调用sleep）<br/>
         * interval 缓冲区空闲时等待数据的时间间隔（为0时调用yield，否则调用sleep）<br/>
         * 在真实环境中，可以通过调节以上4个参数以达到最优效果<br/>
         */
        long st = System.currentTimeMillis();
        final int totalCount = 100000000, pageSize = 1000;
        int producerCount = 1, consumerCount = 1, bufferSize = 1000;
        int fetchDataInterval = 0, interval = 0;
        final AtomicInteger proDataCount = new AtomicInteger(0);
        final AtomicInteger conDataCount = new AtomicInteger(0);

        Random r = new Random(System.currentTimeMillis());
        final Object[] datas = new Object[pageSize];
        for (int i = 0; i < datas.length; i++) {
            datas[i] = new int[]{r.nextInt(1000), r.nextInt(1000)};
        }
        DataProducer dataProducer = new DataProducer() {
            public Object[] fetchData() {
                proDataCount.addAndGet(datas.length);
                return datas;
            }
        };
        DataConsumer dataConsumer = new DataConsumer() {
            public void handleData(Object data) {
                conDataCount.incrementAndGet();
            }
        };
        OriginalSpmc spmc = new OriginalSpmc();
        spmc.setDataProducer(dataProducer);
        spmc.setDataConsumer(dataConsumer);
        spmc.setProducerCount(producerCount);
        spmc.setConsumerCount(consumerCount);
        spmc.setBufferSize(bufferSize);
        spmc.setFetchDataInterval(fetchDataInterval);
        spmc.setInterval(interval);
        spmc.start();
        do {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        } while (proDataCount.get() < totalCount);
        spmc.stop();
        long et = System.currentTimeMillis();
        long useTime = et - st;
        tps = (long) (conDataCount.get() * 1.0D / useTime * 1000);
        System.out.println(
                "生产总数 " + proDataCount.get() + ", 消费总数 " + conDataCount.get() + ", 总耗时 (ms) " + useTime + ", tps "
                        + tps);
    }

    private boolean allLoop = false;
    private int loopTimes = 100;

    @Test
    public void testCorrect() {
        allLoop = true;
        testCorrect1();
        testCorrect2();
        testCorrect3();
    }

    @Test
    @Ignore
    public void testCorrect1() {
        int totalCount = 10000, pageSize = 1000;
        int producerCount = 1, consumerCount = 1, bufferSize = 128;

        int loopTimes = allLoop ? this.loopTimes : 10;
        for (int i = 0; i < loopTimes; i++) {
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, true, false, false);
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, false, true, false);
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, false, false, false);
        }
    }

    @Test
    @Ignore
    public void testCorrect2() {
        int totalCount = 10000, pageSize = 1000;
        int producerCount = 1, consumerCount = 2, bufferSize = 128;

        int loopTimes = allLoop ? this.loopTimes : 10;
        for (int i = 0; i < loopTimes; i++) {
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, true, false, false);
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, false, true, false);
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, false, false, false);
        }
    }

    @Test
    @Ignore
    public void testCorrect3() {
        int totalCount = 10000, pageSize = 1000;
        int producerCount = 2, consumerCount = 2, bufferSize = 128;

        int loopTimes = allLoop ? this.loopTimes : 10;
        for (int i = 0; i < loopTimes; i++) {
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, true, false, false);
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, false, true, false);
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, false, false, false);
        }
    }

    @Test
    @Ignore
    public void testCorrect4() {
        int totalCount = 10000, pageSize = 1000;
        int producerCount = 2, consumerCount = 2, bufferSize = 128;

        int loopTimes = allLoop ? this.loopTimes : 1;
        for (int i = 0; i < loopTimes; i++) {
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, true, false, true);
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, false, true, true);
            test(totalCount, pageSize, producerCount, consumerCount, bufferSize, false, false, true);
        }
    }

    /**
     * totalCount 生产者待生产的数据个数<br/>
     * pageSize 生产者每次批量生产的最大数据个数<br/>
     * producerCount 生产者个数<br/>
     * consumerCount 消费者个数<br/>
     * bufferSize 每个消费者与每个生产者分别对接的缓冲队列大小<br/>
     * proDataCount 生产者实际生产的数据个数<br/>
     * conDataCount 消费者实际消费的数据个数<br/>
     * dataMap 以数据的索引号为key，保存每个数据的被处理次数，
     * 通过判断每个被处理的数据只被处理了一次来验证数据分发处理的逻辑是否正确<br/>
     * threadMap 以线程名为key，保存每个生产者/消费者实际生产/消费的数据个数，
     * 通过计算生产者生产的总数是否与proDataCount一致、
     * 消费者消费的总数是否与conDataCount一致、
     * 生产者生产的总数是否与消费者消费的总数一致来进一步验证数据分发处理的逻辑是否正确，
     * 同时可验证本组件的stop方法是否确实等待所有的数据处理完成才结束<br/>
     */
    private void test(final int totalCount, final int pageSize, int producerCount, int consumerCount, int bufferSize,
                      boolean autoFetch, boolean outPush, boolean forceStop) {
        final AtomicInteger proDataCount = new AtomicInteger(0);
        final AtomicInteger conDataCount = new AtomicInteger(0);
        // Map必须是同步的
        // 虽然代码里对map的操作都是是先put后get，而且没有remove操作，
        // 但是有put操作，会导致map重新hash进而导致get操作无法获取到本来已存在的key
        final Map<String, AtomicInteger> dataMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());
        final Map<String, AtomicInteger> threadMap = Collections.synchronizedMap(new HashMap<String, AtomicInteger>());

        println("---------------------------------");
        println(
                "生产者个数 " + producerCount + ", 消费者个数 " + consumerCount + ", 缓冲区大小 " + bufferSize);
        long st = System.currentTimeMillis();
        final Random r = new Random(System.currentTimeMillis());

        final DataProducer dataProducer = new DataProducer() {
            public Object[] fetchData() {
                try {
                    Object[] datas = new Object[r.nextInt(pageSize + 1)];
                    int index = 0;
                    String key = "pro-" + Thread.currentThread().getName();
                    for (int i = 0; i < datas.length; i++) {
                        index = proDataCount.incrementAndGet();
                        datas[i] = new int[]{r.nextInt(1000), r.nextInt(1000), index};
                        dataMap.put(String.valueOf(index), new AtomicInteger(0));
                        if (!threadMap.containsKey(key)) {
                            threadMap.put(key, new AtomicInteger(0));
                        }
                        threadMap.get(key).incrementAndGet();
                    }
                    return datas;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new Object[0];
            }
        };
        DataConsumer dataConsumer = new DataConsumer() {
            public void handleData(Object data) {
                try {
                    int[] tempData = (int[]) data;
                    String s = (tempData[2] + "\t" + Thread.currentThread().getName() + ", " + tempData[0] + " + "
                            + tempData[1] + " = " + (tempData[0] + tempData[1]));
                    //println(s);
                    conDataCount.incrementAndGet();
                    dataMap.get(String.valueOf(tempData[2])).incrementAndGet();
                    String key = "con-" + Thread.currentThread().getName();
                    if (!threadMap.containsKey(key)) {
                        threadMap.put(key, new AtomicInteger(0));
                    }
                    threadMap.get(key).incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        final OriginalSpmc mpmc = new OriginalSpmc();
        mpmc.setDataProducer(!outPush ? dataProducer : null);
        mpmc.setDataConsumer(dataConsumer);
        mpmc.setProducerCount(producerCount);
        mpmc.setConsumerCount(consumerCount);
        mpmc.setBufferSize(bufferSize);
        mpmc.setAutoFetchData(!outPush ? autoFetch : false);
        mpmc.start();
        if (outPush) {
            // 外部主动推送数据
            Thread[] ts = new Thread[producerCount];
            for (int i = 0; i < producerCount; i++) {
                final int producerIndex = i;
                ts[i] = new Thread() {
                    public void run() {
                        do {
                            try {
                                Thread.sleep(10);
                            } catch (Exception e) {
                            }
                            mpmc.pushData(producerIndex, dataProducer.fetchData());
                        } while (proDataCount.get() < totalCount);
                    }
                };
                ts[i].start();
            }
            for (int i = 0; i < ts.length; i++) {
                try {
                    ts[i].join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        do {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
            if (outPush) {
            } else if (!autoFetch) {
                // 外部触发拉取数据
                mpmc.fetchData();
            }
        } while (proDataCount.get() < totalCount);
        if (forceStop) {
            mpmc.stop(forceStop);
        } else {
            mpmc.stop();
        }

        long et = System.currentTimeMillis();
        long useTime = et - st;
        println("数据总数 " + dataMap.size() +
                ", 生产总数 " + proDataCount.get() + ", 消费总数 " + conDataCount.get() + ", 耗时(ms) "
                + useTime + ", tps " + (int) (
                conDataCount.get() * 1.0D / useTime * 1000));
        long proCount = 0, conCount = 0;
        String[] keys = threadMap.keySet().toArray(new String[]{});
        Arrays.sort(keys);
        for (String key : keys) {
            if (key.startsWith("pro")) {
                println(key + " 生产数 " + threadMap.get(key).get());
                proCount += threadMap.get(key).get();
            }
        }
        for (String key : keys) {
            if (key.startsWith("con")) {
                println(key + " 消费数 " + threadMap.get(key).get());
                conCount += threadMap.get(key).get();
            }
        }
        boolean hasError = false;
        try {
            Assert.assertTrue("生产者生产数之和与生产总数不一致错误", proCount == proDataCount.get());
            Assert.assertTrue("消费者消费数之和与消费总数不一致错误", conCount == conDataCount.get());
            Assert.assertTrue("生产总数与消费总数不一致错误",
                    dataMap.size() == proDataCount.get() && dataMap.size() == conDataCount.get());
            for (Map.Entry<String, AtomicInteger> etr : dataMap.entrySet()) {
                Assert.assertTrue(etr.getKey() + " 数据被处理了 " + etr.getValue().get() + " 次，处理次数不等于1，处理逻辑错误",
                        etr.getValue().get() == 1);
            }
        } catch (Error e) {
            if (!forceStop) {
                throw e;
            }
            e.printStackTrace();
            hasError = true;
        }
        try {
            for (Map.Entry<String, AtomicInteger> etr : dataMap.entrySet()) {
                Assert.assertTrue(etr.getKey() + " 数据被处理了 " + etr.getValue().get() + " 次，处理次数不等于1，处理逻辑错误",
                        etr.getValue().get() == 1);
            }
        } catch (Error e) {
            if (!forceStop) {
                throw e;
            }
            hasError = true;
        }
        if (forceStop && hasError) {
            println("注：强制停止可能出现以下属于预期的异常结果\n"
                    + "\t生产者生产数之和与生产总数不一致错误（数据统计时为做同步，此时生产者可能仍处于生产状态）\n"
                    + "\t消费者消费数之和与消费总数不一致错误（数据统计时为做同步，此时消费者可能仍处于消费状态）\n"
                    + "\t生产总数与消费总数不一致错误（未消费完即被停止）");
        }
    }

    private void println(String log) {
        System.out.println(log);
    }
}