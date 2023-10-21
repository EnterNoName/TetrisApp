package com.michaelrayven.tetris_jetpack.feature_game.presentation

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.google.gson.Gson
import com.michaelrayven.tetris_jetpack.R
import com.michaelrayven.tetris_jetpack.feature_game.data.local.game.tetromino_sets.DefaultTetrominoSet
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

@Composable
fun GameScreen(navController: NavHostController) {
    val viewModel: GameScreenViewModel = hiltViewModel()
    val state = viewModel.state.value

    if (state.isGameOver) {
        LaunchedEffect(Unit)  {
            launch {
                val id = viewModel.saveScore()
                navController.navigate(
                    route = "game_over/$id",
                    navOptions = NavOptions.Builder().setPopUpTo("main_menu", false).build()
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        GameView(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            playfield = state.playfield,
            onTapLeft = { viewModel.onTapLeft() },
            onTapRight = { viewModel.onTapRight() },
            onDoubleTap = { viewModel.onDoubleTap() },
            onDragLeft = { viewModel.onDragLeft() },
            onDragRight = { viewModel.onDragRight() },
            onSwipeDown = { viewModel.onSwipeDown() }
        )
        GameInterface(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp),
            nextTetrominoes = state.nextTetrominoes.let {
                if (it.size > 3) {
                    return@let it.sliceArray(0 .. 2)
                } else {
                    return@let it
                }
            },
            heldTetromino = state.heldTetromino,
            score = state.score,
            lines = state.lines,
            level = state.level,
            combo = state.combo
        ) { viewModel.onHoldPressed() }
    }
}

@Composable
fun GameView(
    modifier: Modifier = Modifier,
    playfield: Array<Array<String?>>,
    onTapLeft: () -> Unit,
    onTapRight: () -> Unit,
    onDoubleTap: () -> Unit,
    onDragLeft: () -> Unit,
    onDragRight: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    val vector = ImageVector.vectorResource(id = R.drawable.point_overlay_default)
    val painter = rememberVectorPainter(image = vector)

    var screenWidthPx by remember {
        mutableStateOf(0f)
    }

    var dragDistanceTraveled by remember {
        mutableStateOf(0f)
    }

    Canvas(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                screenWidthPx = coordinates.size.width.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                    },
                    onTap = {
                        Log.d("SCREEN", it.toString())
                        if (it.x > screenWidthPx / 2) {
                            Log.d("SCREEN", "TAP RIGHT")
                            onTapRight()
                        } else {
                            Log.d("SCREEN", "TAP LEFT")
                            onTapLeft()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    dragDistanceTraveled += dragAmount
                    if (abs(dragDistanceTraveled) > GameScreenViewModel.DRAG_THRESHOLD) {
                        if (dragDistanceTraveled > 0) {
                            onDragRight()
                        } else {
                            onDragLeft()
                        }
                        dragDistanceTraveled = 0f
                    }
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (abs(dragAmount) > GameScreenViewModel.VELOCITY_THRESHOLD && dragAmount > 0) {
                        onSwipeDown()
                    }
                }
            }
    ) {
        inset(10f) {
            val width = 10
            val height = 20
            val usedWidthForPointSize = size.height / height > size.width / width
            val pointSize = min(size.height / height, size.width / width)
            val initialOffset = if (usedWidthForPointSize) {
                size.height - pointSize * height
            } else {
                size.width - pointSize * width
            } / 2

            for (i in 0 until width) {
                for (j in 0 until height) {
                    drawRect(
                        topLeft = Offset(
                            x = if (usedWidthForPointSize) pointSize * i else initialOffset + pointSize * i,
                            y = if (usedWidthForPointSize) initialOffset + pointSize * j else pointSize * j
                        ),
                        color = Color.Gray,
                        size = Size(width = pointSize, height = pointSize),
                        style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            for (i in playfield.indices) {
                for (j in playfield[i].indices) {
                    val name = playfield[i][j] ?: continue
                    val color = if (name == DefaultTetrominoSet().shadowName) DefaultTetrominoSet().shadowColor else DefaultTetrominoSet()[name]?.color

                    if (color != null) {
                        drawRect(
                            topLeft = Offset(
                                x = if (usedWidthForPointSize) pointSize * i else initialOffset + pointSize * i,
                                y = if (usedWidthForPointSize) initialOffset + pointSize * j else pointSize * j
                            ),
                            color =  Color(color),
                            size = Size(width = pointSize, height = pointSize),
                        )
                    }
                    translate(
                        left = if (usedWidthForPointSize) pointSize * i else initialOffset + pointSize * i,
                        top = if (usedWidthForPointSize) initialOffset + pointSize * j else pointSize * j
                    ) {
                        with(painter) {
                            draw(Size(width = pointSize, height = pointSize))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameInterface(
    modifier: Modifier = Modifier,
    nextTetrominoes: Array<String>,
    heldTetromino: String?,
    score: Int,
    lines: Int,
    level: Int,
    combo: Int,
    onHoldPressed: () -> Unit = {}
) {
    val firaSansFamily = FontFamily(
        Font(R.font.fira_sans, FontWeight.Normal),
        Font(R.font.fira_sans_medium, FontWeight.Medium),
        Font(R.font.fira_sans_bold, FontWeight.Bold),
        Font(R.font.fira_sans_black, FontWeight.Black)
    )

    Column(
        modifier = modifier.padding(5.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            text = "Next:"
        )
        nextTetrominoes.mapIndexed { i, name ->
            TetrominoShowcase(name = name)
            if (i != nextTetrominoes.size - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            text = "Held:"
        )
        TetrominoShowcase(
            modifier = Modifier.clickable { onHoldPressed() },
            name = heldTetromino
        )
        GameValueShowcase("Score:", score)
        GameValueShowcase("Lines:", lines)
        GameValueShowcase("Level:", level)
        GameValueShowcase("Combo:", combo)
    }
}

@Composable
fun GameValueShowcase(
    name: String,
    value:Int
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        text = name
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(3.dp, Color.DarkGray)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            text = value.toString()
        )
    }
}

@Composable
fun TetrominoShowcase(
    modifier: Modifier = Modifier,
    name: String?
) {
    Card(
        modifier = modifier
            .aspectRatio(1f / 1f)
            .wrapContentSize(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(3.dp, Color.DarkGray)
    ) {
        if (name == null) return@Card Spacer(modifier = Modifier.fillMaxSize())

        val tetromino = DefaultTetrominoSet()[name]!!
        val tetrominoSize = tetromino.getDimensions()
        val vector = ImageVector.vectorResource(id = tetromino.overlayResId)
        val painter = rememberVectorPainter(image = vector)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val pointSize = min(size.width / (tetrominoSize.width + 2), size.height / (tetrominoSize.height + 2))
            val usedWidthForPointSize = size.width / (tetrominoSize.width + 2) < size.height / (tetrominoSize.height + 2)
            val initialYOffset = if (usedWidthForPointSize) (size.height - tetrominoSize.height * pointSize) / 2 else pointSize
            val initialXOffset = if (usedWidthForPointSize) pointSize else (size.width - tetrominoSize.width * pointSize) / 2

            for (i in tetromino.shapeMatrix.indices) {
                for (j in tetromino.shapeMatrix[0].indices) {
                    if (tetromino.shapeMatrix[i][j]) {
                        drawRect(
                            topLeft = Offset(
                                x = initialXOffset + pointSize * i,
                                y = initialYOffset + pointSize * j
                            ),
                            color = Color(tetromino.color),
                            size = Size(width = pointSize, height = pointSize),
                        )

                        translate(
                            left = initialXOffset + pointSize * i,
                            top = initialYOffset + pointSize * j
                        ) {
                            with(painter) {
                                draw(Size(width = pointSize, height = pointSize))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun GameInterfacePreview() {
    GameInterface(
        nextTetrominoes = arrayOf("T", "Z", "L", "I", "S", "J"),
        heldTetromino = null,
        score = 100,
        lines = 99,
        level = 5,
        combo = 8
    )
}

