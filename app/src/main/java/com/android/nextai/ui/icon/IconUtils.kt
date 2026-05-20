package com.android.nextai.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SVG Path Data
 */
data class SvgPath(
    val path: String,
    val fillColor: Color = Color.Black,
    val strokeColor: Color? = null,
    val strokeWidth: Float = 0f,
    val fillAlpha: Float = 1f,
    val strokeAlpha: Float = 1f,
    val fillType: PathFillType = PathFillType.NonZero,
    val strokeCap: StrokeCap = StrokeCap.Butt,
    val strokeJoin: StrokeJoin = StrokeJoin.Miter,
    val strokeLineMiter: Float = 4f,
    val trimPathStart: Float = 0f,
    val trimPathEnd: Float = 1f,
    val trimPathOffset: Float = 0f,
)

object IconUtils {

    /**
     * Tabler Icons (https://tabler.io/icons) as Compose ImageVector constants.
     * Stroke-based, 24x24 viewport, stroke-width 2, round caps/joins.
     *
     * To add a new icon:
     *   1. Find it at https://tabler.io/icons
     *   2. Copy the SVG path d="" values
     *   3. Add: val IconName by lazy { tabler("M... path data ...") }
     */
    fun tabler(vararg paths: String): ImageVector {
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




    /**
     * Trans SVG -> ImageVector
     *
     * Sport：
     * - Muti-path
     * - fill
     * - stroke
     * - alpha
     * - lineCap
     * - lineJoin
     * - trimPath
     * - size(width & height)
     *
     * Apply to：
     * - Icon font
     * - Material Icons
     * - Compose Vector
     * - Normal SVG
     */
    fun filledIcon(
        viewportWidth: Float = 1024f,
        viewportHeight: Float = 1024f,
        width: Dp = 24.dp,
        height: Dp = 24.dp,
        autoMirror: Boolean = false,
        name: String = "",
        vararg paths: SvgPath,
        ): ImageVector {

        return ImageVector.Builder(
            name = name,
            defaultWidth = width,
            defaultHeight = height,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            autoMirror = autoMirror
        ).apply {
            paths.forEach { item ->
                addPath(
                    pathData = PathParser()
                        .parsePathString(item.path)
                        .toNodes(),
                    fill = SolidColor(item.fillColor),
                    fillAlpha = item.fillAlpha,
                    stroke = item.strokeColor?.let {
                        SolidColor(it)
                    },
                    strokeAlpha = item.strokeAlpha,
                    strokeLineWidth = item.strokeWidth,
                    strokeLineCap = item.strokeCap,
                    strokeLineJoin = item.strokeJoin,
                    strokeLineMiter = item.strokeLineMiter,
                    trimPathStart = item.trimPathStart,
                    trimPathEnd = item.trimPathEnd,
                    trimPathOffset = item.trimPathOffset,
                    pathFillType = item.fillType
                )
            }
        }.build()
    }
}
