package io.github.icecola7.lib_alpha

import android.os.Looper
import android.os.MessageQueue.IdleHandler
import io.github.icecola7.lib_alpha.task.Task
import io.github.icecola7.lib_alpha.task.TaskRunnable
import java.util.*

/**
 * IdleTask调度类（调度分发IdleTask）
 */
class IdleTaskManager private constructor() {

    companion object {
        val instance: IdleTaskManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            IdleTaskManager()
        }
    }

    private val idleTaskQueue: Queue<Task> = LinkedList()

    private val idleHandler = IdleHandler {
        if (idleTaskQueue.size > 0) {
            // 如果CPU空闲了
            val idleTask: Task? = idleTaskQueue.poll()
            TaskRunnable(idleTask).run()
        }
        // 如果返回false，则移除该 IdleHandler
        idleTaskQueue.isNotEmpty()
    }

    fun addTask(task: Task): IdleTaskManager {
        idleTaskQueue.add(task)
        return this
    }

    /**
     * 执行空闲方法，因为用了DispatchRunnable，所以会优先处理需要依赖的task，再处理本次需要处理的task，顺序执行
     */
    fun start() {
        Looper.myQueue().addIdleHandler(idleHandler)
    }
}