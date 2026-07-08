---
name: view-crud-skill
description: Generate MyBatis-Plus view model code from SQL Server CREATE VIEW statements. Use when the input is a database view whose name starts with v_, and you need to generate Java/Kotlin code for the project style in this repo: View class, Dto, MapStruct, and Mapper only. Especially use it when view fields come from multiple existing entities and field Java types must be inferred by matching SELECT aliases back to source entity fields.
---

# View CRUD Skill

## Overview

Generate code for SQL Server views in the `com.gz.xg` project style.

This skill is specialized for `CREATE VIEW` SQL, not ordinary tables. It only generates:
- View (Java)
- Dto (Java)
- MapStruct (Java)
- Mapper (Kotlin)

Do not generate `PlusService` or `Service`.

## Workflow

When the user provides a SQL view definition, follow this sequence:

1. Parse the view name and confirm it starts with `v_`.
2. Convert the view name to an entity class name by removing the `v_` prefix and adding leading `V`.
3. Parse the `SELECT` list in order and keep the alias names exactly as written.
4. For each selected field, locate the matching source entity field in the repo and reuse its Java type.
5. If multiple source entities are involved, resolve each alias against the entity implied by the source column.
6. Generate `View`, `Dto`, `MapStruct`, and `Mapper` only.
7. Preserve project style from nearby real files before writing code.

## Naming Rules

### View name

- SQL view names always start with `v_`.
- Keep the exact database object name in `@TableName("...")`.
- Example:
  - View name: `v_SysUserRole`
  - View class: `VSysUserRole`
  - Dto class: `VSysUserRoleDto`
  - MapStruct: `VSysUserRoleMapStruct`
  - Mapper: `VSysUserRoleMapper`

### Package rules

Base package is `com.gz.xg`.

- View: `com.gz.xg.domain.view`
- Dto: `com.gz.xg.domain.dto`
- MapStruct: `com.gz.xg.domain.mapstruct`
- Mapper: `com.gz.xg.mapper`

## Field Rules

### General field rules

- Java field names must match the final `SELECT` aliases exactly.
- Keep the `SELECT` column order in generated classes.
- Treat the view as read-oriented data. Do not invent extra fields.
- Do not include fields that are not present in the `SELECT`.

### Type inference rules

Do not infer view field types from SQL keywords alone when the source field already exists in the project.

For each `SELECT` item:

1. Identify the source table alias and source column.
2. Find the matching entity class for that source table in `src/main/java/com/gz/xg/domain/entity`.
3. Find the matching field in that entity.
4. Reuse that field's Java type in the generated view class and dto.

If multiple entities contain similarly named fields, prefer the entity that matches the SQL source table of that `SELECT` item.

If a source field cannot be found in existing entities, fall back to the SQL type mapping from `crud-skill`:

| SQL type | Java type |
|---|---|
| bigint | Long |
| int / tinyint | Integer |
| bit | Boolean |
| decimal | BigDecimal |
| varchar / char / text | String |
| datetime / timestamp | LocalDateTime |
| date | LocalDate |

### Primary key rules

Views often have no declared primary key.

- Do not invent an `id` field.
- Do not add `@TableId` unless the user explicitly requires a specific selected field to be treated as the key.
- If the view `SELECT` happens to contain a field named `id`, only add `@TableId` when the user explicitly wants it.

## View Rules

- Language: Java
- Package: `com.gz.xg.domain.view`
- Physical path: `src/main/java/com/gz/xg/domain/view`
- Annotations: `@Data` and `@TableName("viewName")`
- Use field comments only when they can be inherited with confidence from existing entities or were explicitly given by the user.
- Prefer concise imports and match existing project style.
- Do not generate Swagger annotations.
- Do not generate XML or custom query code.

## Dto Rules

- Language: Java
- Class name is `{ViewName}Dto`
- Match view fields and order exactly
- Do not add `deleted` unless it is explicitly present in the view `SELECT`

## MapStruct Rules

- Language: Java
- Use `@Mapper(componentModel = "spring")`
- Match current repo style:
  - import `org.mapstruct.factory.Mappers`
  - declare `INSTANCE = Mappers.getMapper(...)`
  - include single-object and list conversion methods
- Even though the main class is under `domain.view`, keep `toEntity` and `toEntityList` method names to align with current repo convention unless the user explicitly asks to rename them.

## Mapper Rules

- Language: Kotlin
- Use `@Mapper`
- Extend `BaseMapper<Entity>`
- Do not generate XML
- Do not generate custom methods
- Do not use `MPJBaseMapper` unless the user explicitly asks for that style

## Output Rules

Return code in this order:

=== View ===

=== Dto ===

=== MapStruct ===

=== Mapper ===

## Repository Alignment Rules

Before generating final code, inspect nearby real files in this repo when available:

- Existing source entities for field types and comments
- Existing dto files for comment style
- Existing mapstruct files for interface layout
- Existing mappers for Kotlin formatting

Prefer matching the actual repo over generic conventions if there is a conflict.

## Worked Example: `v_SysUserRole`

### Input SQL

```sql
CREATE VIEW [dbo].[v_SysUserRole] AS
SELECT
    SysRole.id AS roleId,
    SysRole.name AS roleName,
    SysRole.remark,
    SysUser.id AS userId,
    SysUser.username,
    SysUser.realName
FROM dbo.SysRole
INNER JOIN dbo.SysUserRole
    ON SysRole.id = SysUserRole.roleId
INNER JOIN dbo.SysUser
    ON SysUserRole.userId = SysUser.id
```

### Type resolution for this example

- `roleId` comes from `SysRole.id` -> use `SysRole.id` type -> `String`
- `roleName` comes from `SysRole.name` -> use `SysRole.name` type -> `String`
- `remark` comes from `SysRole.remark` -> use `SysRole.remark` type -> `String`
- `userId` comes from `SysUser.id` -> use `SysUser.id` type -> `String`
- `username` comes from `SysUser.username` -> use `SysUser.username` type -> `String`
- `realName` comes from `SysUser.realName` -> use `SysUser.realName` type -> `String`

### Expected output shape

```text
=== View ===
package com.gz.xg.domain.view;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("v_SysUserRole")
public class VSysUserRole {

    private String roleId;

    private String roleName;

    private String remark;

    private String userId;

    private String username;

    private String realName;
}

=== Dto ===
package com.gz.xg.domain.dto;

import lombok.Data;

@Data
public class VSysUserRoleDto {

    private String roleId;

    private String roleName;

    private String remark;

    private String userId;

    private String username;

    private String realName;
}

=== MapStruct ===
package com.gz.xg.domain.mapstruct;

import com.gz.xg.domain.dto.VSysUserRoleDto;
import com.gz.xg.domain.view.VSysUserRole;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VSysUserRoleMapStruct {

    VSysUserRoleMapStruct INSTANCE = Mappers.getMapper(VSysUserRoleMapStruct.class);

    VSysUserRoleDto toDto(VSysUserRole entity);

    VSysUserRole toEntity(VSysUserRoleDto dto);

    List<VSysUserRoleDto> toDtoList(List<VSysUserRole> list);

    List<VSysUserRole> toEntityList(List<VSysUserRoleDto> list);
}

=== Mapper ===
package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.gz.xg.domain.view.VSysUserRole
import org.apache.ibatis.annotations.Mapper

@Mapper
interface VSysUserRoleMapper : BaseMapper<VSysUserRole>
```

## Hard Constraints

- Do not generate `Service`
- Do not generate `PlusService`
- Do not generate `Controller`
- Do not generate XML
- Do not generate custom mapper methods
- Do not rename aliases into another naming style
- Do not drop selected fields because they look duplicated
- Do not generate the view class under `domain.entity`
