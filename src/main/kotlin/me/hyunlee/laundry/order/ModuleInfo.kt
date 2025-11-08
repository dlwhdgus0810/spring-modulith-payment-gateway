package me.hyunlee.laundry.order

@org.springframework.modulith.ApplicationModule(
    allowedDependencies = ["common :: adapter", "common :: domain"]
)
object ModuleInfo