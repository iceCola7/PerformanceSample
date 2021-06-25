package io.github.icecola7.lib_alpha

import android.os.Looper
import androidx.annotation.UiThread
import io.github.icecola7.lib_alpha.sort.TaskSortUtil
import io.github.icecola7.lib_alpha.task.ITask
import io.github.icecola7.lib_alpha.task.Task
import io.github.icecola7.lib_alpha.task.TaskRunnable
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Task调度类（调度分发Task）
 */
class TaskManager private constructor() {

    companion object {
        val instance: TaskManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            TaskManager()
        }
    }

    // 维持task和它的依赖Task的依赖关系，这里是仿照EventBus的存放事件的机制设计
    private val dependOfTaskArray = HashMap<Class<out ITask>, MutableList<ITask>?>()

    // 存放已经执行完毕的Task队列
    @Volatile
    private var taskFinishedArray = mutableListOf<Class<out ITask>>()

    // 存放所有的task
    private var taskAll = mutableListOf<Task>()
    private val taskAllClazz = mutableListOf<Class<out Task>>()

    // 需要在主线程中执行的Task队列
    @Volatile
    private var mainThreadTaskArray = mutableListOf<Task>()

    // 主线程需要等待先执行的task数量
    private val mainNeedWaitCount = AtomicInteger()
    private var mCountDownLatch: CountDownLatch? = null

    private val WAITTIME_TIME = 10 * 1000L
    private val futureArray = mutableListOf<Future<*>>()

    /**
     * 添加任务
     */
    fun add(task: Task): TaskManager {
        setDependentOfTask(task)
        taskAll.add(task)
        taskAllClazz.add(task.javaClass)

        // 非主线程且需要wait的
        if (ifNeedWait(task)) {
            // 主线程的锁加一把
            mainNeedWaitCount.getAndIncrement()
        }
        return this
    }

    /**
     * 获取依赖的集合，主要做的为两件事
     *
     * 1.是以依赖类为Key，
     * 2.在从完成的任务集合里面查询，该task所依赖的类是否已经完成，完成的话进行解锁
     */
    private fun setDependentOfTask(task: Task) {
        task.dependentArr()?.forEach { dependTaskClazz ->
            if (dependOfTaskArray[dependTaskClazz] == null) {
                dependOfTaskArray[dependTaskClazz] = mutableListOf()
            }

            // 如果该task所依赖的依赖任务已经加载过了，就解锁其中已经完成的
            dependOfTaskArray[dependTaskClazz]?.add(task)
            if (taskFinishedArray.contains(dependTaskClazz)) {
                task.unlock()
            }
        }
    }

    private fun ifNeedWait(task: Task): Boolean {
        return !task.runOnMainThread() && task.needWait()
    }

    @UiThread
    fun start() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw RuntimeException("current thread must be main thread!")
        }
        if (taskAll.isNotEmpty()) {
            // 效率排序
            taskAll = TaskSortUtil.getSortResult(taskAll, taskAllClazz)
            mCountDownLatch = CountDownLatch(mainNeedWaitCount.get())

            // 分发任务
            dispatchTasks()
            // 运行在主线程
            runOnMainThread()

            startLock()
        }
    }

    /**
     * task分发，根据设定的不同规则，分发到不同的线程
     */
    private fun dispatchTasks() {
        for (task in taskAll) {
            // 如果是需要在主线程中运行的，加入到主线程队列中
            if (task.runOnMainThread()) {
                mainThreadTaskArray.add(task)
            } else {
                // 异步线程中执行，是否执行取决于具体线程池
                task.runOnExecutor()?.let {
                    val future = it.submit(TaskRunnable(task, this))
                    futureArray.add(future)
                }
            }
        }
    }

    private fun runOnMainThread() {
        for (task in mainThreadTaskArray) {
            TaskRunnable(task, this).run()
        }
    }

    @UiThread
    private fun startLock() {
        try {
            if (mainNeedWaitCount.get() > 0) {
                mCountDownLatch?.await(WAITTIME_TIME, TimeUnit.MILLISECONDS)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 取消
     */
    fun cancel() {
        for (future in futureArray) {
            future.cancel(true)
        }
    }

    /**
     * 当完成一个任务之后，通知所有依赖它的任务，并解锁他们
     */
    fun unLockForChildren(task: Task) {
        val arrayList = dependOfTaskArray[task.javaClass]
        arrayList?.forEach { subTask ->
            subTask.unlock()
        }
    }

    fun finish(task: Task) {
        if (ifNeedWait(task)) {
            taskFinishedArray.add(task.javaClass)
            mCountDownLatch?.countDown()
            mainNeedWaitCount.getAndDecrement()
        }
    }
}