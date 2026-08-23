package com.theouterworld.client;

import com.theouterworld.OuterWorldMod;
import com.theouterworld.entity.OxidizableIronGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renderer for the oxidizable iron golem.
 * Selects texture based on oxidation level.
 */
public class OxidizableIronGolemEntityRenderer extends IronGolemRenderer {
    
    private static final Identifier IRON_GOLEM_TEXTURE = Identifier.withDefaultNamespace("textures/entity/iron_golem/iron_golem.png");
    private static final Identifier EXPOSED_IRON_GOLEM_TEXTURE = Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "textures/entity/iron_golem/exposed_iron_golem.png");
    private static final Identifier WEATHERED_IRON_GOLEM_TEXTURE = Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "textures/entity/iron_golem/weathered_iron_golem.png");
    private static final Identifier OXIDIZED_IRON_GOLEM_TEXTURE = Identifier.fromNamespaceAndPath(OuterWorldMod.MOD_ID, "textures/entity/iron_golem/oxidized_iron_golem.png");

    public OxidizableIronGolemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        // Explicitly add crack feature renderer (may not be inherited properly)
        this.addLayer(new IronGolemCrackinessLayer(this));
    }

    @Override
    public Identifier getTextureLocation(IronGolemRenderState state) {
        // The render state doesn't have our custom data, so we need to get it differently
        // We'll use a custom render state that includes oxidation level
        if (state instanceof OxidizableIronGolemRenderState oxidizableState) {
            return switch (oxidizableState.oxidationLevel) {
                case 1 -> EXPOSED_IRON_GOLEM_TEXTURE;
                case 2 -> WEATHERED_IRON_GOLEM_TEXTURE;
                case 3 -> OXIDIZED_IRON_GOLEM_TEXTURE;
                default -> IRON_GOLEM_TEXTURE;
            };
        }
        return IRON_GOLEM_TEXTURE;
    }

    @Override
    public IronGolemRenderState createRenderState() {
        return new OxidizableIronGolemRenderState();
    }

    @Override
    public void extractRenderState(net.minecraft.world.entity.animal.golem.IronGolem entity, IronGolemRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        
        if (state instanceof OxidizableIronGolemRenderState oxidizableState && entity instanceof OxidizableIronGolemEntity oxidizableGolem) {
            oxidizableState.oxidationLevel = oxidizableGolem.getOxidationLevel();
            oxidizableState.waxed = oxidizableGolem.isWaxed();
        }
    }

    /**
     * Custom render state that includes oxidation data
     */
    public static class OxidizableIronGolemRenderState extends IronGolemRenderState {
        public int oxidationLevel = 0;
        public boolean waxed = false;
    }
}

