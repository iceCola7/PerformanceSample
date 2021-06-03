package com.cxz.performance.sample.alpha.utils

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author chenxz
 * @date 2021/6/2
 * @desc
 */
object DispatcherExecutor {

    private var sCPUThreadPoolExecutor: ThreadPoolExecutor
    private var sIOThreadPoolExecutor: ExecutorService
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    // We want at least 2 threads and at most 4 threads in the core pool,
    // preferring to have 1 less than the CPU count to avoid saturating
    // the CPU with background work
    val CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4))
    private val MAXIMUM_POOL_SIZE = CORE_POOL_SIZE
    private const val KEEP_ALIVE_SECONDS = 5L

    private val sPoolWorkQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()
    private val sThreadFactory: DefaultThreadFactory = DefaultThreadFactory()
    private val sHandler = RejectedExecutionHandler { r, executor ->
        Executors.newCachedThreadPool().execute(r)
    }

    init {
        sCPUThreadPoolExecutor = ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            sPoolWorkQueue,
            sThreadFactory,
            sHandler
        )
        sCPUThreadPoolExecutor.allowCoreThreadTimeOut(true)
        sIOThreadPoolExecutor = Executors.newCachedThreadPool(sThreadFactory)
    }


    /**
     * 获取CPU线程池
     * @return
     */
    fun getCPUExecutor(): ThreadPoolExecutor {
        return sCPUThreadPoolExecutor
    }

    /**
     * 获取IO线程池
     * @return
     */
    fun getIOExecutor(): ExecutorService {
        return sIOThreadPoolExecutor
    }

    /**
     * The default thread factory.
     */
    private class DefaultThreadFactory : ThreadFactory {

        private var group: ThreadGroup? = null
        private val threadNumber = AtomicInteger(1)
        private var namePrefix: String = ""

        companion object {
            private val poolNumber = AtomicInteger(1)
        }

        init {
            val s = System.getSecurityManager()
            group = if (s != null) s.threadGroup else Thread.currentThread().threadGroup
            namePrefix = "TaskDispatcherPool- ${poolNumber.getAndIncrement()} -Thread-"
        }

        override fun newThread(r: Runnable): Thread {
            val t = Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0)
            if (t.isDaemon) {
                t.isDaemon = false
            }
            if (t.priority != Thread.NORM_PRIORITY) {
                t.priority = Thread.NORM_PRIORITY
            }
            return t
        }
    }

}