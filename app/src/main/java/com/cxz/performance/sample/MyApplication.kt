package com.cxz.performance.sample

import android.app.Application
import android.os.Debug
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author chenxz
 * @date 2021/6/2
 * @desc
 */
class MyApplication: Application() {

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
    }

}