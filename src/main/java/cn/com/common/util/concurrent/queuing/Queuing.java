package cn.com.common.util.concurrent.queuing;

/**
 * 排队机制接口<br/>
 * 应用场景：<br/>
 * a、队列人数已达到最大限制数，新访问用户无法加入队列；<br/>
 * b、队列中等待用户，等待时间超出最长等待时间，移出队列
 *
 * @author wuliwei
 */
public interface Queuing {
    /**
     * 队列已达到最大限制数，加入队列失败
     */
    final public static String FAIL = "FAIL";
    /**
     * 加入队列成功，等待超时
     */
    final public static String TIME_OUT = "TIME_OUT";
    /**
     * 加入队列成功，等待过程发生异常
     */
    final public static String ERROR = "ERROR";
    /**
     * 成功对排队模块进行访问限制
     */
    final public static String SUCCESS = "SUCCESS";

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
                                Class<?>[] paramTypes, Object[] params);
}
