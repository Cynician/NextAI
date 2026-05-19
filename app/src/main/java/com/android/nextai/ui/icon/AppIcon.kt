package com.android.nextai.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp


object AppIcon {

    /**
     * home screen
     * top bar
     */
    val Menu by lazy { tabler("M4 8l16 0", "M4 16l16 0") }
    val Download by lazy {
        tabler(
            "M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2 -2v-2", "M7 11l5 5l5 -5", "M12 4l0 12"
        )
    }
    val Settings by lazy {
        tabler(
            "M10.325 4.317c.426 -1.756 2.924 -1.756 3.35 0a1.724 1.724 0 0 0 2.573 1.066c1.543 -.94 3.31 .826 2.37 2.37a1.724 1.724 0 0 0 1.065 2.572c1.756 .426 1.756 2.924 0 3.35a1.724 1.724 0 0 0 -1.066 2.573c.94 1.543 -.826 3.31 -2.37 2.37a1.724 1.724 0 0 0 -2.572 1.065c-.426 1.756 -2.924 1.756 -3.35 0a1.724 1.724 0 0 0 -2.573 -1.066c-1.543 .94 -3.31 -.826 -2.37 -2.37a1.724 1.724 0 0 0 -1.065 -2.572c-1.756 -.426 -1.756 -2.924 0 -3.35a1.724 1.724 0 0 0 1.066 -2.573c-.94 -1.543 .826 -3.31 2.37 -2.37c1 .608 2.296 .07 2.572 -1.065",
            "M9 12a3 3 0 1 0 6 0a3 3 0 0 0 -6 0"
        )
    }
    val User by lazy {
        tabler(
            "M8 7a4 4 0 1 0 8 0a4 4 0 0 0 -8 0", "M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"
        )
    }
    val Sparkles by lazy {
        tabler(
            "M16 18a2 2 0 0 1 2 2a2 2 0 0 1 2 -2a2 2 0 0 1 -2 -2a2 2 0 0 1 -2 2",
            "M3 12a6 6 0 0 1 6 6a6 6 0 0 1 6 -6a6 6 0 0 1 -6 -6a6 6 0 0 1 -6 6",
            "M16 6a2 2 0 0 1 2 2a2 2 0 0 1 2 -2a2 2 0 0 1 -2 -2a2 2 0 0 1 -2 2"
        )
    }

    /**
     * home screen
     * bottom bar
     */
    val Send by lazy {
        tabler(
            "M10 14l11 -11",
            "M21 3l-6.5 18a.55 .55 0 0 1 -1 0l-3.5 -7l-7 -3.5a.55 .55 0 0 1 0 -1l18 -6.5"
        )
    }

    /**
     * home screen
     * drawer
     */
    // Empty state
    val Messages by lazy {
        tabler(
            "M21 14l-3 -3h-7a1 1 0 0 1 -1 -1v-6a1 1 0 0 1 1 -1h9a1 1 0 0 1 1 1v10",
            "M14 15v2a1 1 0 0 1 -1 1h-7l-3 3v-10a1 1 0 0 1 1 -1h2"
        )
    }
    val StartSession by lazy {
        filledIcon(
            1024f,
            1024f,
            "M832 128c53.12 0 96 42.88 96 96v384c0 53.12-42.88 96-96 96h-149.76l-273.28 192-37.12-51.84L661.76 640H832c17.92 0 32-14.08 32-32V224c0-17.92-14.08-32-32-32H192c-17.92 0-32 14.08-32 32v384c0 17.92 14.08 32 32 32h192v64H192c-53.12 0-96-43.52-96-96V224c0-53.12 42.88-96 96-96z m-288 172.8v89.6h89.6v64H544V544h-64v-89.6h-89.6v-64H480V300.8h64z"
        )
    }
    val CircleCheck by lazy {
        filledIcon(
            1024f,
            1024f,
            "M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474c-6.1-7.7-15.3-12.2-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1 0.4-12.8-6.3-12.8z"
        )
    }
    val Delete by lazy {
        tabler(
            "M4 7l16 0",
            "M10 11l0 6",
            "M14 11l0 6",
            "M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12",
            "M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3"
        )
    }
    val Cancel by lazy {
        filledIcon(
            1024f,
            1024f,
            "M512 455.431L794.843 172.59a8 8 0 0 1 11.313 0l45.255 45.255a8 8 0 0 1 0 11.313L568.57 512 851.41 794.843a8 8 0 0 1 0 11.313l-45.255 45.255a8 8 0 0 1-11.313 0L512 568.57 229.157 851.41a8 8 0 0 1-11.313 0l-45.255-45.255a8 8 0 0 1 0-11.313L455.43 512 172.59 229.157a8 8 0 0 1 0-11.313l45.255-45.255a8 8 0 0 1 11.313 0L512 455.43z"
        )
    }
    val PushPin by lazy {
        filledIcon(
            1024f,
            1024f,
            "M648.728381 130.779429a73.142857 73.142857 0 0 1 22.674286 15.433142l191.561143 191.756191a73.142857 73.142857 0 0 1-22.137905 118.564571l-67.876572 30.061715-127.341714 127.488-10.093714 140.239238a73.142857 73.142857 0 0 1-124.684191 46.445714l-123.66019-123.782095-210.724572 211.699809-51.833904-51.614476 210.846476-211.821714-127.926857-128.024381a73.142857 73.142857 0 0 1 46.299428-124.635429l144.237715-10.776381 125.074285-125.220571 29.379048-67.779048a73.142857 73.142857 0 0 1 96.207238-38.034285z m-29.086476 67.120761l-34.913524 80.530286-154.087619 154.331429-171.398095 12.751238 303.323428 303.542857 12.044191-167.399619 156.233143-156.428191 80.384-35.59619-191.585524-191.73181z"
        )
    }

    val UnPin by lazy {
        filledIcon(
            1024f,
            1024f,
            "M286.19776 189.98272l36.20352-36.20352L855.68 687.0528l-36.20352 36.1984zM526.848 336.384l57.856-57.856 34.816-80.384 191.488 191.488-80.384 35.84-57.344 57.856 51.712 51.712L773.12 486.4l68.096-30.208c8.192-3.584 15.872-8.704 22.016-15.36 28.672-28.672 28.672-74.752 0-103.424l-191.488-192c-6.656-6.656-14.336-11.776-22.528-15.36-36.864-15.872-80.384 1.024-96.256 37.888L523.776 235.52l-48.128 48.128 51.2 52.736zM615.424 540.672l-40.96 40.96-12.288 167.424L259.072 445.44l171.52-12.8 38.4-38.4-51.712-51.712-18.944 18.944-144.384 10.752c-17.408 1.536-33.792 8.704-46.08 20.992-28.672 28.672-28.672 74.752 0 103.424l128 128-210.944 211.968 51.712 51.712 210.944-211.456 123.904 123.904c12.288 12.288 29.184 19.968 46.592 20.992 40.448 3.072 75.264-27.648 78.336-67.584l10.24-140.288 21.504-21.504-52.736-51.712z"
        )
    }

    val ArrowRight by lazy {
        filledIcon(
            1024f,
            1024f,
            "M374.1740759 703.57949928a24.41125145 24.41125145 0 0 0 31.53933641 37.30039208L682.39053355 507.26421736 405.71341231 273.64854336a24.41125145 24.41125145 0 0 0-31.53933641 37.30039154L606.71565485 507.26421736 374.1740759 703.57949928z"
        )
    }
    val Pin by lazy {
        filledIcon(
            1024f,
            1024f,
            "M768 593.066667c0-4.266667 0-8.533333-4.266667-12.8L682.666667 418.133333V213.333333h4.266666c21.333333-4.266667 38.4-21.333333 38.4-42.666666 0-25.6-17.066667-42.666667-42.666666-42.666667H337.066667c-21.333333 4.266667-38.4 21.333333-38.4 42.666667 0 25.6 17.066667 42.666667 42.666666 42.666666v204.8l-81.066666 162.133334v4.266666c-4.266667 4.266667-4.266667 8.533333-4.266667 12.8v89.6c4.266667 21.333333 21.333333 38.4 42.666667 38.4h170.666666v174.933334c4.266667 21.333333 21.333333 38.4 42.666667 38.4 25.6 0 42.666667-17.066667 42.666667-42.666667v-170.666667h174.933333c21.333333-4.266667 38.4-21.333333 38.4-42.666666v-89.6zM682.666667 640H341.333333v-34.133333l81.066667-162.133334v-4.266666c0-4.266667 4.266667-8.533333 4.266667-12.8V213.333333h170.666666v217.6c0 4.266667 0 8.533333 4.266667 12.8l81.066667 162.133334v34.133333z"
        )
    }

    /**
     * home screen
     * messages
     */
    // code block
    val Copy by lazy {
        tabler(
            "M7 7m0 2.667a2.667 2.667 0 0 1 2.667 -2.667h8.666a2.667 2.667 0 0 1 2.667 2.667v8.666a2.667 2.667 0 0 1 -2.667 2.667h-8.666a2.667 2.667 0 0 1 -2.667 -2.667z",
            "M4.012 16.737a2.005 2.005 0 0 1 -1.012 -1.737v-10c0 -1.1 .9 -2 2 -2h10c.75 0 1.158 .385 1.5 1"
        )
    }
    val Code by lazy { tabler("M7 8l-4 4l4 4", "M17 8l4 4l-4 4") }
    val ChevronDown by lazy { tabler("M6 9l6 6l6 -6") }
    val ChevronUp by lazy { tabler("M6 15l6 -6l6 6") }
}

/**
 * Tabler Icons (https://tabler.io/icons) as Compose ImageVector constants.
 * Stroke-based, 24x24 viewport, stroke-width 2, round caps/joins.
 *
 * To add a new icon:
 *   1. Find it at https://tabler.io/icons
 *   2. Copy the SVG path d="" values
 *   3. Add: val IconName by lazy { tabler("M... path data ...") }
 */
private fun tabler(vararg paths: String): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        paths.forEach { svgPath ->
            addPath(
                pathData = PathParser().parsePathString(svgPath).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
    }.build()
}

/** Builds a Lucide-style stroke icon — same format as Tabler (24x24, stroke 2, round). */
private fun lucide(vararg paths: String): ImageVector = tabler(*paths)

/**
 * Filled SVG -> ImageVector
 *
 * Apply to：
 * - Ali iconfont
 * -  fill SVG
 * - Material Filled Icons
 */
private fun filledIcon(
    viewportWidth: Float,
    viewportHeight: Float,
    vararg paths: String,
): ImageVector {

    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight
    ).apply {

        paths.forEach { svgPath ->

            addPath(
                pathData = PathParser()
                    .parsePathString(svgPath)
                    .toNodes(),
                fill = SolidColor(Color.Black),
                stroke = null,
                pathFillType = PathFillType.NonZero
            )
        }

    }.build()
}