package com.payne.lc_server.Server

class TimeLimitedMap(private val ttl : Long) {
    val map = mutableMapOf<Int, TimedMapEntry>();

    fun add(key : Int, value: Int){
        val end = (System.currentTimeMillis() - Game.startTime) + ttl
        map[key] = TimedMapEntry(value, end)
    }

    fun get(key : Int) : Int?{
        return map[key]!!.value;
    }

    fun remove(key : Int){
        if(map.containsKey(key)){
            map.remove(key);
        }
    }
    fun has(key : Int) : Boolean{
        return map.containsKey(key)
    }

    fun update(){
        val toRemove = map.filter { it.value.endTime < (System.currentTimeMillis() - Game.startTime) };
        if(toRemove.isNotEmpty()){
            toRemove.keys.forEach { map.remove(it) }
        }
    }
}

data class TimedMapEntry(val value : Int, val endTime : Long);