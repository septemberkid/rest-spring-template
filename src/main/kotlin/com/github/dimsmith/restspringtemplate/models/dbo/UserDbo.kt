package com.github.dimsmith.restspringtemplate.models.dbo

import leaf.LeafDb
import leaf.LeafDbRow
import leaf.LeafDbo


class UserDbo : LeafDbo {
    constructor() {
        init()
    }

    constructor(db: LeafDb) : super(db) {
        init()
    }


    private fun init() {
        tableName = "public.user"
        pkey = "id"
        autoinc = true
        fields = mapOf(
            "id" to null,
            "email" to "",
            "name" to "",
            "username" to "",
            "password" to "",
            "created_at" to null,
            "updated_at" to null,
            "is_active" to null
        )
    }

    fun findByUsername(username: String): LeafDbRow? {
        this.where("username=" + this.quote(username))
        val rows = this.get()
        return if (rows.size > 0) rows.first() else null
    }
}
