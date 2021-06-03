package com.cxz.performance.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cxz.performance.sample.alpha.IldeTaskManager
import com.cxz.performance.sample.task.InitBaiduMapTask
import com.cxz.performance.sample.task.InitBuglyTask

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println("************************MainActivity开始执行延迟调用************************")
        IldeTaskManager.instance
            .addTask(InitBaiduMapTask())
            .addTask(InitBuglyTask())
            .start()
    }
}