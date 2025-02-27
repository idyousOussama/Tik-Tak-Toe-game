package com.example.tictachero.Listeners

import com.example.tictachero.Models.OnlinePlayer

interface SearchedItemListener {
fun onFollowing(user : OnlinePlayer)
fun onAcceptFollow(user : OnlinePlayer)
fun onUnFollow(user : OnlinePlayer)
fun onUserItemListener (user : OnlinePlayer)
}