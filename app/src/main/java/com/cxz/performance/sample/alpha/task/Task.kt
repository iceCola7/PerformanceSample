package com.cxz.performance.sample.alpha.task

import android.os.Process
import com.cxz.performance.sample.alpha.utils.DispatcherExecutor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService

/**
 * @author chenxz
 * @date 2021/6/2
 * @desc
 */
abstract class Task : ITask {

    // 当前Task依赖的Task数量（需要等待被依赖的Task执行完毕才能执行自己），默认没有依赖
    private val taskCountDownLatch = CountDownLatch(if (dependentArr() == null) 0 else dependentArr()!!.size)

    /**
     *当前Task等待，让依赖的Task先执行
     */
    override fun startLock() {
        taskCountDownLatch.await()
    }

    /**
     * 依赖的Task执行完
     */
    override fun unlock() {
        taskCountDownLatch.countDown()
    }

    /**
     * Task的优先级，运行在主线程则不要去改优先级
     */
    override fun priority(): Int {
        return Process.THREAD_PRIORITY_BACKGROUND
    }

    /**
     * Task执行在哪个线程池，默认在IO的线程池
     * @return Executor?
     */
    override fun runOnExecutor(): ExecutorService? {
        return DispatcherExecutor.getIOExecutor()
    }

    /**
     * 异步线程执行的Task是否需要在被调用await的时候等待，默认不需要
     */
    override fun needWait(): Boolean = false

    /**
     * 当前Task依赖的Task集合（需要等待被依赖的Task执行完毕才能执行自己），默认没有依赖
     */
    override fun dependentArr(): MutableList<Class<out ITask>>? {
        return null
    }

    override fun runOnMainThread(): Boolean = false

    override fun getTailRunnable(): Runnable? {
        return null
    }

    open fun needRunAsSoon(): Boolean = false
}