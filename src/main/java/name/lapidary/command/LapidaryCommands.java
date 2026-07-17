package name.lapidary.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import name.lapidary.progression.LapidaryInsight;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class LapidaryCommands {

    private LapidaryCommands() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                Commands.literal("lapidary")
                                        .then(
                                                Commands.literal("insight")

                                                        /*
                                                         * /lapidary insight
                                                         */
                                                        .executes(
                                                                LapidaryCommands::showInsight
                                                        )

                                                        /*
                                                         * /lapidary insight get
                                                         */
                                                        .then(
                                                                Commands.literal("get")
                                                                        .executes(
                                                                                LapidaryCommands::showInsight
                                                                        )
                                                        )

                                                        /*
                                                         * /lapidary insight add <amount>
                                                         *
                                                         * This is a development/admin command,
                                                         * so it requires permission level 2.
                                                         */
                                                        .then(
                                                                Commands.literal("add")
                                                                        .requires(
                                                                                source ->
                                                                                        source.hasPermission(2)
                                                                        )
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "amount",
                                                                                                IntegerArgumentType.integer(1)
                                                                                        )
                                                                                        .executes(
                                                                                                LapidaryCommands::addInsight
                                                                                        )
                                                                        )
                                                        )

                                                        /*
                                                         * /lapidary insight set <amount>
                                                         */
                                                        .then(
                                                                Commands.literal("set")
                                                                        .requires(
                                                                                source ->
                                                                                        source.hasPermission(2)
                                                                        )
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "amount",
                                                                                                IntegerArgumentType.integer(0)
                                                                                        )
                                                                                        .executes(
                                                                                                LapidaryCommands::setInsight
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    /**
     * Displays the command user's current Insight.
     */
    private static int showInsight(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {
        ServerPlayer player =
                context.getSource().getPlayerOrException();

        int insight = LapidaryInsight.get(player);

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "You have "
                                + insight
                                + " Lapidary Insight."
                ),
                false
        );

        return 1;
    }

    /**
     * Adds the supplied amount to the command user's Insight.
     */
    private static int addInsight(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {
        ServerPlayer player =
                context.getSource().getPlayerOrException();

        int amount = IntegerArgumentType.getInteger(
                context,
                "amount"
        );

        int newTotal = LapidaryInsight.add(player, amount);

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "Added "
                                + amount
                                + " Lapidary Insight. New total: "
                                + newTotal
                                + "."
                ),
                true
        );

        return 1;
    }

    /**
     * Sets the command user's Insight to the supplied amount.
     */
    private static int setInsight(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {
        ServerPlayer player =
                context.getSource().getPlayerOrException();

        int amount = IntegerArgumentType.getInteger(
                context,
                "amount"
        );

        int newTotal = LapidaryInsight.set(player, amount);

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "Set Lapidary Insight to "
                                + newTotal
                                + "."
                ),
                true
        );

        return 1;
    }
}