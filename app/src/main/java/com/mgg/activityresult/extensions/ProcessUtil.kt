@file:Suppress("UNUSED")

package com.mgg.activityresult.extensions

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log

private const val TAG = "ProcessUtil"

/**
 * 获取所有后台进程
 */
fun Application.getAllBackgroundProcess(): List<String> {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager?
    val infoList = activityManager?.runningAppProcesses ?: emptyList()
    val set = mutableSetOf<String>()
    for (info in infoList) {
        Log.i(TAG, "getAllBackgroundProcess: ${info.processName}")
        for (pkg in info.pkgList) {
            Log.i(TAG, "getAllBackgroundProcess: $pkg")
            set.add(pkg)
        }
    }
    return set.toList()
}

/**
 * 获取所有运行的app信息
 *
 * @return List<PackageInfo>
 */
@SuppressLint("QueryPermissionsNeeded")
fun Application.getRunningApps(): List<PackageInfo>? {
    val packageManager: PackageManager = packageManager ?: return null
    val installedPackages = packageManager.getInstalledPackages(0)
    val result = mutableListOf<PackageInfo>()
    for (pi in installedPackages) {
        val flag = pi.applicationInfo.flags
        if ((flag and ApplicationInfo.FLAG_SYSTEM == 0)// 非系统应用
            && ((flag and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)  // 升级后也不是系统应用
            && ((flag and ApplicationInfo.FLAG_STOPPED) == 0)// 未处于停止状态
            && (pi.packageName != packageName) // 排除自己
        ) {
            result.add(pi)
        }
    }
    return result
}

/**
 * 清除内存返回优化百分比
 */
fun Application.killAllProcessesToPercent(): Float {
    val beforeMemory = getAvailMemory()
    killBackgroundProcesses()
    val afterMemory = getAvailMemory()
    return (afterMemory - beforeMemory) / beforeMemory
}

/**
 * 清除内存返回优化内存长度
 */
fun Application.killAllProcessesToMemory(): Float {
    val beforeMemory = getAvailMemory()
    killBackgroundProcesses()
    val afterMemory = getAvailMemory()
    return beforeMemory - afterMemory
}

/**
 * 清除其它正在运行的App
 */
fun Application.killBackgroundProcesses(): Int {
    val default = 3
    try {
        val activityManager = getActivityManager()
        val appList = getRunningApps() ?: return default
        for (info in appList) {
            activityManager.killBackgroundProcesses(info.packageName)
        }
        return appList.size
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return default
}

/**
 * 杀死进程
 */
fun Application.killBackgroundProcesses(packageName: String) {
    try {
        val activityManager = getActivityManager()
        activityManager.killBackgroundProcesses(packageName)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 判断本应用是否已经位于最前端
 *
 * @return 本应用已经位于最前端时，返回 true；否则返回 false
 */
fun Application.isRunningForeground(): Boolean {
    val context = this
    val activityManager = getActivityManager()
    val appProcessInfoList = activityManager.runningAppProcesses ?: return false
    for (appProcessInfo in appProcessInfoList) {
        if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            if (appProcessInfo.processName == context.applicationInfo.processName) {
                return true
            }
        }
    }
    return false
}


/**
 * 获取手机可用内存（单位M）
 */
private fun Application.getAvailMemory(): Float {
    val activityManager = getActivityManager()
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo.availMem / (1024f * 1024)
}

/**
 * 获取手机可用内存信息
 */
fun Application.getMemory(): ActivityManager.MemoryInfo {
    val activityManager = getActivityManager()
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo
}

/**
 * 是否为主进程
 */
fun Application.isMainProcess(): Boolean {
    val mainProcessName = getMainProcessName()
    if (mainProcessName != null) {
        return packageName == mainProcessName
    }
    return !packageName.contains(":")
}

/**
 * 获取主进程名
 */
fun Application.getMainProcessName(): String? {
    val pid = android.os.Process.myPid()
    var processName: String? = null
    val activityManager = getActivityManager()
    val processList = activityManager.runningAppProcesses ?: return processName
    for (process in processList) {
        if (process.pid == pid) {
            processName = process.processName
        }
    }
    return processName
}

/**
 * 获取ActivityManager
 */
fun Application.getActivityManager(): ActivityManager {
    return getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
}
