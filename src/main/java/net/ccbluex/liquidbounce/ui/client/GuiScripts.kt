package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSlot
import org.apache.commons.io.IOUtils
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipFile

class GuiScripts(private val prevGui: GuiScreen) : GuiScreen() {

    private lateinit var list: GuiList

    override fun initGui() {
        list = GuiList(this)
        list.registerScrollButtons(7, 8)
        list.elementClicked(-1, false, 0, 0)

        val j = 22
        buttonList.add(GuiButton(0, width - 80, height - 65, 70, 20, "Back"))
        buttonList.add(GuiButton(1, width - 80, j + 24, 70, 20, "Import"))
        buttonList.add(GuiButton(2, width - 80, j + 24 * 2, 70, 20, "Delete"))
        buttonList.add(GuiButton(3, width - 80, j + 24 * 3, 70, 20, "Reload"))
        buttonList.add(GuiButton(4, width - 80, j + 24 * 4, 70, 20, "Folder"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        list.drawScreen(mouseX, mouseY, partialTicks)

        Fonts.font40.drawCenteredString("§9§lScripts", width / 2.0f, 28.0f, 0xffffff)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(prevGui)
            1 -> try {
                val file = MiscUtils.openFileChooser() ?: return
                val fileName = file.name

                if (fileName.endsWith(".js")) {
                    DarkNya.scriptManager.importScript(file)

                    DarkNya.clickGui = ClickGui()
                    DarkNya.fileManager.loadConfig(DarkNya.fileManager.clickGuiConfig)
                    return
                } else if (fileName.endsWith(".zip")) {
                    val zipFile = ZipFile(file)
                    val entries = zipFile.entries()
                    val scriptFiles = ArrayList<File>()

                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        val entryName = entry.name
                        val entryFile = File(DarkNya.scriptManager.scriptsFolder, entryName)

                        if (entry.isDirectory) {
                            entryFile.mkdir()
                            continue
                        }

                        val fileStream = zipFile.getInputStream(entry)
                        val fileOutputStream = FileOutputStream(entryFile)

                        IOUtils.copy(fileStream, fileOutputStream)
                        fileOutputStream.close()
                        fileStream.close()

                        if (!entryName.contains("/"))
                            scriptFiles.add(entryFile)
                    }

                    scriptFiles.forEach { scriptFile -> DarkNya.scriptManager.loadScript(scriptFile) }

                    DarkNya.clickGui = ClickGui()
                    DarkNya.fileManager.loadConfig(DarkNya.fileManager.clickGuiConfig)
                    DarkNya.fileManager.loadConfig(DarkNya.fileManager.hudConfig)
                    return
                }

                MiscUtils.showErrorPopup("Wrong file extension.", "The file extension has to be .js or .zip")
            } catch (t: Throwable) {
                ClientUtils.getLogger().error("Something went wrong while importing a script.", t)
                MiscUtils.showErrorPopup(t.javaClass.name, t.message)
            }

            2 -> try {
                if (list.getSelectedSlot() != -1) {
                    val script = DarkNya.scriptManager.scripts[list.getSelectedSlot()]

                    DarkNya.scriptManager.deleteScript(script)

                    DarkNya.clickGui = ClickGui()
                    DarkNya.fileManager.loadConfig(DarkNya.fileManager.clickGuiConfig)
                    DarkNya.fileManager.loadConfig(DarkNya.fileManager.hudConfig)
                }
            } catch (t: Throwable) {
                ClientUtils.getLogger().error("Something went wrong while deleting a script.", t)
                MiscUtils.showErrorPopup(t.javaClass.name, t.message)
            }
            3 -> try {
                DarkNya.scriptManager.reloadScripts()
            } catch (t: Throwable) {
                ClientUtils.getLogger().error("Something went wrong while reloading all scripts.", t)
                MiscUtils.showErrorPopup(t.javaClass.name, t.message)
            }
            4 -> try {
                Desktop.getDesktop().open(DarkNya.scriptManager.scriptsFolder)
            } catch (t: Throwable) {
                ClientUtils.getLogger().error("Something went wrong while trying to open your scripts folder.", t)
                MiscUtils.showErrorPopup(t.javaClass.name, t.message)
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui)
            return
        }

        super.keyTyped(typedChar, keyCode)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        list.handleMouseInput()
    }

    private inner class GuiList(gui: GuiScreen) :
            GuiSlot(mc, gui.width, gui.height, 40, gui.height - 40, 30) {

        private var selectedSlot = 0

        override fun isSelected(id: Int) = selectedSlot == id

        internal fun getSelectedSlot() = if (selectedSlot > DarkNya.scriptManager.scripts.size) -1 else selectedSlot

        override fun getSize() = DarkNya.scriptManager.scripts.size

        public override fun elementClicked(id: Int, doubleClick: Boolean, var3: Int, var4: Int) {
            selectedSlot = id
        }

        override fun drawSlot(id: Int, x: Int, y: Int, var4: Int, var5: Int, var6: Int, f: Float) {
            val script = DarkNya.scriptManager.scripts[id]
            Fonts.font40.drawCenteredString("§9" + script.scriptName + " §7v" + script.scriptVersion, width / 2.0f, y + 2.0f, Color.LIGHT_GRAY.rgb)
            Fonts.font40.drawCenteredString("by §c" + script.scriptAuthors.joinToString(", "), width / 2.0f, y + 15.0f, Color.LIGHT_GRAY.rgb)
        }

        override fun drawBackground() { }
    }
}