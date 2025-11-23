package me.hyunlee.laundry.payment

@org.springframework.modulith.ApplicationModule(allowedDependencies =
    ["common :: adapter", "common :: domain", "monitoring"])
object ModuleInfo