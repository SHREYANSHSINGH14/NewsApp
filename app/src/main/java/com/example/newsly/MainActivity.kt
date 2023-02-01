package com.example.newsly

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var adapter: NewsAdapter
    private var article = mutableListOf<Articles>()
    var isScrolling = false
    var pageNum = 1
    var totalCount: Int = 0
    var outOfScreen = 0
    var currentItems = 0
    var totalResults = 0
    val progressBar = findViewById<ProgressBar>(R.id.progressBar)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = NewsAdapter(this@MainActivity,article)
        val recycleView = findViewById<RecyclerView>(R.id.newsList)
        recycleView.adapter = adapter
//        val manager =
        recycleView.layoutManager = LinearLayoutManager(this@MainActivity)
        getNews()
        recycleView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.i("SCROLL","OnScrollChange Working")
                if(newState == SCROLL_STATE_DRAGGING){
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalCount = (recycleView.layoutManager as LinearLayoutManager).itemCount
                currentItems = (recycleView.layoutManager as LinearLayoutManager).childCount
                outOfScreen = (recycleView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//                Toast.makeText(this@MainActivity,"$totalCount,${currentItems+outOfScreen}",Toast.LENGTH_SHORT).show()
                if(isScrolling && totalResults>totalCount && outOfScreen >= totalCount - 5){
                    isScrolling = false
                    pageNum++
                    Toast.makeText(this@MainActivity,"Calling getNews, $pageNum",Toast.LENGTH_SHORT).show()
                    getNews()
                }
            }
        })
    }

    private fun getNews() {
        val news = NewsService.newsInstance.getHeadlines("in",pageNum)
        news.enqueue(object : Callback<News>{
            override fun onResponse(call: Call<News>, response: Response<News>) {
                val news = response.body()
                if(news!= null){
                    Log.i("NEWSLY",news.toString())
                    totalResults = news.totalResults
                    article.addAll(news.articles)
                    adapter.notifyDataSetChanged()
                    if(response.isSuccessful){
                        progressBar.visibility = GONE
                    }
//                    Toast.makeText(this@MainActivity,"$totalResults",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                Log.i("NEWSLY","Error while connecting ${t.toString()}", t)
            }
        })
    }
}