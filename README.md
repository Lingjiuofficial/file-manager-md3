# 文件管家 · FileKeeper

一个用 **Kotlin + Jetpack Compose** 写的 Android 文件管理器，界面按 **MIUI / HyperOS** 风格设计，底部是类 iOS 的悬浮胶囊导航栏。

> 由 `file-manager-md3` 全面重构而来：不再是「把 6000 行塞进 workflow」的单文件工程，改为正常的 Android 项目结构，方便后续维护。

## 功能

**浏览**
- 列表 / 网格两种视图，按名称 / 大小 / 时间 / 类型排序，显示隐藏文件
- 多标签页、路径面包屑、上级跳转、绝对路径直达
- 收藏夹 + 最近访问

**文件操作**
- 复制 / 剪切 / 粘贴、删除、重命名、新建文件与文件夹
- 多选与批量重命名（前缀 + 起始编号，自动保留扩展名）
- 属性查看、分享、用其他应用打开

**压缩包**
- 浏览 ZIP / 7Z / RAR / TAR / GZ / BZ2 / XZ
- 解压到当前目录，一键压缩为 ZIP
- 防路径穿越（zip-slip）处理

**内置查看器**
- 文本编辑（UTF-8 / GBK 自动识别，可保存）
- 图片（双指缩放、拖动）
- 视频 / 音频（Media3 ExoPlayer）
- APK 信息（包名、版本、SDK、签名 SHA-256、安装）
- 压缩包目录、十六进制查看

**其他**
- 当前目录搜索 + 分类筛选（图片 / 视频 / 音频 / 文档 / 安装包 / 压缩包）
- 存储分析：目录占用排行、条形图、逐级下钻
- 深色 / 浅色 / 跟随系统主题

## 构建

推送到 `main` 后，GitHub Actions 会自动构建 Release APK 并发布到
[Releases](https://github.com/Lingjiuofficial/file-manager-md3/releases) 的 `latest` 标签。

本地构建（需要 JDK 17 + Android SDK 34）：

```bash
./gradlew :app:assembleDebug
```

## 目录结构

```
app/src/main/java/io/github/lingjiuo/filekeeper/
├── MainActivity.kt          # 入口、壳层、权限、返回栈
├── data/
│   ├── FileRepo.kt          # 文件读写、复制移动、搜索、大小统计
│   ├── ArchiveRepo.kt       # ZIP / 7Z / RAR / TAR 读取与解压
│   └── Prefs.kt             # 轻量设置存储
├── model/Models.kt          # 数据模型
├── ui/
│   ├── AppState.kt          # 全局状态（路径、选择、剪贴板、标签）
│   ├── theme/Theme.kt       # MIUI 配色与排版
│   ├── components/          # 悬浮栏、文件行、对话框
│   └── screens/             # 文件页、搜索、存储、设置、查看器
└── util/Fmt.kt              # 格式化、图标、哈希、自然排序
```

## 路线图

- [ ] 双窗格对照
- [ ] Root 分区浏览
- [ ] FTP 客户端 / 服务端
- [ ] PDF 浏览器
- [ ] ZIP 加密与分卷
- [ ] 应用管理（备份 / 卸载）

## 许可

GPL-3.0，与原项目一致。
