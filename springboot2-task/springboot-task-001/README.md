# springboot-task-jdbc 分布式
```
shedlock可以在集群环境下只有一个name为TaskScheduler_scheduledTask的实例运行

运行2个springboot-shedlock项目 @SchedulerLock(name = "TaskScheduler_scheduledTask")name为相同的情况下会等锁释放后抢夺资源去执行，name不同的情况下各自执行任务，name相同的情况下满足了简单的集群环境
```