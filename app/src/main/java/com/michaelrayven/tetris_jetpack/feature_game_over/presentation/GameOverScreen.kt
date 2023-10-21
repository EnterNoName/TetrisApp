package com.michaelrayven.tetris_jetpack.feature_game_over.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.michaelrayven.tetris_jetpack.R

@Composable
fun GameOverScreen(navController: NavHostController) {
    val firaSansFamily = FontFamily(
        Font(R.font.fira_sans, FontWeight.Normal),
        Font(R.font.fira_sans_medium, FontWeight.Medium),
        Font(R.font.fira_sans_bold, FontWeight.Bold),
        Font(R.font.fira_sans_black, FontWeight.Black)
    )

    val viewModel: GameOverViewModel = hiltViewModel()
    val state = viewModel.state.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 96.dp),
            text = "Game Over!",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            fontFamily = firaSansFamily,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 32.dp),
            text = if (state.isNewHighScore) "New high score:\n${state.currentScore.score}"
                else "Current high score:\n${state.highScore.score}",
            textAlign = TextAlign.Center,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = firaSansFamily,
            lineHeight = 28.sp
        )

        Row {
            Text(
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                text = "Score:"
            )
            Spacer(modifier = Modifier.weight(1f))
            Card(
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(3.dp, Color.DarkGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        text = state.currentScore.score.toString()
                    )
                    val scoreDifference = state.currentScore.score - state.highScore.score
                    val scoreDifferenceString = if (scoreDifference >= 0) "+$scoreDifference" else "$scoreDifference"
                    val scoreDifferenceColor = if (scoreDifference > 0) Color(0xFF32a852)
                        else if (scoreDifference < 0) Color(0xFFC52828)
                        else Color(0xFF5C5C5C)
                    Text(
                        textAlign = TextAlign.Center,
                        color = scoreDifferenceColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        text = scoreDifferenceString
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row {
            Text(
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                text = "Lines:"
            )
            Spacer(modifier = Modifier.weight(1f))
            Card(
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(3.dp, Color.DarkGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        text = state.currentScore.lines.toString()
                    )
                    val linesDifference = state.currentScore.lines - state.highScore.lines
                    val linesDifferenceString = if (linesDifference >= 0) "+$linesDifference" else "$linesDifference"
                    val linesDifferenceColor = if (linesDifference > 0) Color(0xFF32a852)
                        else if (linesDifference < 0) Color(0xFFC52828)
                        else Color(0xFF5C5C5C)
                    Text(
                        textAlign = TextAlign.Center,
                        color = linesDifferenceColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        text = linesDifferenceString
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row {
            Text(
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                text = "Level:"
            )
            Spacer(modifier = Modifier.weight(1f))
            Card(
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(3.dp, Color.DarkGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        text = state.currentScore.level.toString()
                    )
                    val levelDifference = state.currentScore.level - state.highScore.level
                    val levelDifferenceString =
                        if (levelDifference >= 0) "+$levelDifference" else "$levelDifference"
                    val levelDifferenceColor = if (levelDifference > 0) Color(0xFF32a852)
                    else if (levelDifference < 0) Color(0xFFC52828)
                    else Color(0xFF5C5C5C)
                    Text(
                        textAlign = TextAlign.Center,
                        color = levelDifferenceColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        text = levelDifferenceString
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Button(
                onClick = {
                    navController.navigate(
                        route = "main_menu",
                        navOptions = NavOptions.Builder().setPopUpTo("main_menu", false).build()
                    )}, modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .weight(1f)
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.round_arrow_back_24),
                    contentDescription = "Go to main menu"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    navController.navigate(
                        route = "game",
                        navOptions = NavOptions.Builder().setPopUpTo("main_menu", false).build()
                    )}, modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .weight(1f)
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.round_refresh_24),
                    contentDescription = "Play again"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { /*TODO*/ }, modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .weight(1f)
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.round_share_24),
                    contentDescription = "Share your results"
                )
            }
        }
    }
}