package com.cxz.performance.sample.task

import io.github.icecola7.lib_alpha.task.Task

/**
 * @author chenxz
 * @date 2021/6/2
 * @desc
 */
class InitBaiduMapTask : Task() {

    override fun needWait(): Boolean {
        return true
    }

    override fun run() {
        try {
            Thread.sleep(2000)
            println("InitBaiduMapTask运行完毕，它所在的线程是：" + Thread.currentThread().name)
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }
}