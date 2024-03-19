
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import org.lwjgl.util.glu.Sphere
import java.awt.Color

@ModuleInfo(name = "Trail", category = ModuleCategory.RENDER, description = "Leaves a trail behind you")
class Trail : Module() {
    private val typeValue = ListValue("Type", arrayOf("Line", "Rect", "Sphere"), "Line")
    private val colorRedValue = IntegerValue("R", 255, 0, 255)
    private val colorGreenValue = IntegerValue("G", 255, 0, 255)
    private val colorBlueValue = IntegerValue("B", 255, 0, 255)
    private val colorAlphaValue = IntegerValue("Alpha", 255, 0, 255)
    private val colorRainbow = BoolValue("Rainbow", false)
    private val fade = BoolValue("Fade", true)
    private val drawplayer = BoolValue("Drawplayer", true)
    private val drawTargets = BoolValue("DrawTargets", true)
    private val fadeTime = IntegerValue("FadeTime", 5, 1, 20)
    private val precision = IntegerValue("Precision", 1, 1, 20)
    private val lineWidth = IntegerValue("LineWidth", 1, 1, 10)
    private val sphereScale = FloatValue("SphereScale", 1f, 0.1f, 2f)

    private val points = mutableMapOf<Int, MutableList<BreadcrumbPoint>>()

    val color: Color
        get() = if (colorRainbow.get()) rainbow() else Color(
            colorRedValue.get(),
            colorGreenValue.get(),
            colorBlueValue.get()
        )

    private val sphereList = GL11.glGenLists(1)

    init {
        GL11.glNewList(sphereList, GL11.GL_COMPILE)

        val shaft = Sphere()
        shaft.drawStyle = GLU.GLU_FILL
        shaft.draw(0.3f, 25, 10)

        GL11.glEndList()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val fTime = fadeTime.get() * 1000
        val fadeSec = System.currentTimeMillis() - fTime
        val colorAlpha = colorAlphaValue.get() / 255.0f

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        mc.entityRenderer.disableLightmap()
        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ
        points.forEach { (_, mutableList) ->
            var lastPosX = 114514.0
            var lastPosY = 114514.0
            var lastPosZ = 114514.0
            when (typeValue.get().toLowerCase()) {
                "line" -> {
                    GL11.glLineWidth(lineWidth.get().toFloat())
                    GL11.glEnable(GL11.GL_LINE_SMOOTH)
                    GL11.glBegin(GL11.GL_LINE_STRIP)
                }
                "rect" -> {
                    GL11.glDisable(GL11.GL_CULL_FACE)
                }
            }
            for (point in mutableList.reversed()) {
                val alpha = if (fade.get()) {
                    val pct = (point.time - fadeSec).toFloat() / fTime
                    if (pct < 0 || pct > 1) {
                        mutableList.remove(point)
                        continue
                    }
                    pct
                } else {
                    1f
                } * colorAlpha
                RenderUtils.glColor2(point.color, alpha)
                when (typeValue.get().toLowerCase()) {
                    "line" -> GL11.glVertex3d(point.x - renderPosX, point.y - renderPosY, point.z - renderPosZ)
                    "rect" -> {
                        if (!(lastPosX == 114514.0 && lastPosY == 114514.0 && lastPosZ == 114514.0)) {
                            GL11.glBegin(GL11.GL_QUADS)
                            GL11.glVertex3d(point.x - renderPosX, point.y - renderPosY, point.z - renderPosZ)
                            GL11.glVertex3d(lastPosX, lastPosY, lastPosZ)
                            GL11.glVertex3d(lastPosX, lastPosY + mc.player!!.height, lastPosZ)
                            GL11.glVertex3d(
                                point.x - renderPosX,
                                point.y - renderPosY + mc.player!!.height,
                                point.z - renderPosZ
                            )
                            GL11.glEnd()
                        }
                        lastPosX = point.x - renderPosX
                        lastPosY = point.y - renderPosY
                        lastPosZ = point.z - renderPosZ
                    }
                    "sphere" -> {
                        GL11.glPushMatrix()
                        GL11.glTranslated(point.x - renderPosX, point.y - renderPosY, point.z - renderPosZ)
                        GL11.glScalef(sphereScale.get(), sphereScale.get(), sphereScale.get())
                        GL11.glCallList(sphereList)
                        GL11.glPopMatrix()
                    }
                }
            }
            when (typeValue.get().toLowerCase()) {
                "line" -> {
                    GL11.glEnd()
                    GL11.glDisable(GL11.GL_LINE_SMOOTH)
                }
                "rect" -> {
                    GL11.glEnable(GL11.GL_CULL_FACE)
                }
            }
        }
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glPopMatrix()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // clear points for entities not exist
        points.forEach { (id, _) ->
            if (mc.world!!.getEntityByID(id) == null) {
                points.remove(id)
            }
        }
        // add new points
        if (mc.player!!.ticksExisted % precision.get() != 0) {
            return // skip if not on tick
        }
        if (drawTargets.get()) {
            mc.world!!.loadedEntityList.forEach {
                if (EntityUtils.isSelected(it, true)) {
                    updatePoints(it as EntityLivingBase)
                }
            }
        }
        if (drawplayer.get()) {
            updatePoints(mc.player)
        }
    }

    private fun updatePoints(entity: EntityLivingBase) {
        (points[entity.entityId] ?: mutableListOf<BreadcrumbPoint>().also { points[entity.entityId] = it })
            .add(
                BreadcrumbPoint(
                    entity.posX,
                    entity.entityBoundingBox.minY,
                    entity.posZ,
                    System.currentTimeMillis(),
                    color.rgb
                )
            )
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        points.clear()
    }

    override fun onDisable() {
        points.clear()
    }

    class BreadcrumbPoint(val x: Double, val y: Double, val z: Double, val time: Long, val color: Int)
}