@file:JvmName("Lwjgl3Launcher")

package net.nerdship.revoluteJointTest.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import net.nerdship.revoluteJointTest.RevoluteJointTest

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(RevoluteJointTest(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("RevoluteJointTest")
        setWindowedMode(640, 480)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}
