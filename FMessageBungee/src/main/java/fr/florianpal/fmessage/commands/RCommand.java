

/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * Last modification : 07/01/2022 23:05
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.utils.FormatUtil;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;
import java.util.UUID;

@CommandAlias("r")
public class RCommand extends BaseCommand {
    private final FMessage plugin;
    private final CommandManager commandManager;

    public RCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
    }

    @Default
    @CommandPermission("fmessage.r")
    @Description("{@@fmessage.r_help_description}")
    public void onR(Player playerSender, String message){
        if(!plugin.havePreviousPlayer(playerSender)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.NO_PREVIOUS_PLAYER);
        } else if (!plugin.isPreviousPlayerOnline(playerSender)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.PLAYER_OFFLINE);
        } else {
            Player playerTarget = plugin.getPreviousPlayer(playerSender);

            TextComponent formatTarget = FormatUtil.format(plugin.getConfigurationManager().getChat().getTargetChatFormat());

            TextReplacementConfig textReplacementConfigSender = TextReplacementConfig.builder()
                    .matchLiteral("{sender}")
                    .replacement(playerSender.getUsername())
                    .build();

            TextReplacementConfig textReplacementConfigTarget = TextReplacementConfig.builder()
                    .matchLiteral("{target}")
                    .replacement(playerTarget.getUsername())
                    .build();

            formatTarget = (TextComponent) formatTarget.replaceText(textReplacementConfigSender);
            formatTarget = (TextComponent) formatTarget.replaceText(textReplacementConfigTarget);

            TextReplacementConfig textReplacementConfigMessageColored = TextReplacementConfig.builder()
                    .matchLiteral("{message}")
                    .replacement(FormatUtil.format(message))
                    .build();

            TextReplacementConfig textReplacementConfigNonColored = TextReplacementConfig.builder()
                    .matchLiteral("{message}")
                    .replacement(message)
                    .build();

            if(playerSender.hasPermission("fmessage.colors")) {
                formatTarget = (TextComponent) formatTarget.replaceText(textReplacementConfigMessageColored);
            } else {
                formatTarget = (TextComponent) formatTarget.replaceText(textReplacementConfigNonColored);
            }

            playerTarget.sendMessage(formatTarget);

            TextComponent formatSender = FormatUtil.format(plugin.getConfigurationManager().getChat().getSenderChatFormat());

            formatSender = (TextComponent) formatSender.replaceText(textReplacementConfigSender);
            formatSender = (TextComponent) formatSender.replaceText(textReplacementConfigTarget);

            if(playerSender.hasPermission("fmessage.colors")) {
                formatSender = (TextComponent) formatSender.replaceText(textReplacementConfigMessageColored);
            } else {
                formatSender = (TextComponent) formatSender.replaceText(textReplacementConfigNonColored);
            }
            playerSender.sendMessage(formatSender);

            TextComponent formatSpy = FormatUtil.format(plugin.getConfigurationManager().getChat().getSpyChatFormat());

            formatSpy = (TextComponent) formatSpy.replaceText(textReplacementConfigSender);
            formatSpy = (TextComponent) formatSpy.replaceText(textReplacementConfigTarget);

            formatSpy = (TextComponent) formatSpy.replaceText(textReplacementConfigMessageColored);

            plugin.getLogger().info(LegacyComponentSerializer.legacyAmpersand().serialize(formatSpy));

            for(UUID uuid : plugin.getPlayerSpy()) {
                Optional<Player> player = plugin.getServer().getPlayer(uuid);
                if(player.isPresent()) {
                    player.get().sendMessage(formatSpy);
                }
            }
        }
    }
}
