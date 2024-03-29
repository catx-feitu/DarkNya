package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.misc.StringUtils

class SayCommand : Command("say") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            mc.player!!.sendChatMessage(StringUtils.toCompleteString(args, 1))
            chat("Message was sent to the chat.")
            return
        }
        chatSyntax("say <message...>")
    }
}