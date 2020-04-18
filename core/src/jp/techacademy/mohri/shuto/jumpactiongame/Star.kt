package jp.techacademy.mohri.shuto.jumpactiongame

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

/**
 * スタークラス.
 *
 * 触れると得点を獲得する.
 * (スターは動かないのでupdateメソッドは実装しない)
 */
class Star(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : Sprite(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val STAR_WIDTH = 0.8f
        val STAR_HEIGHT = 0.8f

        // 状態
        val STAR_EXIST = 0
        val STAR_NONE = 1
    }

    var mState: Int = 0

    /**
     * 初期化処理.
     */
    init {
        setSize(STAR_WIDTH, STAR_HEIGHT)
        mState = STAR_EXIST
    }


    /**
     * スターをゲット時.
     */
    fun get() {
        mState = STAR_NONE
        setAlpha(0f)
    }
}