package com.example.y_ogawara.hue_huis

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by y-ogawara on 17/06/06.
 */


open class HuisData : RealmObject() {

    open var name: String = ""
    @PrimaryKey
    open var keyCode: String = ""
    open var lightState : String= ""
    open var collarR : Int= 0
    open var collarG : Int= 0
    open var collarB : Int= 0
    open var brightness : Int= 0
    open var hueId : String = ""

}
