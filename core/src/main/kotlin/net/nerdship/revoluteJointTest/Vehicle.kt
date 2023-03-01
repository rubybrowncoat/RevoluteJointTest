package net.nerdship.revoluteJointTest

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.polygon
import ktx.log.logger
import kotlin.experimental.or
import kotlin.math.abs
import kotlin.math.sign

class CollisionCategory {
    companion object {
        const val NONE: Short = 0
        const val ALL: Short = -1

        const val PLAYER: Short = 1
        const val ENEMY: Short = 2

        const val TILE: Short = 4

        const val ITEM: Short = 8
        const val PROJECTILE: Short = 16

        const val VEHICLE: Short = 32
        const val WHEEL: Short = 64
    }
}

class Wheel(
    val physicsWorld: World,
    val position: Vector2,

    val steering: Boolean = false,
    val steeringSpeed: Float = 1f,
    val maximumSteeringAngle: Float = 0f,

    val maximumForwardSpeed: Float = 100f,
    val maximumBackwardSpeed: Float = -20f,
    val maximumDriveForce: Float = 150f,
    val maximumLateralImpulse: Float = 8.5f,

    val swivel: Boolean = false,
) {
    val body: Body
    var joint: RevoluteJoint? = null

    val steeringSpeedRadians = steeringSpeed * MathUtils.degreesToRadians
    val maximumSteeringAngleRadians = maximumSteeringAngle * MathUtils.degreesToRadians

    init {
        body = physicsWorld.body(BodyDef.BodyType.DynamicBody) {
            box(width = 4f, height = 8f) {
                density = 1f

                filter.categoryBits = CollisionCategory.WHEEL
                filter.maskBits = CollisionCategory.PLAYER or CollisionCategory.PROJECTILE
            }
        }
    }

    fun getLateralVelocity(): Vector2 {
        val currentRightNormal = body.getWorldVector(Vector2(1f, 0f))

        return currentRightNormal.scl(currentRightNormal.dot(body.linearVelocity))
    }

    fun getForwardVelocity(): Vector2 {
        val currentForwardNormal = body.getWorldVector(Vector2(0f, 1f))

        return currentForwardNormal.scl(currentForwardNormal.dot(body.linearVelocity))
    }

    fun updateFriction() {
        // lateral linear velocity
        var impulse = this.getLateralVelocity().scl(-body.mass)
        if (impulse.len() > maximumLateralImpulse) {
            impulse = impulse.scl(maximumLateralImpulse / impulse.len())
        }

        body.applyLinearImpulse(impulse, body.worldCenter, true)

        // angular velocity
        body.applyAngularImpulse(0.1f * body.inertia * -body.angularVelocity, true)

        // forward linear velocity
        val currentForwardNormal = this.getForwardVelocity()
        val currentForwardSpeed = currentForwardNormal.nor()
        val dragForceMagnitude = currentForwardSpeed.scl(-2f)

        body.applyForce(currentForwardNormal.scl(dragForceMagnitude), body.worldCenter, true)
    }

    fun updateDrive(my: Int) {
        val desiredSpeed = if (my > 0) maximumForwardSpeed else if (my < 0) maximumBackwardSpeed else return

        val currentForwardNormal = body.getWorldVector(Vector2(0f, 1f))
        val currentSpeed = currentForwardNormal.dot(body.linearVelocity)

        val force = if (desiredSpeed > currentSpeed) maximumDriveForce else if (desiredSpeed < currentSpeed) -maximumDriveForce else return

        body.applyForce(currentForwardNormal.scl(force), body.worldCenter, true)
    }
}

class Vehicle(
    val physicsWorld: World,

    val fixtureVertices: List<FloatArray>,
    val centerOfMass: Vector2,

    val wheels: List<Wheel>,
) {
    companion object {
        val log = logger<Vehicle>()
    }

    val body: Body

    init {
        body = physicsWorld.body(BodyDef.BodyType.DynamicBody) {
            fixtureVertices.forEach {
                polygon(it) {
                    density = 0.1f

                    filter.categoryBits = CollisionCategory.VEHICLE
                    filter.maskBits = CollisionCategory.PLAYER or CollisionCategory.TILE or CollisionCategory.ENEMY or CollisionCategory.PROJECTILE
                }
            }

            angularDamping = 3f
        }

        val massData = body.massData
        massData.center.set(centerOfMass)
        body.massData = massData

        wheels.forEach { wheel ->
            log.debug { "wheel: ${wheel.position}" }

            val revoluteJoint = RevoluteJointDef().apply {
                enableLimit = true
                lowerAngle = 0f
                upperAngle = 0f

                bodyA = body
                bodyB = wheel.body
                localAnchorA.set(wheel.position)
                localAnchorB.setZero()
            }

            log.debug { "Joint: ${revoluteJoint.localAnchorA}" }

            wheel.joint = physicsWorld.createJoint(revoluteJoint) as RevoluteJoint
        }
    }

    fun update(movementState: Int, direction: Float) {
        wheels.forEach {
            it.updateFriction()
        }

        wheels.forEach {
            if (it.maximumDriveForce > 0) {
                it.updateDrive(movementState)
            }
        }

        wheels.forEach {
            if (it.steering) {
                val desiredAngle = direction.coerceIn(-it.maximumSteeringAngleRadians, it.maximumSteeringAngleRadians)

                var jointAngle = it.joint?.jointAngle ?: 0f

                if (it.swivel) {
                    val angleDifference = desiredAngle - jointAngle
                    if (abs(angleDifference) > MathUtils.PI) {
                        jointAngle += sign(angleDifference) * MathUtils.PI2
                    }
                }

                var newAngle = MathUtils.lerp(jointAngle, desiredAngle, it.steeringSpeedRadians)
                if (abs(newAngle) > MathUtils.PI) {
                    newAngle -= sign(newAngle) * MathUtils.PI2
                }

                it.joint?.setLimits(newAngle, newAngle)
            }
        }
    }
}
