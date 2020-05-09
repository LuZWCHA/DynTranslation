package com.nowandfuture.translation.command;

import com.google.common.collect.Lists;
import com.nowandfuture.translation.core.TranslationManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.List;

public class TranslationCommand extends CommandBase {
    String[] subCommands = new String[]{"loadConfig","loadMap","startRecord","endRecord","disable","enable","spilt","notSpilt","retainOrg","help"};

    @Override
    public String getName() {
        return "translation";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return I18n.format("commands.dyntranslation.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if(args.length == 1){
            return getListOfStringsMatchingLastWord(args,subCommands);
        }
        return Lists.newArrayList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
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
            String[] usages =getUsage(sender).split(",");
            for (String usage :
                    usages) {
                sender.sendMessage(new TextComponentString(usage));
            }

        } else{
            throw new WrongUsageException(I18n.format("commands.dyntranslation.error"));
        }
    }
}
