data class Vote(val first: Int, val second: Int, val third: Int)

private const val INVALID = -1

fun main() {
    testVotesManually_successCandidate1()
    printSeparator()
    testVotesManually_failureNoMajority()
    printSeparator()
    testVotesChaotically()
}

private fun printSeparator() {
    println("\n${(0..20).joinToString("") { "-" }}\n")
}

private fun testVotesManually_successCandidate1() {
    val votes = listOf(
        Vote(4, 2, 6),
        Vote(3, 2, 1),
        Vote(1, 4, 6),
        Vote(2, 1, 3),
        Vote(1, 2, 6),
        Vote(4, 2, 6),
    )
    settle(votes, votes.size)
}

private fun testVotesManually_failureNoMajority() {
    val votes = listOf(
        Vote(1, 2, 3),
        Vote(2, 3, 1),
        Vote(3, 1, 2),
        Vote(1, 2, 3),
        Vote(2, 3, 1),
        Vote(3, 1, 2),
    )
    settle(votes, votes.size)
}

private fun testVotesChaotically() {
    val numCandidates = 6

    fun <T> permute(input: List<T>): List<List<T>> {
        if (input.size == 1) return listOf(input)
        val perms = mutableListOf<List<T>>()
        val toInsert = input[0]
        for (perm in permute(input.drop(1))) {
            for (i in 0..perm.size) {
                val newPerm = perm.toMutableList()
                newPerm.add(i, toInsert)
                perms.add(newPerm)
            }
        }
        return perms
    }

    val votes = (1..100).map {
        val t = permute((0..numCandidates).toList()).random()
        Vote(t[0], t[1], t[2])
    }
    settle(votes, votes.size)
}

private tailrec fun settle(votes: List<Vote>, totalVoters: Int): Int? {
    val voteCount = votes
        .groupBy { it.first }
        .toSortedMap()
        .filterKeys { it != INVALID }
        .mapValues { it.value.size }
    val lowest = voteCount
        .filter { entry ->
            entry.value == voteCount.minOfOrNull { it.value }
        }.mapNotNull { it.key }
    val max = voteCount
        .maxByOrNull { it.value }

    println("$voteCount")

    return when {
        voteCount.size <= 1 || max == null -> {
            println("Fail - no candiate could get a majority of ${(totalVoters / 2) + 1} out of $totalVoters")
            -1
        }
        max.value > totalVoters / 2 -> {
            println("$max has gained the majority")
            max.key
        }
        else -> {
            println("eliminating candiate with the fewest votes: $lowest")
            votes
                .filter { it.first in lowest }
                .groupBy { it.second }
                .mapValues { it.value.size }
                .let {
                    println("reassigning votes based on next ranked choice: $it")
                    println()
                }
            val votes1 = votes.map {
                if (it.first in lowest) Vote(it.second, it.third, INVALID) else it
            }.filterNot { it == Vote(INVALID, INVALID, INVALID) }
            settle(votes1, totalVoters)
        }
    }
}

