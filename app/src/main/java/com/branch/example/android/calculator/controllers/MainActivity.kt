package com.branch.example.android.calculator.controllers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.branch.example.android.calculator.data.CalculatorViewModel
import com.branch.example.android.calculator.data.Symbol
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.rows.*
import android.content.Intent
import io.branch.referral.Branch
import android.util.Log
import android.view.*
import com.branch.example.android.calculator.R
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.BranchError
import io.branch.referral.SharingHelper
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import io.branch.referral.util.ShareSheetStyle

class MainActivity : AppCompatActivity() {

    private lateinit var invalidOpToast: Toast
    private lateinit var model: CalculatorViewModel
    lateinit var buo: BranchUniversalObject

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme) // https://stackoverflow.com/questions/5486789/how-do-i-make-a-splash-screen
        super.onCreate(savedInstanceState)

        // Initialize the Branch object
        Branch.getAutoInstance(this)
        buo = BranchUniversalObject()
            .setCanonicalIdentifier("content/12345")
            .setTitle("My Content Title")
            .setContentDescription("My Content Description")
            .setContentImageUrl("https://lorempixel.com/400/400")
            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setContentMetadata(ContentMetadata().addCustomMetadata("key1", "value1"))

        setContentView(R.layout.activity_main)

        invalidOpToast = Toast.makeText(this, "Invalid operation", Toast.LENGTH_LONG)

        model = ViewModelProviders.of(this)[CalculatorViewModel::class.java]
        model.expression.observe(this, Observer<MutableList<Symbol>>{ updateDisplay() })

        setOnClickListener()
    }

    public override fun onStart() {
        super.onStart()
        val branch = Branch.getInstance()

        branch.initSession({ referringParams, error ->
            if (error == null) {
                Log.i("BRANCH SDK", referringParams.toString())
                if (referringParams.has("operation")) {
                    val expr = referringParams.getString("operation")
                    // https://docs.branch.io/apps/android/#read-deep-link
                    decodeDeepLinkExpression(expr)
                }
            } else {
                Log.i("BRANCH SDK", error.message)
            }
        }, this.intent.data, this)
//        // latest
//        val sessionParams = branch.latestReferringParams
//        // first
//        val installParams = branch.firstReferringParams
    }

    private fun decodeDeepLinkExpression(s: String) {
        val symbols = s.split(Regex("(?<=[-+*/])|(?=[-+*/])"))
        model.clear()
        for (symbol in symbols) {
            model.add(Symbol(symbol))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    private fun setOnClickListener() {
        val rows: List<ViewGroup> = listOf(row1, row2, row3, row4)
        for (row in rows) {
            for (child in row.children) {
                when (child.id) {
                    delete.id -> child.setOnClickListener { model.removeLast() }
                    equal.id -> child.setOnClickListener { model.compute(this) }
                    else -> child.setOnClickListener { onNumberOrOperatorClick(it) }
                }
            }
        }
    }

    private fun onNumberOrOperatorClick(it: View) {
        if (it !is TextView) return
        model.onNumberOrOperatorClick(Symbol(it.text.toString()), invalidOpToast)
    }

    private fun updateDisplay() {
        computations_display?.text = model.concatenate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> {
                val expr = model.concatenate()
                if (!expr.contains(Regex("\\d+"))) return false
                shareLink(expr)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareLink(messageBody: String) {
        // https://docs.branch.io/apps/android/#share-deep-link
        var lp = LinkProperties()
            .setChannel("facebook")
            .setFeature("sharing")
            .setCampaign("Let's do math together")
            .addControlParameter("desktop_url", "http://wikipedia.org")

        val ss = ShareSheetStyle(this@MainActivity, "Look at this crazy math!", messageBody)
            .setCopyUrlStyle(resources.getDrawable(android.R.drawable.ic_menu_send), "Copy", "Added to clipboard")
            .setMoreOptionStyle(resources.getDrawable(android.R.drawable.ic_menu_search), "Show more")
            .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
            .addPreferredSharingOption(SharingHelper.SHARE_WITH.EMAIL)
            .addPreferredSharingOption(SharingHelper.SHARE_WITH.MESSAGE)
            .addPreferredSharingOption(SharingHelper.SHARE_WITH.HANGOUT)
            .setAsFullWidthStyle(true)
            .setSharingTitle("Share With")

        buo.showShareSheet(this, lp, ss, object : Branch.BranchLinkShareListener {
            override fun onShareLinkDialogLaunched() {
                Log.i("TESTIN", "onShareLinkDialogLaunched")
            }
            override fun onShareLinkDialogDismissed() {
                Log.i("TESTIN", "onShareLinkDialogDismissed")
            }
            override fun onLinkShareResponse(sharedLink: String, sharedChannel: String, error: BranchError) {
                Log.i("TESTIN", "onLinkShareResponse, sharedLink = $sharedLink, sharedChannel = $sharedChannel, error = $error")
            }
            override fun onChannelSelected(channelName: String) {
                Log.i("TESTIN", "onChannelSelected, channelName = $channelName")
            }
        })
    }
}
