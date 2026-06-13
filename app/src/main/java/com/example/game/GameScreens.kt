package com.example.game

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HighScore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

@Composable
fun WormLordApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1C1B1F) // Immersive BG (charcoal)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                Screen.SPLASH -> SplashScreen()
                Screen.TITLE -> TitleScreen(viewModel)
                Screen.MODE_SELECT -> ModeSelectionScreen(viewModel)
                Screen.GAMEPLAY -> GameplayScreen(viewModel)
                Screen.CONTROLS -> ControlsScreen(viewModel)
                Screen.HIGHSCORES -> HighScoresScreen(viewModel)
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF381E72), Color(0xFF1C1B1F)),
                    center = Offset.Unspecified,
                    radius = 900f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Cosmic Bolt",
                tint = Color(0xFFD0BCFF),
                modifier = Modifier
                    .size(100.dp)
                    .animateContentSize()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "WORM LORD",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE6E1E5),
                letterSpacing = 6.sp,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Vampire Swarms vs Snake Titans",
                fontSize = 14.sp,
                color = Color(0xFFCCC2DC),
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(40.dp))
            CircularProgressIndicator(
                color = Color(0xFFD0BCFF),
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TitleScreen(viewModel: GameViewModel) {
    val nameInput by viewModel.playerNameInput.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Background subtle ambient radial glow
                drawCircle(
                    color = Color(0xFF381E72).copy(alpha = 0.4f),
                    radius = 900f,
                    center = Offset(size.width * 0.9f, size.height * 0.1f)
                )
            }
            .background(Color(0xFF1C1B1F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "WORM LORD",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE6E1E5),
                    letterSpacing = 4.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.testTag("app_title")
                )
                Text(
                    text = "THE ARENA SURVIVOR",
                    fontSize = 14.sp,
                    color = Color(0xFFD0BCFF),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Input Name Board
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CHOOSE YOUR WORM LORD IDENTITY",
                        fontSize = 11.sp,
                        color = Color(0xFFCCC2DC),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1B1F), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Name Icon",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = nameInput,
                            onValueChange = { viewModel.setPlayerName(it) },
                            textStyle = TextStyle(
                                color = Color(0xFFE6E1E5),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            cursorBrush = SolidColor(Color(0xFFD0BCFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input")
                        )
                    }
                }
            }

            // Main Menu Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = { viewModel.setScreen(Screen.MODE_SELECT) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("play_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ENTER ARENA", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setScreen(Screen.HIGHSCORES) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("scores_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF2B2930),
                            contentColor = Color(0xFFD0BCFF)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF49454F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Scores")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "PANEL", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.setScreen(Screen.CONTROLS) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("controls_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF2B2930),
                            contentColor = Color(0xFFD0BCFF)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF49454F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Help")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "GUIDE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Small bottom branding token
            Text(
                text = "Worm Lord v1.0.0 • Client-side Arcade Sync",
                fontSize = 11.sp,
                color = Color(0xFF938F99),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun ModeSelectionScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.setScreen(Screen.TITLE) },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "SELECT ARENA MODE",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1E5),
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Survive bat hordes, absorb length, conquer local rivals",
                    fontSize = 13.sp,
                    color = Color(0xFFCCC2DC),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Cards for three modes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mode 1: Solo
                ModeCard(
                    title = "Solo Lord Arena",
                    subtitle = "1 Player vs 4 Rival Worms & Vampire Hordes",
                    desc = "Standard action-survival. Consume XP gems, auto-fire segment spells, grow the longest tail and defeat Reaper swarms.",
                    tint = Color(0xFFD0BCFF),
                    icon = Icons.Default.Person,
                    onClick = {
                        viewModel.setGameMode(GameMode.SOLO)
                        viewModel.startGame()
                    }
                )

                // Mode 2: Split Screen Coop
                ModeCard(
                    title = "Local Split-Co-op",
                    subtitle = "2 Players sharing screen vs Monsters",
                    desc = "Two active players on the same device! Player 1 on bottom, Player 2 on top. Level up independently and cooperate to slay the dragon.",
                    tint = Color(0xFFCCC2DC),
                    icon = Icons.Default.Group,
                    onClick = {
                        viewModel.setGameMode(GameMode.CO_OP)
                        viewModel.startGame()
                    }
                )

                // Mode 3: Local Versus Arena
                ModeCard(
                    title = "Head-to-Head Versus",
                    subtitle = "2 Players + Bots Arena Combat",
                    desc = "Battle your friend on the same screen! Cut them off so they head-crash into your body, immediately turning them into edible gold meat pellets.",
                    tint = Color(0xFFF2B8B5),
                    icon = Icons.Default.LocalFireDepartment,
                    onClick = {
                        viewModel.setGameMode(GameMode.VERSUS)
                        viewModel.startGame()
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    desc: String,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
        border = BorderStroke(1.dp, Color(0xFF49454F)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(tint.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, tint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5))
                Text(text = subtitle, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tint)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = desc, fontSize = 11.sp, color = Color(0xFFCCC2DC))
            }
        }
    }
}

@Composable
fun GameplayScreen(viewModel: GameViewModel) {
    val gameMode by viewModel.gameMode.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val levelupPendingMap by viewModel.levelUpPending.collectAsState()

    // Screen dimensions split
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val totalWidth = constraints.maxWidth.toFloat()
        val totalHeight = constraints.maxHeight.toFloat()

        if (gameMode == GameMode.SOLO) {
            // SINGLE VIEW
            Box(modifier = Modifier.fillMaxSize()) {
                ViewportCanvas(
                    viewModel = viewModel,
                    focusedWormId = "p1",
                    viewportWidth = totalWidth,
                    viewportHeight = totalHeight
                )

                // Controls P1 overlay
                PlayerControlsOverlay(
                    viewModel = viewModel,
                    wormId = "p1",
                    playerLabel = "Player 1",
                    isLeftHand = true,
                    totalHeight = totalHeight,
                    totalWidth = totalWidth
                )
            }
        } else {
            // DOUBLE VIEW SPLIT-SCREEN (Split horizontally)
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Viewport: Player 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFF49454F))
                ) {
                    ViewportCanvas(
                        viewModel = viewModel,
                        focusedWormId = "p2",
                        viewportWidth = totalWidth,
                        viewportHeight = totalHeight / 2f
                    )

                    // Controls P2 Overlay on top half
                    PlayerControlsOverlay(
                        viewModel = viewModel,
                        wormId = "p2",
                        playerLabel = "Player 2 (Top)",
                        isLeftHand = false,
                        totalHeight = totalHeight / 2f,
                        totalWidth = totalWidth
                    )
                }

                // Bottom Viewport: Player 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFF49454F))
                ) {
                    ViewportCanvas(
                        viewModel = viewModel,
                        focusedWormId = "p1",
                        viewportWidth = totalWidth,
                        viewportHeight = totalHeight / 2f
                    )

                    // Controls P1 Overlay on bottom half
                    PlayerControlsOverlay(
                        viewModel = viewModel,
                        wormId = "p1",
                        playerLabel = "Player 1 (Bottom)",
                        isLeftHand = true,
                        totalHeight = totalHeight / 2f,
                        totalWidth = totalWidth
                    )
                }
            }
        }

        // LEVEL UP SELECTION CARDS (Modal style overlays)
        if (levelupPendingMap.isNotEmpty()) {
            levelupPendingMap.forEach { (wormId, choices) ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    LevelUpChoiceDialog(
                        wormName = if (wormId == "p1") "PLAYER 1" else "PLAYER 2",
                        choices = choices,
                        onChoiceSelected = { optionId ->
                            viewModel.makeUpgradeSelection(wormId, optionId)
                        }
                    )
                }
            }
        }

        // GAME OVER SCRIM OVERLAY
        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                GameOverBoard(viewModel)
            }
        }
    }
}

@Composable
fun ViewportCanvas(
    viewModel: GameViewModel,
    focusedWormId: String,
    viewportWidth: Float,
    viewportHeight: Float
) {
    val worms by viewModel.worms.collectAsState()
    val enemies by viewModel.enemies.collectAsState()
    val items by viewModel.gameItems.collectAsState()
    val projectiles by viewModel.projectiles.collectAsState()
    val particles by viewModel.particles.collectAsState()

    // Find the worm that this viewport focuses on to calculate center camera offset
    val cameraTargetWorm = worms.find { it.id == focusedWormId }
    val cameraTargetPos = cameraTargetWorm?.headPosition ?: Offset(viewModel.arenaWidth / 2f, viewModel.arenaHeight / 2f)

    // Center camera
    val cameraOffsetX = viewportWidth / 2f - cameraTargetPos.x
    val cameraOffsetY = viewportHeight / 2f - cameraTargetPos.y

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw starry space dynamic tile floor background
        drawRect(color = Color(0xFF000000)) // Pure pitch black matching immersive viewport

        val step = 100f
        val arenaW = viewModel.arenaWidth
        val arenaH = viewModel.arenaHeight

        // Draw grid floor lines
        var gx = 0f
        while (gx <= arenaW) {
            drawLine(
                color = Color(0xFF49454F).copy(alpha = 0.3f), // subtle starry grid outline
                start = Offset(gx + cameraOffsetX, cameraOffsetY),
                end = Offset(gx + cameraOffsetX, arenaH + cameraOffsetY),
                strokeWidth = 2f
            )
            gx += step
        }

        var gy = 0f
        while (gy <= arenaH) {
            drawLine(
                color = Color(0xFF49454F).copy(alpha = 0.3f), // subtle starry grid outline
                start = Offset(cameraOffsetX, gy + cameraOffsetY),
                end = Offset(arenaW + cameraOffsetX, gy + cameraOffsetY),
                strokeWidth = 2f
            )
            gy += step
        }

        // Draw Arena Bound Outline
        drawRect(
            color = Color(0xFFD0BCFF).copy(alpha = 0.4f), // lilac/lavender boundary outline
            topLeft = Offset(cameraOffsetX, cameraOffsetY),
            size = Size(arenaW, arenaH),
            style = Stroke(width = 8f)
        )

        // Draw projectile acid pools / spikes
        projectiles.forEach { p ->
            val screenPos = p.position + Offset(cameraOffsetX, cameraOffsetY)

            if (p.type == "SPIT") {
                // Toxic Pool
                drawCircle(
                    color = Color(p.color),
                    radius = p.radius,
                    center = screenPos
                )
                // draw smaller nested circles inside acid pool for texture
                drawCircle(
                    color = Color(0x66A7FFEB),
                    radius = p.radius * 0.6f,
                    center = screenPos
                )
            } else if (p.type == "LIGHTNING") {
                // Lightning bolt jagged lines inside velocity(start offset) -> position(end offset)
                val beamStart = p.velocity + Offset(cameraOffsetX, cameraOffsetY)
                drawLine(
                    color = Color(0xFFD0BCFF),
                    start = beamStart,
                    end = screenPos,
                    strokeWidth = 5f
                )
                drawLine(
                    color = Color.White,
                    start = beamStart,
                    end = screenPos,
                    strokeWidth = 2f
                )
            } else {
                // SPIKE - draw wedge
                val spikeAngle = Math.toDegrees(atan2(p.velocity.y.toDouble(), p.velocity.x.toDouble())).toFloat()
                val rAngle = Math.toRadians(spikeAngle.toDouble())
                val endOff = Offset(
                    screenPos.x + cos(rAngle).toFloat() * 15f,
                    screenPos.y + sin(rAngle).toFloat() * 15f
                )
                drawLine(
                    color = Color(p.color),
                    start = screenPos,
                    end = endOff,
                    strokeWidth = 4f
                )
            }
        }

        // Draw Consumable Items
        items.forEach { item ->
            val screenPos = item.position + Offset(cameraOffsetX, cameraOffsetY)
            // check if on-screen
            if (screenPos.x >= -30f && screenPos.x <= viewportWidth + 30f &&
                screenPos.y >= -30f && screenPos.y <= viewportHeight + 30f
            ) {
                when (item.type) {
                    ItemType.GEM_BLUE -> {
                        drawCircle(
                            color = Color(0xFF00E5FF),
                            radius = 6f,
                            center = screenPos
                        )
                    }
                    ItemType.GEM_RED -> {
                        drawCircle(
                            color = Color(0xFFFF1744),
                            radius = 8f,
                            center = screenPos
                        )
                    }
                    ItemType.GEM_GOLD -> {
                        // Diamond shape
                        drawCircle(
                            color = Color(0xFFFFA000),
                            radius = 10f,
                            center = screenPos
                        )
                    }
                    ItemType.MEAT -> {
                        // Meat steak cross
                        drawCircle(
                            color = Color(0xFFFF8A80),
                            radius = 9f,
                            center = screenPos
                        )
                        drawLine(
                            color = Color.White,
                            start = screenPos - Offset(6f, 6f),
                            end = screenPos + Offset(6f, 6f),
                            strokeWidth = 3f
                        )
                    }
                    ItemType.MAGNET_BUFF -> {
                        drawCircle(
                            color = Color(0xFFE040FB),
                            radius = 9f,
                            center = screenPos
                        )
                    }
                }
            }
        }

        // Draw Enemies
        enemies.forEach { enemy ->
            val screenPos = enemy.position + Offset(cameraOffsetX, cameraOffsetY)
            if (screenPos.x >= -60f && screenPos.x <= viewportWidth + 60f &&
                screenPos.y >= -60f && screenPos.y <= viewportHeight + 60f
            ) {
                val cycle = ((System.currentTimeMillis() / 240) % 2).toInt()
                val color = getEnemyRenderColor(enemy.type)

                // BAT
                if (enemy.type == EnemyType.BAT) {
                    // Body
                    drawCircle(color = color, radius = enemy.type.radius, center = screenPos)
                    // Flapping wings
                    val wingSpread = if (cycle == 0) 18f else 10f
                    val wingHeig = if (cycle == 0) -8f else 4f
                    drawLine(
                        color = color,
                        start = screenPos,
                        end = screenPos + Offset(-wingSpread, wingHeig),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = color,
                        start = screenPos,
                        end = screenPos + Offset(wingSpread, wingHeig),
                        strokeWidth = 4f
                    )
                }
                // SKELETON
                else if (enemy.type == EnemyType.SKELETON) {
                    // Skull
                    drawCircle(color = color, radius = enemy.type.radius * 0.6f, center = screenPos - Offset(0f, 6f))
                    // Rib cage spine
                    drawLine(
                        color = color,
                        start = screenPos - Offset(0f, 2f),
                        end = screenPos + Offset(0f, 10f),
                        strokeWidth = 5f
                    )
                    // Arms
                    val cycleArmsX = if (cycle == 0) 8f else -8f
                    drawLine(
                        color = color,
                        start = screenPos,
                        end = screenPos + Offset(-12f, cycleArmsX),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = color,
                        start = screenPos,
                        end = screenPos + Offset(12f, cycleArmsX),
                        strokeWidth = 3f
                    )
                }
                // SLIME
                else if (enemy.type == EnemyType.SLIME) {
                    val floatSquish = if (cycle == 0) 0.8f else 1.2f
                    drawOval(
                        color = color,
                        topLeft = screenPos - Offset(enemy.type.radius, enemy.type.radius * floatSquish),
                        size = Size(enemy.type.radius * 2f, enemy.type.radius * 2f * floatSquish)
                    )
                }
                // ORC
                else if (enemy.type == EnemyType.ORC) {
                    drawCircle(color = color, radius = enemy.type.radius, center = screenPos)
                    drawCircle(color = Color.Black, radius = enemy.type.radius * 0.4f, center = screenPos)
                }
                // BOSS DRAGON
                else {
                    // Giant round beast head
                    drawCircle(color = color, radius = enemy.type.radius, center = screenPos)
                    // Horns
                    drawLine(
                        color = Color.White,
                        start = screenPos - Offset(20f, 10f),
                        end = screenPos - Offset(30f, 35f),
                        strokeWidth = 6f
                    )
                    drawLine(
                        color = Color.White,
                        start = screenPos + Offset(20f, -10f),
                        end = screenPos + Offset(30f, -35f),
                        strokeWidth = 6f
                    )
                    // Eyes
                    drawCircle(color = Color.Yellow, radius = 5f, center = screenPos - Offset(12f, 10f))
                    drawCircle(color = Color.Yellow, radius = 5f, center = screenPos + Offset(12f, -10f))
                }

                // Small Health bar under bosses/mob
                if (enemy.health < enemy.maxHealth) {
                    val barWidth = enemy.type.radius * 1.6f
                    val barHeight = 4f
                    val barTopLeft = screenPos + Offset(-barWidth / 2f, enemy.type.radius + 6f)
                    drawRect(
                        color = Color.DarkGray,
                        topLeft = barTopLeft,
                        size = Size(barWidth, barHeight)
                    )
                    drawRect(
                        color = Color.Green,
                        topLeft = barTopLeft,
                        size = Size(barWidth * (enemy.health / enemy.maxHealth).coerceIn(0f, 1f), barHeight)
                    )
                }
            }
        }

        // Draw Worm Lords (Sinuous bodies)
        worms.forEach { worm ->
            if (worm.isDead) return@forEach

            val headScreenPos = worm.headPosition + Offset(cameraOffsetX, cameraOffsetY)

            // Spells: PLASMA HALO (draw surrounding circles)
            if (worm.weapons.plasmaHalo > 0) {
                val rRadius = 85f + (worm.weapons.plasmaHalo * 15f)
                drawCircle(
                    color = Color(0x33FFA000), // golden aura
                    radius = rRadius,
                    center = headScreenPos
                )
                drawCircle(
                    color = Color(0xFFFFCC00),
                    radius = rRadius,
                    center = headScreenPos,
                    style = Stroke(width = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f))
                )
            }

            // Draw Body segments backwards
            worm.segments.forEachIndexed { idx, segment ->
                val ratio = (worm.length - idx).toFloat() / worm.length.toFloat()
                val segRadius = (16f * ratio).coerceAtLeast(6f)
                val segScreenPos = segment.position + Offset(cameraOffsetX, cameraOffsetY)

                // Alternate primary/accent colors for zebra worm style
                val bodyColor = if (idx % 2 == 0) Color(worm.colorPrimary) else Color(worm.colorAccent)

                drawCircle(
                    color = bodyColor,
                    radius = segRadius,
                    center = segScreenPos
                )
                // Draw a small shiny accent bead on segment
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = segRadius * 0.4f,
                    center = segScreenPos - Offset(segRadius * 0.25f, segRadius * 0.25f)
                )
            }

            // Draw HEAD of worm
            drawCircle(
                color = Color(worm.colorPrimary),
                radius = 18f,
                center = headScreenPos
            )
            // Head neon frame
            drawCircle(
                color = Color(worm.colorAccent),
                radius = 18f,
                center = headScreenPos,
                style = Stroke(width = 3f)
            )

            // Draw Snake Eyes looking in target angle direction
            val lookRad = Math.toRadians(worm.angle.toDouble())
            val eyeX = (cos(lookRad) * 10f).toFloat()
            val eyeY = (sin(lookRad) * 10f).toFloat()
            val rightEyeX = (cos(lookRad + PI/4) * 12f).toFloat()
            val rightEyeY = (sin(lookRad + PI/4) * 12f).toFloat()
            val leftEyeX = (cos(lookRad - PI/4) * 12f).toFloat()
            val leftEyeY = (sin(lookRad - PI/4) * 12f).toFloat()

            drawCircle(color = Color.Black, radius = 4f, center = headScreenPos + Offset(rightEyeX, rightEyeY))
            drawCircle(color = Color.White, radius = 1.8f, center = headScreenPos + Offset(rightEyeX + eyeX*0.2f, rightEyeY + eyeY*0.2f))

            drawCircle(color = Color.Black, radius = 4f, center = headScreenPos + Offset(leftEyeX, leftEyeY))
            drawCircle(color = Color.White, radius = 1.8f, center = headScreenPos + Offset(leftEyeX + eyeX*0.2f, leftEyeY + eyeY*0.2f))

            // Draw crown above Head if P1 (WORM LORD)
            if (worm.id == "p1") {
                val crownBase = headScreenPos - Offset(0f, 22f)
                drawLine(color = Color(0xFFFFD700), start = crownBase - Offset(10f, 0f), end = crownBase + Offset(10f, 0f), strokeWidth = 3f)
                drawLine(color = Color(0xFFFFD700), start = crownBase - Offset(10f, 0f), end = crownBase - Offset(8f, 12f), strokeWidth = 3f)
                drawLine(color = Color(0xFFFFD700), start = crownBase + Offset(10f, 0f), end = crownBase + Offset(8f, 12f), strokeWidth = 3f)
                drawLine(color = Color(0xFFFFD700), start = crownBase - Offset(8f, 12f), end = crownBase - Offset(0f, 4f), strokeWidth = 3f)
                drawLine(color = Color(0xFFFFD700), start = crownBase + Offset(8f, 12f), end = crownBase - Offset(0f, 4f), strokeWidth = 3f)
                drawLine(color = Color(0xFFFFD700), start = crownBase, end = crownBase - Offset(0f, 14f), strokeWidth = 3f)
            }

            // Draw orbiters rotating purple spells
            if (worm.weapons.orbitOrbs > 0) {
                worm.orbiters.forEach { orbAngle ->
                    val orbRad = Math.toRadians(orbAngle.toDouble())
                    val orbRadius = 75f
                    val orbPos = headScreenPos + Offset(
                        (cos(orbRad) * orbRadius).toFloat(),
                        (sin(orbRad) * orbRadius).toFloat()
                    )
                    drawCircle(
                        color = Color(0xFFE040FB),
                        radius = 10f,
                        center = orbPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = orbPos
                    )
                }
            }
        }

        // Draw sparks / splat particles
        particles.forEach { p ->
            val screenPos = p.position + Offset(cameraOffsetX, cameraOffsetY)
            drawCircle(
                color = Color(p.color).copy(alpha = p.alpha),
                radius = p.size * p.life,
                center = screenPos
            )
        }
    }
}

private fun getEnemyRenderColor(type: EnemyType): Color {
    return when (type) {
        EnemyType.BAT -> Color(0xFFC2185B)
        EnemyType.SKELETON -> Color(0xFFECEFF1)
        EnemyType.SLIME -> Color(0xFF00E676)
        EnemyType.ORC -> Color(0xFFE65100)
        EnemyType.DRAGON_BOSS -> Color(0xFFFF1744)
    }
}

@Composable
fun PlayerControlsOverlay(
    viewModel: GameViewModel,
    wormId: String,
    playerLabel: String,
    isLeftHand: Boolean,
    totalHeight: Float,
    totalWidth: Float
) {
    val worms by viewModel.worms.collectAsState()
    val gameTimeSeconds by viewModel.gameTimeSeconds.collectAsState()
    val worm = worms.find { it.id == wormId } ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // TOP GENERAL STATS PANEL
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color(0xE62B2930), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF49454F), RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = playerLabel.uppercase(),
                    color = Color(worm.colorAccent),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "TIME: ${formatTime(gameTimeSeconds)}",
                    color = Color(0xFFE6E1E5),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                // Pause trigger
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { viewModel.startGame() } // Toggle restarting or just reset
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Score, level, and XP gauge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "SCORE: ${worm.currentScore}",
                    color = Color(0xFFD0BCFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Lvl ${worm.level}",
                    color = Color(0xFFCCC2DC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Kills: ${worm.killCount}",
                    color = Color(0xFF938F99),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // XP bar
            LinearProgressIndicator(
                progress = { worm.xp / worm.nextLevelXp.toFloat() },
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color(0xFFD0BCFF),
                trackColor = Color(0xFF49454F)
            )
        }

        // HEAL / STATS BOTTOM OVERLAY
        Column(
            modifier = Modifier
                .align(if (isLeftHand) Alignment.BottomEnd else Alignment.BottomStart)
                .background(Color(0xE62B2930), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF49454F), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Heart & HP Progress
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "HP",
                    tint = Color(0xFFF2B8B5),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${worm.stats.currentHealth.toInt()}/${worm.stats.maxHealth.toInt()}",
                    color = Color(0xFFE6E1E5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            LinearProgressIndicator(
                progress = { (worm.stats.currentHealth / worm.stats.maxHealth).coerceIn(0f, 1f) },
                modifier = Modifier
                    .width(100.dp)
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFFF2B8B5),
                trackColor = Color(0xFF49454F)
            )

            // active weapon badges
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (worm.weapons.orbitOrbs > 0) WeaponBadge("Orb ${worm.weapons.orbitOrbs}", Color(0xFFE040FB))
                if (worm.weapons.spitAcid > 0) WeaponBadge("Acid ${worm.weapons.spitAcid}", Color(0xFF4CAF50))
                if (worm.weapons.spikeSpine > 0) WeaponBadge("Spike ${worm.weapons.spikeSpine}", Color(0xFFFFEB3B))
                if (worm.weapons.plasmaHalo > 0) WeaponBadge("Halo ${worm.weapons.plasmaHalo}", Color(0xFFFF9800))
                if (worm.weapons.stormStrike > 0) WeaponBadge("Storm ${worm.weapons.stormStrike}", Color(0xFF00E5FF))
            }
        }

        // VIRTUAL JOYSTICK ACTUATION
        VirtualJoystick(
            modifier = Modifier
                .align(if (isLeftHand) Alignment.BottomStart else Alignment.BottomEnd)
                .padding(bottom = 20.dp),
            onMove = { angle, strength ->
                val pIdx = if (wormId == "p1") 0 else 1
                viewModel.handleJoystickInput(pIdx, angle, strength)
            }
        )
    }
}

@Composable
fun WeaponBadge(name: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(text = name, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    onMove: (angle: Float, strength: Float) -> Unit
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    var currentOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(130.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        origin = startOffset
                        isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = currentOffset + dragAmount
                        val maxRadius = 110f
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        if (dist > maxRadius) {
                            currentOffset = Offset(
                                (newOffset.x / dist) * maxRadius,
                                (newOffset.y / dist) * maxRadius
                            )
                        } else {
                            currentOffset = newOffset
                        }

                        val curDist = sqrt(currentOffset.x * currentOffset.x + currentOffset.y * currentOffset.y)
                        val strength = (curDist / maxRadius).coerceIn(0f, 1f)
                        val angle = Math.toDegrees(atan2(currentOffset.y.toDouble(), currentOffset.x.toDouble())).toFloat()
                        onMove(angle, strength)
                    },
                    onDragEnd = {
                        currentOffset = Offset.Zero
                        isDragging = false
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        currentOffset = Offset.Zero
                        isDragging = false
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer pad
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = size.minDimension / 2.2f
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = size.minDimension / 2.2f
            )
            drawCircle(
                color = Color(0xFFD0BCFF).copy(alpha = 0.35f),
                radius = size.minDimension / 2.2f,
                style = Stroke(width = 3f)
            )
        }
        // Draggable knob
        Box(
            modifier = Modifier
                .offset {
                    androidx.compose.ui.unit.IntOffset(
                        currentOffset.x.toInt(),
                        currentOffset.y.toInt()
                    )
                }
                .size(54.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFD0BCFF), Color(0xFF381E72))
                    ),
                    shape = CircleShape
                )
                .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
        )
    }
}

@Composable
fun LevelUpChoiceDialog(
    wormName: String,
    choices: List<UpgradeChoice>,
    onChoiceSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .widthIn(max = 440.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
        border = BorderStroke(2.dp, Color(0xFFD0BCFF)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$wormName LEVELED UP!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD0BCFF),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Text(
                text = "Pick a mystical upgrade to evolve your titan segments",
                fontSize = 12.sp,
                color = Color(0xFFCCC2DC),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
                textAlign = TextAlign.Center
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                choices.forEach { choice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1B1F), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(12.dp))
                            .clickable { onChoiceSelected(choice.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF381E72), CircleShape)
                                .border(1.dp, Color(0xFFD0BCFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getUpgradeIcon(choice.iconName),
                                contentDescription = choice.title,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = choice.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E5)
                            )
                            Text(
                                text = choice.description,
                                fontSize = 11.sp,
                                color = Color(0xFFCCC2DC)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getUpgradeIcon(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (name) {
        "adjust" -> Icons.Default.Adjust
        "science" -> Icons.Default.Science
        "navigation" -> Icons.Default.NearMe
        "track_changes" -> Icons.Default.TrackChanges
        "bolt" -> Icons.Default.Bolt
        "speed" -> Icons.Default.Speed
        "favorite" -> Icons.Default.Favorite
        "healing" -> Icons.Default.Healing
        else -> Icons.Default.OfflineBolt
    }
}

@Composable
fun GameOverBoard(viewModel: GameViewModel) {
    val worms by viewModel.worms.collectAsState()
    val play1 = worms.find { it.id == "p1" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .widthIn(max = 440.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
        border = BorderStroke(2.dp, Color(0xFFF2B8B5)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WORM DEFEATED",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF2B8B5),
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Serif
            )

            Text(
                text = "Your segment health has collapsed to the swamp",
                fontSize = 13.sp,
                color = Color(0xFFCCC2DC),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Score Panel
            if (play1 != null) {
                Text(
                    text = "FINAL SCORE",
                    fontSize = 11.sp,
                    color = Color(0xFFD0BCFF),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${play1.currentScore}",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE6E1E5),
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "LEVEL REACHED", fontSize = 10.sp, color = Color(0xFF938F99))
                        Text(text = "${play1.level}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "VAMPS SLAIN", fontSize = 10.sp, color = Color(0xFF938F99))
                        Text(text = "${play1.killCount}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Buttons
            Button(
                onClick = { viewModel.startGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "RE-ENTER ARENA", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { viewModel.setScreen(Screen.TITLE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF2B2930),
                    contentColor = Color(0xFFD0BCFF)
                ),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "MAIN MENU", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ControlsScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { viewModel.setScreen(Screen.TITLE) }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "THE SAGA MANUAL",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1E5),
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "How to navigate and survive the worm arena floors",
                    fontSize = 13.sp,
                    color = Color(0xFFCCC2DC)
                )
            }

            // Guide lists
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ManualItem(
                        title = "Master the Slither Controls",
                        desc = "Drag the glowing physical virtual joystick in your corner of the screen. Your worm automatically slithers and turns towards your thumb direction. Maintain momentum and steer cleanly.",
                        icon = Icons.Default.Gamepad
                    )
                }
                item {
                    ManualItem(
                        title = "Automatic Magic Weapons",
                        desc = "Your primary magical upgrades fire on their own! Purple orbiters rotate to shred skeleton shields, lightning acts from the head, spikes launch from body parts, and toxic acid covers the swamp floor.",
                        icon = Icons.Default.AutoMode
                    )
                }
                item {
                    ManualItem(
                        title = "Gather Gems & Evolve Segments",
                        desc = "Slay vampire bats and slimes to spawn glowing XP Gems. Vacuum them up to level up. Evolving increases your maximum link size, completely restores your HP, and unlocks random upgrade selection cards.",
                        icon = Icons.Default.AddBox
                    )
                }
                item {
                    ManualItem(
                        title = "Lethal Body Crashes (Versus)",
                        desc = "In multiplayer modes, slithering face-first into another worm Lord's side is forbidden. Crash and you take heavy damage! Strategically snake around your friends and trap them in deadly coils.",
                        icon = Icons.Default.Dangerous
                    )
                }
            }

            // Acknowledge Button
            OutlinedButton(
                onClick = { viewModel.setScreen(Screen.TITLE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 300.dp)
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF2B2930),
                    contentColor = Color(0xFFD0BCFF)
                ),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "UNDERSTOOD", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ManualItem(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2B2930), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color(0xFFD0BCFF), modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, fontSize = 12.sp, color = Color(0xFFCCC2DC))
        }
    }
}

@Composable
fun HighScoresScreen(viewModel: GameViewModel) {
    val topScores by viewModel.topScores.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setScreen(Screen.TITLE) }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = "HALL OF LORDS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1E5),
                    fontFamily = FontFamily.Serif
                )

                IconButton(onClick = { viewModel.clearHighScores() }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear", tint = Color(0xFFF2B8B5))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (topScores.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = "Empty", tint = Color(0xFF49454F), modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "NO SANCTIONED RUN RECORDS", fontSize = 14.sp, color = Color(0xFF938F99), fontWeight = FontWeight.Bold)
                    Text(text = "Complete an arena survival score first to carve history", fontSize = 11.sp, color = Color(0xFF938F99))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .widthIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(topScores) { index, score ->
                        ScoreRecordRow(rank = index + 1, item = score)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { viewModel.setScreen(Screen.TITLE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 300.dp)
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF2B2930),
                    contentColor = Color(0xFFD0BCFF)
                ),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "BACK", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScoreRecordRow(rank: Int, item: HighScore) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(item.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2B2930), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#$rank",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = when (rank) {
                    1 -> Color(0xFFD0BCFF)
                    2 -> Color(0xFFCCC2DC)
                    3 -> Color(0xFFCCC2DC).copy(alpha = 0.7f)
                    else -> Color(0xFF938F99)
                },
                modifier = Modifier.width(36.dp)
            )

            Column {
                Text(text = item.playerName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.gameMode, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                    Text(text = "Lvl ${item.level} • $dateStr", fontSize = 10.sp, color = Color(0xFF938F99))
                }
            }
        }

        Text(
            text = "${item.score}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFE6E1E5),
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}
