# GitHub Actions 自动构建 APK

## 📋 工作流说明

### 1. build-apk.yml - 自动构建工作流
**触发条件：**
- 推送到 `main` 或 `master` 分支
- 创建 Pull Request

**功能：**
- 自动构建 Debug APK
- 上传构建产物（保留 7 天）
- PR 自动评论构建结果

---

### 2. release.yml - 发布工作流
**触发条件：**
- 创建标签（如 `v1.0.0`）
- 手动触发

**功能：**
- 构建 Debug 和 Release APK
- 自动创建 GitHub Release
- 上传 APK 到 Release 页面

---

## 🚀 使用方法

### 方法 1：推送代码自动构建

```bash
git add .
git commit -m "Update app"
git push origin main
```

构建完成后：
1. 进入仓库的 **Actions** 标签
2. 点击最新的工作流运行
3. 在 **Artifacts** 部分下载 APK

---

### 方法 2：创建标签发布版本

```bash
# 创建标签
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

发布完成后：
1. 进入仓库的 **Releases** 页面
2. 找到新创建的 Release
3. 下载 APK 文件

---

### 方法 3：手动触发构建

1. 进入仓库的 **Actions** 标签
2. 选择 **Release APK** 工作流
3. 点击 **Run workflow**
4. 输入版本名称（可选）
5. 点击 **Run workflow** 确认

---

## 📥 下载 APK

### 从 Actions 页面下载
1. 进入 **Actions** 标签
2. 点击具体的工作流运行记录
3. 滚动到底部的 **Artifacts** 部分
4. 点击下载 APK 文件

### 从 Releases 页面下载
1. 进入 **Releases** 标签
2. 找到对应版本
3. 在 **Assets** 部分下载 APK

---

## 📝 文件命名规则

- Debug APK: `KotlinAndroidApp-debug-{日期时间}.apk`
- Release APK: `KotlinAndroidApp-release-{版本号}.apk`

示例：
- `KotlinAndroidApp-debug-20240423_143025.apk`
- `KotlinAndroidApp-release-v1.0.0.apk`

---

## 🔧 配置说明

### 基础配置（已完成）

GitHub Actions 无需额外配置，推送代码即可自动运行！

### 高级配置（可选）

#### 1. 构建签名的 APK

如果需要构建可直接安装的签名 APK，需要配置签名密钥：

**步骤 1：生成密钥库**
```bash
keytool -genkey -v -keystore release.keystore \
  -alias my-key-alias \
  -keyalg RSA -keysize 2048 -validity 10000
```

**步骤 2：配置 GitHub Secrets**

进入仓库 **Settings** → **Secrets and variables** → **Actions**，添加：

- `KEYSTORE_BASE64`: 密钥库的 Base64 编码
  ```bash
  base64 release.keystore | tr -d '\n'
  ```
- `KEYSTORE_PASSWORD`: 密钥库密码
- `KEY_ALIAS`: 密钥别名
- `KEY_PASSWORD`: 密钥密码

**步骤 3：更新 build.gradle**

在 `app/build.gradle` 中添加签名配置：
```gradle
android {
    signingConfigs {
        release {
            storeFile file(System.getenv("KEYSTORE_FILE") ?: "release.keystore")
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

#### 2. 自定义构建配置

修改 `.github/workflows/build-apk.yml` 文件：

```yaml
# 修改触发分支
on:
  push:
    branches:
      - main
      - develop  # 添加其他分支

# 修改保留天数
retention-days: 30  # 默认 7 天

# 添加其他构建步骤
- name: Run tests
  run: ./gradlew test
```

---

## ⚠️ 常见问题

### Q1: 构建失败怎么办？

**A:** 查看详细日志：
1. 进入 Actions 页面
2. 点击失败的工作流
3. 展开失败的步骤查看错误信息

常见原因：
- Gradle 配置错误
- 依赖下载失败
- 代码编译错误

**本地测试：**
```bash
./gradlew assembleDebug --stacktrace
```

---

### Q2: 如何查看构建状态？

**A:** 在仓库 README 中添加徽章：

```markdown
![Build Status](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/build-apk.yml/badge.svg)
```

---

### Q3: APK 文件在哪里？

**A:** 
- **开发构建**: Actions → 工作流运行 → Artifacts
- **正式发布**: Releases 页面

---

### Q4: 如何加速构建？

**A:** GitHub Actions 已经配置了 Gradle 缓存：
```yaml
cache: gradle  # 自动缓存依赖
```

首次构建较慢（5-10分钟），后续构建会快很多（2-3分钟）。

---

### Q5: 构建超时怎么办？

**A:** GitHub Actions 免费版限制：
- 单次运行最长 6 小时
- 每月 2000 分钟（公开仓库无限制）

如果超时，可以：
1. 优化 Gradle 配置
2. 减少依赖
3. 使用 Gradle 缓存

---

## 📊 构建状态徽章

在项目 README.md 中添加：

```markdown
# KotlinAndroidApp

![Build](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/build-apk.yml/badge.svg)
![Release](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/release.yml/badge.svg)

## 下载

[📥 下载最新版本](https://github.com/YOUR_USERNAME/YOUR_REPO/releases/latest)
```

---

## 🎯 快速开始检查清单

- [x] 工作流文件已创建（`.github/workflows/`）
- [ ] 推送代码到 GitHub
- [ ] 查看 Actions 页面确认构建开始
- [ ] 等待构建完成（首次约 5-10 分钟）
- [ ] 下载生成的 APK 文件
- [ ] 测试 APK 安装和运行

---

## 📚 相关资源

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Android 构建指南](https://developer.android.com/studio/build)
- [Gradle 优化](https://docs.gradle.org/current/userguide/performance.html)

---

## 💡 提示

1. **公开仓库**：GitHub Actions 完全免费，无限制
2. **私有仓库**：每月 2000 分钟免费额度
3. **构建缓存**：已自动配置，加速后续构建
4. **并行构建**：可以同时运行多个工作流

---

就这么简单！推送代码即可自动构建 APK！🎉
