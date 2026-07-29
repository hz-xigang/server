# XG-WMS Windows Server 生产环境部署指南

## 📋 前置条件

- Windows Server 2016 或更高版本
- SQL Server 2016 或更高版本
- Java 17 或更高版本
- 管理员权限

## 🚀 快速部署步骤

### 1. 创建部署目录

```powershell
# 创建应用目录
New-Item -Path "E:\AppData\xg" -ItemType Directory -Force
New-Item -Path "E:\AppData\xg\logs" -ItemType Directory -Force
New-Item -Path "E:\AppData\xg\upload" -ItemType Directory -Force
New-Item -Path "E:\AppData\xg\backup" -ItemType Directory -Force

# 设置目录权限（可选）
icacls "E:\AppData\xg" /grant "NETWORK SERVICE:(OI)(CI)F" /T
```

### 2. 创建数据库专用账号

```sql
-- 连接到 SQL Server Management Studio
-- 不要使用 sa 账号运行应用！

-- 创建登录账号
CREATE LOGIN xg_app_user WITH PASSWORD = 'XG@Prod#2024!Strong';

-- 切换到应用数据库
USE xg_wms;

-- 创建数据库用户
CREATE USER xg_app_user FOR LOGIN xg_app_user;

-- 授予必要权限（只给数据操作权限，不给DDL权限）
GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::dbo TO xg_app_user;

-- 如果需要执行存储过程
GRANT EXECUTE ON SCHEMA::dbo TO xg_app_user;

-- 验证权限
SELECT dp.name, dp.type_desc, o.name
FROM sys.database_permissions AS p
JOIN sys.database_principals AS dp ON p.grantee_principal_id = dp.principal_id
LEFT JOIN sys.objects AS o ON p.major_id = o.object_id
WHERE dp.name = 'xg_app_user';
```

### 3. 配置环境变量

使用 PowerShell **以管理员身份运行**：

```powershell
# 数据库配置
[System.Environment]::SetEnvironmentVariable('DB_HOST', '生产数据库服务器IP', 'Machine')
[System.Environment]::SetEnvironmentVariable('DB_PORT', '1433', 'Machine')
[System.Environment]::SetEnvironmentVariable('DB_NAME', 'xg_wms', 'Machine')
[System.Environment]::SetEnvironmentVariable('DB_USERNAME', 'xg_app_user', 'Machine')
[System.Environment]::SetEnvironmentVariable('DB_PASSWORD', 'XG@Prod#2024!Strong', 'Machine')

# 生成强随机 JWT 密钥（256位）
$bytes = New-Object byte[] 32
[Security.Cryptography.RNGCryptoServiceProvider]::Create().GetBytes($bytes)
$jwtSecret = [Convert]::ToBase64String($bytes)
[System.Environment]::SetEnvironmentVariable('JWT_SECRET', $jwtSecret, 'Machine')
Write-Host "JWT_SECRET已生成并设置: $jwtSecret"

# JWT 过期时间（分钟）
[System.Environment]::SetEnvironmentVariable('JWT_EXPIRATION_MINUTES', '480', 'Machine')

# 服务器端口
[System.Environment]::SetEnvironmentVariable('SERVER_PORT', '8080', 'Machine')

# 文件存储路径
[System.Environment]::SetEnvironmentVariable('STATIC_BASE_PATH', 'E:\\AppData\\xg\\upload\\', 'Machine')

# 验证环境变量
Write-Host "`n已配置的环境变量："
[System.Environment]::GetEnvironmentVariable('DB_HOST', 'Machine')
[System.Environment]::GetEnvironmentVariable('DB_USERNAME', 'Machine')
[System.Environment]::GetEnvironmentVariable('SERVER_PORT', 'Machine')
[System.Environment]::GetEnvironmentVariable('STATIC_BASE_PATH', 'Machine')

Write-Host "`n请重启 PowerShell 或注销重新登录以使环境变量生效！"
```

### 4. 部署应用文件

```powershell
# 将 JAR 包复制到服务器
Copy-Item "xg-app.jar" -Destination "E:\AppData\xg\xg-app.jar"

# 验证文件
Get-Item "E:\AppData\xg\xg-app.jar" | Format-List
```

### 5. 手动启动测试

```powershell
cd E:\AppData\xg

# 启动应用（生产环境）
java -jar xg-app.jar --spring.profiles.active=prod

# 如果需要指定日志路径
java -jar xg-app.jar --spring.profiles.active=prod --logging.file.path=E:\AppData\xg\logs
```

访问测试：`http://服务器IP:8080`

### 6. 安装为 Windows 服务（推荐）

#### 方法 A：使用 NSSM（推荐）

```powershell
# 1. 下载 NSSM
# 访问 https://nssm.cc/download
# 下载 nssm-2.24.zip，解压到 C:\Tools\nssm

# 2. 安装服务
cd C:\Tools\nssm\win64
.\nssm.exe install XG-WMS "C:\Program Files\Java\jdk-17\bin\java.exe"

# 3. 配置服务参数
.\nssm.exe set XG-WMS AppParameters "-jar E:\AppData\xg\xg-app.jar --spring.profiles.active=prod"
.\nssm.exe set XG-WMS AppDirectory "E:\AppData\xg"
.\nssm.exe set XG-WMS DisplayName "XG-WMS 仓库管理系统"
.\nssm.exe set XG-WMS Description "西港仓库管理系统后端服务"
.\nssm.exe set XG-WMS Start SERVICE_AUTO_START

# 4. 配置日志重定向
.\nssm.exe set XG-WMS AppStdout "E:\AppData\xg\logs\service-stdout.log"
.\nssm.exe set XG-WMS AppStderr "E:\AppData\xg\logs\service-stderr.log"

# 5. 配置日志轮转（可选）
.\nssm.exe set XG-WMS AppRotateFiles 1
.\nssm.exe set XG-WMS AppRotateOnline 1
.\nssm.exe set XG-WMS AppRotateSeconds 86400  # 每天轮转
.\nssm.exe set XG-WMS AppRotateBytes 10485760 # 10MB

# 6. 启动服务
.\nssm.exe start XG-WMS

# 7. 检查服务状态
.\nssm.exe status XG-WMS
Get-Service XG-WMS | Format-List

# 8. 查看日志
Get-Content "E:\AppData\xg\logs\service-stdout.log" -Tail 50

# 其他 NSSM 命令
# 停止服务: .\nssm.exe stop XG-WMS
# 重启服务: .\nssm.exe restart XG-WMS
# 卸载服务: .\nssm.exe remove XG-WMS confirm
```

#### 方法 B：使用 WinSW

```powershell
# 1. 下载 WinSW
# https://github.com/winsw/winsw/releases
# 下载 WinSW-x64.exe，重命名为 xg-wms-service.exe

# 2. 创建配置文件 xg-wms-service.xml
@"
<service>
  <id>XG-WMS</id>
  <name>XG-WMS 仓库管理系统</name>
  <description>西港仓库管理系统后端服务</description>
  <executable>C:\Program Files\Java\jdk-17\bin\java.exe</executable>
  <arguments>-jar E:\AppData\xg\xg-app.jar --spring.profiles.active=prod</arguments>
  <workingdirectory>E:\AppData\xg</workingdirectory>
  <logpath>E:\AppData\xg\logs</logpath>
  <logmode>roll-by-size</logmode>
  <onfailure action="restart" delay="10 sec"/>
  <onfailure action="restart" delay="20 sec"/>
  <onfailure action="reboot"/>
  <startmode>Automatic</startmode>
</service>
"@ | Out-File -FilePath "E:\AppData\xg\xg-wms-service.xml" -Encoding UTF8

# 3. 安装服务
cd E:\AppData\xg
.\xg-wms-service.exe install

# 4. 启动服务
.\xg-wms-service.exe start

# 5. 查看状态
.\xg-wms-service.exe status

# 其他命令
# 停止: .\xg-wms-service.exe stop
# 卸载: .\xg-wms-service.exe uninstall
```

### 7. 配置防火墙

```powershell
# 允许应用端口（如 8080）
New-NetFirewallRule -DisplayName "XG-WMS HTTP" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow

# 查看规则
Get-NetFirewallRule -DisplayName "XG-WMS HTTP" | Format-List
```

### 8. 配置 IIS 反向代理（可选）

如果需要通过 80/443 端口访问：

```powershell
# 安装 URL Rewrite 和 ARR 模块
# 1. 下载安装 URL Rewrite: https://www.iis.net/downloads/microsoft/url-rewrite
# 2. 下载安装 ARR: https://www.iis.net/downloads/microsoft/application-request-routing

# 在 IIS 中配置反向代理规则
# web.config 示例：
@"
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <system.webServer>
        <rewrite>
            <rules>
                <rule name="ReverseProxyInboundRule1" stopProcessing="true">
                    <match url="(.*)" />
                    <action type="Rewrite" url="http://localhost:8080/{R:1}" />
                </rule>
            </rules>
        </rewrite>
    </system.webServer>
</configuration>
"@ | Out-File -FilePath "C:\inetpub\wwwroot\web.config" -Encoding UTF8
```

## 🔍 监控和维护

### 查看应用日志

```powershell
# 实时查看日志
Get-Content "E:\AppData\xg\logs\spring.log" -Wait -Tail 50

# 查看服务日志
Get-Content "E:\AppData\xg\logs\service-stdout.log" -Tail 100
```

### 查看服务状态

```powershell
Get-Service XG-WMS | Format-List
Get-Process java | Where-Object {$_.Path -like "*xg-app.jar*"}
```

### 重启应用

```powershell
# 如果使用 NSSM
nssm restart XG-WMS

# 或使用 Windows 服务管理
Restart-Service XG-WMS
```

### 数据库备份

```powershell
# 创建备份脚本 E:\AppData\xg\backup\backup-db.ps1
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupPath = "E:\AppData\xg\backup\xg_wms_$timestamp.bak"

sqlcmd -S localhost -U sa -P "your_sa_password" -Q "BACKUP DATABASE xg_wms TO DISK='$backupPath' WITH COMPRESSION"

# 删除7天前的备份
Get-ChildItem "E:\AppData\xg\backup\*.bak" | Where-Object {$_.LastWriteTime -lt (Get-Date).AddDays(-7)} | Remove-Item

# 设置定时任务（每天凌晨2点）
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-File E:\AppData\xg\backup\backup-db.ps1"
$trigger = New-ScheduledTaskTrigger -Daily -At 2am
Register-ScheduledTask -TaskName "XG-WMS数据库备份" -Action $action -Trigger $trigger -User "SYSTEM" -RunLevel Highest
```

## ⚠️ 安全检查清单

- [ ] 数据库使用专用账号，不是 sa
- [ ] JWT 密钥已随机生成且足够强
- [ ] 数据库密码强度符合要求（大小写+数字+特号）
- [ ] 环境变量已设置为系统级（Machine），不是用户级
- [ ] 文件上传目录权限正确
- [ ] 防火墙规则已配置
- [ ] 数据库定期备份已配置
- [ ] 日志轮转已配置
- [ ] SSL 证书已配置（如使用 HTTPS）
- [ ] application-local.yaml 已加入 .gitignore

## 🆘 故障排查

### 应用启动失败

```powershell
# 检查 Java 版本
java -version

# 检查环境变量
[System.Environment]::GetEnvironmentVariable('DB_PASSWORD', 'Machine')

# 查看启动日志
Get-Content "E:\AppData\xg\logs\spring.log" -Tail 100

# 测试数据库连接
sqlcmd -S $env:DB_HOST -U $env:DB_USERNAME -P $env:DB_PASSWORD -Q "SELECT @@VERSION"
```

### 端口被占用

```powershell
# 查看端口占用
netstat -ano | findstr :8080

# 查看进程
Get-Process -Id <PID>

# 结束进程
Stop-Process -Id <PID> -Force
```

### 服务无法启动

```powershell
# 查看 Windows 事件日志
Get-EventLog -LogName Application -Source "XG-WMS" -Newest 10

# 查看服务状态
Get-Service XG-WMS | Format-List

# 手动启动测试
cd E:\AppData\xg
java -jar xg-app.jar --spring.profiles.active=prod
```

## 📞 联系支持

如遇问题，请收集以下信息：
- Windows Server 版本：`systeminfo | findstr /B /C:"OS Name" /C:"OS Version"`
- Java 版本：`java -version`
- 应用日志：`E:\AppData\xg\logs\spring.log`
- 服务日志：`E:\AppData\xg\logs\service-stdout.log`
- 环境变量：`Get-ChildItem Env: | Where-Object {$_.Name -like "*DB*" -or $_.Name -like "*JWT*"}`
