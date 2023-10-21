package com.michaelrayven.tetris_jetpack.feature_main_menu.presentation

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.michaelrayven.tetris_jetpack.R

@Composable
fun MainMenuScreen(
    navController: NavHostController
) {
    val activity = (LocalContext.current as? Activity)

    val firaSansFamily = FontFamily(
        Font(R.font.fira_sans, FontWeight.Normal),
        Font(R.font.fira_sans_medium, FontWeight.Medium),
        Font(R.font.fira_sans_bold, FontWeight.Bold),
        Font(R.font.fira_sans_black, FontWeight.Black)
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(0.dp, 0.dp, 0.dp, 8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                Icon(modifier = Modifier.size(32.dp), painter = painterResource(id = R.drawable.round_account_circle_24), contentDescription = "Account")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    text = "Name#001"
                )
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 96.dp),
                text = "Welcome to",
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = firaSansFamily,
                lineHeight = 14.sp
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "TETRIS",
                textAlign = TextAlign.Center,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                fontFamily = firaSansFamily,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.weight(1f))


            Column(modifier = Modifier
                .padding(horizontal = 40.dp)
                .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.navigate("game") }, modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)) {
                    Text(
                        text = "Singleplayer",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /*TODO*/ }, modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)) {
                    Text(
                        text = "Multiplayer",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /*TODO*/ }, modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)) {
                    Text(
                        text = "Settings",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(
                        onClick = { activity?.finish() }, modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .weight(1f)
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(id = R.drawable.round_exit_to_app_24),
                            contentDescription = "Exit app"
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
                            painter = painterResource(id = R.drawable.round_analytics_24),
                            contentDescription = "Go to statistics"
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
                            painter = painterResource(id = R.drawable.round_github_24),
                            contentDescription = "Open GitHub page"
                        )
                    }
                }
            }
        }
    }
}