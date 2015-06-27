# xd-toolkit
工具集（工作过程中常用到的工具类、工具模块）

## SPMC并发组件（同时也支持MPMC模式）
    应用场景：
    a、很多业务场景都是单生产者多消费者的模式，这个模式除了生产者的业务逻辑与消费者的业务逻辑
    不一样外，其他的其实都是通用的，这个组件就是为了这个场景而存在，避免重造轮子
    b、对于业务逻辑简单，要求高并发高响应的场景，改组件同样适用，经测试，在同等条件下，
    吞吐率比Disruptor快10倍以上
### 工具类
    cn.com.common.util.concurrent.spmc.original.OriginalSpmc
### 测试类（可作为使用示例参考）
    cn.com.common.util.concurrent.spmc.original.OriginalSpmcTest

## LMAX Disruptor并发框架封装
    应用场景：
    a、原框架使用起来比较繁杂，其实很多时候我们只关心生产者的业务逻辑和消费者的业务逻辑，
    我们并不想了解太多的关于Disruptor的类，我这个封装的组件的意义就在这，简化开发
### 工具类
    cn.com.common.util.concurrent.spmc.disruptor.DisruptorSpmc
### 测试类（可作为使用示例参考）
    cn.com.common.util.concurrent.spmc.disruptor.DisruptorSpmcTest

## 并发排队等待
    应用场景：
    a、队列人数已达到最大限制数，屏蔽新访问用户加入队列；<br/>
    b、队列中等待用户，等待时间超出最长等待时间，移出队列<br/>
### 工具类
    cn.com.common.util.concurrent.queuing.QueuingProxy
### 测试类（可作为使用示例参考）
    cn.com.common.util.concurrent.queuing.QueuingProxyTest

## 对象转换器
    应用场景：
    a、同类型对象的复制
    b、不同类型对象之间的转换
    c、对象与Map互转
    d、编写这个工具类的原始出发点：在使用MongoDB的时候，发现普通对象与Document的转换不太方便， 
    Document不支持某些对象类型；在这个场景下，使用这个工具类会非常方便
### 工具类
    cn.com.common.util.convert.DefaultConverter
### 测试类（可作为使用示例参考）
    cn.com.common.util.convert.DefaultConverterTest

## EXCEL导出
    应用场景：
    a、数据导出excel格式，而由于数据量大，经常内存溢出或者输出的文件偏大导致下载失败
### 工具类
    cn.com.common.util.excel.impl.ExcelExporter4Xml
### 测试类（可作为使用示例参考）
    cn.com.common.util.excel.ExcelExporterTest
