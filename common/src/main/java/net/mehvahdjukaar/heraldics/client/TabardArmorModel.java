package net.mehvahdjukaar.heraldics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.util.math.Rect2D;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class TabardArmorModel extends HumanoidModel<LivingEntity> {

    public static final int TEXTURE_SIZE = 64;

    //These are here only so we have a shared place to reference these dimensions sicne texture gen needs them
    private static final int DEFORMATION = 1;
    private static final CubeDeformation CHESTPLATE_DEFORMATION = new CubeDeformation(DEFORMATION);
    private static final int BODY_HEIGHT = 8;
    private static final int FLAP_WIDTH = 8;
    private static final int FLAP_HEIGHT = 8;
    private static final float FLAP_SCALE = (8f + 2 * DEFORMATION) / FLAP_WIDTH;
    private static final int BODY_BOTTOM = BODY_HEIGHT + DEFORMATION;
    private static final int BODY_FACE_Z = 2 + DEFORMATION;
    private static final float SEATED_BACK_FLAP = 60 * Mth.DEG_TO_RAD;
    private static final int BODY_TEX_U = 16;
    private static final int BODY_TEX_V = 16;
    public static final Rect2D BODY_FRONT_UV = new Rect2D(20, 20, 8, BODY_HEIGHT);
    public static final Rect2D BODY_BACK_UV = new Rect2D(32, 20, 8, BODY_HEIGHT);
    public static final Rect2D FRONT_FLAP_UV = new Rect2D(0, 32, FLAP_WIDTH, FLAP_HEIGHT);
    public static final Rect2D BACK_FLAP_UV = new Rect2D(FLAP_WIDTH, 32, FLAP_WIDTH, FLAP_HEIGHT);
    public static final Rect2D BODY_TOP_UV = new Rect2D(20, 16, 8, 4);
    public static final Rect2D NECK_HOLE_UV = new Rect2D(22, 16, 4, 4);
    public static final int SHOULDER_WIDTH = (BODY_TOP_UV.width() - NECK_HOLE_UV.width()) / 2;
    public static final int BODY_FACE_SIZE = BODY_HEIGHT + 2 * DEFORMATION;
    public static final int FLAP_FACE_SIZE = Math.round(FLAP_HEIGHT * FLAP_SCALE);

    private final ModelPart frontFlap;
    private final ModelPart backFlap;

    public TabardArmorModel(ModelPart root) {
        super(root);
        this.frontFlap = stretchedFlap(this.body.getChild("front_flap"));
        this.backFlap = stretchedFlap(this.body.getChild("back_flap"));
    }

    private static ModelPart stretchedFlap(ModelPart flap) {
        flap.xScale = FLAP_SCALE;
        flap.yScale = FLAP_SCALE;
        return flap;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CHESTPLATE_DEFORMATION, 0);
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(BODY_TEX_U, BODY_TEX_V)
                        .addBox(-4, 0, -2, 8, BODY_HEIGHT, 4, CHESTPLATE_DEFORMATION),
                PartPose.ZERO);
        body.addOrReplaceChild("front_flap", flap(FRONT_FLAP_UV, Direction.NORTH),
                PartPose.offset(0, BODY_BOTTOM, -BODY_FACE_Z));
        body.addOrReplaceChild("back_flap", flap(BACK_FLAP_UV, Direction.SOUTH),
                PartPose.offset(0, BODY_BOTTOM, BODY_FACE_Z));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static CubeListBuilder flap(Rect2D uv, Direction outward) {
        int texU = outward == Direction.NORTH ? uv.x() : uv.x() - FLAP_WIDTH;
        return CubeListBuilder.create()
                .texOffs(texU, uv.y())
                .addBox(-FLAP_WIDTH / 2f, 0, 0, FLAP_WIDTH, FLAP_HEIGHT, 0, Set.of(outward));
    }

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
        float mostForwardLeg = Math.min(this.rightLeg.xRot, this.leftLeg.xRot);
        float mostBackwardLeg = Math.max(this.rightLeg.xRot, this.leftLeg.xRot);
        boolean noLegBehind = mostBackwardLeg < 0;
        this.frontFlap.xRot = mostForwardLeg - this.body.xRot;
        this.backFlap.xRot = (noLegBehind ? SEATED_BACK_FLAP : mostBackwardLeg) - this.body.xRot;

        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
