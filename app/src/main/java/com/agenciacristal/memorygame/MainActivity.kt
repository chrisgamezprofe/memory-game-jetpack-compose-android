package com.agenciacristal.memorygame

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.tooling.preview.Preview
import com.agenciacristal.memorygame.ui.theme.MemoryGameTheme

import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            MemoryGameTheme {
                // A surface container using the 'background' color from the theme
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MemoryGame()
                }
            }
        }
    }
}


@Composable
fun MemoryGame() {
    val context = LocalContext.current
    var images = remember { generateImages(context) }
    var selectedImages by remember { mutableStateOf(images.map { -1 to false }) }
    var foundPairs by remember { mutableStateOf(0) }
    var intentos by remember { mutableStateOf(0) }
    var lastSelectedImage by remember { mutableStateOf(-1) }
    var imageNumber by remember { mutableStateOf(1) }
    var lastSelectedIndex by remember { mutableStateOf(-1) }
    var canPlay by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var elapsedTime by remember { mutableStateOf(0L) }
    var showTime by remember { mutableStateOf(true) }
    var timeText by remember { mutableStateOf("00:00 sg") }


    LaunchedEffect(showTime) {

        while (showTime) {
            delay(1000)
            elapsedTime ++
            val minutos = elapsedTime / 60
            val segundos = elapsedTime % 60
            val minutosText = if (minutos < 10) "0$minutos" else minutos.toString()
            val segundosText = if (segundos < 10) "0$segundos" else segundos.toString()
            timeText = "$minutosText:$segundosText sg"
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val background: Painter = painterResource(id = R.drawable.background)
        Image(
            painter = background,
            contentDescription = null,
            modifier = Modifier
                .fillMaxHeight()
                .alpha(0.8f),
            contentScale = ContentScale.Crop,

        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Attempts: $intentos", fontSize = 20.sp, color = MyColors.Yellow, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Pairs: $foundPairs", fontSize = 20.sp, color = MyColors.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Time $timeText",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(columns = GridCells.Fixed(4)) {

                items(images.size) { index ->
                    val (image, revealed) = selectedImages[index]

                    MemoryCard(
                        painter = if (revealed) painterResource(id = image) else painterResource(id = imgs.card_back),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                SoundUtil.playSound(context = context, resourceId = R.raw.show)

                                if (!revealed && canPlay) {

                                    try {

                                        selectedImages = selectedImages
                                            .toMutableList()
                                            .also {
                                                it[index] = Pair(images[index].first, true)
                                            }

                                        if (imageNumber == 1) {
                                            imageNumber = 2
                                            lastSelectedImage = images[index].first
                                            lastSelectedIndex = index

                                        } else {
                                            canPlay = false
                                            if (images[index].first == lastSelectedImage) {
                                                foundPairs++
                                                intentos++
                                                SoundUtil.playSound(
                                                    context = context,
                                                    resourceId = R.raw.win
                                                )

                                                if (foundPairs == images.size / 2) {
                                                    scope.launch {
                                                        // Mostrar Snackbar
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                "GANASTE!!!!",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()

                                                        delay(2000) // Cambia esto según la duración deseada

                                                        restartGame(context)


                                                    }
                                                } else {
                                                    canPlay = true
                                                }

                                            } else {

                                                scope.launch {

                                                    delay(1000) // Cambia esto según la duración deseada
                                                    intentos++
                                                    SoundUtil.playSound(
                                                        context = context,
                                                        resourceId = R.raw.lose
                                                    )

                                                    selectedImages = selectedImages
                                                        .toMutableList()
                                                        .also {
                                                            it[index] = Pair(-1, false)
                                                        }
                                                    selectedImages = selectedImages
                                                        .toMutableList()
                                                        .also {
                                                            it[lastSelectedIndex] = Pair(-1, false)
                                                        }
                                                    canPlay = true
                                                }

                                            }
                                            // lastSelectedIndex = -1
                                            imageNumber = 1

                                        }

                                        var cambia = 0
                                    } catch (ex: Exception) {
                                        scope.launch {
                                            Toast
                                                .makeText(context, ex.message, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        Toast
                                            .makeText(
                                                context,
                                                "Debes jugar otra carta",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                }

                            }

                    )
                }
            }
            Spacer(modifier = Modifier.height(50.dp))

            Button(onClick = {
                showTime = !showTime
                if (showTime){
                    restartGame(context)
                }
                             }, modifier = Modifier.fillMaxWidth()) {

                Text(text = if (showTime) "Stop" else "Restart Game", fontSize = 20.sp,
                    fontWeight = FontWeight.Black,modifier = Modifier.padding(vertical = 10.dp))

            }
        }
    }
}

@Composable
fun MemoryCard(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String?
) {
    Box(modifier = modifier.size(100.dp)) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun generateImages(context: Context): List<Pair<Int, Boolean>> {
    val images = mutableListOf<Int>()
    val numberOfPairs = 8 // Change this to adjust the number of pairs

    for (i in 1..numberOfPairs) {
        val imageName = "img_$i"
        val imageResourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        images.add(imageResourceId)
        images.add(imageResourceId)
    }
    images.shuffle()

    return images.map { it to false }
}

fun restartGame(context: Context) {
    Toast
        .makeText(
            context,
            "New game...",
            Toast.LENGTH_SHORT
        )
        .show()

    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

    val options = ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out)
    context.startActivity(intent, options.toBundle())
}


object MyColors{
    val Yellow = Color(0xFFFFEB3B)
    val White = Color(0xFFFFFFFF)
}

// Placeholder for your actual image resource IDs
object imgs {
        val image1 = R.drawable.img_1
        val image2 = R.drawable.img_2
        val image3 = R.drawable.img_3
        val image4 = R.drawable.img_4
        val image5 = R.drawable.img_5
        val image6 = R.drawable.img_6
        val image7 = R.drawable.img_7
        val image8 = R.drawable.img_8
        val card_back = R.drawable.card_back // Placeholder for the back side of the card
}

object SoundUtil {
    var mediaPlayer: MediaPlayer? = null

    fun playSound(context: Context, resourceId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, resourceId)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            release()
        }
    }

    fun release() {
        mediaPlayer?.release()
        //mediaPlayer = null
    }
}