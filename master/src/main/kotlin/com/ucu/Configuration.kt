package com.ucu

object Configuration {
    val env = System.getenv()!!
    val port: Int = env.getOrDefault("PORT", "12000").toInt()
    val jarPath: String = env.getOrDefault("JAR_PATH", "/Users/sasha/programming/map-reduce/example-app/build/libs/example-app-1.jar")
    val mapperName: String = env.getOrDefault("MAPPER_CLASSNAME", "com.ucu.SizeCounter")
    val reducerName: String = env.getOrDefault("REDUCER_CLASSNAME", "com.ucu.SizeReducer")
    val tmpFolder: String = env.getOrDefault("TMP_FOLDER", "/tmp")

}