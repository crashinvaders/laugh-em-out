package com.crashinvaders.laughemout.game.engine.systems

import com.crashinvaders.laughemout.game.engine.components.SkeletonBoneParenting
import com.crashinvaders.laughemout.game.engine.components.SkeletonContainer
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ktx.app.gdxError

class SkeletonBoneParentingSystem : IteratingSystem(
    family { all(Transform, SkeletonBoneParenting) }
), FamilyOnAdd {

    override fun onAddEntity(entity: Entity) {
        val cParentTransform = entity[Transform].parent
        if (cParentTransform == null || SkeletonContainer !in cParentTransform.entity)
            gdxError("SkeletonBoneParenting entity should have a parent with SkeletonContainer.")

        val cBoneParenting = entity[SkeletonBoneParenting]

        val skeleton = cParentTransform.entity[SkeletonContainer].skeleton
        val bone = skeleton.findBone(cBoneParenting.boneName) ?:
            gdxError("Cannot find a bone with the name \"$cBoneParenting.boneName\" in the skeleton \"${skeleton.data.name}\".")

        cBoneParenting.attachedBone = bone

        // Instantly update the transform.
        onTickEntity(entity)
    }

    override fun onTickEntity(entity: Entity) {
        val bone = entity[SkeletonBoneParenting].attachedBone
        entity[Transform].apply {
            localPositionX = bone.worldX
            localPositionY = bone.worldY
            localScaleX = bone.worldScaleX
            localScaleY = bone.worldScaleY
            localRotation = bone.worldRotationX
        }
    }
}