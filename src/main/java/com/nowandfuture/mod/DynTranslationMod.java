package com.nowandfuture.mod;

import com.nowandfuture.mod.core.TranslationManager;
import com.nowandfuture.mod.vanillaopt.ClazzOfModCache;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DynTranslationMod.MOD_ID)
public class DynTranslationMod
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "dyntranslation";

    public DynTranslationMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadedComplete);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        TranslationManager.INSTANCE.init();
        MinecraftForge.EVENT_BUS.register(new RenderHandler());
        MinecraftForge.EVENT_BUS.register(new KeyBindHandler());
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
    }

    private void processIMC(final InterModProcessEvent event)
    {
    }

    private void loadedComplete(final FMLLoadCompleteEvent event){
        ClazzOfModCache.INSTANCE.init();
    }
}
