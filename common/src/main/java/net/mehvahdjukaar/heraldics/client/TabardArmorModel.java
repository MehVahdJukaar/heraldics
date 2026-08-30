package net.mehvahdjukaar.heraldics.client;

import net.mehvahdjukaar.moonlight.api.util.math.Rect2D;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Direction;

import java.util.Set;

public class TabardArmorModel extends HumanoidModel<HumanoidRenderState> {

    public static final int TEXTURE_SIZE = 64;

    private static final int DEFORMATION = 1;
    private static final CubeDeformation CHESTPLATE_DEFORMATION = new CubeDeformation(DEFORMATION);
    //four shorter than a vanilla torso so the cloth stops at the waist. also makes the deformed texels
    //come out square and torso plus skirt land on the 1:2 a banner flag has
    private static final int BODY_HEIGHT = 8;
    private static final int FLAP_WIDTH = 8;
    private static final int FLAP_HEIGHT = 8;
    //torso grows a pixel per side from the deformation but keeps its uvs, so the skirt has to match
    private static final float FLAP_SCALE = (8f + 2 * DEFORMATION) / FLAP_WIDTH;
    private static final int BODY_BOTTOM = BODY_HEIGHT + DEFORMATION;
    private static final int BODY_FACE_Z = 2 + DEFORMATION;

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
        this.head.visible = false;
        this.hat.visible = false;
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;
    }

    //a deformation grows upwards too and the top edge would end up fighting the torso. scaling only pushes down
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

    //a zero depth box puts both faces on the same plane and they z fight, so only the outward one is kept
    private static CubeListBuilder flap(Rect2D uv, Direction outward) {
        int texU = outward == Direction.NORTH ? uv.x() : uv.x() - FLAP_WIDTH;
        return CubeListBuilder.create()
                .texOffs(texU, uv.y())
                .addBox(-FLAP_WIDTH / 2f, 0, 0, FLAP_WIDTH, FLAP_HEIGHT, 0, Set.of(outward));
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        float mostForwardLeg = Math.min(this.rightLeg.xRot, this.leftLeg.xRot);
        float mostBackwardLeg = Math.max(this.rightLeg.xRot, this.leftLeg.xRot);
        this.frontFlap.xRot = mostForwardLeg - this.body.xRot;
        this.backFlap.xRot = mostBackwardLeg - this.body.xRot;
    }
}
