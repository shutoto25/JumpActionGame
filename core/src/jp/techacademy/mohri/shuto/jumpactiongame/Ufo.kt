package jp.techacademy.mohri.shuto.jumpactiongame

import com.badlogic.gdx.graphics.Texture

/**
 * UFOクラス.
 *
 * ゴールとなるので最初の位置から移動しない.
 * (プレイヤーが触れることでゲームクリア)
 */
class Ufo(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : GameObject(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val UFO_WIDTH = 2.0f
        val UFO_HEIGHT = 1.3f
    }

    /**
     * 初期化処理.
     */
    init {
        setSize(UFO_WIDTH, UFO_HEIGHT)
    }
}