package com.example.tetrisapp.util

import android.util.Log
import com.example.tetrisapp.data.local.dao.LeaderboardDao
import com.example.tetrisapp.model.local.LeaderboardEntry
import com.example.tetrisapp.model.remote.request.ScorePayload
import com.example.tetrisapp.model.remote.response.DefaultPayload
import com.example.tetrisapp.model.remote.response.PersonalRecord
import com.example.tetrisapp.model.remote.response.PublicRecord
import com.example.tetrisapp.model.remote.response.ResponseSubmitScore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.*

class LeaderboardUtil(
    private val token: String?,
    private val apiRoute: String,
    private val leaderboardDao: LeaderboardDao
) {
    private val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var call: HttpResponse? = null

    fun insert(entry: LeaderboardEntry) {
        if (user != null) {
            val isoDate = DateTimeUtil.toISOString(entry.date!!)
            val uid = user.uid
            entry.hash = HashUtil.sha256(String.format("%s_%s", uid, isoDate))
        }

        MainScope().launch(Dispatchers.IO) {
            leaderboardDao.insert(entry)
        }
    }

    fun synchronise() {
        if (token == null) return

        generateHashes {
            val coroutineExceptionHandler = CoroutineExceptionHandler{_, t ->
                Log.e(TAG, t.localizedMessage ?: "")
            }

            MainScope().launch(Dispatchers.IO + coroutineExceptionHandler) {
                leaderboardDao.getByUploaded(false)?.let { entries ->
                    if (entries.isEmpty()) return@launch
                    val scores = entries.map { entry ->
                        ScorePayload(
                            entry.score,
                            entry.lines,
                            entry.level,
                            entry.date!!.time
                        )
                    }

                    HttpClient() {
                        install(ContentNegotiation) {
                            gson()
                        }
                    }.use { client ->
                        call = client.post(apiRoute + "leaderboard/submit") {
                            setBody(mapOf(
                                "idToken" to token,
                                "scores" to scores
                            ))
                            contentType(ContentType.Application.Json)
                        }
                        call?.let { response ->
                            val body: DefaultPayload<List<ResponseSubmitScore>> = response.body()

                            if (response.status.isSuccess() && body.success) {
                                body.data?.forEach { score ->
                                    leaderboardDao.getByHash(score.hash)?.let {
                                        it.uploaded = score.completed
                                        leaderboardDao.update(it)
                                    }
                                }
                            } else {
                                throw(Exception(body.message))
                            }
                        }
                    }
                }
            }
        }

        getUploadedScores()
    }

    private fun getUploadedScores() {
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, t ->
            Log.e(TAG, t.localizedMessage ?: "")
        }

        MainScope().launch(Dispatchers.IO + coroutineExceptionHandler) {
            FirebaseAuth.getInstance().currentUser?.uid?.let {
                HttpClient() {
                    install(ContentNegotiation) {
                        gson()
                    }
                }.use { client ->
                    call = client.post(apiRoute + "leaderboard/get/" + it) {
                        setBody(
                            mapOf("idToken" to token)
                        )
                        contentType(ContentType.Application.Json)
                    }
                    call?.let { response ->
                        val body: DefaultPayload<List<PersonalRecord>> = response.body()
                        if (response.status.isSuccess() && body.success) {
                            body.data?.forEach { record ->
                                val entry = LeaderboardEntry()
                                entry.timeInGame = record.timeInGame
                                entry.score  = record.score
                                entry.level = record.level
                                entry.lines = record.lines
                                entry.hash = record.hash
                                entry.date = record.date
                                entry.uploaded = true
                                leaderboardDao.insert(entry)
                            }
                        } else {
                            throw(Exception(body.message))
                        }
                    }
                }
            }
        }
    }

    private fun generateHashes(callback: () -> Unit) {
        MainScope().launch(Dispatchers.IO) {
            leaderboardDao.getWithoutHash()?.let { data ->
                if (user == null) return@launch
                val uid = user.uid
                data.forEach { entry ->
                    val isoDate = DateTimeUtil.toISOString(entry.date!!)
                    entry.hash = HashUtil.sha256(String.format("%s_%s", uid, isoDate))
                    leaderboardDao.update(entry)
                }
                callback()
            }
        }
    }

    companion object {
        const val TAG = "LeaderboardUtil"
    }
}