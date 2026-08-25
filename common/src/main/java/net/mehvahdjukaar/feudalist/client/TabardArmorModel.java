package net.mehvahdjukaar.feudalist.client;

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
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

/**
 * Chestplate model for the tabard. Same shape as a normal chestplate plus two flat panels hanging
 * off the bottom edge, one at the front and one at the back.
 */
public class TabardArmorModel extends HumanoidModel<LivingEntity> {

    public static final int TEXTURE_SIZE = 64;

    private static final int DEFORMATION = 1;
    private static final CubeDeformation CHESTPLATE_DEFORMATION = new CubeDeformation(DEFORMATION);
    //four shorter than a vanilla torso, so the cloth stops at the waist and the skirt takes over.
    //at 8 the deformation stretches the torso's texels by the same 1.25 sideways and down, so they come
    //out square, and torso plus skirt lands on the 1:2 a banner flag has
    private static final int BODY_HEIGHT = 8;
    //texels, not model pixels. the skirt is drawn on the same grid as the torso
    private static final int FLAP_WIDTH = 8;
    private static final int FLAP_HEIGHT = 8;
    //the torso grows a pixel on every side from the chestplate deformation, which leaves its uvs alone.
    //the skirt has to grow by the same amount or its texels would come out smaller than the torso's
    private static final float FLAP_SCALE = (8f + 2 * DEFORMATION) / FLAP_WIDTH;
    //bottom edge of the deformed body box, and how far its faces sit from the center
    private static final int BODY_BOTTOM = BODY_HEIGHT + DEFORMATION;
    private static final int BODY_FACE_Z = 2 + DEFORMATION;

    //the panels the banner is squeezed onto
    public static final Rect2D BODY_FRONT_UV = new Rect2D(20, 20, 8, BODY_HEIGHT);
    public static final Rect2D BODY_BACK_UV = new Rect2D(32, 20, 8, BODY_HEIGHT);
    public static final Rect2D FRONT_FLAP_UV = new Rect2D(0, 32, FLAP_WIDTH, FLAP_HEIGHT);
    public static final Rect2D BACK_FLAP_UV = new Rect2D(3 * FLAP_WIDTH, 32, FLAP_WIDTH, FLAP_HEIGHT);

    //the rest of the torso the pattern spills onto. it unfolds as one strip that wraps all the way
    //around: right side runs back to front, then the front panel, then the left side front to back, then the back panel
    public static final Rect2D BODY_RIGHT_SIDE_UV = new Rect2D(16, 20, 4, BODY_HEIGHT);
    public static final Rect2D BODY_LEFT_SIDE_UV = new Rect2D(28, 20, 4, BODY_HEIGHT);
    //shoulder tops and the underside, both laid out along the front panel's u
    public static final Rect2D BODY_TOP_UV = new Rect2D(20, 16, 8, 4);
    public static final Rect2D BODY_UNDERSIDE_UV = new Rect2D(28, 16, 8, 4);

    //how tall those two panels are on the model, which is not how tall they are on the texture
    public static final int BODY_FACE_SIZE = BODY_HEIGHT + 2 * DEFORMATION;
    public static final int FLAP_FACE_SIZE = Math.round(FLAP_HEIGHT * FLAP_SCALE);

    private final ModelPart frontFlap;
    private final ModelPart backFlap;

    public TabardArmorModel(ModelPart root) {
        super(root);
        this.frontFlap = stretchedFlap(this.body.getChild("front_flap"));
        this.backFlap = stretchedFlap(this.body.getChild("back_flap"));
    }

    //a deformation would do this too, but it grows upwards as well and the top edge would end up inside
    //the torso, fighting it. scaling the part only ever pushes the cloth down from its hinge
    private static ModelPart stretchedFlap(ModelPart flap) {
        flap.xScale = FLAP_SCALE;
        flap.yScale = FLAP_SCALE;
        return flap;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CHESTPLATE_DEFORMATION, 0);
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(BODY_RIGHT_SIDE_UV.x(), BODY_TOP_UV.y())
                        .addBox(-4, 0, -2, 8, BODY_HEIGHT, 4, CHESTPLATE_DEFORMATION),
                PartPose.ZERO);
        body.addOrReplaceChild("front_flap", flap(FRONT_FLAP_UV, Direction.NORTH),
                PartPose.offset(0, BODY_BOTTOM, -BODY_FACE_Z));
        body.addOrReplaceChild("back_flap", flap(BACK_FLAP_UV, Direction.SOUTH),
                PartPose.offset(0, BODY_BOTTOM, BODY_FACE_Z));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    //a zero depth box puts both its faces on the same plane and they fight, so only the outward one is kept.
    //armor doesn't cull, so it still shows from the inside, mirrored, like cloth would.
    //a box lays the north face at its tex offset and the south one right after it
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
        this.frontFlap.xRot = mostForwardLeg - this.body.xRot;
        this.backFlap.xRot = mostBackwardLeg - this.body.xRot;

        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
