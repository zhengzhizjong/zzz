# 忠济堂中医养生连锁管理系统 · Git分支规范（V3.4）

## 一、分支策略

### 1.1 分支模型（Git Flow）

| 分支类型 | 命名规则 | 说明 | 生命周期 |
|----------|----------|------|----------|
| main | main | 生产分支，只接受merge | 永久 |
| develop | develop | 开发主分支 | 永久 |
| feature | feature/{module}/{description} | 功能开发分支 | 合并后删除 |
| bugfix | bugfix/{module}/{description} | Bug修复分支 | 合并后删除 |
| hotfix | hotfix/{module}/{description} | 紧急修复分支 | 合并后删除 |
| release | release/v{version} | 发布分支 | 合并后删除 |

### 1.2 分支命名示例

| 模块 | 分支名 |
|------|--------|
| 预约功能 | feature/appointment/create-appointment |
| 预约修改 | feature/appointment/modify-cancel |
| 技师推广 | feature/technician/promotion |
| 门店管理 | feature/store/crud |
| 预约锁档Bug | bugfix/appointment/lock-timeout |
| 紧急修复 | hotfix/appointment/duplicate-booking |

### 1.3 分支流转规则

```
main ← release/v1.0.0 ← develop ← feature/xxx
main ← hotfix/xxx (紧急修复直接从main拉出)
```

- feature分支从develop拉出，开发完成后提MR合并回develop
- release分支从develop拉出，测试通过后合并到main和develop
- hotfix分支从main拉出，修复后合并到main和develop
- 禁止直接push到main和develop

## 二、版本号规范

### 2.1 语义化版本（SemVer）

格式：v{MAJOR}.{MINOR}.{PATCH}

| 版本类型 | 说明 | 示例 |
|----------|------|------|
| MAJOR | 不兼容的API变更 | v1.0.0 → v2.0.0 |
| MINOR | 向下兼容的功能新增 | v1.0.0 → v1.1.0 |
| PATCH | 向下兼容的Bug修复 | v1.0.0 → v1.0.1 |

### 2.2 版本里程碑

| 版本 | 目标 | 包含模块 |
|------|------|----------|
| v0.1.0 | 骨架搭建 | 基础工程+全局异常+JWT+数据库连接 |
| v0.2.0 | 基础数据 | 门店/技师/时段配置CRUD |
| v0.3.0 | C端预约 | 预约创建+锁档+我的预约 |
| v0.4.0 | 改约取消 | 修改预约+取消预约闭环 |
| v0.5.0 | 技师推广 | 推广模块+佣金+追踪 |
| v0.6.0 | 后台统计 | 数据看板+漏斗+排行 |
| v1.0.0 | 正式发布 | 全功能联调+压测+上线 |

## 三、Commit Message规范

格式：`<type>(<scope>): <subject>`

| Type | 说明 | 示例 |
|------|------|------|
| feat | 新功能 | feat(appointment): 新增预约创建接口 |
| fix | Bug修复 | fix(appointment): 修复并发预约超卖问题 |
| docs | 文档 | docs(api): 更新预约接口文档 |
| style | 格式 | style(appointment): 代码格式化 |
| refactor | 重构 | refactor(appointment): 提取锁档公共方法 |
| perf | 性能 | perf(appointment): 优化时段查询缓存 |
| test | 测试 | test(appointment): 新增预约锁档单元测试 |
| chore | 构建 | chore(deploy): 更新Dockerfile |

## 四、Merge Request规范

### 4.1 MR标题
格式：`[{模块}] {简要描述}`

示例：
- [预约] 新增预约创建功能
- [推广] 新增技师推广海报生成

### 4.2 MR描述模板
```
## 变更说明
简要描述本次变更内容

## 变更文件
- 新增：xxx
- 修改：xxx
- 删除：xxx

## 关联需求
- CUS-004 预约创建

## 测试说明
- [x] 单元测试通过
- [x] 接口测试通过
- [ ] 人工验收待确认

## SQL变更
```sql
-- 新增表
CREATE TABLE ...
```

## 截图（如有UI变更）
```

### 4.3 MR审核规则
- 至少1人审核通过
- CI流水线全部通过
- 无合并冲突
- 代码覆盖率不降低

## 五、标签规范

| 标签 | 说明 |
|------|------|
| v{version} | 版本发布标签 |
| rc{version} | 发布候选标签 |
| beta{version} | 测试版本标签 |
