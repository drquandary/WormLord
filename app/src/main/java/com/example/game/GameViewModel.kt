package com.example.game

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameDatabase
import com.example.data.HighScore
import com.example.data.HighScoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class Screen {
    SPLASH,
    TITLE,
    MODE_SELECT,
    GAMEPLAY,
    CONTROLS,
    HIGHSCORES
}

data class UpgradeChoice(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = androidx.room.Room.databaseBuilder(
        application,
        GameDatabase::class.java,
        "worm_lord_db"
    ).build()

    private val repository = HighScoreRepository(db.highScoreDao)

    val topScores: StateFlow<List<HighScore>> = repository.topScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.SOLO)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    // Map Dimensions
    val arenaWidth = 2200f
    val arenaHeight = 2200f

    // Active Game Variables
    private val _worms = MutableStateFlow<List<Worm>>(emptyList())
    val worms: StateFlow<List<Worm>> = _worms.asStateFlow()

    private val _enemies = MutableStateFlow<List<Enemy>>(emptyList())
    val enemies: StateFlow<List<Enemy>> = _enemies.asStateFlow()

    private val _gameItems = MutableStateFlow<List<GameItem>>(emptyList())
    val gameItems: StateFlow<List<GameItem>> = _gameItems.asStateFlow()

    private val _projectiles = MutableStateFlow<List<Projectile>>(emptyList())
    val projectiles: StateFlow<List<Projectile>> = _projectiles.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private val _gameTimeSeconds = MutableStateFlow(0)
    val gameTimeSeconds: StateFlow<Int> = _gameTimeSeconds.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    // Level up choices queue (mapped by Worm ID)
    private val _levelUpPending = MutableStateFlow<Map<String, List<UpgradeChoice>>>(emptyMap())
    val levelUpPending: StateFlow<Map<String, List<UpgradeChoice>>> = _levelUpPending.asStateFlow()

    // High Score insertion status name
    private val _playerNameInput = MutableStateFlow("Lord Slither")
    val playerNameInput: StateFlow<String> = _playerNameInput.asStateFlow()

    private var gameLoopJob: Job? = null

    init {
        // Go from SPLASH to TITLE screen soon
        viewModelScope.launch {
            delay(2200)
            _currentScreen.value = Screen.TITLE
        }
    }

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setGameMode(mode: GameMode) {
        _gameMode.value = mode
    }

    fun setPlayerName(name: String) {
        _playerNameInput.value = name
    }

    fun startGame() {
        _isPaused.value = false
        _isGameOver.value = false
        _levelUpPending.value = emptyMap()
        _gameTimeSeconds.value = 0
        _enemies.value = emptyList()
        _projectiles.value = emptyList()
        _particles.value = emptyList()

        // Distribute initial gems/mushrooms around map
        val items = mutableListOf<GameItem>()
        repeat(55) {
            items.add(createRandomItem())
        }
        _gameItems.value = items

        // Initialize players & bot worms
        val activeWorms = mutableListOf<Worm>()

        // Player 1
        activeWorms.add(
            createWorm(
                id = "p1",
                name = _playerNameInput.value.ifBlank { "Player 1" },
                startPos = Offset(arenaWidth / 2f - 150f, arenaHeight / 2f + 150f),
                colorPrimary = 0xFFD0BCFF.toInt(), // Lavender
                colorAccent = 0xFF381E72.toInt(),  // Violet
                isBot = false
            ).apply {
                weapons.orbitOrbs = 1 // Start with 1 orbiter
            }
        )

        // Player 2 if multiplayer
        if (_gameMode.value != GameMode.SOLO) {
            activeWorms.add(
                createWorm(
                    id = "p2",
                    name = "Player 2",
                    startPos = Offset(arenaWidth / 2f + 150f, arenaHeight / 2f + 150f),
                    colorPrimary = 0xFFF2B8B5.toInt(), // Soft Coral
                    colorAccent = 0xFF381E72.toInt(),  // Violet
                    isBot = false
                ).apply {
                    weapons.spitAcid = 1 // Start with spit acid
                }
            )
        }

        // Add 4 AI Bot worms
        val botNames = listOf("Slytherick", "Cobranom", "Basilisk", "Vipera", "Naga Shadow")
        val botColorsPrimary = listOf(0xFF9C27B0, 0xFF3F51B5, 0xFF2196F3, 0xFF009688, 0xFFFF9800)
        val botColorsAccent = listOf(0xFFE040FB, 0xFF8C9EFF, 0xFF82B1FF, 0xFFA7FFEB, 0xFFFFD740)

        repeat(4) { idx ->
            activeWorms.add(
                createWorm(
                    id = "bot_$idx",
                    name = botNames[idx],
                    startPos = Offset(Random.nextFloat() * arenaWidth, Random.nextFloat() * arenaHeight),
                    colorPrimary = botColorsPrimary[idx].toInt(),
                    colorAccent = botColorsAccent[idx].toInt(),
                    isBot = true
                ).apply {
                    // Bots start slightly upgraded
                    weapons.orbitOrbs = Random.nextInt(0, 2)
                    weapons.spikeSpine = Random.nextInt(0, 2)
                }
            )
        }

        _worms.value = activeWorms

        _currentScreen.value = Screen.GAMEPLAY

        // Cancel previous loop if running
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTimerUpdateTime = System.currentTimeMillis()
            var lastSpawnTime = System.currentTimeMillis()

            while (!_isGameOver.value) {
                if (!_isPaused.value && _levelUpPending.value.isEmpty()) {
                    val now = System.currentTimeMillis()

                    // 1. Game Timer
                    if (now - lastTimerUpdateTime >= 1000) {
                        _gameTimeSeconds.value += 1
                        lastTimerUpdateTime = now
                    }

                    // 2. Enemy Spawning Wave System
                    if (now - lastSpawnTime >= getSpawnInterval(_gameTimeSeconds.value)) {
                        spawnWave()
                        lastSpawnTime = now
                    }

                    // 3. Update Game Simulation Tick
                    updateGameStates()
                }

                delay(16) // ~60fps ticker
            }
        }
    }

    private fun createWorm(
        id: String,
        name: String,
        startPos: Offset,
        colorPrimary: Int,
        colorAccent: Int,
        isBot: Boolean
    ): Worm {
        val w = Worm(
            id = id,
            name = name,
            headPosition = startPos,
            colorPrimary = colorPrimary,
            colorAccent = colorAccent,
            isBot = isBot,
            length = 8
        )
        // Add starting segments backwards
        repeat(w.length) { i ->
            w.segments.add(
                WormSegment(
                    position = startPos - Offset(0f, (i + 1) * 20f),
                    angle = 270f
                )
            )
        }
        return w
    }

    private fun createRandomItem(): GameItem {
        val types = ItemType.values()
        val r = Random.nextFloat()
        val type = when {
            r < 0.60f -> ItemType.GEM_BLUE  // Standard XP
            r < 0.80f -> ItemType.GEM_RED   // Med XP
            r < 0.90f -> ItemType.GEM_GOLD  // Large XP
            r < 0.97f -> ItemType.MEAT      // Health drop
            else -> ItemType.MAGNET_BUFF    // Range boost
        }

        val xp = when (type) {
            ItemType.GEM_BLUE -> 10
            ItemType.GEM_RED -> 35
            ItemType.GEM_GOLD -> 100
            else -> 0
        }

        val heal = if (type == ItemType.MEAT) 35f else 0f

        return GameItem(
            id = java.util.UUID.randomUUID().toString(),
            type = type,
            position = Offset(
                Random.nextFloat() * (arenaWidth - 80f) + 40f,
                Random.nextFloat() * (arenaHeight - 80f) + 40f
            ),
            xpReward = xp,
            healAmount = heal
        )
    }

    private fun getSpawnInterval(timeSec: Int): Long {
        return when {
            timeSec < 30 -> 3000L   // Every 3.0s
            timeSec < 90 -> 2200L   // Every 2.2s
            timeSec < 180 -> 1500L  // Every 1.5s
            timeSec < 300 -> 1000L  // Every 1s
            else -> 600L            // Mega swarm
        }
    }

    private fun spawnWave() {
        val now = _gameTimeSeconds.value
        val numEnemiesToSpawn = when {
            now < 30 -> 3
            now < 90 -> 5
            now < 180 -> 8
            now < 300 -> 14
            else -> 20
        }

        val currentWorms = _worms.value.filter { !it.isDead }
        if (currentWorms.isEmpty()) return

        val newEnemies = _enemies.value.toMutableList()

        repeat(numEnemiesToSpawn) {
            // Pick a focus target to spawn around
            val targetWorm = currentWorms[Random.nextInt(currentWorms.size)]

            // Spawn at random perimeter around this target worm (offscreen)
            val angle = Random.nextFloat() * 360f
            val radAngle = Math.toRadians(angle.toDouble())
            val spawnDistance = Random.nextFloat() * 300f + 600f // 600 - 900 px
            val spawnPos = Offset(
                (targetWorm.headPosition.x + cos(radAngle) * spawnDistance).toFloat().coerceIn(0f, arenaWidth),
                (targetWorm.headPosition.y + sin(radAngle) * spawnDistance).toFloat().coerceIn(0f, arenaHeight)
            )

            // Select enemy type based on current time
            val type = when {
                now >= 240 && Random.nextFloat() < 0.05f -> EnemyType.DRAGON_BOSS
                now >= 120 && Random.nextFloat() < 0.20f -> EnemyType.ORC
                now >= 60 && Random.nextFloat() < 0.35f -> EnemyType.SLIME
                now >= 30 && Random.nextFloat() < 0.40f -> EnemyType.SKELETON
                else -> EnemyType.BAT
            }

            newEnemies.add(
                Enemy(
                    id = java.util.UUID.randomUUID().toString(),
                    type = type,
                    position = spawnPos,
                    health = type.maxHealth,
                    maxHealth = type.maxHealth
                )
            )
        }

        _enemies.value = newEnemies
    }

    fun handleJoystickInput(playerIdx: Int, angleVal: Float, strength: Float) {
        if (_isPaused.value || _isGameOver.value) return
        val current = _worms.value.toMutableList()
        val targetId = if (playerIdx == 0) "p1" else "p2"

        val wormIdx = current.indexOfFirst { it.id == targetId }
        if (wormIdx != -1) {
            val worm = current[wormIdx]
            if (!worm.isDead) {
                worm.targetAngle = angleVal
                worm.speedMultiplier = when {
                    strength > 0.8f -> 1.4f // Boost slightly at full joystick push
                    strength > 0.1f -> 1.0f
                    else -> 0f // Stop if released completely
                }
                _worms.value = current
            }
        }
    }

    private fun updateGameStates() {
        val activeWorms = _worms.value.toMutableList()
        val currentEnemies = _enemies.value.toMutableList()
        val currentItems = _gameItems.value.toMutableList()
        val currentProjectiles = _projectiles.value.toMutableList()
        val currentParticles = _particles.value.toMutableList()

        val nowMs = System.currentTimeMillis()

        // 1. UPDATE WORMS AND SEGMENT BEHAVIORS
        activeWorms.forEachIndexed { wormIdx, worm ->
            if (worm.isDead) return@forEachIndexed

            // AI Decision Making for bots
            if (worm.isBot) {
                runBotAi(worm, currentItems, currentEnemies)
            }

            // Interpolate toward targetAngle
            // Handle angle wraparound smartly
            var diff = worm.targetAngle - worm.angle
            while (diff < -180) diff += 360
            while (diff > 180) diff -= 360

            val turnSpeed = if (worm.isBot) 5f else 8f
            worm.angle += (diff * 0.18f).coerceIn(-turnSpeed, turnSpeed)

            // If moving
            if (worm.speedMultiplier > 0.1f) {
                val rad = Math.toRadians(worm.angle.toDouble())
                val step = worm.stats.speed * worm.speedMultiplier
                val dx = (cos(rad) * step).toFloat()
                val dy = (sin(rad) * step).toFloat()

                // Move head in bounds
                val finalHeadPos = Offset(
                    (worm.headPosition.x + dx).coerceIn(15f, arenaWidth - 15f),
                    (worm.headPosition.y + dy).coerceIn(15f, arenaHeight - 15f)
                )

                // Check edge crash
                if (finalHeadPos.x <= 15f || finalHeadPos.x >= arenaWidth - 15f ||
                    finalHeadPos.y <= 15f || finalHeadPos.y >= arenaHeight - 15f
                ) {
                    // Turn away
                    worm.targetAngle = (worm.targetAngle + 180f) % 360f
                }

                worm.headPosition = finalHeadPos

                // Move segments
                if (worm.segments.isNotEmpty()) {
                    // Lead segment follows head
                    var prevPos = worm.headPosition
                    var prevAngle = worm.angle

                    for (sIdx in 0 until worm.segments.size) {
                        val segment = worm.segments[sIdx]
                        val segPos = segment.position
                        val segAngle = segment.angle

                        // Target vector
                        val toPrev = prevPos - segPos
                        val dist = sqrt(toPrev.x * toPrev.x + toPrev.y * toPrev.y)
                        val desiredDist = 18f

                        var newPos = segPos
                        var newAngle = segAngle

                        if (dist > desiredDist) {
                            val ratio = (dist - desiredDist) / dist
                            newPos = Offset(
                                segPos.x + toPrev.x * ratio,
                                segPos.y + toPrev.y * ratio
                            )
                            newAngle = Math.toDegrees(atan2(toPrev.y.toDouble(), toPrev.x.toDouble())).toFloat()
                        }

                        worm.segments[sIdx] = WormSegment(newPos, newAngle)
                        prevPos = newPos
                        prevAngle = newAngle
                    }
                }
            }

            // Increment timers
            worm.timeSinceLastSpike += 16L
            worm.timeSinceLastSpit += 16L
            worm.timeSinceLastHalo += 16L
            worm.timeSinceLastStorm += 16L

            // Trigger Weapons automatic firing
            fireWormWeapons(worm, currentProjectiles, currentEnemies)
        }

        // 2. PROJECTILE SIMULATION
        val projectileIterator = currentProjectiles.iterator()
        while (projectileIterator.hasNext()) {
            val proj = projectileIterator.next()
            proj.position += proj.velocity
            proj.durationFrames--

            // COLLISION: Projectiles vs Enemies
            if (proj.type == "SPIT") {
                // Spit creates acid puddles on ground; wait, acid puddles are stationary
                proj.velocity = Offset.Zero // Static hazard
            }

            var hit = false
            currentEnemies.forEach { enemy ->
                val toEnemy = enemy.position - proj.position
                val dist = sqrt(toEnemy.x * toEnemy.x + toEnemy.y * toEnemy.y)
                val isImmune = proj.type == "SPIT" && proj.durationFrames % 10 != 0 // Acid ticks every 10 frames

                if (dist < (enemy.type.radius + proj.radius) && !isImmune) {
                    val wormOwner = activeWorms.find { it.id == proj.sourceId }
                    val finalDmg = proj.damage * (wormOwner?.stats?.damageMultiplier ?: 1f)
                    enemy.health -= finalDmg

                    // Visual sparks
                    createDamageSparks(proj.position, proj.color, currentParticles)

                    if (proj.type == "SPIKE" || proj.type == "LIGHTNING") {
                        hit = true // pierce/destroy bullet on collision
                    }
                }
            }

            if (proj.durationFrames <= 0 || (hit && proj.type == "SPIKE")) {
                projectileIterator.remove()
            }
        }

        // 3. ENEMY BEHAVIOR & MOVEMENT
        currentEnemies.forEachIndexed { eIdx, enemy ->
            // Chase the nearest active worm (or player)
            val nearestWorm = activeWorms
                .filter { !it.isDead }
                .minByOrNull {
                    val dx = it.headPosition.x - enemy.position.x
                    val dy = it.headPosition.y - enemy.position.y
                    dx * dx + dy * dy
                }

            if (nearestWorm != null) {
                val dx = nearestWorm.headPosition.x - enemy.position.x
                val dy = nearestWorm.headPosition.y - enemy.position.y
                val dist = sqrt(dx * dx + dy * dy)

                if (dist > 5f) {
                    enemy.position += Offset(
                        (dx / dist) * enemy.type.speed,
                        (dy / dist) * enemy.type.speed
                    )
                }

                // Impact damage on player / worm
                if (dist < (enemy.type.radius + 18f)) {
                    // Deal periodic damage
                    if (nowMs % 250 < 20) {
                        nearestWorm.stats.currentHealth -= enemy.type.damage
                        createBloodSplat(nearestWorm.headPosition, currentParticles)

                        if (nearestWorm.stats.currentHealth <= 0f) {
                            handleWormDeath(nearestWorm, currentItems, currentParticles)
                        }
                    }
                }
            }
        }

        // 4. ITEM COLLECTION PHYSICS
        val itemIterator = currentItems.iterator()
        while (itemIterator.hasNext()) {
            val item = itemIterator.next()

            activeWorms.filter { !it.isDead }.forEach { worm ->
                val toItem = item.position - worm.headPosition
                val dist = sqrt(toItem.x * toItem.x + toItem.y * toItem.y)
                val currentMagnet = worm.stats.magnetRange

                // Magnet attraction
                if (dist < currentMagnet) {
                    val pullSpeed = (currentMagnet - dist) / 12f + 1f
                    item.position = Offset(
                        item.position.x + (-toItem.x / dist) * pullSpeed,
                        item.position.y + (-toItem.y / dist) * pullSpeed
                    )
                }

                // Vacuum touch
                if (dist < 26f) {
                    if (item.type == ItemType.MEAT) {
                        worm.stats.currentHealth = (worm.stats.currentHealth + item.healAmount)
                            .coerceAtMost(worm.stats.maxHealth)
                    } else if (item.type == ItemType.MAGNET_BUFF) {
                        worm.stats.magnetRange *= 1.5f
                        // boost magnet temporary duration inside loop
                    } else {
                        // Gems
                        worm.currentScore += item.xpReward
                        worm.xp += item.xpReward

                        // Check Level Up
                        if (worm.xp >= worm.nextLevelXp && !worm.isBot) {
                            triggerLevelUp(worm)
                        } else if (worm.xp >= worm.nextLevelXp && worm.isBot) {
                            // Bot auto chooses upgrade
                            botAutoUpgrade(worm)
                        }
                    }

                    // Collect item particles
                    repeat(3) {
                        currentParticles.add(
                            Particle(
                                position = item.position,
                                velocity = Offset(Random.nextFloat() * 4f - 2f, Random.nextFloat() * 4f - 2f),
                                color = getGemColor(item.type),
                                size = 6f,
                                life = 1.0f,
                                decay = 0.05f
                            )
                        )
                    }

                    try {
                        itemIterator.remove()
                    } catch (e: Exception) {
                        // ignore double deletion
                    }
                    return@forEach
                }
            }
        }

        // 5. SLITHER COLLISIONS: Head vs rival segment crash
        activeWorms.forEach { w1 ->
            if (w1.isDead) return@forEach

            activeWorms.forEach { w2 ->
                if (w2.id == w1.id) return@forEach // Self body collision ignored mostly to allow coils

                // Check w1 head hitting w2 segments
                w2.segments.forEachIndexed { sIdx, seg ->
                    val dx = w1.headPosition.x - seg.position.x
                    val dy = w1.headPosition.y - seg.position.y
                    val dist = sqrt(dx * dx + dy * dy)

                    if (dist < 22f) {
                        if (_gameMode.value == GameMode.VERSUS || w1.isBot || w2.isBot) {
                            // Head-on segment crash is lethal or highly damaging!
                            w1.stats.currentHealth -= 34f // Deals massive chunk damage
                            createDamageSparks(w1.headPosition, 0xFFFF0000.toInt(), currentParticles)

                            if (w1.stats.currentHealth <= 0f) {
                                handleWormDeath(w1, currentItems, currentParticles)
                                if (w2.id == "p1" || w2.id == "p2") {
                                    w2.killCount++
                                    w2.currentScore += 300 // Bonus score for defeating rival lords
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. ENEMY DEATHS & CLEANUPS
        val enemyIterator = currentEnemies.iterator()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            if (enemy.health <= 0f) {
                // Drop drops
                val dropChance = Random.nextFloat()
                val isMeat = dropChance < 0.15f
                val isMagnet = dropChance > 0.96f

                val item = GameItem(
                    id = java.util.UUID.randomUUID().toString(),
                    type = when {
                        isMeat -> ItemType.MEAT
                        isMagnet -> ItemType.MAGNET_BUFF
                        enemy.type == EnemyType.DRAGON_BOSS -> ItemType.GEM_GOLD
                        enemy.type == EnemyType.ORC -> ItemType.GEM_RED
                        else -> ItemType.GEM_BLUE
                    },
                    position = enemy.position,
                    xpReward = when (enemy.type) {
                        EnemyType.BAT -> 15
                        EnemyType.SKELETON -> 30
                        EnemyType.SLIME -> 20
                        EnemyType.ORC -> 60
                        EnemyType.DRAGON_BOSS -> 600
                    },
                    healAmount = if (isMeat) 30f else 0f
                )
                currentItems.add(item)

                // Add death blast particle splash
                repeat(8) {
                    currentParticles.add(
                        Particle(
                            position = enemy.position,
                            velocity = Offset(Random.nextFloat() * 6f - 3f, Random.nextFloat() * 6f - 3f),
                            color = getEnemyColor(enemy.type),
                            size = 8f,
                            life = 1.0f,
                            decay = 0.04f
                        )
                    )
                }

                // Score for nearest player
                activeWorms.filter { !it.isDead && !it.isBot }.minByOrNull {
                    val d = it.headPosition - enemy.position
                    d.x * d.x + d.y * d.y
                }?.let { worm ->
                    worm.killCount++
                    worm.currentScore += enemy.type.xpValue

                    // Vamp Fangs check
                    if (worm.stats.vampFangs > 0) {
                        if (Random.nextInt(100) < worm.stats.vampFangs) {
                            worm.stats.currentHealth = (worm.stats.currentHealth + 3f)
                                .coerceAtMost(worm.stats.maxHealth)
                        }
                    }
                }

                enemyIterator.remove()
            }
        }

        // 7. PARTICLES
        val particleIterator = currentParticles.iterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            p.position += p.velocity
            p.life -= p.decay
            p.alpha = p.life
            if (p.life <= 0) {
                particleIterator.remove()
            }
        }

        // Repopulate food if below threshold
        if (currentItems.size < 40) {
            repeat(15) {
                currentItems.add(createRandomItem())
            }
        }

        // Check overall game over
        val humansActive = activeWorms.filter { !it.isDead && !it.isBot }
        if (humansActive.isEmpty()) {
            _isGameOver.value = true
            saveScoresToDb()
        }

        // Apply changes
        _worms.value = activeWorms
        _enemies.value = currentEnemies
        _gameItems.value = currentItems
        _projectiles.value = currentProjectiles
        _particles.value = currentParticles
    }

    private fun handleWormDeath(worm: Worm, currentItems: MutableList<GameItem>, currentParticles: MutableList<Particle>) {
        worm.isDead = true
        // Turn segments into lots of golden gems!
        worm.segments.forEach { seg ->
            currentItems.add(
                GameItem(
                    id = java.util.UUID.randomUUID().toString(),
                    type = ItemType.GEM_RED,
                    position = seg.position,
                    xpReward = 40
                )
            )

            currentParticles.add(
                Particle(
                    position = seg.position,
                    velocity = Offset(Random.nextFloat() * 8f - 4f, Random.nextFloat() * 8f - 4f),
                    color = worm.colorPrimary,
                    size = 12f,
                    life = 1.0f,
                    decay = 0.03f
                )
            )
        }

        // Respawn AI bots
        if (worm.isBot) {
            viewModelScope.launch {
                delay(4000)
                if (!_isGameOver.value) {
                    val bots = _worms.value.toMutableList()
                    val botNames = listOf("Slytherick", "Cobranom", "Basilisk", "Vipera", "Naga Shadow")
                    val botColorsPrimary = listOf(0xFF9C27B0, 0xFF3F51B5, 0xFF2196F3, 0xFF009688, 0xFFFF9800)
                    val botColorsAccent = listOf(0xFFE040FB, 0xFF8C9EFF, 0xFF82B1FF, 0xFFA7FFEB, 0xFFFFD740)
                    val idx = Random.nextInt(5)

                    val b = createWorm(
                        id = "bot_${System.currentTimeMillis()}",
                        name = botNames[idx] + " II",
                        startPos = Offset(Random.nextFloat() * arenaWidth, Random.nextFloat() * arenaHeight),
                        colorPrimary = botColorsPrimary[idx].toInt(),
                        colorAccent = botColorsAccent[idx].toInt(),
                        isBot = true
                    ).apply {
                        weapons.orbitOrbs = Random.nextInt(0, 3)
                        weapons.spitAcid = Random.nextInt(0, 2)
                    }

                    bots.add(b)
                    _worms.value = bots
                }
            }
        }
    }

    private fun runBotAi(bot: Worm, items: List<GameItem>, enemies: List<Enemy>) {
        // Move towards closest gem/mushrooms, or run if health is low
        val head = bot.headPosition
        if (bot.stats.currentHealth < 22f && enemies.isNotEmpty()) {
            // Evasion mode: steer away from closest enemy
            val closestEnemy = enemies.minByOrNull {
                val dx = it.position.x - head.x
                val dy = it.position.y - head.y
                dx * dx + dy * dy
            }
            if (closestEnemy != null) {
                val dx = head.x - closestEnemy.position.x
                val dy = head.y - closestEnemy.position.y
                bot.targetAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                bot.speedMultiplier = 1.3f
                return
            }
        }

        // Standard feeding AI: Go to closest item
        val closestItem = items.minByOrNull {
            val dx = it.position.x - head.x
            val dy = it.position.y - head.y
            dx * dx + dy * dy
        }

        if (closestItem != null) {
            val dx = closestItem.position.x - head.x
            val dy = closestItem.position.y - head.y
            bot.targetAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            bot.speedMultiplier = 1.0f
        }
    }

    private fun fireWormWeapons(worm: Worm, currentProjectiles: MutableList<Projectile>, currentEnemies: List<Enemy>) {
        val head = worm.headPosition

        // 1. SPIKE SPINE: Fires spikes in all directions from body segments
        if (worm.weapons.spikeSpine > 0 && worm.timeSinceLastSpike >= 1800L - (worm.weapons.spikeSpine * 150)) {
            worm.timeSinceLastSpike = 0L

            // Fire spike from every third segment
            for (idx in 0 until worm.segments.size step 3) {
                val seg = worm.segments.getOrNull(idx) ?: continue

                // Find angle perpendicular to segment
                val spikeAngle1 = seg.angle + 90f
                val spikeAngle2 = seg.angle - 90f

                fun addSpike(ang: Float) {
                    val rAngle = Math.toRadians(ang.toDouble())
                    val dirX = cos(rAngle).toFloat()
                    val dirY = sin(rAngle).toFloat()

                    currentProjectiles.add(
                        Projectile(
                            id = java.util.UUID.randomUUID().toString(),
                            type = "SPIKE",
                            position = seg.position,
                            velocity = Offset(dirX * 6.5f, dirY * 6.5f),
                            damage = 22f + (worm.weapons.spikeSpine * 5f),
                            durationFrames = 130,
                            radius = 6f,
                            color = worm.colorAccent,
                            sourceId = worm.id
                        )
                    )
                }

                addSpike(spikeAngle1)
                addSpike(spikeAngle2)
            }
        }

        // 2. ORBITER ORBS: Rotating orba around head (Collision handled instantly, position updated)
        if (worm.weapons.orbitOrbs > 0) {
            // Update orbits
            val count = worm.weapons.orbitOrbs
            worm.orbiters.clear()
            val baseAngle = (System.currentTimeMillis() / 8L) % 360f

            repeat(count) { idx ->
                val calculatedAngle = baseAngle + (idx * (360f / count))
                worm.orbiters.add(calculatedAngle)

                // Sweep contact damage
                val rad = Math.toRadians(calculatedAngle.toDouble())
                val orbitRadius = 75f
                val oOffset = Offset(
                    (head.x + cos(rad) * orbitRadius).toFloat(),
                    (head.y + sin(rad) * orbitRadius).toFloat()
                )

                // Damage any enemies near that orbit node
                currentEnemies.forEach { enemy ->
                    val dist = (enemy.position - oOffset).let { sqrt(it.x * it.x + it.y * it.y) }
                    if (dist < (enemy.type.radius + 15f)) {
                        if (System.currentTimeMillis() % 250 < 20) {
                            enemy.health -= 12f + (worm.weapons.orbitOrbs * 3f)
                        }
                    }
                }
            }
        }

        // 3. ACID SPIT: Spawns damaging hazardpools automatically near nearest enemies
        if (worm.weapons.spitAcid > 0 && worm.timeSinceLastSpit >= 2000L - (worm.weapons.spitAcid * 180)) {
            worm.timeSinceLastSpit = 0L

            // Find closest enemy
            val closest = currentEnemies.minByOrNull { (it.position - head).let { d -> d.x * d.x + d.y * d.y } }
            val spitPos = closest?.position ?: (head + Offset(Random.nextFloat() * 200f - 100f, Random.nextFloat() * 200f - 100f))

            currentProjectiles.add(
                Projectile(
                    id = java.util.UUID.randomUUID().toString(),
                    type = "SPIT",
                    position = spitPos,
                    velocity = Offset.Zero,
                    damage = 7f + (worm.weapons.spitAcid * 3f),
                    durationFrames = 150 + (worm.weapons.spitAcid * 15), // toxic pool lasts ~2.5s
                    radius = 35f + (worm.weapons.spitAcid * 5f),
                    color = 0xAA4CAF50.toInt(), // translucent acid green
                    sourceId = worm.id
                )
            )
        }

        // 4. PLASMA HALO: Protective ring around head
        if (worm.weapons.plasmaHalo > 0) {
            val ringRadius = 85f + (worm.weapons.plasmaHalo * 15f)
            currentEnemies.forEach { enemy ->
                val dist = (enemy.position - head).let { sqrt(it.x * it.x + it.y * it.y) }
                if (dist < ringRadius) {
                    if (System.currentTimeMillis() % 250 < 20) {
                        enemy.health -= 6f + (worm.weapons.plasmaHalo * 2f)
                    }
                }
            }
        }

        // 5. STORM STRIKE: Periodic lightning bolt towards closest enemy
        if (worm.weapons.stormStrike > 0 && worm.timeSinceLastStorm >= 2500L - (worm.weapons.stormStrike * 250)) {
            val closest = currentEnemies.minByOrNull { (it.position - head).let { d -> d.x * d.x + d.y * d.y } }
            if (closest != null) {
                val dist = (closest.position - head).let { sqrt(it.x * it.x + it.y * it.y) }
                if (dist < 400f) {
                    worm.timeSinceLastStorm = 0L

                    closest.health -= 45f + (worm.weapons.stormStrike * 15f)

                    // Draw momentary beam projectile
                    currentProjectiles.add(
                        Projectile(
                            id = java.util.UUID.randomUUID().toString(),
                            type = "LIGHTNING",
                            position = closest.position,
                            velocity = Offset.Zero,
                            damage = 0f, // Instant damage dealt above
                            durationFrames = 10,
                            radius = 12f,
                            color = 0xFF00E5FF.toInt(),
                            sourceId = worm.id
                        ).apply {
                            // Use velocity to save start coordinate for visual lightning drawing
                            velocity = head
                        }
                    )
                }
            }
        }
    }

    private fun botAutoUpgrade(bot: Worm) {
        bot.level++
        bot.nextLevelXp += 150
        bot.length += 1
        // Append section segment
        val tail = bot.segments.lastOrNull()?.position ?: bot.headPosition
        bot.segments.add(WormSegment(tail - Offset(0f, 20f), bot.angle))

        // Increase arbitrary stats or weapon levels
        val r = Random.nextInt(5)
        when (r) {
            0 -> bot.weapons.orbitOrbs = (bot.weapons.orbitOrbs + 1).coerceAtMost(5)
            1 -> bot.weapons.spikeSpine = (bot.weapons.spikeSpine + 1).coerceAtMost(5)
            2 -> bot.weapons.spitAcid = (bot.weapons.spitAcid + 1).coerceAtMost(5)
            3 -> bot.stats.speed += 0.3f
            4 -> {
                bot.stats.maxHealth += 20f
                bot.stats.currentHealth = bot.stats.maxHealth
            }
        }
    }

    private fun triggerLevelUp(worm: Worm) {
        worm.level++
        worm.nextLevelXp = (worm.nextLevelXp * 1.35f + 120f).toInt()

        // Grow length
        worm.length += 1
        val lastPos = worm.segments.lastOrNull()?.position ?: worm.headPosition
        worm.segments.add(WormSegment(lastPos, worm.angle))

        // Fully heal
        worm.stats.currentHealth = worm.stats.maxHealth

        // Generate 3 choices of upgrades
        val allOptions = listOf(
            UpgradeChoice("orbitOrbs", "Purple Orbiters", "Rotate lethal mystical orbs around your head.", "adjust"),
            UpgradeChoice("spitAcid", "Toxic Acid Spit", "Spit green hazard pools that corrode enemy hordes.", "science"),
            UpgradeChoice("spikeSpine", "Spike Spine", "Shoot sharp spikes sideways from body segments.", "navigation"),
            UpgradeChoice("plasmaHalo", "Plasma Halo", "Surround your head with a disintegrating energy circle.", "track_changes"),
            UpgradeChoice("stormStrike", "Storm Strike", "Strike nearby enemies with periodic blue lightning bolts.", "bolt"),
            UpgradeChoice("speed", "Worm Speed Boost", "Increase your steering and crawl speed +15%.", "speed"),
            UpgradeChoice("health", "Hearty Segments", "Gain +40 Max Health and full recovery.", "favorite"),
            UpgradeChoice("vamp", "Vampiric Fangs", "Gain 10% chance to restore 3 HP on enemy defeats.", "healing"),
            UpgradeChoice("magnet", "Cosmic Attraction", "Add 50% magnetic pickup radius for XP gems.", "fullscreen")
        )

        val shuffled = allOptions.shuffled()
        val choices = shuffled.take(3)

        val currentMap = _levelUpPending.value.toMutableMap()
        currentMap[worm.id] = choices
        _levelUpPending.value = currentMap
    }

    fun makeUpgradeSelection(wormId: String, choiceId: String) {
        val currentWorms = _worms.value.toMutableList()
        val wormIdx = currentWorms.indexOfFirst { it.id == wormId }

        if (wormIdx != -1) {
            val worm = currentWorms[wormIdx]
            when (choiceId) {
                "orbitOrbs" -> worm.weapons.orbitOrbs = (worm.weapons.orbitOrbs + 1).coerceAtMost(5)
                "spitAcid" -> worm.weapons.spitAcid = (worm.weapons.spitAcid + 1).coerceAtMost(5)
                "spikeSpine" -> worm.weapons.spikeSpine = (worm.weapons.spikeSpine + 1).coerceAtMost(5)
                "plasmaHalo" -> worm.weapons.plasmaHalo = (worm.weapons.plasmaHalo + 1).coerceAtMost(5)
                "stormStrike" -> worm.weapons.stormStrike = (worm.weapons.stormStrike + 1).coerceAtMost(5)
                "speed" -> worm.stats.speed += 0.6f
                "health" -> {
                    worm.stats.maxHealth += 40f
                    worm.stats.currentHealth = worm.stats.maxHealth
                }
                "vamp" -> worm.stats.vampFangs += 8
                "magnet" -> worm.stats.magnetRange += 60f
            }
            _worms.value = currentWorms
        }

        // Remove from map
        val currentPending = _levelUpPending.value.toMutableMap()
        currentPending.remove(wormId)
        _levelUpPending.value = currentPending
    }

    private fun getGemColor(type: ItemType): Int {
        return when (type) {
            ItemType.GEM_BLUE -> 0xFF00E5FF.toInt()
            ItemType.GEM_RED -> 0xFFFF1744.toInt()
            ItemType.GEM_GOLD -> 0xFFFFD700.toInt()
            ItemType.MEAT -> 0xFFFF8A80.toInt()
            ItemType.MAGNET_BUFF -> 0xFFE040FB.toInt()
        }
    }

    private fun getEnemyColor(type: EnemyType): Int {
        return SampColorMapping[type] ?: 0xFF888888.toInt()
    }

    private val SampColorMapping = mapOf(
        EnemyType.BAT to 0xFFE040FB.toInt(),
        EnemyType.SKELETON to 0xFFECEFF1.toInt(),
        EnemyType.SLIME to 0xFF00E676.toInt(),
        EnemyType.ORC to 0xFFFFA000.toInt(),
        EnemyType.DRAGON_BOSS to 0xFFFF1744.toInt()
    )

    private fun createDamageSparks(pos: Offset, color: Int, list: MutableList<Particle>) {
        repeat(5) {
            list.add(
                Particle(
                    position = pos,
                    velocity = Offset(Random.nextFloat() * 4f - 2f, Random.nextFloat() * 4f - 2f),
                    color = color,
                    size = 5f,
                    decay = 0.06f
                )
            )
        }
    }

    private fun createBloodSplat(pos: Offset, list: MutableList<Particle>) {
        repeat(4) {
            list.add(
                Particle(
                    position = pos,
                    velocity = Offset(Random.nextFloat() * 3f - 1.5f, Random.nextFloat() * 3f - 1.5f),
                    color = 0xFFFF1744.toInt(),
                    size = 7f,
                    decay = 0.05f
                )
            )
        }
    }

    private fun saveScoresToDb() {
        val scoreP1 = _worms.value.find { it.id == "p1" } ?: return
        viewModelScope.launch {
            repository.saveScore(
                HighScore(
                    playerName = scoreP1.name,
                    score = scoreP1.currentScore,
                    level = scoreP1.level,
                    gameMode = _gameMode.value.name
                )
            )
        }
    }

    fun clearHighScores() {
        viewModelScope.launch {
            repository.clearScores()
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        db.close()
    }
}
