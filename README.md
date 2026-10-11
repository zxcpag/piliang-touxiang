# 批量头像 · 联系人头像批量设置工具

一个本地运行的安卓 APP：**给通讯录联系人批量设置头像**，内置可批量导入的头像库，支持一键随机匹配。

## 功能

- **通讯录管理**：读取本机联系人，支持多选 / 全选。
- **头像库**：从相册**批量导入**头像，展示网格，长按可删除。
- **批量设置**：勾选多位联系人后，用指定的一张头像一次设置到所有人。
- **随机匹配**：一键为每位已选联系人从头像库随机分配一张头像。

> 说明：本应用在本地运行，所有头像图片只保存在本机应用私有目录，不上传任何服务器。

---

## 技术栈

- 语言：Kotlin
- UI：XML + RecyclerView + Material Components 3
- 数据：Room 本地数据库（头像库索引）+ ContentProvider（联系人头像读写）
- 构建：Gradle 8.7 / AGP 8.5.2 / compileSdk 34 / minSdk 24

## 项目结构

```
ContactAvatarBatch/
├── app/src/main/java/com/example/avatarbatchsetter/
│   ├── MainActivity.kt            # 入口 + 底部导航 + 权限申请
│   ├── MainViewModel.kt           # 联系人/头像库/批量设置/随机匹配逻辑
│   ├── data/                      # 数据层（联系人读写 + Room 头像库）
│   ├── ui/                        # 界面层（两个 Fragment + 适配器）
│   └── util/ImageUtil.kt          # 图片压缩处理
└── app/src/main/res/              # 布局、图标、菜单、主题
```

## 构建

需 JDK 17 + Android SDK。构建命令：

```
./gradlew assembleDebug
```

> 依赖已配置阿里云 Maven 镜像，Gradle 发行版已切换为腾讯云镜像，适合国内网络环境。
