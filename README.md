# NextAI

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Android API](https://img.shields.io/badge/Android%20API-31%2B-brightgreen.svg)](https://android-arsenal.com/api?level=31)

NextAI是一款基于 Ktolin 开发放的轻量级 AI 聊天客户端，专注于提供灵活、交互友好的 AI 对话体验。

## 已支持的功能

- **自定义模型接入**
  - 遵循 OpenAI API 标准协议，支持配置私有化模型服务。仅需填写 API 地址和 API Key，即可快速接入并选择所需模型。
- **流式响应 (Streaming)**
  - 基于 AST 递归解析策略，实现 Markdown 文本的块级增量解析渲染。
  - 支持 LaTex 行内与块级公式的实时解析与精准呈现。
- **多会话并行架构**
  - 采用独立协程域设计，各会话任务完全隔离运行。支持用户在多个会话间无缝切换，互不阻塞。。
- **会话管理**
  - 基于时间维度的分组管理，支持会话批量操作与置顶功能，帮助用户高效整理会话。
- **性能优化**
  - 引入消息惰性分页加载机制，仅在切换会话时按需从数据库拉取数据，并在滚动触底前预加载下一页。结合解析节点的多级缓存策略，显著提升渲染效率与阅读流畅度。

## 待跟进的功能
- 支持思考内容展示
- 会话内消息相关信息展示（token消耗量、耗时、输出模型等）
- 会话内消息的删改、便捷复制、重新生成等操作。
- 会话内上一消息或下一消息的快速定位。
- 消息搜索及定位。
- 会话内系统提示词设置、模型切换和模型参数调整。
- 默认配置管理（模型提供方、模型、模型参数等）。
- 更多功能性设置，如流式生开启/关闭、流式生成时的滚动速度调整等。

## Demo 预览
[![Video]()](https://github.com/user-attachments/assets/e334fe56-8c06-41c8-bf14-b56aae77c5db)

## 协议

MIT License

```text
Copyright (c) 2021 Jeziel Lago

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
