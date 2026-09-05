package net.mehvahdjukaar.heraldics.client;

import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.horse.Horse;

public class TabardHorseArmorModel extends HorseModel<Horse> {

    public static final int TEXTURE_SIZE = 64;
    private static final int TABARD_HEIGHT = 14;
    //slightly enlarged
    private static final CubeDeformation TABARD_GROW = new CubeDeformation(0.25f);

    public TabardHorseArmorModel(ModelPart root) {
        super(root);
        root.getAllParts().forEach(part -> part.skipDraw = true);
        this.body.getChild("tabard").skipDraw = false;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HorseModel.createBodyMesh(CubeDeformation.NONE);
        PartDefinition body = mesh.getRoot().getChild("body");
        body.addOrReplaceChild("tabard", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5, -8, -17, 10, TABARD_HEIGHT, 22, TABARD_GROW),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
