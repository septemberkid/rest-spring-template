package com.github.dimsmith.restspringtemplate.auth

import leaf.LeafDbRow
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(private val user: LeafDbRow) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return arrayListOf()
    }

    override fun getPassword(): String {
        return user.getString("password")
    }

    override fun getUsername(): String {
        return user.getString("username")
    }

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean {
        return user.getString("is_active") == "t"

    }
}