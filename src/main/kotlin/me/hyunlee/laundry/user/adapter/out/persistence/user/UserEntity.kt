package me.hyunlee.laundry.user.adapter.out.persistence.user

import jakarta.persistence.*
import me.hyunlee.laundry.common.BaseEntity
import java.util.*

@Entity
@Table(name = "users")
class UserEntity (
    @Id
    @Column(columnDefinition = "BINARY(16)", nullable = false)
    var id: UUID,

    @Column(length = 32, nullable = false, unique = true)
    var phone: String,

    @Column(length = 255)
    var email: String? = null,

    @Column(name = "first_name", length = 100, nullable = false)
    var firstName: String,

    @Column(name = "last_name", length = 100, nullable = false)
    var lastName: String,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var addresses: MutableList<AddressEntity> = mutableListOf(),

    @Column(length = 255, unique = true)
    var customerId: String? = null,
) : BaseEntity()