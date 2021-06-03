package com.cxz.performance.sample.alpha.sort

import android.util.ArraySet
import com.cxz.performance.sample.alpha.task.Task

object TaskSortUtil {

    private val taskArrayList = mutableListOf<Task>()

    fun getTasksHigh(): MutableList<Task> {
        return taskArrayList
    }

    /**
     * 任务的有向无环图的拓扑排序
     * @param originTasks MutableList<Task>
     * @param clsLaunchTasks MutableList<Class<out Task>>
     */
    @Synchronized
    fun getSortResult(originTasks: MutableList<Task>, clsLaunchTasks: MutableList<Class<out Task>>): MutableList<Task> {
        val dependSet = ArraySet<Int>()
        val graph = Graph(originTasks.size)
        for (i in originTasks.indices) {
            val task = originTasks[i]
            if (task.dependentArr().isNullOrEmpty()) {
                continue
            }
            task.dependentArr()?.forEach { cls ->
                val indexOfDepend = getIndexOfTask(originTasks, clsLaunchTasks, cls)
                if (indexOfDepend < 0) {
                    throw IllegalStateException("${task.javaClass.simpleName} depends on ${cls.simpleName} can not be found in task list ")
                }
                dependSet.add(indexOfDepend)
                graph.addEdge(indexOfDepend, i)
            }
        }
        val indexList: MutableList<Int> = graph.topologicalSort()
        val newTasksAll = getResultTasks(originTasks, dependSet, indexList)
        return newTasksAll
    }

    private fun getResultTasks(
        originTasks: MutableList<Task>,
        dependSet: Set<Int>,
        indexList: MutableList<Int>
    ): MutableList<Task> {
        val newTasksAll = mutableListOf<Task>()
        // 被别人依赖的
        val newTasksDepended = mutableListOf<Task>()
        // 没有依赖的
        val newTasksWithOutDepend = mutableListOf<Task>()
        // 需要提升自己优先级的，先执行（这个先是相对于没有依赖的先）
        val newTasksRunAsSoon = mutableListOf<Task>()
        for (index in indexList) {
            if (dependSet.contains(index)) {
                newTasksDepended.add(originTasks[index])
            } else {
                val task = originTasks[index]
                if (task.needRunAsSoon()) {
                    newTasksRunAsSoon.add(task)
                } else {
                    newTasksWithOutDepend.add(task)
                }
            }
        }
        // 顺序：被别人依赖的————》需要提升自己优先级的————》需要被等待的————》没有依赖的
        taskArrayList.addAll(newTasksDepended)
        taskArrayList.addAll(newTasksRunAsSoon)
        newTasksAll.addAll(taskArrayList)
        newTasksAll.addAll(newTasksWithOutDepend)
        return newTasksAll
    }

    /**
     * 获取任务在任务列表中的index
     * @param originTasks MutableList<Task>
     * @param clsLaunchTasks MutableList<Class<out Task>>
     * @param cls Class<*>
     * @return Int
     */
    private fun getIndexOfTask(
        originTasks: MutableList<Task>,
        clsLaunchTasks: MutableList<Class<out Task>>,
        cls: Class<*>
    ): Int {
        val index = clsLaunchTasks.indexOf(cls)
        if (index >= 0) {
            return index
        }

        // 仅仅是保护性代码
        val size = originTasks.size
        for (i in 0 until size) {
            if (cls.simpleName == originTasks[i].javaClass.simpleName) {
                return i
            }
        }
        return index
    }
}