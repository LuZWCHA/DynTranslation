package com.nowandfuture.mod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nowandfuture.mod.core.TranslationManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Predicate;

@Deprecated
public class TranslationCommand{
    String[] subCommands = new String[]{"loadConfig","loadMap","startRecord","endRecord","disable","enable","spilt","notSpilt","retainOrg","help"};

    public static String getName() {
        return "translation";
    }

    public static int getRequiredPermissionLevel() {
        return 0;
    }

    public static String getUsage(CommandSource sender) {
        return I18n.format("commands.dyntranslation.usage");
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal(getName())
                .requires(new Predicate<CommandSource>() {
                    @Override
                    public boolean test(CommandSource commandSource) {
                        return commandSource.hasPermissionLevel(getRequiredPermissionLevel());
                    }
                });

        dispatcher.register(builder.then(Commands.literal("loadConfig")
                .executes(context -> {
                    TranslationManager.INSTANCE.loadConfig();
                    return Command.SINGLE_SUCCESS;
                })));

        dispatcher.register(builder.then(Commands.literal("startRecord")
                .executes(context -> {
                    TranslationManager.INSTANCE.startRecord();
                    return Command.SINGLE_SUCCESS;
                })));

        dispatcher.register(builder.then(Commands.literal("endRecord")
                .then(Commands.literal("-n")
                        .executes(context -> {
                            TranslationManager.INSTANCE.setPrintFormatChars(false);
                            TranslationManager.INSTANCE.endRecord();
                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(context -> {
                    TranslationManager.INSTANCE.setPrintFormatChars(true);
                    TranslationManager.INSTANCE.endRecord();
                    return Command.SINGLE_SUCCESS;
                })));

        dispatcher.register(builder.then(Commands.literal("disable")
                .executes(context -> {
                    TranslationManager.INSTANCE.setEnable(false);
                    return Command.SINGLE_SUCCESS;
                })));

        dispatcher.register(builder.then(Commands.literal("enable")
                .executes(context -> {
                    TranslationManager.INSTANCE.setEnable(true);
                    return Command.SINGLE_SUCCESS;
                })));

        dispatcher.register(builder.then(Commands.literal("spilt")
                .then(Commands.argument("spiltBool", BoolArgumentType.bool())
                        .executes(context -> {
                            TranslationManager.INSTANCE.setEnableSpiltWords(BoolArgumentType.getBool(context,"spiltBool"));
                            return Command.SINGLE_SUCCESS;
                        }))
        ));

        dispatcher.register(builder.then(Commands.literal("retainOrg")
                .then(Commands.argument("retainOrgBool", BoolArgumentType.bool())
                        .executes(context -> {
                            TranslationManager.INSTANCE.setRetainOrg(BoolArgumentType.getBool(context,"retainOrgBool"));
                            return Command.SINGLE_SUCCESS;
                        }))
        ));

        dispatcher.register(builder.then(Commands.literal("loadMap")
                                .executes(context -> {
                                    TranslationManager.INSTANCE.loadFromJsonMaps();
                                    return Command.SINGLE_SUCCESS;
                                })));

    }


    private static ArgumentBuilder<CommandSource, LiteralArgumentBuilder<CommandSource>> getHelp(){
        return Commands.literal("?")
                .executes(new Command<CommandSource>() {
                    @Override
                    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
                        String[] usages = getUsage(context.getSource()).split(",");
                        for (String usage :
                                usages) {
                            context.getSource().sendFeedback(new StringTextComponent(usage),false);
                        }
                        return Command.SINGLE_SUCCESS;
                    }
                });
    }

    private static ArgumentBuilder<CommandSource, LiteralArgumentBuilder<CommandSource>> getHelp2(){
        return Commands.literal("help")
                .executes(new Command<CommandSource>() {
                    @Override
                    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
                        String[] usages = getUsage(context.getSource()).split(",");
                        for (String usage :
                                usages) {
                            context.getSource().sendFeedback(new StringTextComponent(usage),false);
                        }
                        return Command.SINGLE_SUCCESS;
                    }
                });
    }

    public static void execute(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if(args.length == 1 && "loadMap".equals(args[0])){
            TranslationManager.INSTANCE.loadFromJsonMaps();
        }else if(args.length == 1 && "loadConfig".equals(args[0])){
            TranslationManager.INSTANCE.loadConfig();
        } else if(args.length == 1 && "startRecord".equals(args[0])){
            TranslationManager.INSTANCE.startRecord();
        }else if(args.length == 1 && "endRecord".equals(args[0])){
            TranslationManager.INSTANCE.setPrintFormatChars(true);
            TranslationManager.INSTANCE.endRecord();
        }else if(args.length == 2 && "endRecord".equals(args[0]) && "-n".equals(args[1])){
            TranslationManager.INSTANCE.setPrintFormatChars(false);
            TranslationManager.INSTANCE.endRecord();
        }else if(args.length == 1 && "disable".equals(args[0])){
            TranslationManager.INSTANCE.setEnable(false);
        }else if(args.length == 1 && "enable".equals(args[0])){
            TranslationManager.INSTANCE.setEnable(true);
        }else if(args.length == 1 && "spilt".equals(args[0])){
            TranslationManager.INSTANCE.setEnableSpiltWords(true);
        }else if(args.length == 1 && "notSpilt".equals(args[0])){
            TranslationManager.INSTANCE.setEnableSpiltWords(false);
        }else if(args.length == 2 && "retainOrg".equals(args[0]) && "true".equals(args[1])){
            TranslationManager.INSTANCE.setRetainOrg(true);
        }else if(args.length == 2 && "retainOrg".equals(args[0]) && "false".equals(args[1])){
            TranslationManager.INSTANCE.setRetainOrg(false);
        } else if(args.length == 1 && "?".equals(args[0]) || "help".equals(args[0])) {
            String[] usages = getUsage(sender).split(",");
            for (String usage :
                    usages) {
                sender.sendFeedback(new StringTextComponent(usage),false);
            }

        }
    }
}
