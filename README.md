# NextAI

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Android API](https://img.shields.io/badge/Android%20API-31%2B-brightgreen.svg)](https://android-arsenal.com/api?level=31)

NextAI is a lightweight AI chat client developed based on Kotlin, focusing on providing a flexible and interactive AI conversation experience.

[中文版本](./README_zh.md)

## Supported Features

- **Custom Model Integration**
  - Complies with OpenAI API standard protocols, supporting the configuration of private model services. Simply enter the API address and API Key to quickly integrate and select the desired model.
- **Streaming Parse & Render**
  - Based on AST recursive parsing strategies, block-level incremental parsing rendering of Markdown text is implemented.
  - Supports real-time parsing and precise presentation of LaTex inline and block-level formulas.
- **Multi-Session Parallel Architecture**
  - Uses an independent coroutine domain design, with all session tasks running completely isolated. Supports seamless switching between multiple sessions without blocking each other.
- **Session Management**
  - Time-based grouping management, supports batch session operations and pinning functions, helping users efficiently organize conversations.
- **Performance Optimization**
  - Introduces a lazy message pagination loading mechanism, pulling data from the database on demand only when switching sessions, and preloading the next page before the scroll bottoms out. Combined with the multi-level caching strategy of parsing nodes, it significantly improves rendering efficiency and reading smoothness.

## Features to be Followed Up
- Supports display of reasoning content.
- Display of message-related information within the session (token consumption, duration, output model, etc.).
- Operations such as deleting, editing, easily copying, and regenerating messages within sessions.
- Quick positioning of the previous or next message within a session.
- Message search and location.
- Session-level system prompt settings, model switching, and model parameter adjustments.
- Default configuration management (model provider, model, model parameters, etc.).
- More functional settings, such as on/off streaming generation and adjusting scroll speed during streaming generation.

## Demo Preview
[![Video]()](https://github.com/user-attachments/assets/e334fe56-8c06-41c8-bf14-b56aae77c5db)