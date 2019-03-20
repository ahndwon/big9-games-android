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
import kr.co.big9.games.utils.ACADE_SCENE
import kr.co.big9.games.utils.PLAY_MODE_REQUEST_CODE
import kr.co.big9.games.utils.SCENE


class GalaxyWarsFragment : Fragment() {

    companion object {
        val TAG: String = GalaxyWarsFragment::class.java.name
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_space, container, false)

        view.startButton.setOnClickListener {
            val intent = Intent(this.context, UnityPlayerActivity::class.java)
            intent.putExtra(SCENE, ACADE_SCENE)
            activity?.startActivityForResult(intent, PLAY_MODE_REQUEST_CODE)
        }

        return view
    }
}
