# 邮件管理系统

一个基于 SSM (Spring + SpringMVC + MyBatis) + Vue3 的现代化邮件管理系统，提供完整的邮件收发、联系人管理、公告发布等功能。

## 🚀 技术栈

### 后端
- **Java**: JDK 23
- **框架**: Spring 5.x + SpringMVC + MyBatis 3.x
- **数据库**: MySQL 8.0+
- **构建工具**: Maven (IDEA 自动导入)
- **服务器**: Tomcat 9.0.106

### 前端
- **框架**: Vue 3.x
- **UI库**: Element Plus
- **构建工具**: Vite
- **包管理**: pnpm (推荐)
- **Node.js**: v22.16.0

## 📋 环境要求

### 必需软件
1. **Java JDK 23** - [下载地址](https://www.oracle.com/java/technologies/downloads/)
2. **MySQL 8.0+** - [下载地址](https://dev.mysql.com/downloads/mysql/)
3. **Node.js v22.16.0+** - [下载地址](https://nodejs.org/)
4. **Maven** - 通过 IDEA 自动导入，无需单独下载
5. **Tomcat 9.0.106** - [下载地址](https://tomcat.apache.org/)

### 推荐软件
- **IDE**: IntelliJ IDEA (推荐使用 IDEA 自动导入 Maven 依赖)
- **数据库管理**: Navicat
- **包管理器**: pnpm (`npm install -g pnpm`)

## 🛠️ 安装步骤

### 1. 解压项目
```bash
# 解压项目压缩包到本地目录
# 项目结构：
JAVA/
├── email-manager-frontend/          # Vue3 前端项目
└── EmailManager/                    # Spring 后端项目
```

### 2. 数据库配置

#### 2.1 创建数据库
```sql
-- SQL 文件位置：EmailManager/src/main/resources/sql/email_manager.sql
-- 使用项目提供的 SQL 脚本创建数据库和表结构
mysql -u root -p < EmailManager/src/main/resources/sql/email_manager.sql
```

#### 2.2 配置数据库连接
编辑 `EmailManager/src/main/resources/jdbc.properties`：
```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/email_manager?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
jdbc.username=你的数据库用户名
jdbc.password=你的数据库密码
```

### 3. 后端配置

#### 3.1 导入项目
- 使用 IntelliJ IDEA 打开 `EmailManager` 文件夹
- IDEA 会自动导入 Maven 依赖，无需手动下载

#### 3.2 配置 Tomcat
1. 在 IDEA 中配置 Tomcat 9.0.106 服务器
2. 部署路径设置为 `/`
3. 端口设置为 `8080` (或其他可用端口)

### 4. 前端配置

#### 4.1 安装依赖
```bash
npm install -g pnpm
cd email-manager-frontend
pnpm install
```

#### 4.2 配置 API 地址
编辑 `src/utils/request.js`，确保 API 地址正确：
```javascript
const baseURL = 'http://localhost:8080/api'
```

## 🚀 启动项目

### 1. 启动后端
1. 在 IDEA 中运行 Tomcat 服务器
2. 或使用命令行：
```bash
cd EmailManager
mvn clean package
java -jar target/EmailManager.war
```

### 2. 启动前端
```bash
cd email-manager-frontend
pnpm run dev
```

### 3. 访问系统
- 前端地址: http://localhost:5173
- 后端API: http://localhost:8080/api

## 📁 重要文件位置

### SQL 脚本
- **位置**: `EmailManager/src/main/resources/sql/email_manager.sql`
- **说明**: 包含完整的数据库创建和初始化脚本

### 配置文件
- **数据库配置**: `EmailManager/src/main/resources/jdbc.properties`
- **MyBatis配置**: `EmailManager/src/main/resources/mybatis/mybatis-config.xml`
- **Spring配置**: `EmailManager/src/main/resources/spring/applicationContext.xml`

### 前端配置
- **API配置**: `email-manager-frontend/src/utils/request.js`
- **路由配置**: `email-manager-frontend/src/router/index.js`

## 👤 基础账号信息

### 管理员账号
- **用户名**: `admin`
- **密码**: `123456`
- **邮箱**: `admin@example.com`
- **权限**: 系统管理员，可管理用户、发布公告、设置全局规则等

### 测试用户账号
- **用户A**: 
  - 用户名: `userA`
  - 密码: `123456`
  - 邮箱: `userA@example.com`
  - 真实姓名: 张三

- **用户B**: 
  - 用户名: `userB`
  - 密码: `123456`
  - 邮箱: `userB@example.com`
  - 真实姓名: 李四

- **禁用用户**: 
  - 用户名: `userC_disabled`
  - 密码: `123456`
  - 邮箱: `userC@example.com`
  - 状态: 已禁用

## 📧 功能特性

### 用户功能
- ✅ 邮件收发管理
- ✅ 联系人管理
- ✅ 垃圾邮件规则设置
- ✅ 邮件搜索与归档
- ✅ 个人资料管理
- ✅ 服务条款查看

### 管理员功能
- ✅ 用户管理
- ✅ 系统公告发布
- ✅ 全局垃圾邮件规则
- ✅ 节假日管理
- ✅ 服务条款管理
- ✅ 系统统计仪表盘

## 🗂️ 项目结构

```
JAVA/
├── email-manager-frontend/          # Vue3 前端项目
│   ├── src/
│   │   ├── api/                     # API 接口
│   │   ├── components/              # 组件
│   │   ├── views/                   # 页面
│   │   ├── router/                  # 路由配置
│   │   ├── stores/                  # 状态管理
│   │   └── utils/                   # 工具函数
│   └── package.json
└── EmailManager/                    # Spring 后端项目
    ├── src/main/java/
    │   └── com/emailmanager/
    │       ├── controller/          # 控制器
    │       ├── service/             # 服务层
    │       ├── mapper/              # MyBatis 映射
    │       ├── entity/              # 实体类
    │       └── util/                # 工具类
    ├── src/main/resources/
    │   ├── sql/                     # SQL 脚本
    │   └── mapper/                  # MyBatis XML
    └── pom.xml
```

## 🔧 常见问题

### 1. 数据库连接失败
- 检查 MySQL 服务是否启动
- 确认数据库用户名密码正确
- 检查数据库端口是否为 3306

### 2. 前端无法访问后端 API
- 确认后端服务已启动
- 检查 CORS 配置
- 验证 API 地址是否正确

### 3. 依赖下载失败
- 检查网络连接
- 尝试使用国内镜像源
- 清除 Maven 缓存: `mvn clean`

### 4. 端口冲突
- 修改 Tomcat 端口配置
- 或修改前端开发服务器端口

## 📝 开发说明

### 添加新功能
1. 后端：在对应包下添加 Controller、Service、Mapper
2. 前端：在 `src/views` 下添加页面，在 `src/router` 中配置路由

### 数据库变更
1. 修改实体类
2. 更新 MyBatis 映射文件
3. 执行 SQL 脚本更新数据库结构

### 部署生产环境
1. 后端：`mvn clean package` 打包
2. 前端：`pnpm build` 构建
3. 将构建文件部署到 Web 服务器


---

**注意**: 首次使用请务必修改默认密码，确保系统安全。 