package com.udc.apptfg.util

import javax.inject.Inject


class EjemploDagger @Inject constructor() {

    fun hello():String{
        return ("Hello World")
    }
}