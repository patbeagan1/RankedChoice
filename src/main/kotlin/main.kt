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

            settle(votes.map {
                if (it.first in lowest) Vote(it.second, it.third, INVALID) else it
            }.filterNot { it == Vote(INVALID, INVALID, INVALID) }, totalVoters)
        }
    }
}

/*

{1=2, 2=1, 3=1, 4=2}
eliminating candiate with the fewest votes: [2, 3]
reassigning votes based on next ranked choice: {2=1, 1=1}

{1=3, 2=1, 4=2}
eliminating candiate with the fewest votes: [2]
reassigning votes based on next ranked choice: {1=1}

{1=4, 4=2}
1=4 has gained the majority

---------------------

{1=2, 2=2, 3=2}
eliminating candiate with the fewest votes: [1, 2, 3]
reassigning votes based on next ranked choice: {2=2, 3=2, 1=2}

{1=2, 2=2, 3=2}
eliminating candiate with the fewest votes: [1, 2, 3]
reassigning votes based on next ranked choice: {3=2, 1=2, 2=2}

{1=2, 2=2, 3=2}
eliminating candiate with the fewest votes: [1, 2, 3]
reassigning votes based on next ranked choice: {-1=6}

{}
Fail - no candiate could get a majority of 4 out of 6

---------------------

{0=9, 1=14, 2=19, 3=16, 4=14, 5=11, 6=17}
eliminating candiate with the fewest votes: [0]
reassigning votes based on next ranked choice: {6=1, 3=2, 1=4, 5=2}

{1=18, 2=19, 3=18, 4=14, 5=13, 6=18}
eliminating candiate with the fewest votes: [5]
reassigning votes based on next ranked choice: {2=5, 3=1, 1=4, 4=1, 6=1, 0=1}

{0=1, 1=22, 2=24, 3=19, 4=15, 6=19}
eliminating candiate with the fewest votes: [0]
reassigning votes based on next ranked choice: {1=1}

{1=23, 2=24, 3=19, 4=15, 6=19}
eliminating candiate with the fewest votes: [4]
reassigning votes based on next ranked choice: {5=4, 1=3, 2=5, 6=3}

{1=26, 2=29, 3=19, 5=4, 6=22}
eliminating candiate with the fewest votes: [5]
reassigning votes based on next ranked choice: {3=2, 1=2}

{1=28, 2=29, 3=21, 6=22}
eliminating candiate with the fewest votes: [3]
reassigning votes based on next ranked choice: {1=4, -1=2, 4=4, 2=4, 5=3, 6=2, 0=2}

{0=2, 1=32, 2=33, 4=4, 5=3, 6=24}
eliminating candiate with the fewest votes: [0]
reassigning votes based on next ranked choice: {4=1, 6=1}

{1=32, 2=33, 4=5, 5=3, 6=25}
eliminating candiate with the fewest votes: [5]
reassigning votes based on next ranked choice: {2=2, 4=1}

{1=32, 2=35, 4=6, 6=25}
eliminating candiate with the fewest votes: [4]
reassigning votes based on next ranked choice: {2=2, -1=2, 1=2}

{1=34, 2=37, 6=25}
eliminating candiate with the fewest votes: [6]
reassigning votes based on next ranked choice: {2=5, 4=4, 0=5, 3=2, 5=4, 1=4, -1=1}

{0=5, 1=38, 2=42, 3=2, 4=4, 5=4}
eliminating candiate with the fewest votes: [3]
reassigning votes based on next ranked choice: {-1=2}

{0=5, 1=38, 2=42, 4=4, 5=4}
eliminating candiate with the fewest votes: [4, 5]
reassigning votes based on next ranked choice: {2=2, -1=1, 5=2, 0=2, 3=1}

{0=7, 1=38, 2=44, 3=1, 5=2}
eliminating candiate with the fewest votes: [3]
reassigning votes based on next ranked choice: {-1=1}

{0=7, 1=38, 2=44, 5=2}
eliminating candiate with the fewest votes: [5]
reassigning votes based on next ranked choice: {-1=2}

{0=7, 1=38, 2=44}
eliminating candiate with the fewest votes: [0]
reassigning votes based on next ranked choice: {4=3, 1=1, -1=3}

{1=39, 2=44, 4=3}
eliminating candiate with the fewest votes: [4]
reassigning votes based on next ranked choice: {-1=3}

{1=39, 2=44}
eliminating candiate with the fewest votes: [1]
reassigning votes based on next ranked choice: {4=3, 3=2, 2=9, 5=6, -1=10, 6=8, 0=1}

{0=1, 2=53, 3=2, 4=3, 5=6, 6=8}
2=53 has gained the majority

Process finished with exit code 0
*/