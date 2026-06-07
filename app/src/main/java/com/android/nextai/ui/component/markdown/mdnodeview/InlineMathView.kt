package com.android.nextai.ui.component.markdown.mdnodeview

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InlineMathView(latex: String, modifier: Modifier = Modifier) {
    // 过滤掉前后的 $ 符号
    val cleanLatex = latex.removePrefix("$").removeSuffix("$")
        .replace("\\|", "|") // 把前面转义的 \| 恢复回数学意义的 |

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // 性能优化：禁用不必要的滚动条和手势
                isHorizontalScrollBarEnabled = false
                isVerticalScrollBarEnabled = false
                settings.javaScriptEnabled = true
                setBackgroundColor(0) // 背景透明，跟随 Compose 颜色

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // 页面加载完成后可根据需要通知外层重新测量高度
                    }
                }
            }
        },
        modifier = modifier,
        update = { webView ->
            // 构建极其轻量的 HTML，直接从 CDN 引入 KaTeX
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
                    <script src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
                    <style>
                        body { 
                            margin: 0; 
                            padding: 0; 
                            display: inline-block; 
                            background-color: transparent;
                            color: #333333; /* 你可以根据主题动态传入颜色 */
                            font-size: 14px;
                        }
                        #math { display: inline-block; white-space: nowrap; }
                    </style>
                </head>
                <body>
                    <span id="math"></span>
                    <script>
                        try {
                            katex.render(String.raw`$cleanLatex`, document.getElementById('math'), {
                                throwOnError: false,
                                displayMode: false // 行内模式
                            });
                        } catch (e) {
                            document.getElementById('math').textContent = "$cleanLatex";
                        }
                    </script>
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        }
    )
}