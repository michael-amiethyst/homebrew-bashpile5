package org.bashpile.core

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.isExecutable

fun Path.writeString(text: String): Path {
    Files.writeString(this, text, StandardOpenOption.APPEND)
    return this
}

fun Path.makeExecutable(): Path {
    val permissions = Files.getPosixFilePermissions(this)
    permissions.add(PosixFilePermission.OWNER_EXECUTE)
    Files.setPosixFilePermissions(this, permissions)
    check(this.isExecutable())
    return this
}
