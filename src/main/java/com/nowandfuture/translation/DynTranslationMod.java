package com.nowandfuture.translation;

import com.nowandfuture.translation.setup.IProxy;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.apache.logging.log4j.Logger;

@Mod(modid = DynTranslationMod.MODID, name = DynTranslationMod.NAME, version = DynTranslationMod.VERSION)
public class DynTranslationMod
{
    public static final String MODID = "dyntranslation";
    public static final String NAME = "DynTranslation Mod";
    public static final String VERSION = "1.0";

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
