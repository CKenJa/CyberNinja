package mod.ckenja.cyninja.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mod.ckenja.cyninja.content.sickle.SickleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SickleEntityRenderer extends EntityRenderer<SickleEntity> {

    private static final ResourceLocation CHAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/block/chain.png");

    private ItemRenderer itemRenderer;

    public SickleEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public boolean shouldRender(SickleEntity p_114836_, Frustum p_114837_, double p_114838_, double p_114839_, double p_114840_) {
        if (super.shouldRender(p_114836_, p_114837_, p_114838_, p_114839_, p_114840_)) {
            return true;
        } else {
            if (p_114836_.getOwner() != null) {
                Vec3 vec3 = this.getPosition(p_114836_.getOwner(), (double) p_114836_.getOwner().getBbHeight() * 0.5, 1.0F);
                Vec3 vec31 = this.getPosition(p_114836_, (double) p_114836_.getEyeHeight(), 1.0F);
                return p_114837_.isVisible(new AABB(vec31.x, vec31.y, vec31.z, vec3.x, vec3.y, vec3.z));
            }

            return false;
        }
    }


    private Vec3 getPosition(Entity p_114803_, double p_114804_, float p_114805_) {
        double d0 = Mth.lerp((double) p_114805_, p_114803_.xOld, p_114803_.getX());
        double d1 = Mth.lerp((double) p_114805_, p_114803_.yOld, p_114803_.getY()) + p_114804_;
        double d2 = Mth.lerp((double) p_114805_, p_114803_.zOld, p_114803_.getZ());
        return new Vec3(d0, d1, d2);
    }


    @Override
    public void render(SickleEntity entityIn, float entityYaw, float partialTicks, PoseStack stackIn, MultiBufferSource bufferIn, int packedLightIn) {
        stackIn.pushPose();


        stackIn.translate(-0.0F, 0, 0.15F);
        stackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot())));

        stackIn.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, -entityIn.xRotO, -entityIn.getXRot())));

        stackIn.scale(1.0F, 1.0F, 1.0F);
        BakedModel bakedmodel = this.itemRenderer.getModel(entityIn.getItem(), entityIn.level(), (LivingEntity) null, entityIn.getId());

        this.itemRenderer.render(entityIn.getItem(), ItemDisplayContext.GROUND, false, stackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, bakedmodel);
        stackIn.popPose();

        stackIn.pushPose();
        Entity owner = entityIn.getOwner();
        if (owner != null) {
            float f = 0.0F;
            float f1 = 0.0F;
            float f2 = f1 * 0.5F % 1.0F;
            float f3 = entityIn.getEyeHeight();
            stackIn.pushPose();
            //stackIn.translate(0.0F, f3, 0.0F);
            Vec3 vec3 = this.getPosition(owner, (double) owner.getBbHeight() * 0.7, partialTicks);
            Vec3 vec31 = this.getPosition(entityIn, (double) f3, partialTicks);
            Vec3 vec32 = vec3.subtract(vec31);
            float f4 = (float) (vec32.length());
            vec32 = vec32.normalize();
            float f5 = (float) Math.acos(vec32.y);
            float f6 = (float) Math.atan2(vec32.z, vec32.x);
            stackIn.mulPose(Axis.YP.rotationDegrees(((float) (Math.PI / 2) - f6) * (180.0F / (float) Math.PI)));
            stackIn.mulPose(Axis.XP.rotationDegrees(f5 * (180.0F / (float) Math.PI)));
            int i = 1;
            float f7 = f1 * 0.05F * -1.5F;
            float f8 = f * f;
            int j = 64 + (int) (f8 * 191.0F);
            int k = 32 + (int) (f8 * 191.0F);
            int l = 128 - (int) (f8 * 64.0F);
            float f9 = 0.2F;
            float f10 = 0.282F;
            float f11 = Mth.cos(f7 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
            float f12 = Mth.sin(f7 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
            float f13 = Mth.cos(f7 + (float) (Math.PI / 4)) * 0.282F;
            float f14 = Mth.sin(f7 + (float) (Math.PI / 4)) * 0.282F;
            float f15 = Mth.cos(f7 + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
            float f16 = Mth.sin(f7 + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
            float f17 = Mth.cos(f7 + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
            float f18 = Mth.sin(f7 + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
            float f19 = Mth.cos(f7 + (float) Math.PI) * 0.2F;
            float f20 = Mth.sin(f7 + (float) Math.PI) * 0.2F;
            float f21 = Mth.cos(f7 + 0.0F) * 0.2F;
            float f22 = Mth.sin(f7 + 0.0F) * 0.2F;
            float f23 = Mth.cos(f7 + (float) (Math.PI / 2)) * 0.2F;
            float f24 = Mth.sin(f7 + (float) (Math.PI / 2)) * 0.2F;
            float f25 = Mth.cos(f7 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
            float f26 = Mth.sin(f7 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
            float f27 = 0.0F;
            float f28 = 0.4999F;
            float f29 = -1.0F + f2;
            float f30 = f4 * 2.5F + f29;
            VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderType.entityCutoutNoCull(CHAIN_LOCATION));
            PoseStack.Pose posestack$pose = stackIn.last();
            vertex(vertexconsumer, posestack$pose, f19, f4, f20, j, k, l, 0.4999F, f30);
            vertex(vertexconsumer, posestack$pose, f19, 0.0F, f20, j, k, l, 0.4999F, f29);
            vertex(vertexconsumer, posestack$pose, f21, 0.0F, f22, j, k, l, 0.0F, f29);
            vertex(vertexconsumer, posestack$pose, f21, f4, f22, j, k, l, 0.0F, f30);
            vertex(vertexconsumer, posestack$pose, f23, f4, f24, j, k, l, 0.4999F, f30);
            vertex(vertexconsumer, posestack$pose, f23, 0.0F, f24, j, k, l, 0.4999F, f29);
            vertex(vertexconsumer, posestack$pose, f25, 0.0F, f26, j, k, l, 0.0F, f29);
            vertex(vertexconsumer, posestack$pose, f25, f4, f26, j, k, l, 0.0F, f30);
            float f31 = 0.0F;

            vertex(vertexconsumer, posestack$pose, f11, f4, f12, j, k, l, 0.0F, f31 - 0.15F);
            vertex(vertexconsumer, posestack$pose, f13, f4, f14, j, k, l, 1.15F, f31 - 0.15F);
            vertex(vertexconsumer, posestack$pose, f17, f4, f18, j, k, l, 1.15F, f31);
            vertex(vertexconsumer, posestack$pose, f15, f4, f16, j, k, l, -0.15F, f31);
            stackIn.popPose();
        }
        stackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, stackIn, bufferIn, packedLightIn);
    }

    private static void vertex(
            VertexConsumer p_253637_,
            PoseStack.Pose p_323627_,
            float p_253994_,
            float p_254492_,
            float p_254474_,
            int p_254080_,
            int p_253655_,
            int p_254133_,
            float p_254233_,
            float p_253939_
    ) {
        p_253637_.addVertex(p_323627_, p_253994_, p_254492_, p_254474_)
                .setColor(p_254080_, p_253655_, p_254133_, 255)
                .setUv(p_254233_, p_253939_)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(p_323627_, 0.0F, 1.0F, 0.0F);
    }


    @Override
    public ResourceLocation getTextureLocation(SickleEntity p_115034_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}