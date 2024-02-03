package com.crashinvaders.laughemout.game.engine.components

import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.SnapshotArray
import com.crashinvaders.common.*
import com.github.quillraven.fleks.*
import ktx.assets.pool
import ktx.collections.GdxArray

class Transform : Component<Transform> {

    lateinit var entity: Entity

    private val tmpVec = Vector2()

    val ownProj = Affine2()
    private val l2wProj = Affine2()
    private val w2lProj = Affine2()

    var nestingLevel: Int = 0
        private set

    var parent: Transform? = null
        set(value) {
            if (field == value)
                return

            if (field != null) {
                field!!.children.removeValue(value, true)
            }
            field = value

            if (value != null) {
                value.children.add(this)
            }

            nestingLevel = evalNestingLevel(0)

            markWorldTransformDirty()
        }

    val children = GdxArray<Transform>()

    private var isOwnTransformDirty = true
    private var isWorldTransformDirty = true
    private var updateDirtyFlags = true

    var ignoreParent = false
        set(value) {
            if (field == value)
                return

            field = value

            if (parent != null) {
                markOwnTransformDirty()
            }
        }

    var localPositionX = 0f
        set(value) {
            field = value
            markOwnTransformDirty()
        }
    var localPositionY = 0f
        set(value) {
            field = value
            markOwnTransformDirty()
        }
    var localScaleX = 1f
        set(value) {
            field = value
            markOwnTransformDirty()
        }
    var localScaleY = 1f
        set(value) {
            field = value
            markOwnTransformDirty()
        }
    var localRotation = 0f
        set(value) {
            field = value
            markOwnTransformDirty()
        }

    var localPosition: Vector2
        get() = tmpVec.set(localPositionX, localPositionY)
        set(value) {
            localScaleX = value.x
            localScaleY = value.y
        }

    var localScale: Vector2
        get() = tmpVec.set(localScaleX, localScaleY)
        set(value) {
            localScaleX = value.x
            localScaleY = value.y
        }

    val worldPositionX: Float
        get() {
            if (parent == null) {
                return localPositionX
            }
            val l2w = localToWorldProj
            return l2w.extractPositionX()
        }

    val worldPositionY: Float
        get() {
            if (parent == null) {
                return localPositionY
            }
            val l2w = localToWorldProj
            return l2w.extractPositionY()
        }

    var worldPosition: Vector2
        get() {
            if (parent == null) {
                return tmpVec.set(localPositionX, localPositionY)
            }
            val l2w = localToWorldProj
            return l2w.extractPosition(tmpVec)
        }
        set(value) = setWorldPosition(value.x, value.y)

    fun setWorldPosition(x: Float, y: Float) {
        if (parent == null) {
            localPositionX = x
            localPositionY = y
            return
        }
        val w2l = getParentWorldToLocal()
        w2l.applyToPoint(tmpVec.set(x, y))
        localPositionX = tmpVec.x
        localPositionY = tmpVec.y
    }

    val worldScaleX: Float
        get() {
            if (parent == null) {
                return localScaleX
            }
            val l2w = localToWorldProj
            return l2w.extractPositionX()
        }
    val worldScaleY: Float
        get() {
            if (parent == null) {
                return localScaleY
            }
            val l2w = localToWorldProj
            return l2w.extractPositionY()
        }

    var worldScale: Vector2
        get() {
            if (parent == null) {
                return tmpVec.set(localScaleX, localScaleY)
            }
            val l2w = localToWorldProj
            return l2w.extractScale(tmpVec)
        }
        set(value) = setWorldScale(value.x, value.y)

    fun setWorldScale(scaleX: Float, scaleY: Float) {
        if (parent == null) {
            localScaleX = scaleX
            localScaleY = scaleY
            return
        }

        TODO("Implement it")
//        //TODO This is kinda expensive and gives small computational error. We need to optimize it.
//        val w2l = parent!!.worldToLocalProj
//        localScaleX = w2l.applyToVector(worldRight.scl(x)).len()
//        localScaleY = w2l.applyToVector(worldUp.scl(y)).len()
//        localScaleY = w2l.applyToVector(localToWorldProj.extractUpReal(tmpVec).scl(y)).len()

//        val worldToLocalProj1 = worldToLocalProj)

//        val (parentScaleX, parentScaleY) = parent!!.worldScale
//        val scaleDiffX = x/parentScaleX
//        val scaleDiffY = y/parentScaleY
//        val tmpMat = Affine2().setToTrnRotScl(0f, 0f, localRotation, scaleDiffX, scaleDiffY)
//        val newLocalScale = tmpMat.extractScale(tmpVec)
//        localScaleX = newLocalScale.x
//        localScaleY = newLocalScale.y


//        val (worldX, worldY) = worldPosition
//        val worldRotation = worldRotation
//        ownProj.setToTrnRotScl(worldX, worldY, worldRotation, scaleX, scaleY).preMul(parent!!.worldToLocalProj)
//        updateDirtyFlags = false
//        localPositionX = ownProj.extractPositionX()
//        localPositionY = ownProj.extractPositionY()
//        localScaleX = ownProj.extractScaleX()
//        localScaleY = ownProj.extractScaleY()
//        localRotation = ownProj.extractRotation()
//        isOwnTransformDirty = false
//        updateDirtyFlags = true
//        markWorldTransformDirty()
    }

    fun setWorld(positionX: Float, positionY: Float, rotation: Float, scaleX: Float, scaleY: Float) {
        if (parent == null) {
            updateDirtyFlags = false
            localPositionX = positionX
            localPositionY = positionY
            localRotation = rotation
            localScaleX = scaleX
            localScaleY = scaleY
            updateDirtyFlags = true
            markOwnTransformDirty()
            return
        }

        ownProj.setToTrnRotScl(positionX, positionY, rotation, scaleX, scaleY).preMul(parent!!.worldToLocalProj)
        updateDirtyFlags = false
        localPositionX = ownProj.extractPositionX()
        localPositionY = ownProj.extractPositionY()
        localScaleX = ownProj.extractScaleX()
        localScaleY = ownProj.extractScaleY()
        localRotation = ownProj.extractRotation()
        updateDirtyFlags = true
        markWorldTransformDirty()
    }

    fun setLocalToWorldProj(matrix: Affine2) {
        if (parent == null) {
            updateDirtyFlags = false
            localPositionX = matrix.extractPositionX()
            localPositionY = matrix.extractPositionY()
            localRotation = matrix.extractRotation()
            localScaleX = matrix.extractScaleX()
            localScaleY = matrix.extractScaleY()
            updateDirtyFlags = true
            markOwnTransformDirty()
            return
        }

        ownProj.set(matrix).preMul(parent!!.worldToLocalProj)
        updateDirtyFlags = false
        localPositionX = ownProj.extractPositionX()
        localPositionY = ownProj.extractPositionY()
        localScaleX = ownProj.extractScaleX()
        localScaleY = ownProj.extractScaleY()
        localRotation = ownProj.extractRotation()
        updateDirtyFlags = true
        markWorldTransformDirty()
    }

    var worldRotation: Float
        get() {
            if (parent == null) {
                return localRotation
            }
            val l2w = localToWorldProj
            return l2w.extractRotation()
        }
        set(rotation) {
            if (parent == null) {
                localRotation = rotation
                return
            }
            localRotation = (rotation - parent!!.worldRotation) % 360f
        }

    val worldRight: Vector2
        get() {
            val l2w = localToWorldProj
            return l2w.extractRight(tmpVec)
        }

    val worldUp: Vector2
        get() {
            val l2w = localToWorldProj
            return l2w.extractUp(tmpVec)
        }

    val localToWorldProj: Affine2
        get() {
            updateDirtyTransform()
            return l2wProj
        }

    val worldToLocalProj: Affine2
        get() {
            updateDirtyTransform()
            return w2lProj
        }

    fun setParent(newParent: Transform?, keepWorldPosition: Boolean) {
        if (parent == newParent) {
            return
        }

        if (!keepWorldPosition) {
            parent = newParent
            return
        }

        val oldPosX = worldPositionX
        val oldPosY = worldPositionY
        val oldScaleX = worldScaleX
        val oldScaleY = worldScaleY
        val oldRotation = worldRotation
        parent = newParent
        setWorld(oldPosX, oldPosY, oldRotation, oldScaleX, oldScaleY)
    }

    private fun markOwnTransformDirty() {
        if (!updateDirtyFlags) {
            return
        }

        isOwnTransformDirty = true
        markWorldTransformDirty()
    }

    private fun markWorldTransformDirty() {
        if (!updateDirtyFlags) {
            return
        }

        isWorldTransformDirty = true
        for (i in 0 until children.size) {
            children[i].markWorldTransformDirty()
        }
    }

    private fun updateDirtyTransform() {
        if (isOwnTransformDirty) {
            isOwnTransformDirty = false
            ownProj.setToTrnRotScl(localPositionX, localPositionY, localRotation, localScaleX, localScaleY)
        }

        if (isWorldTransformDirty) {
            isWorldTransformDirty = false
            if (!ignoreParent && parent != null) {
                l2wProj.set(ownProj).preMul(parent!!.localToWorldProj)
            } else {
                l2wProj.set(ownProj)
            }

            w2lProj.set(l2wProj)
            // This helps to workaround singular matrix case.
            // Which happens to children transform when the parent has X scale set to zero.
            if (w2lProj.det() != 0f) {
                w2lProj.inv()
            }
        }
    }

    private fun getParentWorldToLocal(): Affine2 =
        if (!ignoreParent && parent != null)
            parent!!.worldToLocalProj
        else
            projIdt

    private fun evalNestingLevel(baseLevel: Int): Int {
        if (parent == null) {
            return baseLevel
        }
        return parent!!.evalNestingLevel(baseLevel + 1)
    }

    override fun World.onAdd(entity: Entity) {
        this@Transform.entity = entity
    }

    override fun World.onRemove(entity: Entity) {
        // debug { "${entity[Info].name} is about to be destroyed" }

        if (parent != null) {
            parent!!.children.removeValue(this@Transform, true)
            parent = null
        }

        if (children.size > 0) {
            val snapshotArray = entitySnapshotArrayPool.obtain()
            for (i in 0 until children.size) {
                val childTransform = children[i]
                val childEntity = childTransform.entity
                snapshotArray.add(childEntity)
            }
            children.clear()

            snapshotArray.use { childEntity ->
                if (childEntity in this) { // 'this' is FleksWorld
                    childEntity.remove()
                }
            }

            snapshotArray.clear()
            entitySnapshotArrayPool.free(snapshotArray)
        }
    }

    override fun type() = Transform

    companion object : ComponentType<Transform>() {
        private val projIdt = Affine2()

        private val entitySnapshotArrayPool = pool { SnapshotArray<Entity>() }
    }

    data class Snapshot(
        var positionX: Float,
        var positionY: Float,
        var scaleX: Float,
        var scaleY: Float,
        var rotation: Float) {

        constructor() : this(0f, 0f, 1f, 1f, 0f)

        fun from(other: Snapshot) {
            this.positionX = other.positionX
            this.positionY = other.positionY
            this.scaleX = other.scaleX
            this.scaleY = other.scaleY
            this.rotation = other.rotation
        }

        fun readLocalFrom(transform: Transform) {
            this.positionX = transform.localPositionX
            this.positionY = transform.localPositionY
            this.scaleX = transform.localScaleX
            this.scaleY = transform.localScaleY
            this.rotation = transform.localRotation
        }

        fun readWorldFrom(transform: Transform) {
            this.positionX = transform.worldPositionX
            this.positionY = transform.worldPositionY
            this.scaleX = transform.worldScaleX
            this.scaleY = transform.worldScaleY
            this.rotation = transform.worldRotation
        }

        fun writeLocalTo(transform: Transform) {
            transform.localPositionX = this.positionX
            transform.localPositionY = this.positionY
            transform.localScaleX = this.scaleX
            transform.localScaleY = this.scaleY
            transform.localRotation = this.rotation
        }

        fun writeWorldTo(transform: Transform) {
            transform.setWorld(positionX, positionY, rotation, scaleX, scaleY)
        }
    }
}

data object TransformDebugRenderTag : EntityTag()
