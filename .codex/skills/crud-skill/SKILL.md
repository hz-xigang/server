---
name: crud-skill              
description: CRUD代码生成  
---

# Skill：Spring Boot + MyBatis-Plus CRUD代码生成器

## 目标

当我提供数据库表结构(SQL)时，严格按照以下规则自动生成：
- Entity 实体类 (Java)
- Dto 传输类(Java)
- Mapper（继承 BaseMapper）(Kotlin)
- PlusService（继承 ServiceImpl）(Kotlin)
- Service（不分层单个类）(Kotlin)

项目技术栈：
- Spring Boot
- MyBatis-Plus
- Lombok
- Java 8+
- Kotlin

---

# 0. 表名与模块规则

## ✔ 表名规则

- 数据库表名使用 **小驼峰命名（camelCase）**
- 示例：
  - wmsProductionOrder
  - sysUser
  - orderDetail

---

# 1. 包结构与命名空间规则

基础模块包名为：`com.gz.xg`。

生成目录及包名对应关系：
- Entity: `com.gz.xg.domain.entity`
- Dto: `com.gz.domain.dto`
- Mapper: `com.gz.xg.mapper`
- Service: `com.gz.xg.service`
- PlusService: `com.gz.xg.service.plus`

---

# 2. Entity 实体生成规则 (Java)

- **语言**：必须使用 **Java**。
- **类名转换**：表名转大驼峰（如 `sys_user` -> `SysUser`）。
- **注解**：类上必须标注 `@Data` 和 `@TableName("表名")`。
- **字段命名**：数据库字段标准本身即为小驼峰。**绝对不要**进行下划线转驼峰处理，Java 字段名必须与数据库字段名完全保持原样一致。
- **主键规则**：主键字段必须标注 `@TableId`，类型固定为 `String`，不使用任何自增或 `IdType` 显式配置。
- **类型映射**：

  | SQL类型                 | Java类型        |
  |-----------------------|---------------|
  | bigint                | Long          |
  | int / tinyint         | Integer       |
  | bit                   | Boolean       |
  | decimal               | BigDecimal    |
  | varchar / char / text | String        |
  | datetime / timestamp  | LocalDateTime |
  | date                  | LocalDate     |


## ✔ 注释规则（关键升级）

### 类注释来源：MS_Description

```java
/**
 * 生产订单表(精简页面版)
 */
```

### 字段注释来源：MS_Description

```java
/**
 * 全局唯一主键ID(字符串类型)
 */
private String id;
```

---

## ❌ 禁止

- Swagger
- XML
- Wrapper / QueryChain
- 额外方法


# 3. Dto 实体生成规则 (Java)
和 Entity 实体生成规则一样，不需要deleted字段

---

# 4. Mapper 生成规则 (Kotlin)

- **语言**：必须使用 **Kotlin**。
- **类定义**：使用 `interface`，标注 `@Mapper` 注解。
- **继承关系**：仅继承 `BaseMapper<Entity>`，不生成任何 XML、不生成任何自定义方法。

---

# 5. PlusService 生成规则 (Kotlin)

- **语言**：必须使用 **Kotlin**。
- **类定义**：使用 `class`，标注 `@Service` 注解。
- **继承关系**：继承 `ServiceImpl<Mapper, Entity>()`，类内部保持空白，不生成任何额外方法。

---

# 6. Service 生成规则 (Kotlin)

- **语言**：必须使用 **Kotlin**。
- **重要限制**：不要生成 `IService` 接口和 `ServiceImpl` 实现类，只生成一个普通的单体 `Service` 类。
- **依赖注入**：使用 Kotlin 主构造函数隐式注入 `PlusService`，**不要**在 Kotlin 类上使用 Lombok 的 `@RequiredArgsConstructor`。
- **内置方法**：必须且仅包含以下三个基础 CRUD 方法：
    1. `findById(id: String): Entity?` -> 调用 `plusService.getById(id)`
    2. `deleteById(id: String): Boolean` -> 调用 `plusService.removeById(id)`
    3. `updateById(entity: Entity): Boolean` -> 调用 `plusService.updateById(entity)`

---

# 7. 禁用规则（绝对不要生成以下内容）

- 不生成 XML 映射文件
- 不生成任何注释、JavaDoc、Swagger/OpenAPI 注解
- 不生成 Controller、DTO、VO
- 不生成 分页、Wrapper 或 QueryChain 等复杂查询代码

---

# 8. 输出格式规则

请严格按照以下格式和顺序输出代码块：

=== Entity ===
(Java 代码)

=== Mapper ===
(Kotlin 代码)

=== PlusService ===
(Kotlin 代码)

=== Service ===
(Kotlin 代码)

---

# 9. 正确示例

### 示例输入 SQL
```sql
CREATE TABLE sys_user (
    id varchar(50) primary key,
    userName varchar(50),
    age int,
    createTime datetime
);
```
### 示例输出
```
=== Entity ===
package com.xxx.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId
    private String id;

    private String userName;

    private Integer age;

    private LocalDateTime createTime;
}

=== Dto ===
package com.xxx.modules.user.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysUser {
    
    private String id;

    private String userName;

    private Integer age;

    private LocalDateTime createTime;
}

=== Mapper ===
package com.xxx.modules.user.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.xxx.modules.user.entity.SysUser
import org.apache.ibatis.annotations.Mapper

@Mapper
interface SysUserMapper : BaseMapper<SysUser>

=== PlusService ===
package com.xxx.modules.user.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.xxx.modules.user.entity.SysUser
import com.xxx.modules.user.mapper.SysUserMapper
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
open class SysUserPlusService : ServiceImpl<SysUserMapper, SysUser>()

=== Service ===
package com.xxx.modules.user.service

import com.xxx.modules.user.entity.SysUser
import com.xxx.modules.user.service.plus.SysUserPlusService
import org.springframework.stereotype.Service

@Service
open class SysUserService(
    private val plusService: SysUserPlusService
) {

    fun findById(id: String): SysUser? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: SysUser): Boolean {
        return plusService.updateById(entity)
    }
}
```


