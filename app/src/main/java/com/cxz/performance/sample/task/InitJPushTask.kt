package com.cxz.performance.sample.task

import com.cxz.performance.sample.alpha.task.ITask
import com.cxz.performance.sample.alpha.task.Task

/**
 * @author chenxz
 * @date 2021/6/2
 * @desc
 */
class InitJPushTask : Task() {

    override fun dependentArr(): MutableList<Class<out ITask>> {
        val tasks = mutableListOf<Class<out ITask>>()
        tasks.add(InitShareTask::class.java)
        return tasks
    }

    override fun run() {
        try {
            Thread.sleep(1500)
            println("InitJPushTask运行完毕，它所在的线程是：" + Thread.currentThread().name)
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }
}