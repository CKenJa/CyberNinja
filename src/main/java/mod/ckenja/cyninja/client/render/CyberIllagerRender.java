package mod.ckenja.cyninja.client.render;

import bagu_chan.bagus_lib.client.layer.CustomArmorLayer;
import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.client.ModModelLayers;
import mod.ckenja.cyninja.client.model.CyberIllagerModel;
import mod.ckenja.cyninja.entity.CyberIllager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class CyberIllagerRender extends MobRenderer<CyberIllager, CyberIllagerModel<CyberIllager>> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "textures/entity/cyber_ninja/cyber_ninja.png");
    private static final ResourceLocation EYE_LOCATION = ResourceLocation.fromNamespaceAndPath(Cyninja.MODID, "textures/entity/cyber_ninja/cyber_ninja_eye.png");

    public CyberIllagerRender(EntityRendererProvider.Context p_173956_) {
        super(p_173956_, new CyberIllagerModel<>(p_173956_.bakeLayer(ModModelLayers.CYBER_NINJA)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return RenderType.eyes(EYE_LOCATION);
            }
        });
        this.addLayer(new CustomArmorLayer<>(this, p_173956_));
        this.addLayer(new ItemInHandLayer<>(this, p_173956_.getItemInHandRenderer()));
    }


    public ResourceLocation getTextureLocation(CyberIllager p_114029_) {
        return LOCATION;
    }
}