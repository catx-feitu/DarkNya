package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.DarkNya;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.*;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketCloseWindow;
import org.lwjgl.input.Keyboard;

import java.awt.*;

@ModuleInfo(name = "ClickGUI", description = "Opens the ClickGUI.", category = ModuleCategory.CLIENT, keyBind = Keyboard.KEY_RSHIFT, canEnable = false)
public class ClickGUI extends Module {
    private final ListValue styleValue = new ListValue("Style", new String[] {"LiquidBounce", "Null", "Slowly", "Astolfo", "Jello", "New"}, "New") {
        @Override
        protected void onChanged(final String oldValue, final String newValue) {
            updateStyle();
        }
    };

    public final FloatValue scaleValue = new FloatValue("Scale", 1F, 0.7F, 2F);
    public final IntegerValue maxElementsValue = new IntegerValue("MaxElements", 15, 1, 20);

    private static final IntegerValue colorRedValue = new IntegerValue("R", 0, 0, 160);
    private static final IntegerValue colorGreenValue = new IntegerValue("G", 160, 0, 160);
    private static final IntegerValue colorBlueValue = new IntegerValue("B", 255, 0, 255);
    private static final BoolValue colorRainbow = new BoolValue("Rainbow", false);
    public final ListValue backgroundValue = new ListValue("Background", new String[] {"Default","None"}, "Default");
    public final ListValue animationValue = new ListValue("Animation", new String[] {"Azura", "Slide", "SlideBounce", "Zoom", "ZoomBounce", "None"}, "Azura");
    public static Color generateColor() {
        return colorRainbow.get() ? ColorUtils.rainbow() : new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get());
    }

    @Override
    public void onEnable() {
        updateStyle();

        mc.displayGuiScreen(DarkNya.clickGui);
    }

    private void updateStyle() {
        switch(styleValue.get().toLowerCase()) {
            case "liquidbounce":
                DarkNya.clickGui.style = new LiquidBounceStyle();
                break;
            case "null":
                DarkNya.clickGui.style = new NullStyle();
                break;
            case "slowly":
                DarkNya.clickGui.style = new SlowlyStyle();
                break;
            case "astolfo" :
                DarkNya.clickGui.style = new AstolfoStyle();
                break;
            case "jello" :
                DarkNya.clickGui.style = new JelloStyle();
                break;
            case "new" :
                DarkNya.clickGui.style = new NewStyle();
                break;
        }
    }

    @EventTarget(ignoreCondition = true)
    public void onPacket(final PacketEvent event) {
        final Packet packet = event.getPacket();

        if (packet instanceof SPacketCloseWindow && mc.currentScreen instanceof ClickGui) {
            event.cancelEvent();
        }
    }
}
