package com.nowandfuture.translation;

import com.nowandfuture.translation.setup.IProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = DynTranslationMod.MOD_ID, name = DynTranslationMod.NAME, version = DynTranslationMod.VERSION)
public class DynTranslationMod
{
    public static final String MOD_ID = "dyntranslation";
    public static final String NAME = "DynTranslation Mod";
    public static final String VERSION = "1.3.3";

    @Mod.Instance
    public static DynTranslationMod instance;

    @SidedProxy(serverSide = "com.nowandfuture.translation.setup.ServerProxy",
            clientSide = "com.nowandfuture.translation.setup.ClientProxy")
    public static IProxy proxy;

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);
        logger = event.getModLog();

        //getRemoteGuiContainer to inject codes to ge open gui modid
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }
}
