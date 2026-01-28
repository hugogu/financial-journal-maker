# Feature Specification: AI会计流程设计平台 (AI Accounting Designer)

**Feature Branch**: `001-ai-accounting-designer`  
**Created**: 2026-01-28  
**Status**: Draft  
**Input**: 公司内部会计流程设计辅助平台，通过AI分析业务场景生成会计科目设计和记账规则

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 业务场景AI分析 (Priority: P1)

财务人员进入平台，描述一个新的业务场景（如"电商平台用户充值"），AI分析该场景的资金流和信息流，生成初始的会计科目设计、记账规则和三层模型（订单→交易→账务）。用户可以查看AI的分析结果并进行下一步操作。

**Why this priority**: 这是平台的核心价值主张——通过AI辅助完成会计流程设计的初始分析，是所有后续功能的基础。

**Independent Test**: 用户输入业务场景描述，系统返回结构化的会计科目建议和记账规则，可独立验证AI分析能力。

**Acceptance Scenarios**:

1. **Given** 用户在平台首页, **When** 用户输入业务场景描述"电商平台用户充值", **Then** AI返回包含会计科目、记账规则、订单-交易-账务三层模型的初始设计方案
2. **Given** AI已生成初始设计, **When** 用户查看设计详情, **Then** 系统展示每个科目的用途说明、借贷方向、适用场景
3. **Given** AI分析完成, **When** 用户请求生成Numscript, **Then** 系统输出符合Formance Ledger规范的Numscript DSL代码

---

### User Story 2 - 设计修改与AI验证 (Priority: P2)

用户对AI生成的会计科目或记账规则进行修改（如调整科目名称、添加新科目、修改借贷规则），AI实时验证修改的合理性，检测潜在冲突或不一致，并给出改进建议。

**Why this priority**: 用户需要根据实际业务需求调整AI建议，AI验证确保修改后的设计仍然合理有效。

**Independent Test**: 用户修改任意科目属性，系统立即反馈验证结果和建议，可独立验证人机协作流程。

**Acceptance Scenarios**:

1. **Given** 用户正在查看AI生成的科目设计, **When** 用户修改某科目的借贷方向, **Then** AI验证修改并提示是否存在借贷不平衡风险
2. **Given** 用户添加新的会计科目, **When** 科目名称与现有科目冲突, **Then** 系统提示冲突并建议合并或重命名
3. **Given** 用户完成一系列修改, **When** 用户请求AI建议下一步, **Then** AI根据当前设计状态推荐最合适的后续操作

---

### User Story 3 - 现有科目导入 (Priority: P3)

用户上传公司现有的会计科目表文件，系统解析并导入，作为后续场景设计的基础科目库。新的场景设计将优先复用已导入的科目。

**Why this priority**: 企业通常有既定的会计科目体系，支持导入可大幅提升平台实用性。

**Independent Test**: 用户上传科目表文件，系统成功解析并展示导入结果，可独立验证导入功能。

**Acceptance Scenarios**:

1. **Given** 用户在科目管理页面, **When** 用户上传Excel格式的科目表, **Then** 系统解析文件并展示待导入的科目列表供确认
2. **Given** 导入过程中发现格式错误, **When** 某行数据不符合规范, **Then** 系统标记错误行并允许用户修正后继续
3. **Given** 科目导入完成, **When** 用户创建新场景设计, **Then** AI优先推荐使用已导入的科目

---

### User Story 4 - 分层设计管理 (Priority: P4)

用户可以按产品→场景→类型的层级结构组织设计，底层科目和规则可跨产品/场景共享。在某一层级修改共享组件时，系统提示影响范围。

**Why this priority**: 层级结构和共享机制确保设计一致性，是企业级应用的关键特性。

**Independent Test**: 用户创建多个产品/场景，验证共享科目的变更能正确传播到所有引用处。

**Acceptance Scenarios**:

1. **Given** 用户已创建"电商"产品下的多个场景, **When** 用户修改共享的"用户余额"科目, **Then** 系统列出所有受影响的场景并请求确认
2. **Given** 用户正在设计"退款"场景, **When** 场景需要使用"充值"场景已定义的科目, **Then** 系统自动推荐复用并显示关联关系

---

### User Story 5 - Numscript导出与验证 (Priority: P5)

用户可以将完成的设计导出为Formance Ledger兼容的Numscript DSL代码，系统在导出前进行语法和语义验证。

**Why this priority**: Numscript是平台核心输出格式，确保输出可直接用于下游系统。

**Independent Test**: 导出任意完成的设计为Numscript，验证语法正确性。

**Acceptance Scenarios**:

1. **Given** 用户完成某场景的设计, **When** 用户点击"导出Numscript", **Then** 系统生成符合Formance规范的DSL代码并提供下载
2. **Given** 设计中存在未定义的科目引用, **When** 用户尝试导出, **Then** 系统阻止导出并提示需要补充的定义

---

### User Story 6 - LLM模型配置 (Priority: P6)

管理员可以配置第三方AI API连接信息和可用的LLM模型列表，用户可以在使用时选择不同的模型。

**Why this priority**: 支持多模型选择增加灵活性，但非核心业务功能。

**Independent Test**: 配置API密钥后，用户可选择不同模型进行分析，验证模型切换功能。

**Acceptance Scenarios**:

1. **Given** 管理员在设置页面, **When** 管理员配置OpenAI API密钥和可用模型列表, **Then** 配置被保存且用户可在下拉框中选择模型
2. **Given** 用户开始新的分析, **When** 用户选择不同的LLM模型, **Then** 系统使用所选模型完成分析

---

### Edge Cases

- 用户输入的业务场景描述过于模糊或缺乏关键信息时，AI应请求补充信息而非生成低质量结果
- 导入的科目表包含重复科目编码时，系统应提供合并或覆盖选项
- AI API调用失败或超时时，系统应提供友好的错误提示和重试选项
- 用户删除被其他设计引用的共享科目时，系统应阻止删除并显示依赖列表
- Numscript生成时遇到无法映射的复杂规则，系统应标记为手动处理项

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统MUST接受自然语言描述的业务场景并调用AI进行分析
- **FR-002**: 系统MUST生成包含会计科目、记账规则的结构化设计方案
- **FR-003**: 系统MUST支持订单→交易→账务三层模型的设计和展示
- **FR-004**: 用户MUST能够修改AI生成的任何设计元素（科目、规则、模型）
- **FR-005**: 系统MUST在用户修改后实时进行AI验证并反馈
- **FR-006**: 系统MUST支持从文件导入现有会计科目表
- **FR-007**: 系统MUST按产品→场景→类型的层级组织设计
- **FR-008**: 系统MUST支持底层科目和规则的跨层级共享
- **FR-009**: 系统MUST生成Formance Ledger兼容的Numscript DSL
- **FR-010**: 系统MUST在导出Numscript前进行语法验证
- **FR-011**: 系统MUST支持配置第三方AI API连接
- **FR-012**: 用户MUST能够选择可用的LLM模型
- **FR-013**: 系统MUST根据当前状态给出下一步操作建议
- **FR-014**: 系统MUST追踪设计元素间的依赖关系
- **FR-015**: 系统MUST在修改共享元素时提示影响范围

### Key Entities

- **会计科目 (Account)**: 会计核算的基本单位，包含科目编码、名称、类型（资产/负债/权益/收入/费用）、借贷方向、层级关系
- **记账规则 (Booking Rule)**: 定义特定业务场景下的借贷分录模板，关联触发条件、借方科目、贷方科目、金额计算逻辑
- **产品 (Product)**: 业务产品线的顶层组织单元，如"电商平台"、"支付系统"
- **场景 (Scenario)**: 产品下的具体业务场景，如"用户充值"、"订单退款"
- **类型 (Type)**: 场景下的细分类型，如"银行卡充值"、"余额充值"
- **订单 (Order)**: 业务层面的交易请求，包含业务属性和状态流转
- **交易 (Transaction)**: 订单产生的资金流动记录，关联支付渠道和金额
- **账务 (Accounting Entry)**: 交易产生的会计分录，包含借贷科目和金额
- **设计项目 (Design Project)**: 用户创建的设计工作空间，包含产品-场景-类型层级结构

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 用户从输入业务描述到获得初始AI分析结果的时间不超过30秒
- **SC-002**: AI生成的初始设计方案被用户采纳（无需大幅修改）的比例达到70%以上
- **SC-003**: 导入包含500个科目的Excel文件耗时不超过10秒
- **SC-004**: 系统生成的Numscript代码100%通过Formance Ledger语法验证
- **SC-005**: 80%的用户能够在首次使用时独立完成一个完整场景的设计
- **SC-006**: 用户修改后AI验证响应时间不超过5秒
- **SC-007**: 系统支持同时管理至少10个产品、每产品50个场景的设计规模

## Assumptions

- 用户具备基本的会计知识，了解借贷记账法的基本原理
- 公司已有Formance Ledger或兼容系统作为下游记账系统
- 第三方AI API（如OpenAI）可用且响应稳定
- 导入的科目表文件格式为Excel（.xlsx）或CSV
- 第一版不包含用户账户管理，所有用户共享同一工作空间
