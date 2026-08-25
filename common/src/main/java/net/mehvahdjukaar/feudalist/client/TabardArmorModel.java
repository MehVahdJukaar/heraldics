package net.mehvahdjukaar.feudalist.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/**
 * Chestplate model for the tabard. Same shape as a normal chestplate plus two flat panels hanging
 * off the bottom edge, one at the front and one at the back.
 */
public class TabardArmorModel extends HumanoidModel<LivingEntity> {

    private static final CubeDeformation CHESTPLATE_DEFORMATION = new CubeDeformation(1);
    private static final int FLAP_WIDTH = 10;
    private static final int FLAP_HEIGHT = 4;
    //bottom edge of the deformed body box, and how far its faces sit from the center
    private static final int BODY_BOTTOM = 13;
    private static final int BODY_FACE_Z = 3;

    private final ModelPart frontFlap;
    private final ModelPart backFlap;

    public TabardArmorModel(ModelPart root) {
        super(root);
        this.frontFlap = this.body.getChild("front_flap");
        this.backFlap = this.body.getChild("back_flap");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CHESTPLATE_DEFORMATION, 0);
        PartDefinition body = mesh.getRoot().getChild("body");
        body.addOrReplaceChild("front_flap", flap(0, 32),
                PartPose.offset(0, BODY_BOTTOM, -BODY_FACE_Z));
        body.addOrReplaceChild("back_flap", flap(0, 40),
                PartPose.offset(0, BODY_BOTTOM, BODY_FACE_Z));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static CubeListBuilder flap(int u, int v) {
        return CubeListBuilder.create()
                .texOffs(u, v)
                .addBox(-FLAP_WIDTH / 2f, 0, 0, FLAP_WIDTH, FLAP_HEIGHT, 0);
    }

    /**
     * Copies the pose of the entity being rendered and hides everything that is not part of a chestplate.
     */
    @SuppressWarnings("unchecked")
    public void setupFrom(HumanoidModel<?> source) {
        ((HumanoidModel<LivingEntity>) source).copyPropertiesTo(this);
        this.setAllVisible(false);
        this.body.visible = true;
        this.rightArm.visible = true;
        this.leftArm.visible = true;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        //each flap follows the leg that swings furthest its way so a leg never pokes through it.
        //legs are posed relative to the root while the flaps hang off the body, hence the body angle
        float mostForwardLeg = Math.min(this.rightLeg.xRot, this.leftLeg.xRot);
        float mostBackwardLeg = Math.max(this.rightLeg.xRot, this.leftLeg.xRot);
        this.frontFlap.xRot = mostForwardLeg - this.body.xRot;
        this.backFlap.xRot = mostBackwardLeg - this.body.xRot;

        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
