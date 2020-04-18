package jp.techacademy.mohri.shuto.jumpactiongame

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/**
 * ログ用タグ.
 */
const val TAG = "JumpActionGame"

/**
 * JumpActionGame.
 * GameScreenより先に呼ばれる.
 */
class JumpActionGame(val mRequestHandler: ActivityRequestHandler)
    : Game() {
    lateinit var batch: SpriteBatch

    override fun create() {
        batch = SpriteBatch()

        // ゲーム画面を表示.
        setScreen(GameScreen(this))
    }
}
