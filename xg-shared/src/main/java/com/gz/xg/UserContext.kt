package com.gz.xg

import com.gz.xg.domain.auth.LoginUser

object UserContext {

    private val holder = ThreadLocal<LoginUser>()

    fun set(loginUser: LoginUser) {
        holder.set(loginUser)
    }

    fun get(): LoginUser? {
        return holder.get()
    }

    fun require(): LoginUser {
        return get() ?: throw IllegalStateException("Current user not found in context")
    }

    fun clear() {
        holder.remove()
    }
}