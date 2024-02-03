package com.crashinvaders.laughemout.game.engine.components;

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.Skeleton
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.app.gdxError

class SkeletonContainer(
    val skeleton: Skeleton,
    val animState: AnimationState
) : Component<SkeletonContainer> {

    fun getBonePosition(boneName: String): Vector2 {
        val boneOverheadAnchor = skeleton.findBone(boneName)
        if (boneOverheadAnchor == null) {
            gdxError("Cannot find a bone with the name \"$boneName\" in the skeleton \"${skeleton.data.name}\".")
        }
        return tmpVec2.set(boneOverheadAnchor.worldX, boneOverheadAnchor.worldY)
    }

    override fun type() = SkeletonContainer
    companion object : ComponentType<SkeletonContainer>() {
        private val tmpVec2 = Vector2()
    }
}