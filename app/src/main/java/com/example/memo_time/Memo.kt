package com.example.memo_time

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Memo: RealmObject() {
    @PrimaryKey
    var id = 0
    var dataTime = Date()
    var lat = 0.0
    var lng = 0.0
    var memo =""

}