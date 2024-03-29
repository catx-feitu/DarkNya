package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.ui.cnfont.FontDrawer;
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;

@Mixin(GuiButton.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiButton extends Gui {

    @Shadow
    @Final
    protected static ResourceLocation BUTTON_TEXTURES;
    @Shadow
    public boolean visible;
    @Shadow
    public int x;
    @Shadow
    public int y;
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    public boolean enabled;
    @Shadow
    public String displayString;
    @Shadow
    protected boolean hovered;
    private float cut;
    private float alpha;
    private float rectY;

    @Shadow
    protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

    /**
     * @author CCBlueX
     * @reason DarkNya Inject
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            final FontDrawer fontRenderer = FontLoaders.F16;
            hovered = (mouseX >= this.x && mouseY >= this.y &&
                    mouseX < this.x + this.width && mouseY < this.y + this.height);

            final int delta = RenderUtils.deltaTime;

            if (enabled && hovered) {
                cut += 0.05F * delta;

                if (cut >= 4) cut = 4;

                alpha += 0.3F * delta;

                if (alpha >= 210) alpha = 210;

                rectY += 0.1F * delta;

                if (rectY >= height) rectY = height;
            } else {
                cut -= 0.05F * delta;

                if (cut <= 0) cut = 0;

                alpha -= 0.3F * delta;

                if (alpha <= 120) alpha = 120;

                rectY -= 0.05F * delta;

                if (rectY <= 4) rectY = 4;
            }

            RenderUtils.drawRect(this.x + (int) this.cut, this.y,
                    this.x + this.width - (int) this.cut, this.y + this.height,
                    this.enabled ? new Color(0F, 0F, 0F, this.alpha / 255F).getRGB() :
                            new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB());

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            mouseDragged(mc, mouseX, mouseY);

            fontRenderer.drawStringWithShadow(displayString,
                    (float) ((this.x + this.width / 2) -
                            fontRenderer.getStringWidth(displayString) / 2),
                    this.y + (this.height - 5) / 2F, Color.WHITE.getRGB());


            GlStateManager.resetColor();
        }
    }
}