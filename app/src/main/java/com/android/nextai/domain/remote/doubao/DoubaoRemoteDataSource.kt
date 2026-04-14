package com.android.nextai.domain.remote.doubao

import com.android.nextai.domain.remote.AIModelDataSource
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.viewmodel.chat.entity.Message
import kotlinx.coroutines.delay
import kotlin.random.Random

object DoubaoRemoteDataSource : AIModelDataSource {
    private const val TAG = "DoubaoRemoteDataSource"
    private const val TEST_STRING =
        "这是一个用于测试 Markdown 渲染效果的示例文档。它包含了标题、列表、代码块、表格、引用以及各种文本样式。\n" +
                "\n" +
                "## 1. 文本样式测试\n" +
                "\n" +
                "- **粗体文本**：这是加粗的内容。\n" +
                "- *斜体文本*：这是斜体的内容。\n" +
                "- ***粗斜体***：这是既加粗又斜体的内容。\n" +
                "- ~~删除线~~：这是被删除的内容。\n" +
                "- `行内代码`：这是 `print(\"Hello World\")` 行内代码示例。\n" +
                "- > 引用块：\n" +
                "> 这是一段引用文本，通常用于强调某句话或引用他人的观点。\n" +
                "> > 嵌套引用：这是第二层引用。\n" +
                "\n" +
                "## 2. 列表测试\n" +
                "\n" +
                "### 无序列表\n" +
                "- \uD83C\uDF4E 苹果\n" +
                "- \uD83C\uDF4C 香蕉\n" +
                "- \uD83C\uDF47 葡萄\n" +
                "  - 红葡萄\n" +
                "  - 青葡萄\n" +
                "\n" +
                "### 有序列表\n" +
                "1. 第一步：打开终端\n" +
                "2. 第二步：输入命令 `npm install`\n" +
                "3. 第三步：运行项目\n" +
                "   1. 检查端口\n" +
                "   2. 启动服务"
    override suspend fun getAIAnswer(messageList: List<Message>): String {
        return TEST_STRING
    }

    override suspend fun getAIStreamingAnswer(
        messageList: List<Message>,
        callback: (GenerationEvent) -> Unit,
    ) {
        try {
            val words = TEST_STRING.chunked(2)
            for (word in words) {
                delay(Random.nextLong(50, 150))
                callback(GenerationEvent.Word(word))
            }
            callback(GenerationEvent.Done)
        } catch (e: Exception) {
            callback(GenerationEvent.Error(e.message ?: "Unknown error"))
        }
    }
}
