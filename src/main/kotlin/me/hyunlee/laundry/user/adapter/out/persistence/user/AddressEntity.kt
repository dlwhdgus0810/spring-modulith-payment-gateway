package me.hyunlee.laundry.user.adapter.out.persistence.user

import jakarta.persistence.*
import me.hyunlee.laundry.common.BaseEntity

@Entity
@Table(name = "addresses")
class AddressEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null,

    @Column(nullable = false) var street: String,
    @Column(nullable = false) var city: String,
    @Column(nullable = false) var state: String,
    @Column(name = "postal_code", nullable = false) var postalCode: String,

    var secondary: String? = null,
    var instructions: String? = null,
    var isPrimary: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null
) : BaseEntity()