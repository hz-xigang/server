# MEMORY.md

## 项目决策与约束

- **xg-u8 模块保留**：用友 U8 ERP 对接模块，当前为开发阶段（销售订单接口开发中），后续会投入使用，**不要排除/移除该模块**。
- **构建环境**：项目要求 JDK 17。本机系统 `JAVA_HOME` 指向 JDK 8（`C:\Program Files (x86)\jdk`）会导致编译失败（报 "Couldn't find type org.springframework.stereotype.Component"）；需用 `D:\env\jvms\store\jdk17` 编译。
- 仓储模块 `mapper-locations` 配置为 `classpath:mapper/**/*.xml`，XML Mapper 文件需放在 `resources/mapper/` 下才会被加载。
