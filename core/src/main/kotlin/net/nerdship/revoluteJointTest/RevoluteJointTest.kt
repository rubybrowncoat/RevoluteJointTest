package net.nerdship.revoluteJointTest

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.log.logger
import ktx.math.vec2
import ktx.math.vec3
import kotlin.math.cos
import kotlin.math.sin

class RevoluteJointTest : KtxGame<KtxScreen>() {
    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        addScreen(FirstScreen())
        setScreen<FirstScreen>()
    }
}

class FirstScreen : KtxScreen {
    companion object {
        val log = logger<FirstScreen>()
    }

    private val viewport = FitViewport(320f, 240f)

    private val debugRenderer = Box2DDebugRenderer()

    val physicsWorld = createWorld().apply {
        //
    }

    val testVehicleStar = Vehicle(physicsWorld, listOf(
        floatArrayOf(0f, 0f, 55.4327f, 22.9610f, 100f, 0f, 55.4327f, -22.9610f),
        floatArrayOf(0f, 0f, 22.9610f, 55.4327f, 70.7106f, 70.7106f, 55.4327f, 22.9610f),
        floatArrayOf(0f, 0f, -22.9610f, 55.4327f, 0f, 100f, 22.9610f, 55.4327f),
        floatArrayOf(0f, 0f, -55.4327f, 22.9610f, -70.7106f, 70.7106f, -22.9610f, 55.4327f),
        floatArrayOf(0f, 0f, -55.4327f, -22.9610f, -100f, 0f, -55.4327f, 22.9610f),
        floatArrayOf(0f, 0f, -22.9610f, -55.4327f, -70.7106f, -70.7106f, -55.4327f, -22.9610f),
        floatArrayOf(0f, 0f, 22.9610f, -55.4327f, 0f, -100f, -22.9610f, -55.4327f),
        floatArrayOf(0f, 0f, 55.4327f, -22.9610f, 70.7106f, -70.7106f, 22.9610f, -55.4327f),
    ), vec2(0f, 0f), listOf(
        Wheel(physicsWorld, position = vec2(70f, 0f), true, 0.5f, 360f, 0f, 0f, 0f, 3.5f, true),
        Wheel(physicsWorld, position = vec2(49.4974f, 49.4974f), true, 0.5f, 360f, 0f, 0f, 0f, 3.5f, true),
        Wheel(physicsWorld, position = vec2(0f, 70f), true, 0.5f, 360f, 0f, 0f, 0f, 3.5f, true),
        Wheel(physicsWorld, position = vec2(-49.4974f, 49.4974f), true, 0.5f, 360f, 0f, 0f, 0f, 3.5f, true),
        Wheel(physicsWorld, position = vec2(-70f, 0f), true, 0.5f, 360f, 0f, 0f, 0f, 3.5f, true),
        Wheel(physicsWorld, position = vec2(-49.4974f, -49.4974f), true, 0.5f, 360f, 0f, 0f, 0f, 3.5f, true),
        Wheel(physicsWorld, position = vec2(0f, -70f), true, 0.5f, 360f, 0f, 0f, 0f, 3.5f, true),
        Wheel(physicsWorld, position = vec2(49.4974f, -49.4974f), true, 0.5f, 360f, 0f, 0f, 0f, 3.5f, true),
        Wheel(physicsWorld, position = vec2(0f, 0f), true, 0.5f, 360f, 300f, -100f, 20000f, swivel = false),
    ))

    override fun show() {
        viewport.camera.position.set(vec3(0f, 0f, 0f))
        viewport.apply()

        super.show()
    }

    override fun render(delta: Float) {
        clearScreen(red = 0.3f, green = 0.3f, blue = 0.3f)

        val mousePosition = viewport.camera.unproject(vec3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))

        val ab = vec2(mousePosition.x - testVehicleStar.body.position.x, mousePosition.y - testVehicleStar.body.position.y)
        val v = vec2(cos(testVehicleStar.body.angle + MathUtils.HALF_PI), sin(testVehicleStar.body.angle + MathUtils.HALF_PI))

        val abvAngle = ab.angleRad(v) % MathUtils.PI

        testVehicleStar.update(0, abvAngle) // ISSUE: The issue lies inside this method
        physicsWorld.step(delta, 6, 2)

        debugRenderer.render(physicsWorld, viewport.camera.combined)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)

        super.resize(width, height)
    }

    override fun dispose() {
        physicsWorld.disposeSafely()
    }
}
