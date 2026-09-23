# Global Working Rules

默认使用简体中文沟通；代码、命令、路径和技术术语保持原文。

## Terminal

- **Windows Shell 默认使用 PowerShell 7（`pwsh`）。**
- 执行 Windows 终端命令时，必须优先通过 `pwsh` 执行。
- **禁止默认使用 Windows PowerShell 5.1（`powershell.exe`）。**
- 只有用户明确要求兼容 PowerShell 5.1，或某个特定工具明确要求 5.1 时，才允许使用 `powershell.exe`。



## Tool Strategy

- **工具禁令与替换**：严禁使用内置 `Grep` 工具。所有文本/正则搜索、关键字查找，必须通过 `bash` 调用 `rg` (Ripgrep) 执行。
- 代码架构、调用链、跨文件行为、Bug 定位或修改前影响分析，优先使用代码图工具；简单符号/源码上下文优先 **CodeGraph**，已索引仓库的影响面、执行流程、API、路由、安全或跨仓库分析优先 **GitNexus**。
- 大范围文本/正则搜索、计数、文件列表、未索引或索引缺失场景下的快速定位，优先使用 **Ripgrep (rg)**；其尊重 `.gitignore`、跳过隐藏/二进制、并行遍历的默认行为远快于内置 Grep，适合作为前置过滤。
- 浏览器 UI、DOM、Console、Network、Storage、响应式布局或性能问题，使用 **Chrome DevTools**。
- 已知精确路径的 1-3 个文件读取、简单配置和非代码小任务，直接使用内置 Read、Glob 即可；涉及内容搜索时仍优先 `rg` 而非内置 Grep。
- 不要默认同时调用两个 MCP；只使用当前问题需要的工具；`rg` 为本地 CLI（bash `rg`），可与任一 MCP 并行使用，无需互斥。
- 如果 CodeGraph 和 GitNexus 都适用，先选更贴近任务目标的一个；只有结果不足、索引缺失或需要互相验证时才切换到另一个，并说明原因。

## Subagent Dispatch

- 在任务明显符合专用 subagent 的职责时主动派发，不等待用户点名；主 agent 负责整合结果、最终验证和面向用户的答复。
- 派发判定门槛：开始非平凡任务前先判断是否存在明确匹配的专用 agent；如果匹配且任务需要多步操作，先划分工作单元、依赖关系和文件所有权，再由主 agent 派发并整合，不要先自行完成同一份工作。
- 派发依据是任务边界和专长，不是当前可用模型数量；没有明确收益时直接由当前 agent 完成。
- **并行是独立工作单元的默认执行方式**：所有当前已知、互不依赖且不会修改同一批文件的工作单元，必须在同一个 assistant 消息中同时发出多个 `Task` 工具调用（single message with multiple tool calls）；禁止先等待一个 Task 完成，再派发另一个早已确定为独立的 Task。
- 按依赖图分波次执行：只串行化真实依赖链；每个前置结果返回后，应立即在同一轮并行派发全部新解锁的独立单元，而不是逐个派发。
- 前后端等跨层任务应先由主 agent 固定最小共享契约（接口、数据形状、文件边界），随后并行派发，例如用 `general` 实现后端、用 `frontend` 实现前端，最后由主 agent 做集成与契约验证。只有前端确实无法在契约或 mock 下推进时才等待后端实现。
- 通常并行派发 2-4 个边界清晰的 agent；不要为追求数量拆分琐碎工作，也不要让多个 agent 修改同一文件、同一符号或同一职责范围。
- `explore`：用于未知位置、跨多个目录或需要多轮搜索的代码库探索。已知具体文件、单个符号或只需读取 2 到 3 个文件时直接使用 Read、Glob、rg，不要派发。
- `scout`：用于官方文档、第三方依赖源码、上游实现和外部行为核对。优先要求返回官方或一手来源，并保持只读。
- `frontend`：用于非平凡的前端实现、CSS/Tailwind、响应式布局、设计系统和视觉交互打磨。应要求遵循现有视觉语言，并在可行时使用 Chrome DevTools 验证桌面与移动端。
- `reviewer`：在完成较大、跨模块、高风险或用户明确要求评审的改动后主动调用，进行独立只读审查；简单单文件改动不必机械调用。
- `coder`：用于简单、快速、边界清晰的编码任务（小改动、单文件或少量文件、机械性实现）。不要把复杂多步骤、跨模块或架构性工作交给 `coder`。
- `general`：用于复杂多步骤任务，或可与当前工作并行且边界清晰的独立实现单元。不要把小型直接修改交给 `general`，改派 `coder`。
- `plan`：只委托 `explore`、`scout`、`reviewer` 等只读角色；不要让 Plan 通过 subagent 间接修改项目文件。
- 多个前台 Task 在同一消息中调用也会并行；“前台”只表示主 agent 等待这一批结果，不表示必须逐个执行。只有任务可独立运行且主流程无需等待时才使用后台模式，并遵守后台任务的实验开关和不要轮询规则。
- 派发后不要重复子 agent 已承担的工作；主 agent 只继续处理不重叠的单元、准备集成，或等待并行结果。
- 每次派发都给出完整上下文、明确说明是研究还是写代码、期望返回内容、文件边界和验证命令；结果需要继续追问时复用原 task session。
- 子 agent 返回后由主 agent 判断、合并和验证，不直接把未经核对的结论或改动交给用户。
- 子 agent 返回“达到最大步骤数”、仅有进展汇总、修改未落盘或验收尚未完成时，一律视为未完成；主 agent 必须在当前用户回合立即复用原 `task_id` 续跑，并在续跑 prompt 中只列剩余工作和验证要求，不得等待用户再次要求。
- 子 agent 因瞬时错误或步骤限制中断时持续重试，优先复用 `task_id`；续跑仍失败则由主 agent 接管剩余工作，或在边界清晰时重新派发，除非确认永久阻塞。

## CodeGraph

- 将 CodeGraph 作为快速获取相关符号源码、调用路径和局部结构上下文的首选工具，尤其适合“先看懂这段代码/这个 symbol/这个文件”的场景。
- 始终传入目标项目内的绝对 `projectPath`，查询应包含符号名、文件名、路由或简短流程描述。
- CodeGraph 返回的源码视为已读取，不要重复打开；只补读未覆盖的文件或上下文。
- 如果结果过宽，使用更具体的路径和符号重试一次，然后用内置工具补足。
- 全局启动插件会为 Git 仓库和带常见项目清单的目录自动初始化、同步 CodeGraph；不要仅因索引暂时不可用就直接回退到文本搜索。
- MCP 报告索引缺失时，主动运行 `codegraph init "<project-root>"`；已有索引先运行 `codegraph status "<project-root>" --json`，普通改动用 `codegraph sync "<project-root>" --quiet`，`reindexRecommended`、提取器版本变化或 worktree 不匹配时用 `codegraph index "<project-root>" --quiet` 完整重建。
- `codegraph init` 是当前本机 CLI 首次创建 `.codegraph/` 的必要命令；全局插件只在 OpenCode 已识别出的项目目录中调用，并跳过用户主目录和磁盘根目录。
- 只有 CodeGraph 创建/重建失败、项目不适合索引，或任务只是精确字符串/已知文件读取时，才回退到内置工具。

## GitNexus

- 将 GitNexus 作为已索引仓库的深度图分析工具：执行流程、影响面、调用链、API 路由/响应形状、工具定义、结构检查、改动影响、跨仓库 contract/group 分析优先使用它。
- 第一次使用或不确定仓库名时先用 `list_repos` 或读取 `gitnexus://repos`；当存在多个索引仓库时，后续 GitNexus 调用必须显式传 `repo`。
- 理解代码流用 `query`；查看单个符号的 360 度上下文用 `context`；修改共享函数、类、接口或字段前用 `impact`；提交前或评审本地改动用 `detect_changes`。
- 修改 API route handler 前优先用 `api_impact`；排查前端/后端契约漂移用 `route_map` 和 `shape_check`；查 MCP/RPC 工具实现用 `tool_map`。
- 需要精确路径时用 `trace`；需要复杂结构查询时先读取 `gitnexus://repo/{name}/schema`，再用 `cypher`；不要把 raw Cypher 当作默认入口。
- 安全数据流、taint、控制/数据依赖只在仓库使用 `gitnexus analyze --pdg` 建索引后可用；这类任务用 `explain` 或 `pdg_query`，没有 PDG layer 时不要把空结果当成安全结论。
- GitNexus 的 `rename` 会改文件；除非用户明确要求重命名，否则只用 dry-run/预览或改用只读分析工具。
- 全局启动插件会对 Git 仓库幂等执行 `gitnexus analyze "<project-root>" --index-only --worker-timeout 120`，自动创建缺失索引并增量更新过期索引；索引尚在后台构建时应等待或按同一命令前台完成，再调用 GitNexus MCP。
- 只要需要创建或更新 GitNexus 索引，必须使用 `gitnexus analyze "<project-root>" --index-only --worker-timeout 120`。禁止使用会生成或更新 `AGENTS.md`、`CLAUDE.md`、skills、agent/context 文件的索引路径；如果无法保证 `index-only`，直接回退到内置工具或 CodeGraph，不要继续尝试写入式索引。
- 不要把 `--skip-agents-md --skip-skills` 当作默认方案；它只是历史兼容写法。当前规范以 `--index-only` 为唯一允许的索引模式。
- 不要自行运行 `gitnexus setup`、`clean`、`group sync` 或 `--force` 完整重建，除非用户明确要求；缺失或普通过期索引不受此限制，应主动用增量 `analyze --index-only` 维护。

## Ripgrep

- 将 Ripgrep (`rg`) 作为大范围文本/正则搜索的首选 CLI，而非内置 Grep；适用于未索引仓库、索引缺失/过期、需要计数/列表/快速粗筛的场景。当前环境已安装 `ripgrep 15.2.0`，通过 `bash` 以 `rg` 调用（MCP 之外，可与 CodeGraph/GitNexus 并行使用）。
- 核心优势（对标 `grep`/`ack`/`ag`）：默认递归、尊重 `.gitignore`/`.ignore`/`.rgignore`、跳过隐藏文件/二进制/符号链接、并行遍历、基于 Rust 有限自动机保证线性最坏时间、Unicode 默认开启且性能稳定；需环视/反向引用时加 `--pcre2`（`rg --pcre2`）。
- 优先用 `rg` 替代内置 Grep 的场景：跨多目录/全仓关键字/正则扫描、文件清单过滤、出现次数计数、定位后再用 CodeGraph/GitNexus 做精细结构分析（`rg` 粗筛 → 图工具精读）。
- 常用形态（均在 `bash` 中执行）：
  - 基础搜索：`rg -n "pattern" [path]`；只列文件：`rg -l "pattern"`；计数：`rg -c "pattern"`；文件列表：`rg --files | rg "name"` 或 `rg --files -g "*.ts"`
  - 类型/Glob 过滤：`rg -t ts "pattern"` / `rg -g "*.{ts,tsx}" "pattern"` / `rg --type-not test "pattern"`
  - 忽略规则控制：默认已过滤 ignore/隐藏/二进制；需包含时显式加 `--no-ignore` / `--hidden` / `--binary` / `-u`(`--unrestricted`)
  - 多行/编码/压缩：`rg -U --multiline "a.*b"` / `rg --encoding utf-8` / `rg -z`（搜压缩包）
  - 只在图工具前做“定位”时，配合 `--max-count` / `--max-columns` / `head` 控制输出规模，避免超长上下文。
- 与 CodeGraph/GitNexus 的协作：`rg` 不替代符号图；当需要调用链/影响面/执行流时，先用 `rg` 缩小文件范围，再用 `codegraph_explore` / `query`/`impact` 精确展开，避免全量 Grep + 多轮 Read。
- 约束：除 `rg` 不可用或任务仅需读取 1-3 个已知精确路径文件外，不使用内置 Grep；需要精确字符串且已知文件时直接 Read。

## Chrome DevTools

- 前端问题在可行时先复现，再根据页面快照、Console、Network 和真实 DOM 状态判断原因。
- 优先等待可观察状态，不使用无依据的固定延时。
- 修复后重复原失败流程，并检查相关 Console 与 Network；视觉改动同时验证桌面和移动视口。
- 仅在性能任务中采集性能 trace，不默认执行宽泛审计。
- 不输出凭据、Cookie、Token 或敏感响应内容；未经明确授权不修改生产数据。

## Fallback

- MCP 不可用时使用内置工具继续，并仅在影响结论可信度或造成阻塞时说明。
- 工具证据不能替代 diff 检查、相关测试和必要的浏览器回归验证。
