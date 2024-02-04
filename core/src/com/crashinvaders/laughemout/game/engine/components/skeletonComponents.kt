package com.crashinvaders.laughemout.game.engine.components;

import com.badlogic.gdx.math.Vector2
import com.crashinvaders.laughemout.game.engine.TimeMode
import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.app.gdxError

class SkeletonContainer(
    val skeleton: Skeleton,
    val animState: AnimationState,
    val timeMode: TimeMode = TimeMode.GameTime,
) : Component<SkeletonContainer> {

    fun getBonePosition(boneName: String): Vector2 {
        val bone = skeleton.findBone(boneName)
            ?: gdxError("Cannot find a bone with the name \"$boneName\" in the skeleton \"${skeleton.data.name}\".")
        return tmpVec2.set(bone.worldX, bone.worldY)
    }

    override fun type() = SkeletonContainer
    companion object : ComponentType<SkeletonContainer>() {
        private val tmpVec2 = Vector2()
    }
}

class SkeletonBoneParenting(
    val boneName: String
) : Component<SkeletonBoneParenting> {

    lateinit var attachedBone: Bone

    override fun type() = SkeletonBoneParenting
    companion object : ComponentType<SkeletonBoneParenting>()
}