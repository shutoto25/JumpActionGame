package jp.techacademy.mohri.shuto.jumpactiongame

import com.badlogic.gdx.graphics.Texture
import jp.techacademy.mohri.shuto.jumpactiongame.Step.Companion.STEP_WIDTH

/**
 * 敵クラス.
 *
 * 触れるとゲームオーバー.
 * (常に動いている)
 */
class Enemy(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : GameObject(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val STAR_WIDTH = 1.0f
        val STAR_HEIGHT = 1.0f

        // タイプ（通常と動くタイプ）
        val ENEMY_TYPE_STATIC = 0
        val ENEMY_TYPE_MOVING = 1

        // 状態
        val ENEMY_EXIST = 0
        val ENEMY_NONE = 1

        // 速度
        val ENEMY_VELOCITY = 3.0f
    }

    var mState: Int = 0
    var mType: Int = 1

    /**
     * 初期化処理.
     */
    init {
        setSize(STAR_WIDTH, STAR_HEIGHT)
        mState = ENEMY_EXIST
        mType = ENEMY_TYPE_MOVING
        velocity.x = ENEMY_VELOCITY
    }

    /**
     * 座標更新.
     */
    fun update(deltaTime: Float) {

        // 動くタイプの場合
        if (mType == ENEMY_TYPE_MOVING) {
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
}