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
import net.mehvahdjukaar.moonlight.api.util.math.Rect2D;
import net.minecraft.world.entity.LivingEntity;

/**
 * Chestplate model for the tabard. Same shape as a normal chestplate plus two flat panels hanging
 * off the bottom edge, one at the front and one at the back.
 */
public class TabardArmorModel extends HumanoidModel<LivingEntity> {

    public static final int TEXTURE_SIZE = 64;

    private static final int DEFORMATION = 1;
    private static final CubeDeformation CHESTPLATE_DEFORMATION = new CubeDeformation(DEFORMATION);
    private static final int FLAP_WIDTH = 10;
    private static final int FLAP_HEIGHT = 4;
    //bottom edge of the deformed body box, and how far its faces sit from the center
    private static final int BODY_BOTTOM = 12 + DEFORMATION;
    private static final int BODY_FACE_Z = 2 + DEFORMATION;

    //the two panels the banner is squeezed onto. a zero depth box lays its two faces side by side, outer one first
    public static final Rect2D BODY_FRONT_UV = new Rect2D(20, 20, 8, 12);
    public static final Rect2D BODY_BACK_UV = new Rect2D(32, 20, 8, 12);
    public static final Rect2D FRONT_FLAP_OUTER_UV = new Rect2D(0, 32, FLAP_WIDTH, FLAP_HEIGHT);
    public static final Rect2D FRONT_FLAP_INNER_UV = new Rect2D(FLAP_WIDTH, 32, FLAP_WIDTH, FLAP_HEIGHT);
    public static final Rect2D BACK_FLAP_INNER_UV = new Rect2D(0, 40, FLAP_WIDTH, FLAP_HEIGHT);
    public static final Rect2D BACK_FLAP_OUTER_UV = new Rect2D(FLAP_WIDTH, 40, FLAP_WIDTH, FLAP_HEIGHT);

    //everything else the pattern spills onto. the torso unfolds as one strip that wraps all the way
    //around: right side runs back to front, then the front panel, then the left side front to back, then the back panel
    public static final Rect2D BODY_RIGHT_SIDE_UV = new Rect2D(16, 20, 4, 12);
    public static final Rect2D BODY_LEFT_SIDE_UV = new Rect2D(28, 20, 4, 12);
    //shoulder tops and the underside, both laid out along the front panel's u
    public static final Rect2D BODY_TOP_UV = new Rect2D(20, 16, 8, 4);
    public static final Rect2D BODY_UNDERSIDE_UV = new Rect2D(28, 16, 8, 4);
    //an arm block is the same kind of strip, [side][front][side][back], plus a cap above it
    public static final Rect2D RIGHT_ARM_UV = new Rect2D(40, 16, 16, 16);
    public static final Rect2D LEFT_ARM_UV = new Rect2D(0, 48, 16, 16);
    public static final int ARM_SIZE = 4;

    //how tall those two panels are on the model, which is not how tall they are on the texture
    public static final int BODY_FACE_SIZE = 12 + 2 * DEFORMATION;
    public static final int FLAP_FACE_SIZE = FLAP_HEIGHT;

    private final ModelPart frontFlap;
    private final ModelPart backFlap;

    public TabardArmorModel(ModelPart root) {
        super(root);
        this.frontFlap = this.body.getChild("front_flap");
        this.backFlap = this.body.getChild("back_flap");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CHESTPLATE_DEFORMATION, 0);
        PartDefinition root = mesh.getRoot();

        //vanilla mirrors the left arm onto the right arm's texture, which would force the pattern to be
        //symmetric. give it its own corner of the texture instead
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(LEFT_ARM_UV.x(), LEFT_ARM_UV.y())
                        .addBox(-1, -2, -2, ARM_SIZE, 12, ARM_SIZE, CHESTPLATE_DEFORMATION),
                PartPose.offset(5, 2, 0));

        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("front_flap", flap(FRONT_FLAP_OUTER_UV),
                PartPose.offset(0, BODY_BOTTOM, -BODY_FACE_Z));
        body.addOrReplaceChild("back_flap", flap(BACK_FLAP_INNER_UV),
                PartPose.offset(0, BODY_BOTTOM, BODY_FACE_Z));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static CubeListBuilder flap(Rect2D firstFaceUv) {
        return CubeListBuilder.create()
                .texOffs(firstFaceUv.x(), firstFaceUv.y())
                .addBox(-FLAP_WIDTH / 2f, 0, 0, FLAP_WIDTH, FLAP_HEIGHT, 0);
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
        this.frontFlap.xRot = mostForwardLeg - this.body.xRot;
        this.backFlap.xRot = mostBackwardLeg - this.body.xRot;

        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
