package mod.ckenja.cyninja.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.ckenja.cyninja.content.shuriken.ThrownItemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;

public class ThrownItemEntityRenderer extends EntityRenderer<ThrownItemEntity> {
    private ItemRenderer itemRenderer;

    public ThrownItemEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(ThrownItemEntity entityIn, float entityYaw, float partialTicks, PoseStack stackIn, MultiBufferSource bufferIn, int packedLightIn) {
        stackIn.pushPose();

        stackIn.translate(-0.0F, 0, 0.15F);
        stackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot())));

        stackIn.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, -entityIn.xRotO, -entityIn.getXRot())));
        stackIn.mulPose(Axis.XP.rotationDegrees(90.0F));

        stackIn.mulPose(Axis.ZP.rotationDegrees((entityIn.tickCount + partialTicks) * (((float) 80.0F))));

        stackIn.scale(1.0F, 1.0F, 1.0F);
        BakedModel bakedmodel = this.itemRenderer.getModel(entityIn.getItem(), entityIn.level(), (LivingEntity) null, entityIn.getId());

        this.itemRenderer.render(entityIn.getItem(), ItemDisplayContext.GROUND, false, stackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, bakedmodel);
        stackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, stackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownItemEntity p_115034_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}