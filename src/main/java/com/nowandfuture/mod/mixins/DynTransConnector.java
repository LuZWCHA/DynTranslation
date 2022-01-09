package com.nowandfuture.mod.mixins;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class DynTransConnector implements IMixinConnector {
    @Override
    public void connect() {
        Mixins.addConfiguration("mixins.translation.json");
    }
}
