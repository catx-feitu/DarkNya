package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.DebugManage
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.network.play.server.SPacketTitle

@ModuleInfo(name = "LHXYClientPauseDisable", description = "落花星雨封禁崩端修复 | by CatX_feitu", category = ModuleCategory.MISC)
class LHXYClientPauseDisable : Module() {
    private val debug = BoolValue("Debug", true)
    private val pauseMove = BoolValue("PauseMove", true)

    private var cancelPacket = false
    // 不拦截封包列表
    private val packetClasses = arrayOf(SPacketTitle::class, SPacketChat::class)

    @EventTarget
    fun onPacket(event:PacketEvent) {
        val packet = event.packet
        if (packet is SPacketChat) {
            if (packet.chatComponent.unformattedText.contains("分析检测到作弊，如有疑问请保留该页面，请不要结束进程！")) {
                cancelPacket = true
                if (debug.get()) DebugManage.warn("§b检测到落花星雨封禁消息")
            }
        }

        if (cancelPacket && !packetClasses.any { it.isInstance(packet) } && packet is INetHandlerPlayClient) {
            event.cancelEvent()
            if (debug.get()) DebugManage.info("§a拦截${packet::class.simpleName}成功")
        }
    }
    @EventTarget
    fun onWorld(worldEvent: WorldEvent) {
        cancelPacket = false
    }
    @EventTarget
    fun onMove(event: MoveEvent){
        if (cancelPacket && pauseMove.get()) event.zero()
    }
}