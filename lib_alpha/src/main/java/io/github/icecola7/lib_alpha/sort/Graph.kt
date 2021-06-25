package io.github.icecola7.lib_alpha.sort

import java.util.*

class Graph {

    // 顶点数
    private var mVertexCount = 0

    // 邻接表
    private var mAdj: Array<MutableList<Int>>

    constructor(vertexCount: Int) {
        this.mVertexCount = vertexCount
        mAdj = Array(vertexCount, init = { mutableListOf() })
        for (i in 0 until vertexCount) {
            mAdj[i] = mutableListOf()
        }
    }

    /**
     * 添加边
     * @param u Int
     * @param v Int
     */
    fun addEdge(u: Int, v: Int) {
        mAdj[u].add(v)
    }

    /**
     * 拓扑排序
     */
    fun topologicalSort(): Vector<Int> {
        val inDegree = IntArray(mVertexCount)
        // 初始化所有点的入度数量
        for (i in 0 until mVertexCount) {
            val temp = mAdj[i] as ArrayList<Int>
            for (node in temp) {
                inDegree[node]++
            }
        }
        val queue: Queue<Int> = LinkedList()
        // 找出所有入度为0的点
        for (i in 0 until mVertexCount) {
            if (inDegree[i] == 0) {
                queue.add(i)
            }
        }
        var cnt = 0
        val topOrder = Vector<Int>()
        while (!queue.isEmpty()) {
            val u = queue.poll()
            topOrder.add(u)
            // 找到该点（入度为0）的所有邻接点
            for (node in mAdj[u]) {
                // 把这个点的入度减一，如果入度变成了0，那么添加到入度0的队列里
                if (--inDegree[node] == 0) {
                    queue.add(node)
                }
            }
            cnt++
        }
        // 检查是否有环，理论上拿出来的点的次数和点的数量应该一致，如果不一致，说明有环
        check(cnt == mVertexCount) {
            "Exists a cycle in the graph"
        }
        return topOrder
    }
}