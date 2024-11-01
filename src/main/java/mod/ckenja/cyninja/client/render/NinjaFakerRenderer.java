package mod.ckenja.cyninja.client.render;

import mod.ckenja.cyninja.entity.NinjaFaker;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class NinjaFakerRenderer extends MobRenderer<NinjaFaker, PlayerModel<NinjaFaker>> {
    public NinjaFakerRenderer(EntityRendererProvider.Context p_174289_) {
        super(p_174289_, new PlayerModel<>(p_174289_.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(
                new HumanoidArmorLayer<>(
                        this,
                        new HumanoidArmorModel(p_174289_.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                        new HumanoidArmorModel(p_174289_.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                        p_174289_.getModelManager()
                )
        );
        this.addLayer(new ItemInHandLayer<>(this, p_174289_.getItemInHandRenderer()));
    }

    @Override
    protected @Nullable RenderType getRenderType(NinjaFaker p_115322_, boolean p_115323_, boolean p_115324_, boolean p_115325_) {
        return super.getRenderType(p_115322_, p_115323_, p_115324_, p_115325_);
    }

    @Override
    protected boolean isBodyVisible(NinjaFaker p_115341_) {
        return false;
    }


    @Override
    public ResourceLocation getTextureLocation(NinjaFaker p_114482_) {
        return ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    }
}
