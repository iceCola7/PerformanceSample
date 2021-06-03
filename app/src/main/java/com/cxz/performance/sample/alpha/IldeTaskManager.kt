package com.cxz.performance.sample.alpha

import android.os.Looper
import android.os.MessageQueue.IdleHandler
import com.cxz.performance.sample.alpha.task.Task
import com.cxz.performance.sample.alpha.task.TaskRunnable
import java.util.*


class IldeTaskManager private constructor() {

    companion object {
        val instance: IldeTaskManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            IldeTaskManager()
        }
    }

    private val ildeTaskQueue: Queue<Task> = LinkedList()

    private val idleHandler = IdleHandler {
        if (ildeTaskQueue.size > 0) {
            // 如果CPU空闲了
            val idleTask: Task = ildeTaskQueue.poll()
            TaskRunnable(idleTask).run()
        }
        // 如果返回false，则移除该 IldeHandler
        !ildeTaskQueue.isEmpty()
    }

    fun addTask(task: Task): IldeTaskManager {
        ildeTaskQueue.add(task)
        return this
    }

    /**
     * 执行空闲方法，因为用了DispatchRunnable，所以会优先处理需要依赖的task，再处理本次需要处理的task，顺序执行
     */
    fun start() {
        Looper.myQueue().addIdleHandler(idleHandler)
    }
}