package net.mehvahdjukaar.heraldics.client;

import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class TabardHorseMailModel extends HorseModel {

    public static final int TEXTURE_SIZE = 64;

    private static final CubeDeformation ARMOR_GROW = new CubeDeformation(0.1f);
    private static final CubeDeformation BODY_GROW = new CubeDeformation(0.15f);

    public TabardHorseMailModel(ModelPart root) {
        super(root);
        this.body.skipDraw = true;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = AbstractEquineModel.createBodyMesh(ARMOR_GROW);
        PartDefinition body = mesh.getRoot().getChild("body");
        body.addOrReplaceChild("mail_body", CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-5, -8, -17, 10, 10, 22, BODY_GROW),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE).apply(MeshTransformer.scaling(1.1f));
    }
}
