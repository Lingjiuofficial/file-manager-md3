# Files · MD3

![Build](https://github.com/Lingjiuofficial/file-manager-md3/actions/workflows/build.yml/badge.svg)
![License](https://img.shields.io/badge/License-GPLv3-blue.svg)

一个自制 Android 文件管理器：**Kotlin + Jetpack Compose**，全局 **Material 3** 设计（动态取色跟随壁纸，可开关；关闭后使用经典 MD3 配色）。

特色：**单文件构建** —— 全部源码内嵌在 GitHub Actions workflow 里，推开即构建，全程手机浏览器完成开发与发布。

## ✨ 功能

- **文件管理**：浏览 / 复制 / 剪切 / 粘贴 / 删除 / 重命名 / 新建（文件 + 文件夹），**多选批量操作**（批量复制 / 剪切 / 删除 / 重命名）
- **压缩包**：ZIP 创建 / **包内增删改名**；**zip / jar / rar（RAR4+RAR5）/ 7z / tar / tgz 全格式浏览 + 解压**
- **内置查看器全家桶**（不依赖外部应用）：
  - **APK 详情**：图标 / 包名 / 版本 / SDK / 签名指纹（SHA-256、MD5）/ 权限 / 组件 / DEX
  - **十六进制查看**：16KB 分页 + 偏移跳转，任意大文件安全
  - **PDF**：内置渲染 + 滑动翻页
  - **SQLite**：只读表列表 + 数据浏览
  - **HTML**：内置 WebView 预览（JS 关闭）
  - 文本编辑（**UTF-8 / GBK 自动识别 + BOM 保留**、查找替换 + 匹配计数、**查找定位（上一个/下一个 + 计数）**、行数统计、大文件虚拟化预览）、图片（双指缩放 1–6x / 左右滑切图 / 双击复位 / **90° 旋转** / **设为壁纸**）、视频、音频、字体预览
- **FTP 客户端**：多连接配置保存、远程浏览、上传 / 下载 / 新建文件夹 / 删除 / 重命名；**传输进度显示**、**FTPS 安全连接（显式 AUTH TLS）**；**UTF-8 / GBK 编码切换**（中文服务器不乱码）；被动模式
- **Root 文件管理**（KernelSU / Magisk，需授权）：浏览系统分区、新建文件夹、查看文本、导出到 Download、重命名、删除（**系统关键路径删除保护**）
- **双窗格切换**：顶栏一键在两个目录间来回切换（对照目录记忆在本地）
- **搜索**：当前目录搜索 + 全局搜索；分类筛选（图片 / 视频 / 音频 / 文档 / 安装包 …）；**打开所在文件夹**
- **快速滚动条**：长列表右侧拖动快速定位（>40 项自动出现）
- **工具箱**：Base64 / URL 编解码、哈希（MD5 / SHA-1）、文本转换
- **存储分析**：目录占用排行 + 可视化条形图，逐级下钻；设置页含存储空间卡片（总/已用/可用）
- **快捷跳转**：常用目录一键直达（下载 / 文档 / 相机 / 图片 / Android data 等）+ 绝对路径输入
- **其他**：收藏 / 最近 / 隐藏文件 / 排序 / 网格与列表 / **深色模式三态（跟随系统 / 浅色 / 深色）** / 属性 / 校验哈希 / 文件夹大小统计 / 分享（minSdk 26，Android 8.0+）

## 📦 下载

- **[Releases](../../releases)** —— 最新 APK（每次构建自动更新，无需登录直接下载）
- 或 Actions → 最近一次运行 → Artifacts（需登录 GitHub）

## 🏗️ 项目结构（重要）

本项目采用 **单文件构建** 架构：

- 全部源码（Gradle 配置 + Kotlin）内嵌在 [`.github/workflows/build.yml`](.github/workflows/build.yml) 中；
- 推送到 `main` 后，GitHub Actions 自动生成完整 Android 工程并构建 Release APK；
- 同一工作流会把最新源码同步到 `generated/` 目录（方便在仓库里直接阅读）；
- 好处：零本地环境依赖，全程手机浏览器即可完成开发、构建与发布。

> 想阅读 Kotlin 源码：打开 `build.yml`，`KOTLIN_EOF` 之间即为完整的 `MainActivity.kt`（约 6300 行的 Compose 单文件 UI）。

## 🔧 自行构建

1. Fork 本仓库
2. 在 Fork 的 Actions 页面启用 workflow
3. push 任意提交（或手动 Run workflow）
4. 在 Actions 产物 / Releases 中获取 APK

> 装机提示：构建使用固定签名，安装新版可直接覆盖升级；若此前装过随机签名的旧包，需先卸载再装。

## 📄 许可

**GNU General Public License v3.0** —— 本程序是自由软件：你可以依据 GPL-3.0 条款重新分发和/或修改；完整许可证见 [LICENSE](LICENSE)。

Copyright (C) 2026 Lingjiuofficial
