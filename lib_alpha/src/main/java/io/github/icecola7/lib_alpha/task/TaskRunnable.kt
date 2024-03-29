package io.github.icecola7.lib_alpha.task

import android.os.Process
import androidx.core.os.TraceCompat
import io.github.icecola7.lib_alpha.TaskManager

/**
 * @author chenxz
 * @date 2021/6/2
 * @desc 任务真正执行的地方
 */
class TaskRunnable : Runnable {

    private var task: Task? = null
    private var taskManager: TaskManager? = null

    constructor(task: Task?) {
        this.task = task
    }

    constructor(task: Task?, taskManager: TaskManager?) {
        this.task = task
        this.taskManager = taskManager
    }

    override fun run() {
        task?.let {
            TraceCompat.beginSection(it.javaClass.simpleName)
            Process.setThreadPriority(it.priority())
            it.startLock()
            it.run()

            // 执行Task的尾部任务
            val tailRunnable = task?.getTailRunnable()
            tailRunnable?.run()

            if (!it.runOnMainThread()) {
                taskManager?.unLockForChildren(it)
                taskManager?.finish(it)
            }
            TraceCompat.endSection()
        }
    }
}