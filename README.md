# 伏魔记 (fmj)

经典国产 RPG《伏魔记》的 Android 平台复刻版。

本项目使用现代 Android 工具链重新实现了原经典手机（J2ME/塞班）游戏《伏魔记》，完整还原了剧情脚本、回合制战斗、法术系统、道具装备与角色养成等核心玩法，并复用原版游戏的数据资源（`DAT.LIB`）与点阵字库（`HZK16`/`ASC16`）。

> 📖 游戏玩法与流程文档见 [`docs/`](./docs) 目录：
> - [伏魔记_完全游戏指南.md](./docs/伏魔记_完全游戏指南.md)
> - [伏魔记_核心攻略.md](./docs/伏魔记_核心攻略.md)

---

## 📦 下载

可直接下载已编译的 APK 体验：

👉 **[Releases · v2.7](https://github.com/WalterWj/fmj-android/releases/tag/v2.7)**

如需自行编译，参见下方 [构建与运行](#-构建与运行) 章节。

---

## ✨ 功能特性

- **完整剧情脚本系统**：基于脚本驱动（`script` 包），还原原版剧情与事件流程
- **回合制战斗**：物理攻击 / 法术 / 道具 / 防御 / 逃跑 / 合击 / 群体攻击等丰富战斗动作（`combat` 包）
- **法术系统**：攻击、恢复、辅助、强化、特殊及连携法术（`magic` 包）
- **道具与装备**：武器、防具、饰品、药品、暗器、土遁、剧情道具等（`goods` 包）
- **角色养成**：玩家、NPC、怪物，含升级链与成长数值（`characters` 包）
- **原版资源解析**：直接读取原版 `DAT.LIB` / `DAT2.LIB` 数据库（`lib` 包）
- **菜单与界面**：游戏主菜单、物品、装备、状态、存读档等界面（`gamemenu` / `views` / `scene` 包）
- **横屏全屏**：在内部 160×106 点阵画布上渲染并自适应放大，保持原版视觉风格

---

## 🧰 内置功能开关（个人增强）

在原版基础上，个人添加了两个便于体验的内置开关，作为屏幕**顶部的触摸按钮**显示，默认关闭，点击即可切换：

| 开关 | 位置 | 说明 |
| --- | --- | --- |
| **遇敌**（不遇敌） | 左上角 | 开启后地图行走**不再触发随机战斗**，可畅通探索 / 推进剧情。按钮高亮表示「遇敌开启」，熄灭表示「不遇敌」。 |
| **百倍**（百倍获取） | 右上角 | 开启后**全方位 ×100**，远不止战斗金钱经验，详见下方。 |

**「百倍」开启后的具体效果：**
- 🪙 **战斗结算**：胜利所得**金钱 ×100**、**经验 ×100**
- 🎁 **物品获取**：战斗掉落战利品数量 ×100；剧情触发「获得物品」时数量 ×100
- 💊 **药品使用**：服用药品的效果 ×100
  - 普通药品：HP / MP 恢复量 ×100
  - 百分比恢复药品：恢复比例 ×100（封顶即满血 / 满蓝）
  - 永久提升类药品（灵丹）：最大 HP / MP、攻击、防御、灵力、速度、运气等永久提升量 ×100

> 💡 这两个开关均为运行时切换，互不影响，可任意组合使用。

**相关实现位置：**
- 开关状态：`Global.java` → `sCheatNoEncounter` / `sCheatExpGold`
- 按钮绘制与点击切换：`GameView.java`（`TouchControls` 顶部区域）
- 不遇敌逻辑：`scene/ScreenMainGame.java` → `triggerMapEvent()`
- 百倍战斗结算：`combat/Combat.java`（金钱 / 经验 / 战利品数量）
- 百倍物品获取：`script/ScriptProcess.java`（剧情获得物品数量）
- 百倍药品效果：`goods/GoodsMedicine.java`、`GoodsMedicineLife.java`、`GoodsMedicineChg4Ever.java`

---

## 🛠️ 技术栈

| 项目 | 版本 / 说明 |
| --- | --- |
| 平台 | Android |
| 语言 | Java 17 |
| compileSdk / targetSdk | 34 |
| minSdk | 21（Android 5.0+） |
| Application ID | `hz.cdj.game.fmj` |
| Android Gradle Plugin | 8.5.2 |
| Gradle | 8.9 |
| 依赖 | AndroidX AppCompat 1.6.1 |

游戏采用 `SurfaceView` + 独立游戏线程的经典游戏循环架构，内部维护一个屏幕栈（`Stack<BaseScreen>`）管理各游戏场景与菜单。

---

## 📦 项目结构

```
fmj-android/
├── app/
│   └── src/main/
│       ├── java/hz/cdj/game/fmj/
│       │   ├── MainActivity.java        # 入口 Activity（横屏全屏）
│       │   ├── GameView.java            # SurfaceView 游戏主循环 + 屏幕栈
│       │   ├── Global.java              # 全局状态
│       │   ├── lib/                     # 原版资源库解析（DAT.LIB 等）
│       │   ├── graphics/                # 图形绘制 / 点阵文字渲染
│       │   ├── characters/              # 角色：玩家、NPC、怪物、升级链
│       │   ├── goods/                   # 道具与装备系统
│       │   ├── magic/                   # 法术系统
│       │   ├── combat/                  # 回合制战斗（动作、动画、UI）
│       │   ├── script/                  # 剧情脚本解释执行
│       │   ├── gamemenu/                # 游戏内菜单界面
│       │   ├── views/                   # 通用屏幕（消息框、动画、菜单）
│       │   └── scene/                   # 主游戏场景与存读档
│       ├── assets/                      # 原版数据与字库
│       │   ├── DAT.LIB / DAT2.LIB       # 原版游戏资源库
│       │   └── HZK16 / ASC16            # 汉字 / ASCII 点阵字库
│       └── res/                         # 图标、主题、字符串
├── docs/                                # 游戏指南与攻略
├── gradle/wrapper/                      # Gradle Wrapper
├── dump_*.py                            # 数据库分析辅助脚本
├── build.gradle / settings.gradle       # Gradle 构建配置
└── gradlew / gradlew.bat                # Gradle Wrapper 脚本
```

---

## 🚀 构建与运行

### 环境要求

- JDK 17
- Android SDK（compileSdk 34）
- Android Studio（推荐）或命令行 Gradle

### 使用 Android Studio

1. `File → Open` 选择本项目根目录
2. 等待 Gradle 同步完成
3. 连接 Android 设备或启动模拟器（API 21+）
4. 点击 ▶️ 运行

### 使用命令行

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建产物位于：
# app/build/outputs/apk/debug/app-debug.apk
```

> ⚠️ `local.properties`（本机 SDK 路径）已被 `.gitignore` 忽略，首次用 Android Studio 打开时会自动生成。

---

## 🔧 辅助脚本

根目录下的 Python 脚本用于分析 / 比对原版 `DAT.LIB` 数据库（如升级数值、数据对比），供开发调试使用：

- `dump_levelup.py` / `dump_levelup2.py` —— 导出角色升级数据
- `dump_compare.py` —— 数据库内容比对

---

## 📄 许可

本项目仅供学习与交流目的，复刻经典游戏玩法。原版游戏《伏魔记》的资源、名称及相关版权归其原始权利人所有。
