package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class UsernameCommand : Command("username", "ign") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val username = mc.player!!.name

        chat("Username: $username")

        val stringSelection = StringSelection(username)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, stringSelection)
    }
}