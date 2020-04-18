package jp.techacademy.mohri.shuto.jumpactiongame

import com.badlogic.gdx.graphics.Texture

/**
 * 足場クラス.
 *
 * 静止しているタイプ,左右に移動するタイプが存在する.
 * プレイヤーが乗ると一定確率で消える仕様.
 */
class Step(type: Int, texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : GameObject(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val STEP_WIDTH = 2.0f
        val STEP_HEIGHT = 0.5f

        // タイプ（通常と動くタイプ）
        val STEP_TYPE_STATIC = 0
        val STEP_TYPE_MOVING = 1

        // 状態（通常と消えた状態）
        val STEP_STATE_NORMAL = 0
        val STEP_STATE_VANISH = 1

        // 速度
        val STEP_VELOCITY = 2.0f
    }

    var mState: Int = 0
    var mType: Int

    /**
     * 初期化処理.
     */
    init {
        setSize(STEP_WIDTH, STEP_HEIGHT)
        mType = type
        if (mType == STEP_TYPE_MOVING) {
            velocity.x = STEP_VELOCITY
        }
    }

    /**
     * 座標更新.
     */
    fun update(deltaTime: Float) {

        // 動くタイプの場合
        if (mType == STEP_TYPE_MOVING) {
            x += velocity.x * deltaTime

            if (x < STEP_WIDTH / 2) {
                velocity.x = -velocity.x
                x = STEP_WIDTH / 2
            }
            if (x > GameScreen.WORLD_WIDTH - STEP_WIDTH / 2) {
                velocity.x = -velocity.x
                x = GameScreen.WORLD_WIDTH - STEP_WIDTH / 2
            }
        }
    }

    /**
     * 足場を消す
     */
    fun vanish() {
        mState = STEP_STATE_VANISH
        //透明化、速度0
        setAlpha(0f)
        velocity.x = 0f
    }
}