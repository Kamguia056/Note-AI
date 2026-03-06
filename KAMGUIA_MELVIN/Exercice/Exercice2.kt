fun main() {

    val words = listOf("apple", "cat", "banana", "dog", "elephant")

    val wordLengthMap = words.associateWith { it.length }

    val filtered = wordLengthMap.filter { it.value > 4 }
    
    filtered.forEach { (word, length) ->
        println("$word has length $length")
    }
}