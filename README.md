# 船票预订应用

## 项目简介
这是一个基于 Android Jetpack Compose 开发的船票预订应用，用于展示船票预订信息和航段详情。

## 功能特点
- 显示船票预订基本信息
- 展示航段列表
- 支持下拉刷新
- 本地数据缓存
- 剩余有效时间显示
- 错误处理和重试机制

## 技术栈
- Kotlin
- Jetpack Compose
- Hilt（依赖注入）
- Coroutines & Flow
- MVVM 架构
- Material3 设计

## 项目结构 
app/src/main/java/com/org/zrek/accenturetest/
├── data/ # 数据层
│ ├── BookingDataManager.kt
│ └── local/ # 本地存储
│ └── BookingCache.kt
├── di/ # 依赖注入
│ └── BookingModule.kt
├── model/ # 数据模型
│ ├── BookingResponse.kt
│ └── Segment.kt
├── service/ # 网络服务
│ └── BookingService.kt
├── ui/ # 界面层
│ ├── screen/ # 界面组件
│ │ └── BookingScreen.kt
│ ├── theme/ # 主题相关
│ └── viewmodel/ # ViewModel
│ └── BookingViewModel.kt
└── util/ # 工具类
├── Logger.kt
└── Result.kt
