package com.nowandfuture.translation.setup;


import com.nowandfuture.translation.KeyBindHandler;
import com.nowandfuture.translation.RenderHandler;
import com.nowandfuture.translation.command.TranslationCommand;
import com.nowandfuture.translation.core.TranslationManager;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends DefaultClientProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        TranslationManager.INSTANCE.init();
        ClientCommandHandler.instance.registerCommand(new TranslationCommand());
        MinecraftForge.EVENT_BUS.register(new RenderHandler());
        MinecraftForge.EVENT_BUS.register(new KeyBindHandler());
    }
}
