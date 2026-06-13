package com.example.game

import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

enum class GameMode {
    SOLO,
    CO_OP,
    VERSUS
}

enum class EnemyType(
    val maxHealth: Float,
    val speed: Float,
    val damage: Float,
    val xpValue: Int,
    val radius: Float
) {
    BAT(15f, 1.8f, 5f, 10, 15f),
    SKELETON(40f, 1.0f, 12f, 25, 18f),
    SLIME(25f, 1.3f, 8f, 15, 16f),
    ORC(80f, 0.8f, 18f, 50, 22f),
    DRAGON_BOSS(400f, 0.6f, 30f, 500, 35f)
}

enum class ItemType {
    GEM_BLUE,
    GEM_RED,
    GEM_GOLD,
    MEAT, // Heals
    MAGNET_BUFF // Temporary magnet range booster
}

data class OrbiterState(
    var angle: Float = 0f
)

data class WeaponLevels(
    var spitAcid: Int = 0,     // Spits green puddles that damage over time
    var orbitOrbs: Int = 0,    // Purple balls rotating around head
    var spikeSpine: Int = 0,   // Shoots thorns out of its body segments
    var plasmaHalo: Int = 0,   // Burning protective ring around the head
    var stormStrike: Int = 0   // Periodic chain lightning strikes
)

data class Stats(
    var maxHealth: Float = 100f,
    var currentHealth: Float = 100f,
    var speed: Float = 4f,
    var magnetRange: Float = 80f,
    var vampFangs: Int = 0,    // Percent chance to heal on kill
    var damageMultiplier: Float = 1f
)

data class WormSegment(
    val position: Offset,
    val angle: Float
)

data class Worm(
    val id: String,
    val name: String,
    var headPosition: Offset,
    val segments: MutableList<WormSegment> = mutableListOf(),
    var length: Int = 10,
    var angle: Float = 0f,
    var targetAngle: Float = 0f,
    val colorPrimary: Int, // Hex ARGB integer representation of Color
    val colorAccent: Int,
    val isBot: Boolean = false,
    val stats: Stats = Stats(),
    val weapons: WeaponLevels = WeaponLevels(),
    var xp: Int = 0,
    var nextLevelXp: Int = 100,
    var level: Int = 1,
    var currentScore: Int = 0,
    var isDead: Boolean = false,
    var killCount: Int = 0,
    val orbiters: MutableList<Float> = mutableListOf(0f), // angles of orbs
    var speedMultiplier: Float = 1f,
    var timeSinceLastSpike: Long = 0L,
    var timeSinceLastSpit: Long = 0L,
    var timeSinceLastHalo: Long = 0L,
    var timeSinceLastStorm: Long = 0L
)

data class Enemy(
    val id: String,
    val type: EnemyType,
    var position: Offset,
    var health: Float,
    var maxHealth: Float,
    var spawnTime: Long = System.currentTimeMillis()
)

data class GameItem(
    val id: String,
    val type: ItemType,
    var position: Offset,
    val xpReward: Int,
    val healAmount: Float = 0f
)

data class Projectile(
    val id: String,
    val type: String, // "SPIT", "SPIKE", "LIGHTNING", "HALO"
    var position: Offset,
    var velocity: Offset = Offset.Zero,
    val damage: Float,
    var durationFrames: Int, // countdown
    val radius: Float,
    val color: Int,
    val sourceId: String // ID of Worm owner
)

data class Particle(
    var position: Offset,
    var velocity: Offset,
    var color: Int,
    var size: Float,
    var alpha: Float = 1.0f,
    var life: Float = 1.0f, // 1.0 to 0.0
    val decay: Float = 0.02f
)
