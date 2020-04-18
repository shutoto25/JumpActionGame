package jp.techacademy.mohri.shuto.jumpactiongame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport
import java.util.*
import kotlin.collections.ArrayList

/**
 * ゲーム画面.
 */
class GameScreen(private val mGame: JumpActionGame) : ScreenAdapter() {
    companion object {
        /**
         * カメラのサイズを表す定数
         * (画面解像度に依存しない座標管理の為)
         */
        val CAMERA_WIDTH = 10f
        val CAMERA_HEIGHT = 15f

        val WORLD_WIDTH = 10f
        val WORLD_HEIGHT = 15 * 20 // 20画面分登れば終了
        val GUI_WIDTH = 320f
        val GUI_HEIGHT = 480f
        /**
         * ゲームステータス.
         */
        val GAME_STATE_READY = 0
        val GAME_STATE_PLAYING = 1
        val GAME_STATE_GAMEOVER = 2
        /**
         * 重力
         */
        val GRAVITY = -12
    }

    // スプライトとはコンピュータの処理の負荷を上げずに高速に画像を描画する仕組み.
    //「プレイヤーや地面などの画像を表示するためのもの」という認識.
    private val mBg: Sprite
    private val mCamera: OrthographicCamera
    private val mGuiCamera: OrthographicCamera
    private val mViewPort: FitViewport
    private val mGuiViewPort: FitViewport

    /**
     * 効果音.
     */
    private val mSoundEnemy: Sound
    private val mSoundStar: Sound

    private var mRandom: Random
    private var mSteps: ArrayList<Step>
    private var mStars: ArrayList<Star>
    private var mEnemy: ArrayList<Enemy>
    private lateinit var mUfo: Ufo
    private lateinit var mPlayer: Player

    /**
     * ゲームステータス.
     */
    private var mGameState: Int

    private var mHeightSoFar: Float = 0f
    private var mTouchPoint: Vector3

    private var mFont: BitmapFont
    private var mScore: Int
    private var mHighScore: Int

    /**
     * スコア保持Preferences
     */
    private var mPrefs: Preferences
    private val PREFERENCE_KEY_HIGH_SCORE = "high score"

    /**
     * 初期化処理.
     */
    init {
        // 背景の準備
        val bgTexture = Texture("back.png")
        // TextureRegionで切り出す時の原点は左上
        mBg = Sprite(TextureRegion(bgTexture, 0, 0, 540, 810))
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
        mBg.setPosition(0f, 0f)

        // カメラ、ViewPortを生成、設定する.
        mCamera = OrthographicCamera()
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT)
        mViewPort = FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera)

        // GUI用のカメラを設定する
        mGuiCamera = OrthographicCamera()
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT)
        mGuiViewPort = FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera)

        // 効果音.
        mSoundEnemy = Gdx.audio.newSound(Gdx.files.internal("fall-down.mp3"))
        mSoundStar = Gdx.audio.newSound(Gdx.files.internal("kira.mp3"))

        // プロパティの初期化.
        mRandom = Random()
        mSteps = ArrayList<Step>()
        mStars = ArrayList<Star>()
        mEnemy = ArrayList<Enemy>()
        mGameState = GAME_STATE_READY
        mTouchPoint = Vector3()

        mFont = BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false)
        mScore = 0
        mHighScore = 0

        // ハイスコアをPreferencesから取得する.
        mPrefs = Gdx.app.getPreferences("jp.techacademy.mohri.shuto.jumpactiongame")
        mHighScore = mPrefs.getInteger(PREFERENCE_KEY_HIGH_SCORE, 0)

        // ステージ作成.
        createStage()
    }


    /**
     * 基本的に1/60秒ごとに自動的に呼び出される.
     * 性能の低い古いAndroid端末や処理が重たい場合にはもう少し間隔が伸びる場合がある.
     */
    override fun render(delta: Float) {

        update(delta)

        // クリアするときの色(      赤,        緑,       青,      透明)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        // その色で塗り潰しをする
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // カメラの中心を超えたらカメラを上に移動させる.
        // これによりキャラが画面の上半分に移動することはなくなる.
        if (mPlayer.y > mCamera.position.y) {
            mCamera.position.y = mPlayer.y
        }

        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mCamera.update()
        mGame.batch.projectionMatrix = mCamera.combined

        mGame.batch.begin()
        // 原点は左下
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2,
                mCamera.position.y - CAMERA_HEIGHT / 2)
        mBg.draw(mGame.batch)

        // steps
        for (i in 0 until mSteps.size) {
            mSteps[i].draw(mGame.batch)
        }

        // stars
        for (i in 0 until mStars.size) {
            mStars[i].draw(mGame.batch)
        }

        // enemy
        for (i in 0 until mEnemy.size) {
            mEnemy[i].draw(mGame.batch)
        }

        // ufo
        mUfo.draw(mGame.batch)

        //player
        mPlayer.draw(mGame.batch)

        mGame.batch.end()

        // スコア表示.
        mGuiCamera.update()
        mGame.batch.projectionMatrix = mGuiCamera.combined
        mGame.batch.begin()
        mFont.draw(mGame.batch, "HighScore: $mHighScore", 16f, GUI_HEIGHT - 15)
        mFont.draw(mGame.batch, "Score: $mScore", 16f, GUI_HEIGHT - 35)
        mGame.batch.end()
    }


    /**
     * ステージ作成.
     */
    private fun createStage() {

        // テクスチャの準備
        val stepTexture = Texture("step.png")
        val starTexture = Texture("star.png")
        val playerTexture = Texture("uma.png")
        val ufoTexture = Texture("ufo.png")
        val enemyTexture = Texture("enemy.png")

        // StepとStarをゴールの高さまで配置
        var y = 0f

        val maxJumpHeight =
                Player.PLAYER_JUMP_VELOCITY * Player.PLAYER_JUMP_VELOCITY / (2 * -GRAVITY)

        // 足場配置設定.
        while (y < WORLD_HEIGHT - 5) {
            //足場のタイプ　8:2 = 動くタイプ:通常タイプ
            val type = if (mRandom.nextFloat() > 0.8f)
                Step.STEP_TYPE_MOVING else Step.STEP_TYPE_STATIC
            val x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH)

            val step = Step(type, stepTexture, 0, 0, 144, 36)
            step.setPosition(x, y)
            mSteps.add(step)

            // スター配置設定.
            if (mRandom.nextFloat() > 0.6f) {
                val star = Star(starTexture, 0, 0, 72, 72)
                star.setPosition(step.x + mRandom.nextFloat(),
                        step.y + Star.STAR_HEIGHT + mRandom.nextFloat() * 3)
                mStars.add(star)
            }

            // 敵キャラ配置設定.
            if (mRandom.nextFloat() > 0.8f) {
                val enemy = Enemy(enemyTexture, 0, 0, 72, 72)
                enemy.setPosition(step.x + mRandom.nextFloat(),
                        step.y + Enemy.STAR_HEIGHT + mRandom.nextFloat() * 3)
                mEnemy.add(enemy)
            }
            y += (maxJumpHeight - 0.5f)
            y -= mRandom.nextFloat() * (maxJumpHeight / 3)
        }

        // Playerを配置
        mPlayer = Player(playerTexture, 0, 0, 72, 72)
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.width / 2, Step.STEP_HEIGHT)

        // ゴールのUFOを配置
        mUfo = Ufo(ufoTexture, 0, 0, 120, 74)
        mUfo.setPosition(WORLD_WIDTH / 2 - Ufo.UFO_WIDTH / 2, y)
    }


    /**
     * 各オブジェクトの状態をアップデートする.
     */
    private fun update(delta: Float) {

        when (mGameState) {
            GAME_STATE_READY -> updateReady()
            GAME_STATE_PLAYING -> updatePlaying(delta)
            GAME_STATE_GAMEOVER -> updateGameOver()
        }
    }


    /**
     * ゲーム開始.
     */
    private fun updateReady() {
        if (Gdx.input.justTouched()) {
            mGameState = GAME_STATE_PLAYING
        }
    }


    /**
     * ゲーム中.
     */
    private fun updatePlaying(delta: Float) {
        var accel = 0f
        if (Gdx.input.isTouched) {
            mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            val left = Rectangle(0f, 0f, GUI_WIDTH / 2, GUI_HEIGHT)
            val right = Rectangle(GUI_WIDTH / 2, 0f, GUI_WIDTH / 2, GUI_HEIGHT)
            if (left.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = 5.0f
            }
            if (right.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = -5.0f
            }
        }

        // step
        for (i in 0 until mSteps.size) {
            mSteps[i].update(delta)
        }

        // enemy
        for (i in 0 until mEnemy.size) {
            mEnemy[i].update(delta)
        }

        // player
        if (mPlayer.y <= 0.5f) {
            mPlayer.hitStep()
        }
        mPlayer.update(delta, accel)
        mHeightSoFar = Math.max(mPlayer.y, mHeightSoFar)

        // 当たり判定を行う.
        checkCollision()

        // ゲームオーバか判定する.
        checkGameOver()
    }


    /**
     * ゲームオーバー.
     */
    private fun updateGameOver() {
        if (Gdx.input.justTouched()) {
            mGame.screen = ResultScreen(mGame, mScore)
        }
    }


    /**
     * 当たり判定.
     */
    private fun checkCollision() {

        // UFOとの当たり判定(ゲームクリア).
        if (mPlayer.boundingRectangle.overlaps(mUfo.boundingRectangle)) {
            mGameState = GAME_STATE_GAMEOVER
            return
        }

        // starとの当たり判定.
        for (i in 0 until mStars.size) {
            val star = mStars[i]

            if (star.mState == Star.STAR_NONE) {
                continue
            }

            // star獲得.
            if (mPlayer.boundingRectangle.overlaps(star.boundingRectangle)) {
                star.get()
                mSoundStar.play()
                mScore++
                if (mScore > mHighScore) {
                    mHighScore = mScore
                    // ハイスコアをPreferencesに保存.
                    mPrefs.putInteger(PREFERENCE_KEY_HIGH_SCORE, mHighScore)
                    mPrefs.flush()
                }
                break
            }
        }

        // 敵キャラとの当たり判定.
        for (i in 0 until mEnemy.size) {
            val enemy = mEnemy[i]

            if (enemy.mState == Star.STAR_NONE) {
                continue
            }
            if (mPlayer.boundingRectangle.overlaps(enemy.boundingRectangle)) {
                mGameState = GAME_STATE_GAMEOVER
                // 効果音play.
                mSoundEnemy.play()
                return
            }
        }

        //stepとの当たり判定.
        // 上昇中は当たり判定をしない.
        if (mPlayer.velocity.y > 0) {
            return
        }
        for (i in 0 until mSteps.size) {
            val step = mSteps[i]

            if (step.mState == Step.STEP_STATE_VANISH) {
                continue
            }

            if (mPlayer.y > step.y) {
                if (mPlayer.boundingRectangle.overlaps(step.boundingRectangle)) {
                    mPlayer.hitStep()
                    // 1/2の確率で足場が消える.
                    if (mRandom.nextFloat() > 0.5f) {
                        step.vanish()
                    }
                    break
                }
            }
        }
    }


    /**
     * ゲームオーバー判定.
     */
    private fun checkGameOver() {
        if (mHeightSoFar - CAMERA_HEIGHT / 2 > mPlayer.y) {
            Gdx.app.log(TAG, "GAME OVER")
            mGameState = GAME_STATE_GAMEOVER
        }
    }


    /**
     * 物理的な画面サイズが変更されたときにコールされるメソッド.
     */
    override fun resize(width: Int, height: Int) {
        mViewPort.update(width, height)
        mGuiViewPort.update(width, height)
    }
}