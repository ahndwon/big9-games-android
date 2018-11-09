package kr.co.big9.games.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_bomb.view.*
import kr.co.big9.games.R
import kr.co.big9.games.ui.UnityPlayerActivity
import kr.co.big9.games.utils.POO_SCENE
import kr.co.big9.games.utils.SCENE


class BombFragment : Fragment() {

    companion object {
        val TAG: String = BombFragment::class.java.name
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bomb, container, false)

        view.startButton.setOnClickListener {
            val intent = Intent(this.context, UnityPlayerActivity::class.java)
            intent.putExtra(SCENE, POO_SCENE)
            view.context.startActivity(intent)
        }

        return view
    }
}
