package com.cxz.performance.sample.alpha.task

import android.os.Process
import androidx.annotation.IntRange
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

/**
 * @author chenxz
 * @date 2021/6/2
 * @desc
 */
interface ITask {

    /**
     * 优先级
     */
    @IntRange(from = Process.THREAD_PRIORITY_FOREGROUND.toLong(), to = Process.THREAD_PRIORITY_LOWEST.toLong())
    fun priority(): Int

    fun run()

    /**
     * Task执行所在的线程池，可指定，一般默认
     */
    fun runOnExecutor(): ExecutorService?

    /**
     * 存放需要先执行的task任务集合(也就是添加需要先执行的依赖)
     */
    fun dependentArr(): MutableList<Class<out ITask>>?

    /**
     * 开始锁
     */
    fun startLock()

    /**
     * 解锁
     */
    fun unlock()

    /**
     * 异步线程执行的Task是否需要在被调用await的时候等待，默认不需要
     */
    fun needWait(): Boolean

    /**
     * 是否在主线程执行
     */
    fun runOnMainThread(): Boolean

    /**
     * Task主任务执行完成之后需要执行的任务
     */
    fun getTailRunnable(): Runnable?

}