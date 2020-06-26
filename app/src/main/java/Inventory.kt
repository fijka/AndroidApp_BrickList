class Inventory {
    var id: Int = 0
    var name: String? = null
    var active: Int = 1
    var lastAccessed: Int = 0

    constructor(id: Int, name: String) {
        this.id = id
        this.name = name
    }
}