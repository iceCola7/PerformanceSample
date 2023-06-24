package com.cxz.performance.sample

import android.app.Application
import android.os.StrictMode
import com.cxz.performance.sample.task.InitBaiduMapTask
import com.cxz.performance.sample.task.InitBuglyTask
import com.cxz.performance.sample.task.InitJPushTask
import com.cxz.performance.sample.task.InitShareTask
import io.github.icecola7.lib_alpha.TaskManager

/**
 * @author chenxz
 * @date 2021/6/2
 * @desc
 */
class MyApplication : Application() {

    //获得当前CPU的核心数
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    //设置线程池的核心线程数2-4之间,但是取决于CPU核数
    private val CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4))

    override fun onCreate() {
        super.onCreate()

        // 常规方式
//        Debug.startMethodTracing("app")
//        val latch = CountDownLatch(1)
//        val executorService: ExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE)
//
//        executorService.submit {
//            initBugly()
//            latch.countDown()
//        }
//
//        executorService.submit { initBaiduMap() }
//
//        executorService.submit { initJPushInterface() }
//
//        executorService.submit { initShareSDK() }
//
//        try {
//            latch.await()
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//        Debug.stopMethodTracing()


        // 使用启动器的方式
//        final CountDownLatch latch = new CountDownLatch (1);
//        ExecutorService executorService = Executors.newFixedThreadPool(CORE_POOL_SIZE);
//
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                initBugly();
//                latch.countDown();
//            }
//        });
//
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                initBaiduMap();
//            }
//        });
//
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                initJPushInterface();
//            }
//        });
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                initShareSDK();
//            }
//        });
//
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//            Debug.stopMethodTracing();

        // 使用启动器的方式
        println("************************MyApplication开始执行************************")
        TaskManager.instance
            .add(InitBuglyTask()) // 默认添加，并发处理
            .add(InitBaiduMapTask()) // 在这里需要先处理了另外一个耗时任务initShareSDK，才能再处理它
            .add(InitJPushTask()) // 等待主线程处理完毕，再进行执行
            .add(InitShareTask())
            .start()
        println("************************MyApplication执行完毕************************")
    }

    /**
     * 卡顿优化检测
     */
    private fun initStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectCustomSlowCalls()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork() // or .detectAll()
                .penaltyLog() // 在Logcat中打印违规异常信息
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectActivityLeaks()
                .setClassInstanceLimit(MyApplication::class.java, 1)
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
    }

}